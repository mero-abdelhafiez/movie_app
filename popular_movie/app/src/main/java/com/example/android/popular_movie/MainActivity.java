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
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
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
import com.example.android.popular_movie.Utilities.LayoutUtilities;
import com.example.android.popular_movie.Utilities.MoviesAdapter;
import com.example.android.popular_movie.Utilities.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler , LoaderManager.LoaderCallbacks<String>  {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SEARCH_QUERY_URL_EXTRA = "query";

    private static final String USER_CHOICE_KEY = "UserChoice";
    private static final String RV_POSITION_KEY = "RvPosition";

    private Context mContext = MainActivity.this;
    private SharedPreferences sharedPreferences , rvState;
    @BindView(R.id.rv_movies) RecyclerView mMoviesRecyclerView;
    @BindView(R.id.pb_loading_bar) ProgressBar mLoadingBar;
    @BindView(R.id.tv_error_message) TextView mErrorMessage;
    private MoviesAdapter adapter;
    private Movie[] FavoriteMovies;

    private Cursor mDataCursor;

    private GridLayoutManager layoutManager;

    private int mTitleCol ,mVoteCntCol , mRatingCol , mImageCol , mBacdropCol , mPopularityCol ,
    mGenresCol , mIdCol , mAdultCol , mOverviewCol , mOrigionalTitleCol , mVideoCol , mReleaseDateCol ;

    private static int GenresLoaderId = 101;
    private static int MoviesLoaderId = 102;

    int CurrentPosition = 0;

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

        if(savedInstanceState != null ){
            if(savedInstanceState.containsKey(USER_CHOICE_KEY)) {
                UserPreference.setSortType(savedInstanceState.getInt(USER_CHOICE_KEY));
            }
        }

        ButterKnife.bind(this);
        getMoviesDataFromAPI();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int value = displayMetrics.widthPixels;
        int valueDp = (int) LayoutUtilities.convertPxToDp(this, (float)value);

        boolean IsLargeScreen = (valueDp > 600);
        int span , scalingFactor;
        if(IsLargeScreen){
            scalingFactor = 200;
        }else {
            scalingFactor = 150;
        }
        span = calculateNoOfColumns(this , scalingFactor);
        layoutManager = new GridLayoutManager(this , span);
        mMoviesRecyclerView.setLayoutManager(layoutManager);
        adapter = new MoviesAdapter(this);
        mMoviesRecyclerView.setAdapter(adapter);
        hideErrorMessage();
        getSupportLoaderManager().initLoader(MoviesLoaderId , null , this);
        getSupportLoaderManager().initLoader(GenresLoaderId , null , this);
    }

    public static int calculateNoOfColumns(Context context , int scalingFactor) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / scalingFactor);
        if(noOfColumns < 2)
            noOfColumns = 2;
        return noOfColumns;
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
                layoutManager.scrollToPositionWithOffset(0,0);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void getMoviesDataFromAPI(){
        hideErrorMessage();
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
    }

    private void launchDetailActivity(Movie movie){
        Intent intent = new Intent(MainActivity.this , DetailActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT , movie);
        startActivity(intent);
    }

    private void showErrorMessage(){
        mMoviesRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.VISIBLE);
    }
    private void hideErrorMessage(){
        mErrorMessage.setVisibility(View.INVISIBLE);
        mMoviesRecyclerView.setVisibility(View.VISIBLE);
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
    protected void onSaveInstanceState(Bundle outState) {
        rvState = this.getSharedPreferences("RVStateFile",0);
        SharedPreferences.Editor editor = rvState.edit();
        int currentVisiblePosition = 0;
        currentVisiblePosition = layoutManager.findFirstVisibleItemPosition();
        editor.putInt("position" , currentVisiblePosition);
        editor.apply();
        super.onSaveInstanceState(outState);
    }

//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//
//        if(savedInstanceState.containsKey(RV_POSITION_KEY)){
//            int position = savedInstanceState.getInt(RV_POSITION_KEY);
//            CurrentPosition = position;
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        rvState = this.getSharedPreferences("RVStateFile",0);
        int currentVisiblePosition  = rvState.getInt("position" , 0);
        CurrentPosition = currentVisiblePosition;
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
        if(loader.getId() == GenresLoaderId){
            if(data != null && !data.equals("")) {
                JsonUtils.parseGenreObject(data, MainActivity.this);
            }
        }else if(loader.getId() == MoviesLoaderId){
            hideLoadingSpinner();
            if(data != null && !data.equals("")){
                Movie[] movies = JsonUtils.parseWholeMoviesData(data);
                adapter.setMovies(movies);
                layoutManager.scrollToPositionWithOffset(CurrentPosition , 0);
            }else{
                mErrorMessage.setText(mContext.getString(R.string.error_message));
                showErrorMessage();
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    class FavouriteMoviesDataQuery extends AsyncTask<Void , Void , Cursor>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoadingSpinner();
        }

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
            if(mDataCursor == null){
                mErrorMessage.setText(mContext.getString(R.string.favorite_list_empty_message));
                showErrorMessage();
                hideLoadingSpinner();
            }
            getColumnIndicies(mDataCursor);
            convertCursorToList();
        }
    }

    private void getColumnIndicies(Cursor cursor){
        if(cursor == null) return;
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

    private void convertCursorToList(){
        List<Movie> movies = new ArrayList<>();
        Movie movie;
        if (mDataCursor == null) {
            return;
        }
        while (mDataCursor.moveToNext()) {
            movie = new Movie();
            movie.setID(mDataCursor.getInt(mIdCol));
            movie.setOverview(mDataCursor.getString(mOverviewCol));
            try {
                Date ReleaseDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(mDataCursor.getString(mReleaseDateCol));

                movie.setReleaseDate(ReleaseDate);
            } catch (ParseException e) {
                Log.d(TAG, e.getMessage());
            }
            movie.setGenres(movie.getGenresFromString(mDataCursor.getString(mGenresCol)));
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
        if(movies != null ){
            int len = movies.toArray().length;
            FavoriteMovies = movies.toArray(new Movie[len]);
            setDataToTheAdapter();
            if(movies.size() == 0){
                mErrorMessage.setText(mContext.getString(R.string.favorite_list_empty_message));
                showErrorMessage();
            }
        }
        hideLoadingSpinner();
    }

    private void setDataToTheAdapter(){
        adapter.setMovies(FavoriteMovies);
        layoutManager.scrollToPosition(CurrentPosition);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDataCursor != null) {
            mDataCursor.close();
        }
    }
}