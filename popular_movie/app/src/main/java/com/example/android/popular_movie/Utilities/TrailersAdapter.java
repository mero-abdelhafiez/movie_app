package com.example.android.popular_movie.Utilities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.popular_movie.DataModels.Video;
import com.example.android.popular_movie.R;
import com.squareup.picasso.Picasso;

import java.io.Console;
import java.net.URL;
import java.util.zip.Inflater;

public class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailersAdapterViewHolder> {

    private static final String TAG = TrailersAdapter.class.getSimpleName();
    private int numOfItems;
    private Video[] videos;
    private Context context;

    public TrailersAdapter(){

    }

    public void setVideos(Video[] videos){
        this.videos = videos;
        if(videos != null) {
            numOfItems = videos.length;
        }else{
            numOfItems = 0;
        }
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public TrailersAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.rv_video_item , parent , false);
        TrailersAdapterViewHolder viewHolder = new TrailersAdapterViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TrailersAdapterViewHolder holder, int position) {
        String videoKey = videos[position].getKey();

        String videoThumbnailUrl = NetworkUtils.getVideoThumbnail(videoKey);
        Log.d(TAG , videoThumbnailUrl);
        Picasso.with(context)
                .load(videoThumbnailUrl)
                .error(R.drawable.noimage)
                .into(holder.mVideoThumbnail);

        final Uri videoUri = NetworkUtils.BuildYoutubeUrl(videos[position].getKey());
        holder.mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW , videoUri);
                Log.d(TAG , videoUri.toString());

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return numOfItems;
    }

    public class TrailersAdapterViewHolder extends RecyclerView.ViewHolder{
        Button mPlayButton;
        ImageView mVideoThumbnail;
        public TrailersAdapterViewHolder(View itemView) {
            super(itemView);
            mPlayButton = (Button) itemView.findViewById(R.id.play_trailer_btn);
            mVideoThumbnail = (ImageView) itemView.findViewById(R.id.video_thumnnail);
        }
    }
}
