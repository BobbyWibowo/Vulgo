package me.fiery.bobby.vulgo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import me.fiery.bobby.vulgo.models.Broadcast;

/**
 * Created by bobby on 12/19/17.
 */

public class BaseActivity extends AppCompatActivity
{
    public String TAG = "BaseActivity";

    public DatabaseReference mDatabase;
    public FirebaseAuth mAuth;

    private ProgressDialog mProgressDialog;

    public View mRootView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mRootView = findViewById(android.R.id.content);
    }

    private void makeProgressDialog()
    {
        if (mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
        }
    }

    public void showProgressDialog(@NonNull String message)
    {
        makeProgressDialog();

        mProgressDialog.setMessage(message);

        if (!mProgressDialog.isShowing())
        {
            mProgressDialog.show();
        }
    }

    public void showProgressDialog()
    {
        makeProgressDialog();

        mProgressDialog.setMessage(getString(R.string.loading));

        if (!mProgressDialog.isShowing())
        {
            mProgressDialog.show();
        }
    }

    public void hideProgressDialog()
    {
        if (mProgressDialog != null && mProgressDialog.isShowing())
        {
            mProgressDialog.dismiss();
        }
    }

    public void showConfirmationAlert(@Nullable String title, String message, DialogInterface.OnClickListener onClickListener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(R.string.yes, onClickListener)
                .setNegativeButton(R.string.no, null);

        if (title != null)
        {
            builder.setTitle(title);
        }

        builder.show();
    }

    public String getUid()
    {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void showSnackbar(String message)
    {
        Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG).show();
    }

    public void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void launchShareIntentForBroadcast(Broadcast broadcast)
    {
        if (broadcast != null)
        {
            try
            {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                String shareMessage = getString(R.string.share_message, broadcast.title, broadcast.latitude, broadcast.longitude, broadcast.description, broadcast.contact);
                intent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(intent, getString(R.string.share_via)));
            }
            catch (Exception e)
            {
                Log.d(TAG, "launchShareIntentForBroadcast", e);
            }
        }
        else
        {
            Log.d(TAG, "launchShareIntentForBroadcast:null");
        }
    }

    public void launchViewMapsIntentForBroadcast(Broadcast broadcast, @Nullable String geocodeAddress)
    {
        if (broadcast == null || broadcast.latitude == null || broadcast.longitude == null)
        {
            showToast(getString(R.string.location_missing));
        }
        else
        {
            Intent viewMapIntent = new Intent(this, ViewMapsActivity.class);

            viewMapIntent.putExtra(ViewMapsActivity.EXTRA_TITLE, broadcast.title);
            viewMapIntent.putExtra(ViewMapsActivity.EXTRA_LATITUDE_KEY, broadcast.latitude);
            viewMapIntent.putExtra(ViewMapsActivity.EXTRA_LONGITUDE_KEY, broadcast.longitude);

            if (geocodeAddress != null)
            {
                viewMapIntent.putExtra(ViewMapsActivity.EXTRA_GEOCODE_ADDRESS, geocodeAddress);
            }

            startActivity(viewMapIntent);
        }
    }
}