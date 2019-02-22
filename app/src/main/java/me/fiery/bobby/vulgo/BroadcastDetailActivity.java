package me.fiery.bobby.vulgo;

import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.fiery.bobby.vulgo.models.Broadcast;
import me.fiery.bobby.vulgo.models.Comment;
import me.fiery.bobby.vulgo.models.User;
import me.fiery.bobby.vulgo.translations.firebase_database.TDatabaseError;

/**
 * Created by bobby on 12/19/17.
 */

public class BroadcastDetailActivity extends BaseActivity
{
    private static final String TAG = "BroadcastDetailActivity";

    public static final String EXTRA_BROADCAST_KEY = "extra_broadcast_key";
    public static final String EXTRA_COARSE_LOCATION = "extra_coarse_location";

    private String mBroadcastUid;

    private DatabaseReference mDatabase;
    private DatabaseReference mBroadcastReference;
    private StorageReference mStorage;
    private String mBroadcastKey;
    private ValueEventListener mBroadcastListener;

    private CommentAdapter mCommentAdapter;
    private DatabaseReference mCommentsReference;

    private TextView mTitleView;
    private TextView mDescriptionView;
    private TextView mAuthorView;
    private TextView mContactView;
    private TextView mDateView;
    private TextView mRewardView;
    private TextView mLocationView;
    private TextView mDistanceView;
    private Button mLocationViewButton;
    private ImageView mPhotoView;

    private EditText mCommentField;
    private Button mCommentButton;
    private RecyclerView mCommentsRecycler;

    private Broadcast mBroadcast;

    private Location mCoarseLocation;

