package net.kjulio.rxlocation;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import io.reactivex.ObservableEmitter;

/**
 * Base class for all helpers, manages the GoogleApiClient connection.
 *
 * Contract:
 *  - Instantiate object.
 *  - Call start() once to begin.
 *  - Call stop() once when finished.
 *  - Subclasses should release any resources in onGooglePlayServicesDisconnecting().
 */
abstract class BaseHelper implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private final Context context;
    final Handler handler;
    final ObservableEmitter<Location> emitter;
    final GoogleApiClient googleApiClient;

    BaseHelper(Context context, GoogleApiClientFactory googleApiClientFactory,
               ObservableEmitter<Location> emitter) {

        // Returns either the current thread's looper or, if the current thread is not a looper
        // thread, the main looper.Returns either the current thread's looper or, if the current
        // thread is not a looper thread the main looper.
        Looper looper = Looper.myLooper();
        if (looper == null) {
            looper = Looper.getMainLooper();
        }

        this.context = context;
        handler = new Handler(looper);
        googleApiClient = googleApiClientFactory.create(context, handler, this, this);
        this.emitter = emitter;
    }

    void start() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                googleApiClient.connect();
            }
        });
    }

    void stop() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                // Calling Google APIs when the initial connection to GoogleApiClient failed
                // will lead to a crash so we must check for this case.
                // .disconnect() is always safe to call but onGooglePlayServicesDisconnecting()
                // should be invoked only if googleApiClient is not disconnected or not connecting.
                if (googleApiClient.isConnected()) {
                    onGooglePlayServicesDisconnecting();
                }
                googleApiClient.disconnect();
            }
        });
    }

    abstract void onLocationPermissionsGranted();

    abstract void onGooglePlayServicesDisconnecting();

    void onLocationPermissionDialogDismissed() {
        // when globalLock is released by PermissionsActivity recheck permissions and go on.
        if (PermissionsActivity.checkPermissions(context)) {
            onLocationPermissionsGranted();
        } else {
            emitter.onError(new SecurityException("Location permission not granted."));
        }
    }

    void onErrorResolutionActivityDismissed() {
        // when globalLock is released by ErrorResolutionActivity recheck permissions and go on.
        // TODO: Is it safe to call again connect() inside onConnectionFailed() ?
        // Maybe we should use a counter variable to avoid recalling googleApiClient.connect()
        // twice and instead execute emitter.onError();
        start();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (PermissionsActivity.checkPermissions(context)) {
            onLocationPermissionsGranted();
        } else {
            // Spawn permission request activity
            PermissionsActivity.requestPermissions(context, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Method's body left empty as we deem it's the best behavior.
        //
        // http://stackoverflow.com/a/27350444/972721
        // onConnectionSuspended() is called for example when a user intentionally disables or
        // uninstalls Google Play Services.
        //
        // http://stackoverflow.com/a/26147518/972721
        // After the library calls onConnectionSuspended() it will automatically try to reconnect,
        // if it fails onConnectionFailed() will be invoked otherwise normal operation will resume.
        //
        // Shall we propagate the onConnectionSuspended() as onError() or shall we just ignore it?
        // If we propagate it the subscription will be cancelled when a connection suspension
        // occurs, if we ignore it the subscription will stay and upon successful reconnection the
        // observable will go on emitting items. If the reconnection fails onConnectionFailed()
        // will be called and our subscription will be cancelled.
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            // Spawn error resolution activity
            ErrorResolutionActivity.resolveError(context, this, connectionResult);
        }  else {
            // TODO: Show the default UI when there is no resolution.
            // https://developers.google.com/android/guides/api-client
            emitter.onError(new GapiConnectionFailedException(connectionResult));
        }
    }
}
