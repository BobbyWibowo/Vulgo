package me.fiery.bobby.vulgo;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import me.fiery.bobby.vulgo.fragments.MyBroadcastsFragment;
import me.fiery.bobby.vulgo.fragments.RecentBroadcastsFragment;
import me.fiery.bobby.vulgo.models.User;

/**
 * Created by bobby on 12/19/17.
 */

public class MainActivity extends BaseActivity
{
    private static final String TAG = "MainActivity";

    // This data will be sent from SignUpActivity
    public static final String EXTRA_DISPLAY_NAME = "extra_display_name";

    public final int mCoarseLocationPermissionRequestCode = 1010;

    final private String TOPIC = "broadcast";

    private String mExtraDisplayName;
    private Boolean mActivityInitiated = false;

    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    private FirebaseUser mUser;
    private String mPhotoUrl;

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;

    private DatabaseReference mUserReference;
    private ValueEventListener mUserListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        mExtraDisplayName = getIntent().getStringExtra(EXTRA_DISPLAY_NAME);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Create the adapter that will return a fragment for each section
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager())
        {
            private final Fragment[] mFragments = new Fragment[] {
                    new RecentBroadcastsFragment(),
                    new MyBroadcastsFragment()
            };

            private final String[] mFragmentNames = new String[] {
                    getString(R.string.heading_recent),
                    getString(R.string.heading_mine)
            };

            @Override
            public Fragment getItem(int position)
            {
                return mFragments[position];
            }

            @Override
            public int getCount()
            {
                return mFragments.length;
            }

            @Override
            public CharSequence getPageTitle(int position)
            {
                return mFragmentNames[position];
            }
        };
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // Button launches NewBroadcastActivity
        findViewById(R.id.fab_new_broadcast).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivity.this, NewBroadcastActivity.class));
            }
        });

        /*
         * Codes below will someday be used to handle notifications.
         * At the moment I don't know anything about them though.
         * They aren't working yet anyways.
         */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            if (notificationManager != null)
            {
                notificationManager.createNotificationChannel(new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW));
            }
        }

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        if (getIntent().getExtras() != null)
        {
            for (String key : getIntent().getExtras().keySet())
            {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(onSuccessListener);
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, mCoarseLocationPermissionRequestCode);
        }

        if (!mActivityInitiated)
        {
            showProgressDialog(getString(R.string.user_profile_load));
            checkUserProfile();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case mCoarseLocationPermissionRequestCode:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(onSuccessListener);
                }
                break;
        }
    }

    OnSuccessListener<Location> onSuccessListener = new OnSuccessListener<Location>()
    {
        @Override
        public void onSuccess(Location location)
        {
            Log.d(TAG, "getLocation:onSuccess");
            mLastLocation = location;
        }
    };

    public Location getLastLocation()
    {
        return this.mLastLocation;
    }

    private void checkUserProfile()
    {
        final String userUid = getUid();

        Uri photoUrl = mUser.getPhotoUrl();
        if (photoUrl != null)
        {
            mPhotoUrl = photoUrl.toString();
        }

        mUserReference = mDatabase.child("users").child(userUid);
        mUserListener = mUserReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Log.d(TAG, "readUser:onDataChange");

                // Get user value
                User user = dataSnapshot.getValue(User.class);

                // If profile data do not exist
                // or if profile data is null
                // or if photo from FirebaseAuth exist but it is not recorded in profile data
                if (!dataSnapshot.exists() || user == null || (mPhotoUrl != null && !dataSnapshot.hasChild("photoUri")))
                {
                    // register user profile (it will also overwrite existing)
                    registerUserProfile();
                }
                else
                {
                    hideProgressDialog();

                    // Remove value event listener
                    mUserReference.removeEventListener(mUserListener);

                    showSnackbar(getString(R.string.logged_in_as, user.displayName));
                    mActivityInitiated = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.d(TAG, "readUser:onCancelled");

                finish(); // Close app
            }
        });
    }

    private void registerUserProfile()
    {
        // NOTE: This is not part of readUserProfile() due to doubts
        // as to how the value event listener thingy work.
        User userProfile;

        // Write new user's profile
        if (mExtraDisplayName != null)
        {
            Log.d(TAG, "mExtraDisplayName: " + String.valueOf(mExtraDisplayName));
            userProfile = new User(mUser.getEmail(), mExtraDisplayName, mPhotoUrl);
        }
        else
        {
            Log.d(TAG, "getDisplayName(): " + String.valueOf(mUser.getDisplayName()));
            userProfile = new User(mUser.getEmail(), mUser.getDisplayName(), mPhotoUrl);
        }

        mDatabase.child("users").child(mUser.getUid()).setValue(userProfile);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // TODO
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        String msg;
        int i = item.getItemId();
        switch(i)
        {
            case R.id.action_logout:
                showConfirmationAlert(
                        null,
                        getString(R.string.alert_sign_out_message),
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                Intent intent = new Intent(getBaseContext(), SignInActivity.class);
                                intent.putExtra(SignInActivity.EXTRA_IS_SIGN_OUT, true);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }
                );
                return true;
            case R.id.action_subscribe:
                FirebaseMessaging.getInstance().subscribeToTopic(TOPIC);
                msg = getString(R.string.ok);
                Log.d(TAG, msg);
                showToast(msg);
                return true;
            case R.id.action_unsubscribe:
                FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC);
                msg = getString(R.string.ok);
                Log.d(TAG, msg);
                showToast(msg);
                return true;
            case R.id.action_token:
                // Get token
                String token = FirebaseInstanceId.getInstance().getToken();
                msg = getString(R.string.your_token, token);
                Log.d(TAG, msg);
                showToast(msg);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

