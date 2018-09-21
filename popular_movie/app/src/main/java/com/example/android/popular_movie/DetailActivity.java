package com.example.android.popular_movie;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popular_movie.Data.FavoriteListContract;
import com.example.android.popular_movie.DataModels.Movie;
import com.example.android.popular_movie.DataModels.Review;
import com.example.android.popular_movie.DataModels.Video;
import com.example.android.popular_movie.Utilities.JsonUtils;
import com.example.android.popular_movie.Utilities.LayoutUtilities;
import com.example.android.popular_movie.Utilities.NetworkUtils;
import com.example.android.popular_movie.Utilities.ReviewsAdapter;
import com.example.android.popular_movie.Utilities.TrailersAdapter;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.text.Format;
import java.text.SimpleDateFormat;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    private static final String TAG = DetailActivity.class.getSimpleName();

    private static final String SEARCH_QUERY_URL_EXTRA = "query";
    private Movie movie;
    @BindView(R.id.iv_movie_poster) ImageView mMoviePoster;
    @BindView(R.id.iv_movie_backdrop) ImageView mMovieBackdrop;
    @BindView(R.id.tv_original_name ) TextView mMovieOriginalTitle;
    @BindView(R.id.tv_overview) TextView mMovieOverview ;
    @BindView(R.id.tv_popularity) TextView mMoviePopularity ;
    @BindView(R.id.tv_genres) TextView mMovieGenres ;
    @BindView(R.id.tv_movie_rating) TextView mMovieRatingText;
    @BindView(R.id.tv_movie_rate_count) TextView mMovieRatingCount;
    @BindView(R.id.iv_movie_rating) ImageView mMovieRatingImage;
    private SharedPreferences sharedPreferences;
    @BindView(R.id.add_to_fav_lbl) TextView mAddToFavoritesLabel;
    @BindView(R.id.trailers_error_message_tv) TextView mTrailersErrorMessage;
    @BindView(R.id.fav_btn) CheckBox mAddToFavs;

    private TrailersAdapter trailersAdapter;

    @BindView(R.id.trailers_rv) RecyclerView  mTrailersRecyclerView;

    @BindView(R.id.trailer_pb) ProgressBar mTrailersProgressBar;

    @BindView(R.id.open_reviews_btn) Button mGoToReviewsButton;

    private static int TrailersLoaderId = 201;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        final Context context = DetailActivity.this;

        sharedPreferences = context.getSharedPreferences("GenresDataFile",0);
        Intent callingIntent = getIntent();
        if (callingIntent.hasExtra(Intent.EXTRA_TEXT)) {
            movie = (Movie) callingIntent.getParcelableExtra(Intent.EXTRA_TEXT);
        }

        // Find the views
        ButterKnife.bind(this);

        mAddToFavs.setChecked(getState());
        if(mAddToFavs.isChecked()){
            mAddToFavoritesLabel.setText(context.getString(R.string.remove_to_fav_lbl));
        }else{
            mAddToFavoritesLabel.setText(context.getString(R.string.add_to_fav_lbl));
        }

        mAddToFavs.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    addMovieToFavorites();
                    mAddToFavoritesLabel.setText(context.getString(R.string.remove_to_fav_lbl));
                    Toast.makeText(DetailActivity.this, "Movie Added To Favorites", Toast.LENGTH_SHORT).show();
                }else{
                    removeMovieFromFavorites();
                    mAddToFavoritesLabel.setText(context.getString(R.string.add_to_fav_lbl));
                    Toast.makeText(DetailActivity.this, "Movie Removed From Favorites", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mGoToReviewsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openReviewsActivity();
            }
        });

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int value = displayMetrics.widthPixels;
        int valueDp = (int) LayoutUtilities.convertPxToDp(this, (float)value);


        populateFields(movie);
        setTitle(movie.getTitle() + " (" + movie.getYear() + ")");
        getTrailersDataFromAPI();
        boolean IsLarge = valueDp > 600;
        int Orientation  = LinearLayoutManager.HORIZONTAL;
        if(IsLarge){
            Orientation = LinearLayoutManager.VERTICAL;
        }
        RecyclerView.LayoutManager trailersLayoutManager = new LinearLayoutManager(this , Orientation , false);
        mTrailersRecyclerView.setLayoutManager(trailersLayoutManager);
        mTrailersRecyclerView.setHasFixedSize(true);
        trailersAdapter = new TrailersAdapter();
        mTrailersRecyclerView.setAdapter(trailersAdapter);

        hideTrailersErrorMessage();

        getSupportLoaderManager().initLoader(TrailersLoaderId , null , this);
        Log.d(TAG , "Created");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showTrailersErrorMessage(){
        mTrailersErrorMessage.setVisibility(View.VISIBLE);
        mTrailersProgressBar.setVisibility(View.INVISIBLE);
    }

    private void hideTrailersErrorMessage(){
        mTrailersErrorMessage.setVisibility(View.INVISIBLE);
    }

    private void showTrailersProgressBar(){
        mTrailersRecyclerView.setVisibility(View.INVISIBLE);
        mTrailersProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideTrailersProgressBar(){
        mTrailersRecyclerView.setVisibility(View.VISIBLE);
        mTrailersProgressBar.setVisibility(View.INVISIBLE);
    }
    // Get the Trailers Data from the API
    private void getTrailersDataFromAPI(){
        URL url = NetworkUtils.BuildQueryTrailerUrl(Integer.toString(movie.getID()));
        Bundle bundle = new Bundle();
        bundle.putString(SEARCH_QUERY_URL_EXTRA , url.toString());

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> trailersLoader = loaderManager.getLoader(TrailersLoaderId);
        if(trailersLoader != null){
            loaderManager.restartLoader(TrailersLoaderId , bundle , this);
        }else{
            loaderManager.initLoader(TrailersLoaderId , bundle , this);
        }
    }

    // Radio Button Methods to add and remove the current movie from the Favorites Database
    private void addMovieToFavorites(){
        ContentResolver resolver = getContentResolver();
        ContentValues contentValues = new ContentValues();

        contentValues.put(FavoriteListContract.FavoriteListEntry.BACKDROP_COL , movie.getBackdropPath());
        contentValues.put(FavoriteListContract.FavoriteListEntry.GENRES_COL , movie.getGenresString());
        contentValues.put(FavoriteListContract.FavoriteListEntry.IMAGE_COL , movie.getPoster_Path());
        contentValues.put(FavoriteListContract.FavoriteListEntry.Origional_Title , movie.getOrigionalName());
        contentValues.put(FavoriteListContract.FavoriteListEntry.MOVIE_TITLE_COL , movie.getTitle());
        contentValues.put(FavoriteListContract.FavoriteListEntry.Overview , movie.getOverview());
        contentValues.put(FavoriteListContract.FavoriteListEntry.VIDEO_COL , movie.getHasVideoTrailer());
        contentValues.put(FavoriteListContract.FavoriteListEntry.ID , movie.getID());
        contentValues.put(FavoriteListContract.FavoriteListEntry.POP_COL , movie.getPopularity());
        contentValues.put(FavoriteListContract.FavoriteListEntry.RATING_COL , movie.getRating());
        contentValues.put(FavoriteListContract.FavoriteListEntry.VOTE_CNT_COL , movie.getRateCount());
        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        String ReleaseDateStr = formatter.format(movie.getReleaseDate());
        contentValues.put(FavoriteListContract.FavoriteListEntry.Release_Date , ReleaseDateStr);

        contentValues.put(FavoriteListContract.FavoriteListEntry.Adult , movie.getAdult());
        resolver.insert(FavoriteListContract.FavoriteListEntry.CONTENT_URI ,contentValues);
    }

    private void removeMovieFromFavorites(){
        Uri uri = FavoriteListContract.FavoriteListEntry.CONTENT_URI;
        String movieId = Integer.toString(movie.getID());
        uri = uri.buildUpon().appendPath(movieId).build();
        ContentResolver resolver = getContentResolver();
        resolver.delete(uri , null , null);
    }

    private boolean getState(){
        ContentResolver resolver = getContentResolver();
        String movieIdStr = Integer.toString(movie.getID());
        Uri queryUri = FavoriteListContract.FavoriteListEntry.CONTENT_URI.buildUpon().appendPath(movieIdStr).build();
        Cursor cursor = resolver.query(queryUri,
                null,null,null,null );
        if(cursor == null || cursor.getCount() <= 0){
            return false;
        }else{
            return true;
        }
    }
    // Use the movie Data to Populate the fields
    private void populateFields(Movie movie){
        String imageUrl = NetworkUtils.formImageFullPath(movie.getBackdropPath() , true);
        Picasso.with(DetailActivity.this)
                .load(imageUrl)
                .placeholder(R.drawable.noimage)
                .error(R.drawable.noimage)
                .into(mMovieBackdrop);
        imageUrl = NetworkUtils.formImageFullPath(movie.getPoster_Path(),false);
        Picasso.with(DetailActivity.this)
                .load(imageUrl)
                .placeholder(R.drawable.noimage)
                .error(R.drawable.noimage)
                .into(mMoviePoster);
        mMovieOverview.setText(movie.getOverview());
        mMovieOriginalTitle.setText(movie.getOrigionalName());
        mMoviePopularity.setText(String.valueOf(movie.getPopularity()));
        //mMovieRating.setNumStars(10);
        mMovieRatingCount.setText(String.valueOf(movie.getRateCount()) + " Votes");
        //mMovieRating.setRating((float) movie.getRating());
        mMovieRatingText.setText(String.valueOf(movie.getRating()) + " / 10");

        Log.d(TAG , String.valueOf(movie.getRating()));
        if(movie.getGenres() != null && movie.getGenres().length > 0){
            int[] genresIds = movie.getGenres();
            for(int i = 0 ; i < genresIds.length ; i++){
                if(i != 0){
                    mMovieGenres.append(" , ");
                }
                mMovieGenres.append(sharedPreferences.getString(String.valueOf(genresIds[i]),""));
            }
        }else{
            mMovieGenres.setText("Others");

        }
    }


    private void openReviewsActivity(){
        Intent intent = new Intent(DetailActivity.this , ReviewActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT , movie.getID());
        startActivity(intent);
    }

    // Loader methods to get the trailers data for the movie
    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable final Bundle args) {
        if(id == TrailersLoaderId){
            showTrailersProgressBar();
            return new AsyncTaskLoader<String>(this) {
                String mTrailersData = null;
                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    if(args == null){
                        return;
                    }
                    if(mTrailersData != null){
                        deliverResult(mTrailersData);
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
                    mTrailersData = data;
                    super.deliverResult(data);
                }
            };
        }else{
            return new android.support.v4.content.AsyncTaskLoader<String>(this) {
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
        int loaderId = loader.getId();
        if(loaderId == TrailersLoaderId){
            hideTrailersProgressBar();
            if(data != null && !data.equals("")){
                Video[] videos = JsonUtils.parseWholeVideosData(data);
                trailersAdapter.setVideos(videos);

                //trailersProgressBar.setVisibility(View.INVISIBLE);
            }else{
                showTrailersErrorMessage();
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
}
