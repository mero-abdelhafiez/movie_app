package com.example.android.popular_movie;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
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
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.android.popular_movie.Data.FavoriteListContract;
import com.example.android.popular_movie.DataModels.Movie;
import com.example.android.popular_movie.DataModels.UserPreference;
import com.example.android.popular_movie.Utilities.JsonUtils;
import com.example.android.popular_movie.Utilities.MoviesAdapter;
import com.example.android.popular_movie.Utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler , LoaderManager.LoaderCallbacks<String>  {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SEARCH_QUERY_URL_EXTRA = "query";

    private SharedPreferences sharedPreferences;
    private RecyclerView mMoviesRecyclerView;
    private ProgressBar mLoadingBar;
    private TextView mErrorMessage;
    private MoviesAdapter adapter;
    private boolean IsLand = false;

    private Cursor mDataCursor;

    private int mTitleCol ,mVoteCntCol , mRatingCol , mImageCol , mBacdropCol , mPopularityCol ,
    mGenresCol , mIdCol , mAdultCol , mOverviewCol , mOrigionalTitleCol , mVideoCol , mReleaseDateCol ;

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
        getFavoriteMoviesData();
        int span = 2;
        IsLand = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) ? true:false;
        if(IsLand){
            span = 3;
        }else{
            span = 2;
        }
        Log.d(TAG , Integer.toString(span));
        GridLayoutManager layoutManager = new GridLayoutManager(this , span);
        mMoviesRecyclerView.setLayoutManager(layoutManager);
        adapter = new MoviesAdapter(this);
        mMoviesRecyclerView.setAdapter(adapter);

        getSupportLoaderManager().initLoader(MoviesLoaderId , null , this);
        getSupportLoaderManager().initLoader(GenresLoaderId , null , this);
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
            mPopularityRadio.setChecked(true);
            mRatingRadio.setChecked(false);
            mFavoritesRadio.setChecked(false);
        }else if(sortType ==2){
            mPopularityRadio.setChecked(false);
            mRatingRadio.setChecked(true);
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
                        mFavoritesRadio.setChecked(false);
                        UserPreference.setSortType(1);
                        break;
                    case R.id.rd_rating:
                        mPopularityRadio.setChecked(false);
                        mFavoritesRadio.setChecked(false);
                        UserPreference.setSortType(2);
                        break;
                    case R.id.rd_favorites:
                        mRatingRadio.setChecked(false);
                        mPopularityRadio.setChecked(false);
                        UserPreference.setSortType(3);
                        break;
                }
                getMoviesDataFromAPI();

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void getMoviesDataFromAPI(){
        if(UserPreference.getSortType() == 3){
            getFavoriteMoviesData();
        }else {
            URL url = NetworkUtils.BuildQueryMovieURL();
            //new MoviesDataQuery().execute(url);
            Bundle bundle = new Bundle();
            bundle.putString(SEARCH_QUERY_URL_EXTRA , url.toString());

            LoaderManager loaderManager = getSupportLoaderManager();
            Loader<String> moviesLoader = loaderManager.getLoader(MoviesLoaderId);
            if(moviesLoader != null){
                loaderManager.restartLoader(MoviesLoaderId , bundle , this);
            }else{
                loaderManager.initLoader(MoviesLoaderId , bundle , this);
            }
        }
    }

    private void getGenresDataFromAPI(){
        URL url = NetworkUtils.BuildQueryGenreURL();
        //new GenresDataQuery().execute(url);
        Bundle bundle = new Bundle();
        bundle.putString(SEARCH_QUERY_URL_EXTRA , url.toString());
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> genresLoader = loaderManager.getLoader(GenresLoaderId);
        if(genresLoader != null){
            loaderManager.restartLoader(GenresLoaderId , bundle , this);
        }else{
            loaderManager.initLoader(GenresLoaderId , bundle , this);
        }
    }

    private void getFavoriteMoviesData()
    {
        new FavouriteMoviesDataQuery().execute();
        List<Movie> movies = new ArrayList<>();

        Movie movie ;
        if(mDataCursor == null) {return;}
        while(mDataCursor.moveToNext()){
            movie = new Movie();
            movie.setID(mDataCursor.getInt(mIdCol));
            movie.setOverview(mDataCursor.getString(mOverviewCol));
            //movie.setReleaseDate(mDataCursor.getString(mReleaseDateCol));
            //movie.setGenres(mDataCursor.getString(mGenresCol));
            movie.setRateCount(mDataCursor.getInt(mVoteCntCol));
            movie.setPoster_Path(mDataCursor.getString(mImageCol));
            movie.setPopularity(mDataCursor.getDouble(mPopularityCol));
            movie.setOrigionalName(mDataCursor.getString(mOrigionalTitleCol));
            movie.setTitle(mDataCursor.getString(mTitleCol));
            movie.setBackdropPath(mDataCursor.getString(mBacdropCol));
            movie.setRating(mDataCursor.getDouble(mRatingCol));
            movie.setAdult((mDataCursor.getInt(mAdultCol) == 0) ? false : true);
            movie.setHasVideoTrailer((mDataCursor.getInt(mVideoCol) == 0) ? false : true);
            movies.add(movie);
        }
        hideLoadingSpinner();
        if(movies == null){
            showErrorMessage();
        }else {
            int len = movies.toArray().length;
            Movie[] moviesArray = movies.toArray(new Movie[len]);
            adapter.setMovies( moviesArray);
        }
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
        }else if(id == FavsLoaderId){
            return new AsyncTaskLoader<String>(this) {
                @Nullable
                @Override
                public String loadInBackground() {
                    return null;
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
        if(loader.getId() == GenresLoaderId){
            if(data != null && !data.equals("")){
                JsonUtils.parseGenreObject(data, MainActivity.this);
            }else{

            }
        }else if(loader.getId() == MoviesLoaderId){
            hideLoadingSpinner();
            if(data != null && !data.equals("")){
                Movie[] movies = JsonUtils.parseWholeMoviesData(data);
                adapter.setMovies(movies);

            }else{
                showErrorMessage();
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    class FavouriteMoviesDataQuery extends AsyncTask<Void , Void , Cursor>{

        @Override
        protected Cursor doInBackground(Void... voids) {
            ContentResolver resolver = getContentResolver();

            Cursor cursor = resolver.query(FavoriteListContract.FavoriteListEntry.CONTENT_URI
            , null , null , null , null);

            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);
            mDataCursor = cursor;

            // Get Coloumns index
            mIdCol = cursor.getColumnIndex(FavoriteListContract.FavoriteListEntry.ID);
            mAdultCol = cursor.getColumnIndex(FavoriteListContract.FavoriteListEntry.Adult);
            mBacdropCol = cursor.getColumnIndex(FavoriteListContract.FavoriteListEntry.BACKDROP_COL);
            mGenresCol = cursor.getColumnIndex(FavoriteListContract.FavoriteListEntry.GENRES_COL);
            mImageCol = cursor.getColumnIndex(FavoriteListContract.FavoriteListEntry.IMAGE_COL);
            mOverviewCol = cursor.getColumnIndex(FavoriteListContract.FavoriteListEntry.Overview);
            mPopularityCol = cursor.getColumnIndex(FavoriteListContract.FavoriteListEntry.POP_COL);
            mRatingCol = cursor.getColumnIndex(FavoriteListContract.FavoriteListEntry.RATING_COL);
            mReleaseDateCol = cursor.getColumnIndex(FavoriteListContract.FavoriteListEntry.Release_Date);
            mOrigionalTitleCol = cursor.getColumnIndex(FavoriteListContract.FavoriteListEntry.Origional_Title);
            mTitleCol = cursor.getColumnIndex(FavoriteListContract.FavoriteListEntry.MOVIE_TITLE_COL);
            mVideoCol = cursor.getColumnIndex(FavoriteListContract.FavoriteListEntry.VIDEO_COL);
            mVoteCntCol = cursor.getColumnIndex(FavoriteListContract.FavoriteListEntry.VOTE_CNT_COL);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDataCursor != null) {
            mDataCursor.close();
        }
    }
}