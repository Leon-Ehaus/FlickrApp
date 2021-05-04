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
            imageView = itemView.findViewById(R.id.item_image);
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
        //Load image from URL into the imageView
        Picasso.get().load(urlString).into(imageView);
    }


    /**
     * @return number if items in the data list or Int_Max if the adapter should loop
     */
    @Override
    public int getItemCount() {
        return looping ? Integer.MAX_VALUE : mData.size();
    }

    /**
     * @param looping enables looping if true, disables otherwise
     */
    public void setLooping(boolean looping) {
        if (this.looping != looping) {
            this.looping = looping;
            notifyDataSetChanged();
        }
    }

    /**
     * Deletes all items from the adapter
     */
    public void clear() {
        int curSize = getItemCount();
        this.mData.clear();
        notifyItemRangeRemoved(0, curSize);
    }

    /**
     * Adds the items to the adapter at the end of the list
     * @param newData a list of urls to be added
     */
    public void addData(List<String> newData) {
        int curSize = getItemCount();
        this.mData.addAll(newData);
        notifyItemRangeInserted(curSize, newData.size());
    }
}
