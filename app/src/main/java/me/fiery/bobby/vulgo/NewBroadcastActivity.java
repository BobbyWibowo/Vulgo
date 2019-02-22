package me.fiery.bobby.vulgo;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import me.fiery.bobby.vulgo.models.Broadcast;
import me.fiery.bobby.vulgo.models.User;

/**
 * Created by bobby on 12/19/17.
 */

public class NewBroadcastActivity extends BaseActivity
{
    private static final String TAG = "NewBroadcastActivity";

    private final int RC_MAP = 101;
    private final int RC_PHOTO = 102;

    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    private EditText mTitleField;
    private EditText mDescriptionField;
    private EditText mContactField;
    private EditText mRewardField;

    private TextView mLocationTextView;
    private ColorStateList mLocationTextViewColor;
    private Button mMapButton;
    private ImageView mPhotoImageView;
    private Button mPhotoButton;
    private FloatingActionButton mSubmitButton;

    private String mLatitude;
    private String mLongitude;

    private Uri mPhoto;

    private Geocoder mGeocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_broadcast);

        getSupportActionBar().setTitle(getString(R.string.new_broadcast_activity_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        mTitleField = findViewById(R.id.field_title);
        mDescriptionField = findViewById(R.id.field_description);
        mContactField = findViewById(R.id.field_contact);
        mRewardField = findViewById(R.id.field_reward);
        mLocationTextView = findViewById(R.id.tv_location);
        mMapButton = findViewById(R.id.button_set_location);
        mPhotoImageView = findViewById(R.id.img_item_photo);
        mPhotoButton = findViewById(R.id.button_set_photo);
        mSubmitButton = findViewById(R.id.fab_submit_broadcast);

        mLocationTextViewColor = mLocationTextView.getTextColors();

        mPhotoButton.setOnClickListener(onClickListener);
        mMapButton.setOnClickListener(onClickListener);
        mSubmitButton.setOnClickListener(onClickListener);

        mGeocoder = new Geocoder(this);

        // Set user's email address as the default value for contact field
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null)
        {
            mContactField.setText(firebaseUser.getEmail());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int i = item.getItemId();
        switch(i)
        {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            switch (requestCode)
            {
                case RC_MAP:
                    mLatitude = data.getStringExtra(SetMapsActivity.EXTRA_LATITUDE_KEY);
                    mLongitude = data.getStringExtra(SetMapsActivity.EXTRA_LONGITUDE_KEY);

                    mLocationTextView.setText(R.string.location_load);
                    mLocationTextView.setTextColor(mLocationTextViewColor);

                    GetAddressAsyncTask getAddressAsyncTask = new GetAddressAsyncTask(getBaseContext(), Double.valueOf(mLatitude), Double.valueOf(mLongitude), new GetAddressAsyncTask.OnTaskCompleted()
                    {
                        @Override
                        public void onTaskCompleted(String address)
                        {
                            if (address != null)
                            {
                                mLocationTextView.setText(address);
                            }
                            else
                            {
                                mLocationTextView.setText(getString(R.string.location_inline, mLatitude, mLongitude));
                                showToast(getString(R.string.location_address_failure));
                            }
                        }
                    });
                    getAddressAsyncTask.execute();

                    break;
                case RC_PHOTO:
                    mPhoto = data.getData();
                    mPhotoImageView.setImageURI(mPhoto);
                    break;
            }
        }
        else
        {
            Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            switch (view.getId())
            {
                case R.id.button_set_location:
                    Intent mapIntent = new Intent(NewBroadcastActivity.this, SetMapsActivity.class);
                    if (mLatitude != null && mLongitude != null)
                    {
                        // Pass previously selected latitude & longitude back to map intent.
                        mapIntent.putExtra(SetMapsActivity.EXTRA_LATITUDE_KEY, mLatitude);
                        mapIntent.putExtra(SetMapsActivity.EXTRA_LONGITUDE_KEY, mLongitude);
                    }
                    startActivityForResult(mapIntent, RC_MAP);
                    break;
                case R.id.button_set_photo:
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, getString(R.string.picture_via)), RC_PHOTO);
                    break;
                case R.id.fab_submit_broadcast:
                    submitPost();
                    break;
            }
        }
    };

    private void submitPost()
    {
        if (!mSubmitButton.isEnabled())
        {
            return;
        }

        final String title = mTitleField.getText().toString();
        final String description = mDescriptionField.getText().toString();
        final String contact = mContactField.getText().toString();
        final String reward = mRewardField.getText().toString();
        final String latitude = mLatitude;
        final String longitude = mLongitude;

        boolean result = true;

        // Title is required
        if (TextUtils.isEmpty(title))
        {
            mTitleField.setError(getString(R.string.required));
            result = false;
        }
        else
        {
            mTitleField.setError(null);
        }

        // Description is required
        if (TextUtils.isEmpty(description))
        {
            mDescriptionField.setError(getString(R.string.required));
            result = false;
        }
        else
        {
            mDescriptionField.setError(null);
        }

        // Contact is required
        if (TextUtils.isEmpty(contact))
        {
            mContactField.setError(getString(R.string.required));
            result = false;
        }
        else
        {
            mContactField.setError(null);
        }

        // Location is required
        if (latitude == null || longitude == null)
        {
            mLocationTextView.setText(getString(R.string.location_required));
            mLocationTextView.setTextColor(Color.RED);
            result = false;
        }

        if (!result)
        {
            return;
        }

        showConfirmationAlert(
                null,
                getString(R.string.alert_new_broadcast_message),
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        // Disable button so there are no multi-posts
                        setEditingEnabled(false);
                        showProgressDialog(getString(R.string.broadcast_writing));

                        final String userId = getUid();
                        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                                new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot)
                                    {
                                        // Get user value
                                        User user = dataSnapshot.getValue(User.class);

                                        if (user == null)
                                        {
                                            // User is null, error out
                                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                                            showSnackbar("Gagal memuat profil user!");

                                            hideProgressDialog();
                                            setEditingEnabled(true);
                                        }
                                        else
                                        {
                                            startWritingBroadcast(userId, user.displayName, title, description, contact, reward, latitude, longitude);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError)
                                    {
                                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());

                                        hideProgressDialog();
                                        setEditingEnabled(true);
                                    }
                                }
                        );
                    }
                }
        );
    }

    private void setEditingEnabled(boolean enabled)
    {
        mTitleField.setEnabled(enabled);
        mDescriptionField.setEnabled(enabled);
        mContactField.setEnabled(enabled);
        mRewardField.setEnabled(enabled);
        mMapButton.setEnabled(enabled);
        mPhotoButton.setEnabled(enabled);
        mSubmitButton.setEnabled(enabled);
    }

    private void startWritingBroadcast(
            String userId,
            String displayName,
            String title,
            String description,
            String contact,
            String reward,
            String latitude,
            String longitude)
    {
        final String key = mDatabase.child("broadcasts").push().getKey();

        if (mPhoto == null)
        {
            // If there's no photo, directly write new broadcast.
            writeNewBroadcast(key, userId, displayName, title, description, contact, reward, latitude, longitude, null);
        }
        else
        {
            // If there's a photo, upload it first -- writeNewBroadcast() will be called by uploadPhoto() later.
            uploadPhoto(key, userId, displayName, title, description, contact, reward, latitude, longitude);
        }
    }

    private void uploadPhoto(
            final String key,
            final String userId,
            final String displayName,
            final String title,
            final String description,
            final String contact,
            final String reward,
            final String latitude,
            final String longitude)
    {
        final StorageReference mStorageRef = mStorage.child("broadcasts/" + key);
        mStorageRef.putFile(mPhoto)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        Log.d(TAG, "uploadPhoto:onSuccess");

                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        mStorageRef.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>()
                                {
                                    @Override
                                    public void onSuccess(Uri uri)
                                    {
                                        // Write new broadcast
                                        writeNewBroadcast(key, userId, displayName, title, description, contact, reward, latitude, longitude, uri.toString());
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener()
                                {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        Log.d(TAG, "getUploadUrl:onFailure", e);

                                        hideProgressDialog();
                                        showToast(getString(R.string.broadcast_upload_failure));
                                        setEditingEnabled(true);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Log.d(TAG, "uploadPhoto:onFailure", e);

                        hideProgressDialog();
                        showToast(getString(R.string.broadcast_upload_failure));
                        setEditingEnabled(true);
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        showProgressDialog(getString(R.string.broadcast_upload_progress, progress));
                    }
                });
    }

    private void writeNewBroadcast(
            final String key,
            final String userId,
            final String displayName,
            final String title,
            final String description,
            final String contact,
            @Nullable final String reward,
            final String latitude,
            final String longitude,
            @Nullable final String photo)
    {
        long timestamp = System.currentTimeMillis() / 1000;

        // Create new post at /user-broadcasts/$userid/$broadcastid and at
        // /broadcasts/$broadcastid simultaneously
        Broadcast broadcast = new Broadcast(userId, displayName, title, description, contact, reward, latitude, longitude, photo, timestamp);
        Map<String, Object> broadcastValues = broadcast.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/broadcasts/" + key, broadcastValues);
        childUpdates.put("/user-broadcasts/" + userId + "/" + key, broadcastValues);

        // TODO: Preferably only add "uid" property for auth purpose.
        // childUpdates.put("/broadcast-comments/" + key + "/.data", null);

        mDatabase.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                hideProgressDialog();

                if (task.isSuccessful())
                {
                    Log.d(TAG, "writeBroadcast:onComplete:isSuccessful");

                    // Finish this Activity, back to the stream
                    showToast(getString(R.string.broadcast_sent));
                    finish();
                }
                else
                {
                    Log.d(TAG, "writeBroadcast:onComplete:isFailure");

                    showToast(getString(R.string.broadcast_create_failure));
                    setEditingEnabled(true);
                }
            }
        });
    }
}