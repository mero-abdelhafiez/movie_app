package com.example.android.popular_movie;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.android.popular_movie.DataModels.Genre;
import com.example.android.popular_movie.DataModels.Movie;
import com.example.android.popular_movie.Utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = DetailActivity.class.getSimpleName();
    private Movie movie;
    private ImageView mMoviePoster , mMovieBackdrop;
    private TextView mMovieOriginalTitle , mMovieOverview , mMoviePopularity , mMovieGenres , mMovieRatingText;
    private RatingBar mMovieRating;
    private TextView mMovieRatingCount;
    private SharedPreferences sharedPreferences;
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
        mMovieRating = findViewById(R.id.rb_movie_rating);
        mMovieRatingText = findViewById(R.id.tv_movie_rating);
        mMovieRatingCount = (TextView) findViewById(R.id.tv_movie_rate_count);
        Intent callingIntent = getIntent();
        if(callingIntent.hasExtra(Intent.EXTRA_TEXT)){
            movie = (Movie) callingIntent.getParcelableExtra(Intent.EXTRA_TEXT);
        }
        populateFields(movie);
        setTitle(movie.getTitle() + " (" + movie.getYear() + ")");
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
        mMovieRating.setNumStars(10);
        mMovieRatingCount.setText(String.valueOf(movie.getRateCount()) + " Votes");
        mMovieRating.setRating((float) movie.getRating());
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
}
