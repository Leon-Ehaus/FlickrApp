package com.example.flickrapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ButtonRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> mData;
    private LayoutInflater mInflater;
    private Context context;

    protected class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        Button button;

        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.queryText);
            button = itemView.findViewById(R.id.button);
        }
    }

    ButtonRecyclerViewAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.button_item, parent, false);
        return new ButtonRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String query = mData.get(position);
        TextView text = ((ViewHolder) holder).textView;
        text.setText(query);
        Button button = ((ViewHolder) holder).button;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ScrollingActivity)context).newSearch(query);
            }
        });

    }


    @Override
    public int getItemCount() {
        return mData.size();
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