    private String mGeocodeAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_broadcast);

        getSupportActionBar().setTitle(getString(R.string.broadcast_activity_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        showProgressDialog();

        mBroadcastKey = getIntent().getStringExtra(EXTRA_BROADCAST_KEY);
        mCoarseLocation = getIntent().getParcelableExtra(EXTRA_COARSE_LOCATION);

        if (mBroadcastKey == null)
        {
            throw new IllegalArgumentException("Must pass EXTRA_BROADCAST_KEY");
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        // Initialize Database
        mBroadcastReference = mDatabase.child("broadcasts").child(mBroadcastKey);
        mCommentsReference = mDatabase.child("broadcast-comments").child(mBroadcastKey);

        // Initialize Views
        mTitleView = findViewById(R.id.tv_item_title);
        mAuthorView = findViewById(R.id.tv_item_author);
        mDescriptionView = findViewById(R.id.tv_item_description);
        mContactView = findViewById(R.id.tv_item_contact);
        mDateView = findViewById(R.id.tv_item_date);
        mRewardView = findViewById(R.id.tv_item_reward);
        mLocationView = findViewById(R.id.tv_item_location);
        mDistanceView = findViewById(R.id.tv_item_distance);
        mLocationViewButton = findViewById(R.id.button_view_location);
        mPhotoView = findViewById(R.id.img_item_photo);

        mCommentField = findViewById(R.id.field_comment_text);
        mCommentButton = findViewById(R.id.button_post_comment);
        mCommentsRecycler = findViewById(R.id.recycler_comments);

        mCommentButton.setOnClickListener(onClickListener);
        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initValueEventListener()
    {
        // Add value event listener to the broadcast.
        // Save to variable so it can be removed when activity ends.
        mBroadcastListener = mBroadcastReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Get Broadcast object and use the values to update the UI.
                mBroadcast = dataSnapshot.getValue(Broadcast.class);

                if (mBroadcast != null)
                {
                    double dLatitude = Double.valueOf(mBroadcast.latitude);
                    double dLongitude = Double.valueOf(mBroadcast.longitude);

                    mBroadcastUid = mBroadcast.uid;

                    mTitleView.setText(mBroadcast.title);
                    mDescriptionView.setText(mBroadcast.description);
                    mAuthorView.setText(mBroadcast.author);
                    mContactView.setText(mBroadcast.contact);
                    mDateView.setText(
                            DateUtils.getRelativeDateTimeString(
                                    getBaseContext(),
                                    mBroadcast.timestamp * 1000,
                                    DateUtils.SECOND_IN_MILLIS,
                                    DateUtils.DAY_IN_MILLIS,
                                    DateUtils.FORMAT_ABBREV_ALL)
                    );

                    if (!TextUtils.isEmpty(mBroadcast.reward))
                    {
                        mRewardView.setText(mBroadcast.reward);
                    }

                    if (mCoarseLocation != null)
                    {
                        float[] results = new float[1];
                        Location.distanceBetween(
                                mCoarseLocation.getLatitude(),
                                mCoarseLocation.getLongitude(),
                                dLatitude,
                                dLongitude,
                                results);
                        mDistanceView.setText(getString(R.string.distance, String.format(Locale.getDefault(), "%.2f", results[0] / 1000)));
                    }

                    Glide.with(getBaseContext())
                            .load(mBroadcast.photo)
                            .placeholder(R.drawable.image_placeholder_500px).dontAnimate()
                            .into(mPhotoView);

                    mLocationViewButton.setOnClickListener(onClickListener);

                    GetAddressAsyncTask getAddressAsyncTask = new GetAddressAsyncTask(getBaseContext(), dLatitude, dLongitude, new GetAddressAsyncTask.OnTaskCompleted()
                    {
                        @Override
                        public void onTaskCompleted(String address)
                        {
                            if (address != null)
                            {
                                mGeocodeAddress = address;
                                mLocationView.setText(mGeocodeAddress);
                            }
                            else
                            {
                                mLocationView.setText(getString(R.string.location_inline, mBroadcast.latitude, mBroadcast.longitude));
                                showToast(getString(R.string.location_address_failure));
                            }
                        }
                    });
                    getAddressAsyncTask.execute();

                    hideProgressDialog();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                // Getting Broadcast failed, log a message.
                Log.w(TAG, "loadBroadcast:onCancelled", databaseError.toException());

                hideProgressDialog();

                Toast.makeText(BroadcastDetailActivity.this, TDatabaseError.GetDatabaseErrorTranslation(databaseError), Toast.LENGTH_SHORT).show();
            }
        });

        // Listen for comments.
        mCommentAdapter = new CommentAdapter(this, mCommentsReference);
        mCommentsRecycler.setAdapter(mCommentAdapter);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        initValueEventListener();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        // Remove post value event listener.
        if (mBroadcastListener != null)
        {
            mBroadcastReference.removeEventListener(mBroadcastListener);
        }

        // Clean up comments listener.
        mCommentAdapter.cleanUpListener();
    }

    /*
    @Override
    protected void onResume()
    {
        super.onResume();

        initValueEventListener();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        // Remove post value event listener.
        if (mBroadcastListener != null)
        {
            mBroadcastReference.removeEventListener(mBroadcastListener);
        }

        // Clean up comments listener.
        mCommentAdapter.cleanUpListener();
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_broadcast_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int i = item.getItemId();
        switch(i)
        {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_share:
                launchShareIntentForBroadcast(mBroadcast);
                return true;
            case R.id.action_view_location:
                launchViewMapsIntentForBroadcast(mBroadcast, mGeocodeAddress);
                return true;
            case R.id.action_delete:
                deleteThisBroadcast();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            switch (view.getId())
            {
                case R.id.button_post_comment:
                    postComment();
                    break;
                case R.id.button_view_location:
                    launchViewMapsIntentForBroadcast(mBroadcast, mGeocodeAddress);
                    break;
            }
        }
    };

    private void deleteThisBroadcast()
    {
        final String userId = getUid();

        if (mBroadcastUid.equals(userId))
        {
            showConfirmationAlert(
                    null,
                    getString(R.string.alert_delete_broadcast_message),
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int whichButton)
                        {
                            showProgressDialog(getString(R.string.broadcast_deleting));

                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/broadcasts/" + mBroadcastKey, null);
                            childUpdates.put("/user-broadcasts/" + userId + "/" + mBroadcastKey, null);

                            // TODO: Update rule to allow broadcast owner to delete all broadcast comments in Firebase.
                            // TODO: For the time being, only the owner of the comment can delete their own comments.
                            // TODO: This may need to be restructured, or at least add "uid" property?
                            // childUpdates.put("/broadcast-comments/" + mBroadcastKey, null);

                            mDatabase.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        StorageReference mStorageRef = mStorage.child("broadcasts/" + mBroadcastKey);
                                        mStorageRef.delete().addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                hideProgressDialog();

                                                // NOTE: No need to check whether the task was successful or not.
                                                showToast(getString(R.string.broadcast_deleted));
                                                finish();
                                            }
                                        });
                                    } else
                                    {
                                        hideProgressDialog();
                                        showToast(getString(R.string.broadcast_deleted_failure));
                                    }
                                }
                            });

                        }
                    }
            );
        }
        else
        {
            showToast(getString(R.string.broadcast_can_not_delete));
        }
    }

    private void postComment()
    {
        final int MIN_LENGTH = 3;
        final int MAX_LENGTH = 2000;

        final String uid = getUid();

        mDatabase.child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        Log.d(TAG, "postComment:readUser:onDataChange");

                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.displayName;

                        // Create new comment object
                        String commentText = mCommentField.getText().toString();

                        if (commentText.length() >= 3 && commentText.length() <= 2000)
                        {
                            Comment comment = new Comment(uid, authorName, commentText);

                            // Push the comment, it will appear in the list
                            mCommentsReference.push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        // Clear the field
                                        mCommentField.setText(null);
                                        showToast(getString(R.string.comment_sent));
                                    }
                                    else
                                    {
                                        showToast(task.getException().getLocalizedMessage());
                                    }
                                }
                            });
                        }
                        else
                        {
                            showToast(getString(R.string.comment_min_max, String.valueOf(MIN_LENGTH), String.valueOf(MAX_LENGTH)));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {
                        Log.d(TAG, "postComment:readUser:onCancelled");
                    }
                });
    }
}

