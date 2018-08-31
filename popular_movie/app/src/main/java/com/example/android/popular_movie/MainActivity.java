package com.example.android.popular_movie;

import android.app.Dialog;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.popular_movie.DataModels.Genre;
import com.example.android.popular_movie.DataModels.GenresData;
import com.example.android.popular_movie.DataModels.Movie;
import com.example.android.popular_movie.DataModels.UserPreference;
import com.example.android.popular_movie.Utilities.JsonUtils;
import com.example.android.popular_movie.Utilities.MoviesAdapter;
import com.example.android.popular_movie.Utilities.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler , LoaderManager.LoaderCallbacks<String> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SEARCH_QUERY_URL_EXTRA = "query";

    private SharedPreferences sharedPreferences;
    private RecyclerView mMoviesRecyclerView;
    private ProgressBar mLoadingBar;
    private TextView mErrorMessage;
    private MoviesAdapter adapter;
    private boolean IsLand = false;

    private static int GenresLoaderId = 101;
    private static int MoviesLoaderId = 102;
    private static int FavsLoaderId = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = MainActivity.this;

        // Saving the genres in shared Preferences so i can get the name of the genre by ID
        sharedPreferences = context.getSharedPreferences("GenresDataFile",0);
        if(!sharedPreferences.contains("initialized")){
            getGenresDataFromAPI();
        }
        mMoviesRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);
        mLoadingBar = findViewById(R.id.pb_loading_bar);
        mErrorMessage = (TextView) findViewById(R.id.tv_error_message);
        getMoviesDataFromAPI();
        int span = 2;
        if(IsLand){
            span = 3;
        }else{
            span = 2;
        }
        GridLayoutManager layoutManager = new GridLayoutManager(this , span);
        mMoviesRecyclerView.setLayoutManager(layoutManager);
        adapter = new MoviesAdapter(this);
        mMoviesRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        IsLand = (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ? true : false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(MainActivity.this);
        inflater.inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId){
            case R.id.action_sort:
                openDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void openDialog(){
        // Initialize Dialog
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setTitle("Sort By");
        dialog.setContentView(R.layout.custom_dialog);

        // Get views
        RadioGroup mSortRadioGroup = (RadioGroup) dialog.findViewById(R.id.rg_sort);
        final RadioButton mRatingRadio = dialog.findViewById(R.id.rd_rating);
        final RadioButton mPopularityRadio = dialog.findViewById(R.id.rd_popularity);
        final RadioButton mFavoritesRadio = dialog.findViewById(R.id.rd_favorites);

        // The radio group doesn't automatically so i have to do this (check and uncheck)
        int sortType = UserPreference.getSortType();
        if(sortType == 1){
            mPopularityRadio.setChecked(false);
            mRatingRadio.setChecked(true);
            mFavoritesRadio.setChecked(false);
        }else if(sortType ==2){
            mPopularityRadio.setChecked(true);
            mRatingRadio.setChecked(false);
            mFavoritesRadio.setChecked(false);
        }else{
            mPopularityRadio.setChecked(false);
            mRatingRadio.setChecked(false);
            mFavoritesRadio.setChecked(true);
        }
        mSortRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rd_popularity:
                        mRatingRadio.setChecked(false);
                        UserPreference.setSortType(1);
                        break;
                    case R.id.rd_rating:
                        mPopularityRadio.setChecked(false);
                        UserPreference.setSortType(2);
                        break;
                    case R.id.rd_favorites:
                        mFavoritesRadio.setChecked(false);
                        UserPreference.setSortType(3);
                        break;
                }
                // make request again and populate the rv

                getMoviesDataFromAPI();
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private void getMoviesDataFromAPI(){
        if(UserPreference.getSortType() == 3){

        }else {
            URL url = NetworkUtils.BuildQueryMovieURL();
            new MoviesDataQuery().execute(url);
        }
    }

    private void getGenresDataFromAPI(){
        URL url = NetworkUtils.BuildQueryGenreURL();
        new GenresDataQuery().execute(url);
    }
    private void launchDetailActivity(Movie movie){
        Intent intent = new Intent(MainActivity.this , DetailActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT , movie);
        startActivity(intent);
    }
    private void showErrorMessage(){
        mErrorMessage.setVisibility(View.VISIBLE);
    }
    private void showLoadingSpinner(){
        mLoadingBar.setVisibility(View.VISIBLE);
        mMoviesRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void hideLoadingSpinner(){
        mLoadingBar.setVisibility(View.INVISIBLE);
        mMoviesRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(Movie movie) {
        launchDetailActivity(movie);
    }


    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable final Bundle args) {
        if(id == GenresLoaderId){

            return new android.support.v4.content.AsyncTaskLoader<String>(this) {
                String mGenresData = null;
                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    if(args == null){
                        return;
                    }
                    if(mGenresData != null){
                        deliverResult(mGenresData);
                    }else{
                        forceLoad();
                    }
                }

                @Nullable
                @Override
                public String loadInBackground() {
                    String searchQuery = args.getString(SEARCH_QUERY_URL_EXTRA);
                    if(searchQuery == null || searchQuery.isEmpty()){
                        return null;
                    }
                    try{
                        URL url = new URL(searchQuery.toString());
                        String results = NetworkUtils.getQueryResult(url);
                        return results;
                    }catch (IOException e){
                        return null;
                    }
                }

                @Override
                public void deliverResult(@Nullable String data) {
                    mGenresData = data;
                    super.deliverResult(data);
                }
            };
        }else if(id == MoviesLoaderId){
            return new android.support.v4.content.AsyncTaskLoader<String>(this) {
                String mMoviesData = null;

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    showLoadingSpinner();
                    if(args == null){
                        return;
                    }
                    if(mMoviesData != null){
                        deliverResult(mMoviesData);
                    }else{
                        forceLoad();
                    }
                }

                @Nullable
                @Override
                public String loadInBackground() {
                    String searchQuery = args.getString(SEARCH_QUERY_URL_EXTRA);
                    if(searchQuery == null || searchQuery.isEmpty()){
                        return null;
                    }
                    try{
                        URL url = new URL(searchQuery.toString());
                        String results = NetworkUtils.getQueryResult(url);
                        return results;
                    }catch (IOException e){
                        return null;
                    }
                }

                @Override
                public void deliverResult(@Nullable String data) {
                    mMoviesData = data;
                    super.deliverResult(data);
                }
            };
        }else{
            return new android.support.v4.content.AsyncTaskLoader<String>(this) {
                Cursor mCursor = null;
                @Nullable
                @Override
                public String loadInBackground() {
                    return null;
                }
            };
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {

    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    class MoviesDataQuery extends AsyncTask<URL, Void , String>
    {
        @Override
        protected void onPreExecute() {
            showLoadingSpinner();
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            if(url != null) {
                String queryResult = null;
                try{
                    queryResult = NetworkUtils.getQueryResult(url);
                }catch(IOException e){

                }
                return queryResult;
            }else{
                return null;
            }
        }

        @Override
        protected void onPostExecute(String queryResult) {
            hideLoadingSpinner();
            if(queryResult != null && !queryResult.equals("")){
                Movie[] movies = JsonUtils.parseWholeData(queryResult);
                adapter.setMovies(movies);

            }else{
                showErrorMessage();
            }
            /*
            if(genres != null && genres.length > 0){
                str = sharedPreferences.getString(String.valueOf(genres[0]),null);
                Log.d(TAG,sharedPreferences.getAll().toString());
            }
            */
            //textView.setText(movies[0].getOrigionalName() + " " + movies[0].getBackdropPath() + "\n" + str);
        }
    }


    class GenresDataQuery extends AsyncTask<URL , Void , String>{

        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            if(url != null) {
                String queryResult = null;
                try{
                    queryResult = NetworkUtils.getQueryResult(url);

                }catch(IOException e){

                }
                return queryResult;
            }else{
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {

            if(s != null && !s.equals("")){
                JsonUtils.parseGenreObject(s, MainActivity.this);
            }else{

            }
        }
    }
}
