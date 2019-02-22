package me.fiery.bobby.vulgo;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by bobby on 1/17/18.
 */

public class GetAddressAsyncTask extends AsyncTask<Void, Void, String>
{
    final String TAG = "GetAddressAsyncTask";

    private OnTaskCompleted mListener;

    private WeakReference<Context> mContext;

    private Double mLatitude;
    private Double mLongitude;

    private Geocoder mGeocoder;

    GetAddressAsyncTask(Context context, Double latitude, Double longitude, OnTaskCompleted listener)
    {
        this.mContext = new WeakReference<>(context);

        this.mLatitude = latitude;
        this.mLongitude = longitude;

        this.mListener = listener;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();

        Context context = mContext.get();

        if (context != null)
        {
            mGeocoder = new Geocoder(context);
        }
    }

    @Override
    protected String doInBackground(Void... voids)
    {
        if (mGeocoder != null)
        {
            try
            {
                // We are using AsyncTask since otherwise this function will cause main task to freeze.
                List<Address> addresses = mGeocoder.getFromLocation(mLatitude, mLongitude, 1);
                return addresses.get(0).getAddressLine(0);
            }
            catch (IOException e)
            {
                Log.e(TAG, "GetAddressAsyncTask", e);
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s)
    {
        super.onPostExecute(s);
        mListener.onTaskCompleted(s);
    }

    public interface OnTaskCompleted
    {
        void onTaskCompleted(String address);
    }
}
