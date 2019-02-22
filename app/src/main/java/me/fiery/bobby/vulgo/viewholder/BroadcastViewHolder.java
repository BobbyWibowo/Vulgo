package me.fiery.bobby.vulgo.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import me.fiery.bobby.vulgo.R;
import me.fiery.bobby.vulgo.models.Broadcast;

/**
 * Created by bobby on 12/19/17.
 */

public class BroadcastViewHolder extends RecyclerView.ViewHolder
{
    public Context context;

    public TextView titleView;
    public TextView descriptionView;
    public TextView dateView;
    public ImageView photoView;
    public Button shareButton;
    public Button locationViewButton;

    public BroadcastViewHolder(View itemView)
    {
        super(itemView);

        context = itemView.getContext();

        titleView = itemView.findViewById(R.id.tv_item_title);
        descriptionView = itemView.findViewById(R.id.tv_item_description);
        dateView = itemView.findViewById(R.id.tv_item_date);
        photoView = itemView.findViewById(R.id.img_item_photo);

        shareButton = itemView.findViewById(R.id.button_share);
        locationViewButton = itemView.findViewById(R.id.button_view_location);
    }

    public void bindToBroadcast(Broadcast broadcast, View.OnClickListener onClickListener)
    {
        titleView.setText(broadcast.title);
        descriptionView.setText(broadcast.description);
        dateView.setText(
                DateUtils.getRelativeDateTimeString(
                        context,
                        broadcast.timestamp * 1000,
                        DateUtils.SECOND_IN_MILLIS,
                        DateUtils.DAY_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL)
        );

        Glide.with(context)
                .load(broadcast.photo)
                .placeholder(R.drawable.image_placeholder_500px).dontAnimate()
                .into(photoView);

        shareButton.setOnClickListener(onClickListener);
        locationViewButton.setOnClickListener(onClickListener);
    }
}