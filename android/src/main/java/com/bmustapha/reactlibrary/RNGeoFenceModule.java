package com.bmustapha.reactlibrary;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class RNGeoFenceModule extends ReactContextBaseJavaModule implements
        ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status>, LocationListener {

    private String TAG = "GeofenceTransitionsIS";

    private GoogleApiClient mGoogleApiClient;

    // to keep track of user location
    private Location lastLocation;

    /**
     * The list of geofences used in this sample.
     */
    private ArrayList<Geofence> mGeofenceList;

    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;


    // global list of coordinates
    public static ArrayList<ReadableMap> GlobalReadableMap;
    public static final int RNGeoFenceModule_REQ_PERMISSION = 819283;
    public static ReactApplicationContext SReactApplicationContext;
    public static RNGeoFenceModule RNGeoFenceModuleContext;



    public RNGeoFenceModule(ReactApplicationContext reactContext) {
        super(reactContext);

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<>();

        // Empty list for storing global geofences.
        GlobalReadableMap = new ArrayList<>();

        SReactApplicationContext = reactContext;

        // reference context in static object
        RNGeoFenceModuleContext = this;

        buildGoogleApiClient();
    }

    @Override
    public String getName() {
        return "RNGeoFenceModule";
    }


    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getReactApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged ["+location+"]");
        lastLocation = location;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    /**
     * Runs when the result of calling addGeofences() and removeGeofences() becomes available.
     * Either method can complete successfully or with an error.
     * <p>
     * Since this activity implements the {@link ResultCallback} interface, we are required to
     * define this method.
     *
     * @param status The Status returned through a PendingIntent when addGeofences() or
     *               removeGeofences() get called.
     */
    @Override
    public void onResult(@NonNull Status status) {
        if (status.isSuccess()) {
            Log.e(TAG, "Geofence operation successful");
        } else {
            Log.e(TAG, "onResult Failure");
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(getReactApplicationContext(),
                    status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }

    public void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if (checkPermission()) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (lastLocation != null) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
            // start geofence
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } else {
            askPermission();
        }
    }

    private void askPermission() {
        Log.d(TAG, "askPermission()");
        Activity currentActivity = getCurrentActivity();
        if (currentActivity != null) {
            ActivityCompat.requestPermissions(
                    getCurrentActivity(),
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    RNGeoFenceModule_REQ_PERMISSION
            );
        }
    }

    private void startLocationUpdates() {
        // Defined in milliseconds.
        // This number in extremely low, and should be used only for debug
        int UPDATE_INTERVAL =  1000; // update later to 3 * 60 * 1000; // 3 minutes
        int FASTEST_INTERVAL = 900; // update later to 30 * 1000;  // 30 secs
        Log.i(TAG, "startLocationUpdates()");
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (checkPermission()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this, Looper.getMainLooper());
        }
    }

    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(getReactApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }


    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }


    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(getReactApplicationContext(), GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(getReactApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }




    /*
    * React methods
    *
    *
    * */



    /**
     * Adds a list of geofence coordinates to a list to be used for geofencing
     *
     * @param readableArray The array containing the list of geofence readableMaps to be
     *               added to geofence list
     *
     * @param geofenceRadiusInMetres geofence radius in metres
     *
     *
     *@param geofenceExpirationInMilliseconds geofence expiration time in milliseconds
     *
     */
    @ReactMethod
    @SuppressWarnings("unused")
    public void populateGeofenceList(ReadableArray readableArray,
                                     int geofenceRadiusInMetres,
                                     int geofenceExpirationInMilliseconds) {
        if (readableArray != null) {
            for (int i = 0; i < readableArray.size(); i++) {
                ReadableMap geofence = readableArray.getMap(i);
                GlobalReadableMap.add(geofence);
                mGeofenceList.add(new Geofence.Builder()
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
                        .setRequestId(geofence.getString("key"))

                        // Set the circular region of this geofence.
                        .setCircularRegion(
                                geofence.getDouble("latitude"),
                                geofence.getDouble("longitude"),
                                geofenceRadiusInMetres
                        )

                        .setNotificationResponsiveness(20)

                        // Set the expiration duration of the geofence. This geofence gets automatically
                        // removed after this period of time.
                        .setExpirationDuration(geofenceExpirationInMilliseconds)

                        // Set the transition types of interest. Alerts are only generated for these transition
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT)

                        // Create the geofence.
                        .build());
            }
        }
    }


    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
    @ReactMethod
    @SuppressWarnings("unused")
    public void beginGeofencing() {
        if (!mGoogleApiClient.isConnected()) {
            Log.e(TAG, "GoogleApiClient no yet connected. Try again.");
            return;
        }
        try {
            // get user last location
            getLastKnownLocation();
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }


    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    @ReactMethod
    @SuppressWarnings("unused")
    public void stopGeofencing() {
        if (!mGoogleApiClient.isConnected()) {
            Log.e(TAG, "GoogleApiClient no yet connected. Try again.");
            return;
        }
        try {
            // Remove geofences.
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }
}
