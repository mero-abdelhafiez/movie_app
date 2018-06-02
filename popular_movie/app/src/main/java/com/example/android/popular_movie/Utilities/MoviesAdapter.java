package com.example.android.popular_movie.Utilities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popular_movie.DataModels.Movie;
import com.example.android.popular_movie.MainActivity;
import com.example.android.popular_movie.R;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieAdapterViewHolder> {
    private static final String TAG = MoviesAdapter.class.getSimpleName();
    private Movie[] movies;
    private int numOfItems;
    private Context context;
    private MoviesAdapterOnClickHandler itemClickHandler;

    public MoviesAdapter(MoviesAdapterOnClickHandler handler){
        this.itemClickHandler = handler;
    }

    @NonNull
    @Override
    public MovieAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflator = LayoutInflater.from(context);
        boolean attachToParentImmediately = false;
        View view = inflator.inflate(R.layout.rv_list_item, parent , attachToParentImmediately);
        MovieAdapterViewHolder viewHolder = new MovieAdapterViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MovieAdapterViewHolder holder, int position) {
        holder.mMovieName.setText(movies[position].getTitle() + "(" + String.valueOf(movies[position].getYear()) + ")");
        Log.d(TAG , String.valueOf(movies[position].getYear()));
        holder.mMovieRating.setText(String.valueOf(movies[position].getRating()));
        String imageUrl = NetworkUtils.formImageFullPath(movies[position].getPoster_Path() , false);
        Picasso.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.noimage)
                .error(R.drawable.noimage)
                .into(holder.mMoviePoster);
    }

    @Override
    public int getItemCount() {
        return  numOfItems;
    }

    public void setMovies(Movie[] movies){
        this.movies = movies;
        this.numOfItems = movies.length;
        notifyDataSetChanged();
    }
    public interface MoviesAdapterOnClickHandler{
        void onClick(Movie data);
    }
    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView mMoviePoster;
        TextView mMovieName;
        TextView mMovieRating;
        public MovieAdapterViewHolder(View itemView) {
            super(itemView);
            mMovieName = (TextView) itemView.findViewById(R.id.tv_item_name);
            mMoviePoster = (ImageView) itemView.findViewById(R.id.iv_item_image);
            mMovieRating = (TextView) itemView.findViewById(R.id.tv_item_value);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            itemClickHandler.onClick(movies[position]);
        }
    }
}
