package com.example.android.popular_movie.Utilities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popular_movie.DataModels.Review;
import com.example.android.popular_movie.R;

import org.w3c.dom.Text;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewAdapterViewHolder> {

    private int numOfItems;
    private Review[] reviews;
    private Context context;

    public ReviewsAdapter(){

    }

    public void setReviews(Review[] reviews) {
        this.reviews = reviews;
        if(reviews == null){
            numOfItems = 0;
        }else{
            numOfItems = reviews.length;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.rv_review_item , parent , false);
        ReviewAdapterViewHolder viewHolder = new ReviewAdapterViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapterViewHolder holder, int position) {
        holder.mAuthorName.setText(reviews[position].getAuthor());
        holder.mContent.setText(reviews[position].getContent());
    }

    @Override
    public int getItemCount() {
        return numOfItems;
    }

    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder{
        TextView mAuthorName;
        TextView mContent;
        public ReviewAdapterViewHolder(View itemView) {
            super(itemView);
            mAuthorName = (TextView) itemView.findViewById(R.id.author_name);
            mContent = (TextView) itemView.findViewById(R.id.content);
        }
    }
}
