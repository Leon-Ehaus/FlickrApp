package com.example.flickrapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ImageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> mData;

    private LayoutInflater mInflater;

    private boolean looping = false;

    protected class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    ImageRecyclerViewAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.picture_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String urlString = mData.get(position % mData.size());
        ImageView imageView = ((ViewHolder) holder).imageView;
        Picasso.get().load(urlString).into(imageView);
    }


    @Override
    public int getItemCount() {
        return looping ? Integer.MAX_VALUE : mData.size();
    }

    public void setLooping(boolean looping) {
        if (this.looping != looping) {
            this.looping = looping;
            notifyDataSetChanged();
        }
    }

    public void clear() {
        int curSize = getItemCount();
        this.mData.clear();
        notifyItemRangeRemoved(0, curSize);
    }

    public void addData(List<String> newData) {
        int curSize = getItemCount();
        this.mData.addAll(newData);
        notifyItemRangeInserted(curSize, newData.size());
    }
}
