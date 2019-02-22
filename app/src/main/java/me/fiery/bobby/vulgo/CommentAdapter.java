package me.fiery.bobby.vulgo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

import me.fiery.bobby.vulgo.models.Comment;
import me.fiery.bobby.vulgo.viewholder.CommentViewHolder;

/**
 * Created by bobby on 1/17/18.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder>
{
    private final String TAG = "CommentAdapter";

    private Context mContext;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    private List<String> mCommentIds = new ArrayList<>();
    private List<Comment> mComments = new ArrayList<>();

    public CommentAdapter(final Context context, DatabaseReference ref)
    {
        mContext = context;
        mDatabaseReference = ref;

        // Create child event listener
        // Store reference to listener so it can be removed on app stop
        mChildEventListener = ref.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName)
            {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new comment has been added, add it to the displayed list
                Comment comment = dataSnapshot.getValue(Comment.class);

                // Update RecyclerView
                mCommentIds.add(dataSnapshot.getKey());
                mComments.add(comment);
                notifyItemInserted(mComments.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName)
            {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so displayed the changed comment.
                Comment newComment = dataSnapshot.getValue(Comment.class);
                String commentKey = dataSnapshot.getKey();

                int commentIndex = mCommentIds.indexOf(commentKey);
                if (commentIndex > -1)
                {
                    // Replace with the new data
                    mComments.set(commentIndex, newComment);

                    // Update the RecyclerView
                    notifyItemChanged(commentIndex);
                }
                else
                {
                    Log.w(TAG, "onChildChanged:unknown_child:" + commentKey);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                String commentKey = dataSnapshot.getKey();

                int commentIndex = mCommentIds.indexOf(commentKey);
                if (commentIndex > -1)
                {
                    // Remove data from the list
                    mCommentIds.remove(commentIndex);
                    mComments.remove(commentIndex);

                    // Update the RecyclerView
                    notifyItemRemoved(commentIndex);
                }
                else
                {
                    Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName)
            {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A comment has changed position, use the key to determine if we are
                // displaying this comment and if so move it.
                Comment movedComment = dataSnapshot.getValue(Comment.class);
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(mContext, mContext.getResources().getString(R.string.comment_load_failure), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position)
    {
        Comment comment = mComments.get(position);
        holder.authorView.setText(comment.author);
        holder.bodyView.setText(comment.text);
    }

    @Override
    public int getItemCount()
    {
        return mComments.size();
    }

    public void cleanUpListener()
    {
        if (mChildEventListener != null)
        {
            mDatabaseReference.removeEventListener(mChildEventListener);
        }
    }
}
