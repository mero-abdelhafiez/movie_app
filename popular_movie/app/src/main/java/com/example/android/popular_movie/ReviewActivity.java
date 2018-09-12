package com.example.android.popular_movie;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.android.popular_movie.DataModels.Movie;
import com.example.android.popular_movie.DataModels.Review;
import com.example.android.popular_movie.Utilities.JsonUtils;
import com.example.android.popular_movie.Utilities.NetworkUtils;
import com.example.android.popular_movie.Utilities.ReviewsAdapter;

import java.io.IOException;
import java.net.URL;

public class ReviewActivity extends AppCompatActivity implements android.support.v4.app.LoaderManager.LoaderCallbacks<String>{

    private static final String TAG = ReviewActivity.class.getSimpleName();

    private int ReviewsLoaderId = 201;
    private ReviewsAdapter mReviewsAdapter;

    private int mMovieId;

    private static final String SEARCH_QUERY_URL_EXTRA = "query";

    private RecyclerView mReviewsRecyclerView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);


        // Get Movie Id from the intent

        Intent callingIntent = getIntent();
        if(callingIntent.hasExtra(Intent.EXTRA_TEXT)){
            mMovieId = callingIntent.getIntExtra(Intent.EXTRA_TEXT , 1);
        }



        mReviewsRecyclerView = (RecyclerView) findViewById(R.id.reviews_rv);

        RecyclerView.LayoutManager reviewsLayoutManager = new LinearLayoutManager(this , LinearLayoutManager.VERTICAL , false);
        mReviewsRecyclerView.setLayoutManager(reviewsLayoutManager);
        mReviewsRecyclerView.setHasFixedSize(true);
        mReviewsAdapter = new ReviewsAdapter();
        mReviewsRecyclerView.setAdapter(mReviewsAdapter);

        getReviewsDataFromAPI();

        getSupportLoaderManager().initLoader(ReviewsLoaderId , null , this);
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
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
        }else{
            return new AsyncTaskLoader<String>(this) {
                @Nullable
                @Override
                public String loadInBackground() {
                    return null;
                }
            };
        }
    }


    private void getReviewsDataFromAPI(){
        URL url = NetworkUtils.BuildQueryReviewsUrl(Integer.toString(mMovieId));
        Bundle bundle = new Bundle();
        bundle.putString(SEARCH_QUERY_URL_EXTRA , url.toString());

        android.support.v4.app.LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> reviewsLoader = loaderManager.getLoader(ReviewsLoaderId);
        if(reviewsLoader != null){
            loaderManager.restartLoader(ReviewsLoaderId , bundle , this);
        }else{
            loaderManager.initLoader(ReviewsLoaderId , bundle , this);
        }
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        int loaderId = loader.getId();
        if(loaderId == ReviewsLoaderId){
            if(data != null && !data.equals("")){
                Review[] reviews = JsonUtils.parseWholeReviewsData(data);
                mReviewsAdapter.setReviews(reviews);
                //reviewProgressBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
