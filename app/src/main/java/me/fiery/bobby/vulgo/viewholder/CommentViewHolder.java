package me.fiery.bobby.vulgo.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import me.fiery.bobby.vulgo.R;

/**
 * Created by bobby on 1/17/18.
 */

public class CommentViewHolder extends RecyclerView.ViewHolder
{
    public TextView authorView;
    public TextView bodyView;

    public CommentViewHolder(View itemView)
    {
        super(itemView);

        authorView = itemView.findViewById(R.id.comment_author);
        bodyView = itemView.findViewById(R.id.comment_body);
    }
}