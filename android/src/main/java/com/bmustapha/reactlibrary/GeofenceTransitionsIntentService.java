package com.bmustapha.reactlibrary;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import static com.bmustapha.reactlibrary.RNGeoFenceModule.GlobalReadableMap;
import static com.bmustapha.reactlibrary.RNGeoFenceModule.SReactApplicationContext;

/**
 * Listener for geofence transition changes.
 *
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a notification
 * as the output.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    protected static final String TAG = "GeofenceTransitionsIS";

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public GeofenceTransitionsIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            WritableArray writableArray = new WritableNativeArray();

            for (int i = 0; i < triggeringGeofences.size(); i++) {
                Geofence geofence = triggeringGeofences.get(i);
                String key = geofence.getRequestId();
                ReadableMap geofenceMap = getGeofenceFromKey(key);
                WritableMap writableMap = Arguments.createMap();
                writableMap.merge(geofenceMap);
                writableMap.putString("transition", getTransitionString(geofenceTransition));
                writableArray.pushMap(writableMap);
            }

            // create final event emitter object argument
            WritableMap finalEventEmitterObject = Arguments.createMap();
            finalEventEmitterObject.putArray("data", writableArray);
            finalEventEmitterObject.putString("event", "geofenceTrigger");

            SReactApplicationContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("GeofenceEvent", finalEventEmitterObject);
        } else {
            // Log the error.
            Log.e(TAG, "Geofence transition error: invalid transition type " + geofenceTransition);
        }
    }

    private ReadableMap getGeofenceFromKey(String key) {
        for (int i = 0; i < GlobalReadableMap.size(); i++) {
            ReadableMap readableMap = GlobalReadableMap.get(i);
            if (readableMap.getString("key").equals(key)) {
                return readableMap;
            }
        }
        return null;
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "Entered";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "Exited";
            default:
                return "Unknown Transition";
        }
    }
}
