package com.example.android.popular_movie;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.android.popular_movie.DataModels.Genre;
import com.example.android.popular_movie.DataModels.Movie;
import com.example.android.popular_movie.DataModels.Review;
import com.example.android.popular_movie.DataModels.Video;
import com.example.android.popular_movie.Utilities.JsonUtils;
import com.example.android.popular_movie.Utilities.NetworkUtils;
import com.example.android.popular_movie.Utilities.ReviewsAdapter;
import com.example.android.popular_movie.Utilities.TrailersAdapter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.net.URL;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    private static final String TAG = DetailActivity.class.getSimpleName();

    private static final String SEARCH_QUERY_URL_EXTRA = "query";
    private Movie movie;
    private ImageView mMoviePoster , mMovieBackdrop;
    private TextView mMovieOriginalTitle , mMovieOverview , mMoviePopularity , mMovieGenres , mMovieRatingText;
    private ImageView mMovieRatingImage;
    private TextView mMovieRatingCount;
    private SharedPreferences sharedPreferences;

    private CheckBox mAddToFavs;

    private ReviewsAdapter reviewsAdapter;
    private TrailersAdapter trailersAdapter;

    private ProgressBar reviewProgressBar , trailersProgressBar;
    private RecyclerView mReviewRecyclerView , mTrailersRecyclerView;
    private static int TrailersLoaderId = 201;
    private static int ReviewsLoaderId = 202;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Context context = DetailActivity.this;
        sharedPreferences = context.getSharedPreferences("GenresDataFile",0);
        mMoviePoster = findViewById(R.id.iv_movie_poster);
        mMovieBackdrop = findViewById(R.id.iv_movie_backdrop);
        mMovieOriginalTitle = findViewById(R.id.tv_original_name);
        mMovieOverview = findViewById(R.id.tv_overview);
        mMoviePopularity = findViewById(R.id.tv_popularity);
        mMovieGenres = findViewById(R.id.tv_genres);
        mMovieRatingImage = (ImageView)findViewById(R.id.rb_movie_rating);
        mMovieRatingText = findViewById(R.id.tv_movie_rating);
        mMovieRatingCount = (TextView) findViewById(R.id.tv_movie_rate_count);
//        mAddToFavs = (CheckBox) findViewById(R.id.fav_btn);
//
//        Picasso.with(this)
//                .load(R.drawable.fav_btn_selector)
//                .into((Target) mAddToFavs);
        //reviewProgressBar = (ProgressBar) findViewById(R.id.reviews_pb);
        //trailersProgressBar = (ProgressBar) findViewById(R.id.trailer_pb);
        mReviewRecyclerView = (RecyclerView) findViewById(R.id.reviews_rv);
        mTrailersRecyclerView = (RecyclerView) findViewById(R.id.trailers_rv);

        Intent callingIntent = getIntent();
        if(callingIntent.hasExtra(Intent.EXTRA_TEXT)){
            movie = (Movie) callingIntent.getParcelableExtra(Intent.EXTRA_TEXT);
        }
        populateFields(movie);
        setTitle(movie.getTitle() + " (" + movie.getYear() + ")");
        getTrailersDataFromAPI();
        getReviewsDataFromAPI();

        RecyclerView.LayoutManager trailersLayoutManager = new LinearLayoutManager(this , LinearLayoutManager.HORIZONTAL , false);
        mTrailersRecyclerView.setLayoutManager(trailersLayoutManager);
        mTrailersRecyclerView.setHasFixedSize(true);
        trailersAdapter = new TrailersAdapter();
        mTrailersRecyclerView.setAdapter(trailersAdapter);

        RecyclerView.LayoutManager reviewsLayoutManager = new LinearLayoutManager(this , LinearLayoutManager.VERTICAL , false);
        mReviewRecyclerView.setLayoutManager(reviewsLayoutManager);
        mReviewRecyclerView.setHasFixedSize(true);
        reviewsAdapter = new ReviewsAdapter();
        mReviewRecyclerView.setAdapter(reviewsAdapter);
//
//        getSupportLoaderManager().initLoader(ReviewsLoaderId , null , this);
//        getSupportLoaderManager().initLoader(TrailersLoaderId , null , this);
    }

    private void getReviewsDataFromAPI(){
        URL url = NetworkUtils.BuildQueryReviewsUrl(Integer.toString(movie.getID()));
        Bundle bundle = new Bundle();
        bundle.putString(SEARCH_QUERY_URL_EXTRA , url.toString());

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> reviewsLoader = loaderManager.getLoader(ReviewsLoaderId);
        if(reviewsLoader != null){
            loaderManager.restartLoader(ReviewsLoaderId , bundle , this);
        }else{
            loaderManager.initLoader(ReviewsLoaderId , bundle , this);
        }
    }

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

    // Get Reviews and Trailers for that Movie
    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable final Bundle args) {
        if(id == ReviewsLoaderId){
            return new AsyncTaskLoader<String>(this) {

                String mReviewsData = null;
                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    if(args == null){
                        return;
                    }
                    if(mReviewsData != null){
                        deliverResult(mReviewsData);
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
                    mReviewsData = data;
                    super.deliverResult(data);
                }
            };
        }else if(id == TrailersLoaderId){
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
        if(loaderId == ReviewsLoaderId){
            if(data != null && !data.equals("")){
                Review[] reviews = JsonUtils.parseWholeReviewsData(data);
                reviewsAdapter.setReviews(reviews);
                //reviewProgressBar.setVisibility(View.INVISIBLE);
            }
        }else if(loaderId == TrailersLoaderId){
            if(data != null && !data.equals("")){
                Video[] videos = JsonUtils.parseWholeVideosData(data);
                trailersAdapter.setVideos(videos);
                //trailersProgressBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
}
