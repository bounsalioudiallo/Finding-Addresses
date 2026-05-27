package com.findingaddresses.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private static final int LOCATION_REQUEST_CODE = 74;
    private WebView webView;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new NativeGpsBridge(), "NativeGps");
        webView.loadUrl("file:///android_asset/www/index.html");
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (hasLocationPermission()) {
                startLocationUpdates();
            } else {
                sendLocationError("Location permission was denied.");
            }
        }
    }

    private boolean hasLocationPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        requestPermissions(
            new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION },
            LOCATION_REQUEST_CODE
        );
    }

    private void startLocationUpdates() {
        if (!hasLocationPermission()) {
            requestLocationPermission();
            return;
        }

        stopLocationUpdates();
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                sendLocation(location);
            }

            @Override
            public void onProviderEnabled(String provider) {
                sendLocationStatus(provider + " enabled.");
            }

            @Override
            public void onProviderDisabled(String provider) {
                sendLocationError(provider + " disabled. Turn on location services.");
            }
        };

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, locationListener);
                Location lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastGps != null) {
                    sendLocation(lastGps);
                }
            }

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000L, 3f, locationListener);
                Location lastNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (lastNetwork != null) {
                    sendLocation(lastNetwork);
                }
            }

            sendLocationStatus("Native GPS is active.");
        } catch (SecurityException error) {
            sendLocationError("Location permission is missing.");
        } catch (IllegalArgumentException error) {
            sendLocationError("No native location provider is available.");
        }
    }

    private void stopLocationUpdates() {
        if (locationListener != null && locationManager != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException ignored) {
                // Permission can be revoked while the app is open.
            }
            locationListener = null;
        }
    }

    private void sendLocation(Location location) {
        JSONObject data = new JSONObject();
        try {
            data.put("latitude", location.getLatitude());
            data.put("longitude", location.getLongitude());
            data.put("accuracy", location.hasAccuracy() ? location.getAccuracy() : JSONObject.NULL);
            data.put("speed", location.hasSpeed() ? location.getSpeed() : JSONObject.NULL);
            data.put("provider", location.getProvider());
            data.put("time", location.getTime());
            runJavaScript("window.handleNativeLocation && window.handleNativeLocation(" + data + ");");
        } catch (JSONException error) {
            sendLocationError("Could not read native GPS data.");
        }
    }

    private void sendLocationStatus(String message) {
        runJavaScript("window.handleNativeLocationStatus && window.handleNativeLocationStatus(" + JSONObject.quote(message) + ");");
    }

    private void sendLocationError(String message) {
        runJavaScript("window.handleNativeLocationError && window.handleNativeLocationError(" + JSONObject.quote(message) + ");");
    }

    private void runJavaScript(String script) {
        runOnUiThread(() -> webView.evaluateJavascript(script, null));
    }

    public class NativeGpsBridge {
        @JavascriptInterface
        public boolean isAvailable() {
            return true;
        }

        @JavascriptInterface
        public void start() {
            runOnUiThread(() -> {
                if (hasLocationPermission()) {
                    startLocationUpdates();
                } else {
                    requestLocationPermission();
                }
            });
        }

        @JavascriptInterface
        public void stop() {
            runOnUiThread(() -> {
                stopLocationUpdates();
                sendLocationStatus("Native GPS stopped.");
            });
        }
    }
}
