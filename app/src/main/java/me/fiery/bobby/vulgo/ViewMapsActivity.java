package me.fiery.bobby.vulgo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class ViewMapsActivity extends BaseMapsActivity
{
    private static final String TAG = "BaseMapsActivity";

    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_LATITUDE_KEY = "extra_latitude_key";
    public static final String EXTRA_LONGITUDE_KEY = "extra_longitude_key";
    public static final String EXTRA_GEOCODE_ADDRESS = "extra_geocode_address";

    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        super.onMapReady(googleMap);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String latitude = getIntent().getStringExtra(EXTRA_LATITUDE_KEY);
        String longitude = getIntent().getStringExtra(EXTRA_LONGITUDE_KEY);
        String geocodeAddress = getIntent().getStringExtra(EXTRA_GEOCODE_ADDRESS);

        double dLatitude = Double.valueOf(latitude);
        double dLongitude = Double.valueOf(longitude);

        if (latitude != null && longitude != null)
        {
            int size_in_dp = 32;  // 32 dps
            final float scale = getResources().getDisplayMetrics().density;
            int size_in_px = (int) (size_in_dp * scale + 0.5f);

            Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pin_map);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, size_in_px, size_in_px, false);

            LatLng latlng = new LatLng(dLatitude, dLongitude);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latlng)
                    .title(title)
                    .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap));

            if (geocodeAddress != null)
            {
                markerOptions.snippet(geocodeAddress);
            }
            else
            {
                GetAddressAsyncTask getAddressAsyncTask = new GetAddressAsyncTask(getBaseContext(), dLatitude, dLongitude, new GetAddressAsyncTask.OnTaskCompleted()
                {
                    @Override
                    public void onTaskCompleted(String address)
                    {
                        if (address != null && mMarker != null)
                        {
                            mMarker.setSnippet(address);
                        }
                        else
                        {
                            Toast.makeText(getBaseContext(), getString(R.string.location_address_failure), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                getAddressAsyncTask.execute();
            }

            // Add a marker and move camera to the marker
            mMarker = mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        }
    }
}
