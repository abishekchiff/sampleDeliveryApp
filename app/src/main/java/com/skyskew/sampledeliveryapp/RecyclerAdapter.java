package com.skyskew.sampledeliveryapp;

/**
 * Created by Chiffon on 22/06/17.
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.PhotoHolder> {

    private ArrayList<ConsigneeDetail> mDetails;

    public RecyclerAdapter(ArrayList<ConsigneeDetail> details) {
        mDetails = details;
    }

    @Override
    public RecyclerAdapter.PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item_row, parent, false);
        return new PhotoHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.PhotoHolder holder, int position) {
        ConsigneeDetail item = mDetails.get(position);
        if(!item.isCompleted()) {
            holder.bindItem(item);
        }
    }

    @Override
    public int getItemCount() {
        return mDetails.size();
    }

    public static class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //2

        private TextView mItemName;
        private TextView mItemDistance;
        private ConsigneeDetail mDetail;

        //
        private static final String PHOTO_KEY = "PHOTO";

        //4
        public PhotoHolder(View v) {
            super(v);


            mItemName = (TextView) v.findViewById(R.id.item_name);
            mItemDistance = (TextView) v.findViewById(R.id.item_distance);
            v.setOnClickListener(this);
        }

        //5
        @Override
        public void onClick(View v) {
            Context context = itemView.getContext();
            Intent showViewIntent = new Intent(context, ViewConsignmentActivity.class);
//            showViewIntent.putExtra("name",mDetail.getName());
            showViewIntent.putExtra("dist", mDetail.getDistance());
//            showViewIntent.putExtra("locLat",mDetail.getLat());
//            showViewIntent.putExtra("locLon",mDetail.getLon());

            Bundle args= new Bundle();
            args.putSerializable("CONSIGNEE DETAIL", (Serializable)mDetail);
            showViewIntent.putExtra("BUNDLE FROM CARD",args);
            context.startActivity(showViewIntent);
        }
        public void bindItem(ConsigneeDetail item) {
            mDetail = item;

            Log.d("detail hash",String.valueOf(mDetail.hashCode()));
            //Picasso.with(mItemImage.getContext()).load(p.getUrl()).into(mItemImage);
            mItemName.setText(mDetail.getName());


            Log.d("mdetialdistance",String.valueOf(mDetail.getDistance()));

            float distance= mDetail.getDistance();
            if(distance>1000)
            {
                distance = distance/1000;

                mItemDistance.setText(String.format("%.1f",distance) + " KM");
            }else {
               mItemDistance.setText(String.valueOf((int)distance+" M"));
            }

        }
    }
}
