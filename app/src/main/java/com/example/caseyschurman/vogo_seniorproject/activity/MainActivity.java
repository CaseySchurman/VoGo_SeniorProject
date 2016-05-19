/*
* Created By: Casey Schurman
* Purpose: MainActivity is the activity for the app's main page.
* Summary: The user's basic information is displayed to the screen, as well as the user's current
* location being grabbed and displayed on a google map.
* */

package com.example.caseyschurman.vogo_seniorproject.activity;

//Project libraries
import com.example.caseyschurman.vogo_seniorproject.R;
import com.example.caseyschurman.vogo_seniorproject.app.LocationCoordinates;
import com.example.caseyschurman.vogo_seniorproject.app.VolunteerOpportunity;
import com.example.caseyschurman.vogo_seniorproject.app.getComplete;
import com.example.caseyschurman.vogo_seniorproject.helper.OpportunitiesMySQLHandler;
import com.example.caseyschurman.vogo_seniorproject.helper.SQLiteHandler;
import com.example.caseyschurman.vogo_seniorproject.helper.SessionManager;
import com.example.caseyschurman.vogo_seniorproject.volunteermatch.ApiService;
import com.example.caseyschurman.vogo_seniorproject.volunteermatch.SearchResult;
import com.example.caseyschurman.vogo_seniorproject.volunteermatch.SearchQuery;
import com.example.caseyschurman.vogo_seniorproject.volunteermatch.SearchOpportunities;

//Java library
import java.util.ArrayList;
import java.util.HashMap;

//Android libraries
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.support.v4.app.FragmentActivity;

//Google (Current user location) libraries
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

//Google (Maps) libraries
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback{

    private ListView listView;
    private TextView txtFirst_Name;
    private TextView txtLast_Name;
    private TextView txtEmail;
    private Button btnLogout;
    private GoogleApiClient mGoogleApiClient;

    private SQLiteHandler db;
    private SessionManager session;

    private GoogleMap mMap;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    public static final String TAG = MainActivity.class.getSimpleName();

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


    @Override
    public void onConnected(Bundle connectionHint) {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        catch (SecurityException e){
            Log.e(TAG, "Error: App must have access to device location. Please enable.");
        }
        if (mLastLocation == null) {
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
            catch (SecurityException e){
                Log.e(TAG, "Error: App must have access to device location. Please enable.");
            }
        }
        else {
            handleNewLocation(mLastLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        setUpMap();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.activity_list_item, android.R.id.text1);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        //10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); //1 second, in milliseconds

        ArrayList<VolunteerOpportunity> opportunities =  OpportunitiesMySQLHandler.getOpportunities(new getComplete() {

            @Override
            public void onGetCompleted(final ArrayList<VolunteerOpportunity> opportunities, boolean error, String message) {

                listView = (ListView) findViewById(R.id.activityList);

                String[] values = new String[opportunities.size()];

                for(int i = 0; i < opportunities.size(); i++){
                    values[i] = opportunities.get(i).getName();
                    adapter.add(values[i]);
                }

                // Assign adapter to ListView
                listView.setAdapter(adapter);

                BitmapDescriptor bitmapDescriptor
                        = BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_AZURE);

                for(int i = 0; i <opportunities.size(); i++){
                    LatLng location = LocationCoordinates.getAddressFromLocation(opportunities.get(i).getAddress(), getApplicationContext());
                    mMap.addMarker(new MarkerOptions().position(location).icon(bitmapDescriptor).title(opportunities.get(i).getName()));
                }

                listView.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> a, View v, int position, long id) {

                        int ID = (int) id;

                        TextView title;
                        TextView relevantSkills;
                        TextView organization;
                        TextView address;

                        String sRelevantSkills = new String();

                        PopupWindow popup = new PopupWindow(MainActivity.this);
                        View layout = getLayoutInflater().inflate(R.layout.popup_content, null);
                        popup.setContentView(layout);


                        title = (TextView) layout.findViewById(R.id.detailsTitle);
                        relevantSkills = (TextView) layout.findViewById(R.id.detailsRelevantSkills);
                        organization = (TextView) layout.findViewById(R.id.detailsOrganization);
                        address = (TextView) layout.findViewById(R.id.detailsAddress);

                        title.setText("Title: " + opportunities.get(ID).getName());
                        relevantSkills.setText("Relevant Skills: " + opportunities.get(ID).getRelevantSkills());
                        organization.setText("Organization: " + opportunities.get(ID).getOrganization());
                        address.setText("Address: " + opportunities.get(ID).getAddress());

                        // Set content width and height
                        popup.setHeight(600);
                        popup.setWidth(800);
                        // Closes the popup window when touch outside of it - when looses focus
                        popup.setOutsideTouchable(true);
                        popup.setFocusable(true);
                        popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
                    }
                });


                MapFragment mapFragment = (MapFragment) getFragmentManager()
                        .findFragmentById(R.id.map);

                //SqLite database handler
                db = new SQLiteHandler(getApplicationContext());

                //session manager
                session = new SessionManager(getApplicationContext());

                if (!session.isLoggedIn()) {
                    logoutUser();
                }

                //Fetching user details from sqlite
                HashMap<String, String> user = db.getUserDetails();

                String first_name = user.get("first_name");
                String last_name = user.get("last_name");
                String email = user.get("email");

                myToolbar.setTitle(first_name + " " + last_name);
                setSupportActionBar(myToolbar);
            }
        });
    }


    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        //Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                //Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                //Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("Current Location");
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
    }

    private void setUpMapIfNeeded() {
        //Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            //Try to obtain the map from getFragmentManager.
            MapFragment mapFragment = (MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }
}