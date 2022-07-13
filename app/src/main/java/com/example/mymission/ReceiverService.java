package com.example.mymission;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import android.util.Log;
import android.view.Gravity;

import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import java.util.ArrayList;

import java.util.List;

public class ReceiverService extends Service {

   public IBinder serviceBinder = new ServiceBinder();

    Handler hand;
    String MOTOBOYS = "TesteMotoboysOnline";

    String idToken;
    FirebaseUser user;
    MotoboyAccount account;
    GeoFire geoFire;
    FusedLocationProviderClient fusedLocation;
    LocationRequest locationRequest;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();



    @Override
    public IBinder onBind(Intent intent) {

        return serviceBinder;
    }

    @SuppressLint("MissingPermission")
    public void startListen() {
        // monitorando se o gps ta ativo



        locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(4000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocation = LocationServices.getFusedLocationProviderClient(this);
        fusedLocation.requestLocationUpdates(locationRequest, locationCallback, hand.getLooper());

        FirebaseAuth.getInstance().addIdTokenListener(new FirebaseAuth.IdTokenListener() {
            @Override
            public void onIdTokenChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    TokenRefresh();
                }
            }
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            double latitude = locationResult.getLastLocation().getLatitude();
            double longitude = locationResult.getLastLocation().getLongitude();
             Log.i("Atualizando0", String.valueOf(latitude));
              Log.i("Atualizando0", String.valueOf(longitude));
            geoFire.setLocation(user.getUid(), new GeoLocation(latitude, longitude));
        }


    };


    public void TokenRefresh() {

        user.getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            idToken = task.getResult().getToken();
                            Log.d("IdToken", idToken);

                        }
                    }
                });
    }



    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(getBaseContext());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(MOTOBOYS);
        geoFire = new GeoFire(ref);

        hand = new Handler(getMainLooper());

        startListen();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            user = FirebaseAuth.getInstance().getCurrentUser();

            final DocumentReference docRef = db.collection("user_motoboy").document(user.getUid());
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                        @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        account = snapshot.toObject(MotoboyAccount.class);
                        if (!account.getPermissao()) {
                            onStopCommand();
                        }
                    } else {
                        geoFire.removeLocation(user.getUid());
                    }
                }
            });
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID;
        String channelName = "Motoboy Online";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName,
                NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                    NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setColor(getResources().getColor(R.color.purple_200))
                    .setContentTitle("Voçê está online")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);
        } else {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,
                    NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setColor(getResources().getColor(R.color.purple_200))
                    .setContentTitle("Voçê está online")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);

        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());

        TokenRefresh();
        return START_STICKY;

    }

    public void onStopCommand() {
        Intent intent = new Intent(getApplicationContext(), ReceiverService.class);
        stopService(intent);

    }

    @Override
    public void onDestroy() {
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("pending_order");
            fusedLocation.removeLocationUpdates(locationCallback);
            geoFire.removeLocation(user.getUid());

        } catch (Exception e) {
        }
        super.onDestroy();

    }

    public class ServiceBinder extends Binder{
        public ReceiverService getService(){
            return ReceiverService.this;
        }
    }
}
