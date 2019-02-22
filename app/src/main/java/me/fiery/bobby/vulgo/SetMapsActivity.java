package me.fiery.bobby.vulgo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class SetMapsActivity extends BaseMapsActivity
{
    private static final String TAG = "SetMapsActivity";

    public static final String EXTRA_LATITUDE_KEY = "latitude_key";
    public static final String EXTRA_LONGITUDE_KEY = "longitude_key";

    private Button mSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_maps);

        mSubmitButton = findViewById(R.id.button_submit_location);
        mSubmitButton.setOnClickListener(onSubmitButtonClicked);

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

        enableMyLocationButton();

        mMap.setPadding(0, 0, 0, mSubmitButton.getHeight());

        String latitude = getIntent().getStringExtra(EXTRA_LATITUDE_KEY);
        String longitude = getIntent().getStringExtra(EXTRA_LONGITUDE_KEY);

        if (latitude != null && longitude != null)
        {
            // Move camera to previously selected latitude & longitude
            LatLng latlng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        }
    }

    protected void returnMapData(LatLng location)
    {
        Log.d(TAG, "returnMapData() called");

        Intent intent = this.getIntent();
        intent.putExtra(EXTRA_LATITUDE_KEY, String.valueOf(location.latitude));
        intent.putExtra(EXTRA_LONGITUDE_KEY, String.valueOf(location.longitude));
        this.setResult(RESULT_OK, intent);

        finish();
    }

    View.OnClickListener onSubmitButtonClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            if (mMap != null)
            {
                returnMapData(mMap.getCameraPosition().target);
            }
        }
    };
}
