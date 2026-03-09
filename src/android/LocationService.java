package com.plugin.widgetfloat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LocationService extends Service {
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                try {
                    // Always notify local listeners (bubble/app), even when no backend URL is configured.
                    Intent intent = new Intent("location_update");
                    intent.putExtra("latitude", latitude);
                    intent.putExtra("longitude", longitude);
                    sendBroadcast(intent);

                    if (url != null && !url.trim().isEmpty()) {
                        Map<String, String> headers = new HashMap<>();
                        JSONObject objectData = buildPayloadWithLocation(latitude, longitude);
                        JSONArray datas = new JSONArray();
                        datas.put(objectData);
                        requestApi.sendPost(getApplicationContext(), url, datas.toString(), headers);
                    }
                } catch (JSONException e) {
                    Log.e("LocationService", "Falha ao montar payload de localização", e);
                }
            }
        }
    };

    private final RequestApi requestApi = new RequestApi();
    private String url = null;
    private String data = null;
    public static final String ACTION_PROCESS_UPDATE = "com.plugin.widgetfloat.UPDATE_LOCATION";
    private long interval = 20000;
    private long fastestInterval = 20000;

    private JSONObject buildPayloadWithLocation(double latitude, double longitude) throws JSONException {
        JSONObject objectData;
        if (data != null && !data.trim().isEmpty()) {
            objectData = new JSONObject(data);
        } else {
            objectData = new JSONObject();
        }

        objectData.put("latitude", latitude);
        objectData.put("longitude", longitude);
        return objectData;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void startLocationService() {
        String channelId = "location_notification_channel";
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_IMMUTABLE
        );
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                channelId
        );

        builder.setSmallIcon(getApplication().getResources().getIdentifier("ic_launcher", "drawable", getPackageName()));
        builder.setContentTitle("Radar de viagem");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Filtrando viagens para seu perfil");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            if (notificationManager != null
                    && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        channelId,
                        "Location Service",
                        NotificationManager.IMPORTANCE_HIGH
                );

                notificationChannel.setDescription("This channel is used location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(fastestInterval);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        startForeground(Constants.LOCATION_SERVICE_ID, builder.build());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopForeground(true);
            stopSelf();
            return;
        }

        LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationService(){
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            String action = intent.getAction();
            if(action != null){
                if(action.equals(Constants.ACTION_START_LOCATION_SERVICE)){
                    if (intent.getExtras() != null) {
                        url = intent.getExtras().getString("url");
                        data = intent.getExtras().getString("data");
                        interval = intent.getExtras().getLong("interval",20000);
                        fastestInterval = intent.getExtras().getLong("fastestInterval",20000);
                    }
                    startLocationService();
                } else if (action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)){
                    stopLocationService();
                }
            }
        }
        return START_STICKY;
    }
}
