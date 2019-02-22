package me.fiery.bobby.vulgo;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;

/**
 * Created by bobby on 1/4/18.
 */

public class BaseMapsActivity extends FragmentActivity implements OnMyLocationButtonClickListener, OnMyLocationClickListener, OnMapReadyCallback
{
    public final int mFineLocationPermissionRequestCode = 1002;

    public GoogleMap mMap;

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location)
    {
        Toast.makeText(
                this,
                getString(R.string.location_inline, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude())),
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    public boolean onMyLocationButtonClick()
    {
        // TODO

        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    public void enableMyLocationButton()
    {
        // Request permission if necessary
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, mFineLocationPermissionRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {
            case mFineLocationPermissionRequestCode:
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    mMap.setMyLocationEnabled(true);
                    mMap.setOnMyLocationButtonClickListener(this);
                    mMap.setOnMyLocationClickListener(this);
                }
                break;
        }
    }
}
