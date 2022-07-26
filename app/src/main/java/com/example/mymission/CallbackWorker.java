package com.example.mymission;

import static android.content.Context.WINDOW_SERVICE;
import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;
import static android.os.Looper.getMainLooper;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.concurrent.futures.ResolvableFuture;
import androidx.work.ForegroundInfo;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.impl.utils.futures.SettableFuture;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CallbackWorker  extends ListenableWorker {

    MyBubbles myBubbles ;
    MyAppsNotificationManager myAppsNotificationManager;
   Handler hand;
    String MOTOBOYS = "TesteMotoboysOnline";
    CallbackToFutureAdapter.Completer<Result> c;
    String idToken;
    Boolean cancelar;
    FirebaseUser user;
    CallbackToFutureAdapter.Completer<Result> ccompleter;
    MotoboyAccount account;
    private Context mContext;
    GeoFire geoFire;
    FusedLocationProviderClient fusedLocation;
    LocationRequest locationRequest;
    SharedPreferences sharedPref;

    public CallbackWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        cancelar=false;
        mContext=appContext;
        myAppsNotificationManager = MyAppsNotificationManager.getInstance(appContext);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(MOTOBOYS);
        geoFire = new GeoFire(ref);
        hand = new Handler(getMainLooper());

       sharedPref = appContext.getSharedPreferences("mFloatingview", Context.MODE_PRIVATE);
    }


    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {

        //Icone Notification
        getForegroundInfoAsync();

        return CallbackToFutureAdapter.getFuture(completer -> {
            myBubbles=  new MyBubbles(mContext);


                 startListen(completer);


            return  completer;
        });

    }


    @SuppressLint("MissingPermission")
    public void startListen(CallbackToFutureAdapter.Completer<Result> completer) {
        // monitorando se o gps ta ativo
        locationRequest = LocationRequest.create()
                          .setInterval(4000)
                          .setFastestInterval(8000)
                          .setPriority(Priority.PRIORITY_HIGH_ACCURACY);

       fusedLocation = LocationServices.getFusedLocationProviderClient(mContext);
        fusedLocation.requestLocationUpdates(locationRequest, locationCallback, hand.getLooper());

        FirebaseAuth.getInstance().addIdTokenListener(new FirebaseAuth.IdTokenListener() {
            @Override
            public void onIdTokenChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    TokenRefresh();
                    completer.set(Result.failure());
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
            geoFire.setLocation("YJgmEZXTa7ZLe6SNsuLkczI5ZzM2", new GeoLocation(latitude, longitude));
        }


    };

    @Override
    public void onStopped() {

        fusedLocation.removeLocationUpdates(locationCallback);
       geoFire.removeLocation("YJgmEZXTa7ZLe6SNsuLkczI5ZzM2");
        Log.d("ClearFromRecentService", "Service Destroyed");
        super.onStopped();
    }

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

    @NonNull
    @SuppressLint("RestrictedApi")
    public ListenableFuture<ForegroundInfo> getForegroundInfoAsync() {
        SettableFuture<ForegroundInfo> future = SettableFuture.create();

        Notification notification = myAppsNotificationManager.getNotification(MainActivity.class,
                "getForegroundInfoAsync Running...",
                1,
                false,
                10);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            future.set(new ForegroundInfo(10, notification, FOREGROUND_SERVICE_TYPE_LOCATION));
        }else{
            future.set(new ForegroundInfo(10, notification));
        }

        return future;
    }

    private  void preferences(int valor){
        int highScore = sharedPref.getInt("mFloatingview",Context.MODE_PRIVATE);
        Log.d("oskdoskdsodk","sodksodksd"+ highScore);


        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt("mFloatingview", valor);
        editor.apply();
        Log.d("oskdoskdsodk","sodksodksd"+ highScore);


        // Log.d("osakdsodksodk","asas "+sharedPref);
    }
}
