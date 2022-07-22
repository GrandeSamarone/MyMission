package com.example.mymission;

import static android.content.Context.WINDOW_SERVICE;
import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION;
import static android.os.Looper.getMainLooper;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
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
    int LAYOUT_FLAG;
    View mFloatingview;
    WindowManager windowManager;
    ImageView imageViewClose;
    int MAX_CLICK_DURATION=200;
    TextView txtWidget;
    float height,width;


    public CallbackWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        cancelar=false;
        mContext=appContext;
        myAppsNotificationManager = MyAppsNotificationManager.getInstance(appContext);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(MOTOBOYS);
        geoFire = new GeoFire(ref);
        hand = new Handler(getMainLooper());




    }


    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
      //  setForegroundAsync(createForeGroundInfo());
        new MyBubbles(mContext);
        getForegroundInfoAsync();

        return CallbackToFutureAdapter.getFuture(completer -> {
            ccompleter=completer;

            startListen(ccompleter);
           // Bubbles(ccompleter);

            return  ccompleter;
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    public void Bubbles(CallbackToFutureAdapter.Completer<Result> completer){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            LAYOUT_FLAG= WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else{
            LAYOUT_FLAG= WindowManager.LayoutParams.TYPE_PHONE;
        }


        ///INFLA
        mFloatingview= LayoutInflater.from(mContext).inflate(R.layout.layout_widget,null);

        WindowManager.LayoutParams layoutParams= new
                WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        ///Position inicial
        layoutParams.gravity= Gravity.TOP|Gravity.RIGHT;
        layoutParams.x=0;
        layoutParams.y=100;

        ///Parametro do layout close button
        WindowManager.LayoutParams imageparams= new WindowManager.LayoutParams(
                140,
                140,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        imageparams.gravity=Gravity.BOTTOM|Gravity.CENTER;
        imageparams.y=100;

        windowManager=(WindowManager)mContext.getSystemService(WINDOW_SERVICE);

        imageViewClose=new ImageView(mContext);
        imageViewClose.setImageResource(R.drawable.close);
        imageViewClose.setVisibility(View.INVISIBLE);
        windowManager.addView(imageViewClose,imageparams);
        windowManager.addView(mFloatingview,layoutParams);
        mFloatingview.setVisibility(View.VISIBLE);

        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        height= metrics.heightPixels;
        width =metrics.widthPixels;

        txtWidget=(TextView) mFloatingview.findViewById(R.id.text_widget);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                txtWidget.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()));
                handler.postDelayed(this,1000);
            }
        },10);

        //movimentar
        txtWidget.setOnTouchListener(new View.OnTouchListener() {

            int initialX,initialY;
            float initialTouchX,initialTouchY;
            long startclickTime;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()){

                    case  MotionEvent.ACTION_DOWN:
                        startclickTime= Calendar.getInstance().getTimeInMillis();
                        imageViewClose.setVisibility(View.VISIBLE);

                        initialX=layoutParams.x;
                        initialY=layoutParams.y;
                        initialTouchX=motionEvent.getRawX();
                        initialTouchY=motionEvent.getRawY();

                        return true;

                    case MotionEvent.ACTION_UP:
                        long clickDuration=Calendar.getInstance().getTimeInMillis()-startclickTime;
                        imageViewClose.setVisibility(View.GONE);
                        layoutParams.x=initialX+(int)(initialTouchX-motionEvent.getRawX());
                        layoutParams.y=initialY+(int)(motionEvent.getRawY()-initialTouchY);

                        if(clickDuration<MAX_CLICK_DURATION){
                              Toast.makeText(mContext, "Time"+txtWidget.getText().toString(), Toast.LENGTH_SHORT).show();
                        }else{
                            if(layoutParams.y>(height*0.6)){
                                //onStopped();
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:

                        //calcular cordenadas da view
                        layoutParams.x=initialX+(int)(initialTouchX-motionEvent.getRawX());
                        layoutParams.y=initialY+(int)(motionEvent.getRawY()-initialTouchY);

                        //Atualizar layout
                        windowManager.updateViewLayout(mFloatingview,layoutParams);


//                        if(layoutParams.y>(height*0.6)){
//                            imageViewClose.setImageResource(R.drawable.close);
//                        }else{
//                            imageViewClose.setImageResource(R.drawable.close_white);
//                        }
                        return true;

                }


                return false;
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void startListen(CallbackToFutureAdapter.Completer<Result> completer) {
        // monitorando se o gps ta ativo



        locationRequest = LocationRequest.create()
                          .setInterval(4000)
                          .setFastestInterval(8000)
                          .setPriority(Priority.PRIORITY_HIGH_ACCURACY);
       // geoFire.setLocation("YJgmEZXTa7ZLe6SNsuLkczI5ZzM2", new GeoLocation(-10.9063283, -61.9253517));
       fusedLocation = LocationServices.getFusedLocationProviderClient(mContext);
        fusedLocation.requestLocationUpdates(locationRequest, locationCallback, hand.getLooper());

        FirebaseAuth.getInstance().addIdTokenListener(new FirebaseAuth.IdTokenListener() {
            @Override
            public void onIdTokenChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    TokenRefresh();
                    completer.set(Result.success());
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
        cancelar=true;
        Log.d("osakdsodksd","ONSTOPPED "+cancelar);

        fusedLocation.removeLocationUpdates(locationCallback);
        geoFire.removeLocation("YJgmEZXTa7ZLe6SNsuLkczI5ZzM2");

        Log.d("ClearFromRecentService", "Service Destroyed");
        if(mFloatingview!=null){
            windowManager.removeView(mFloatingview);
        }
        if(imageViewClose!=null){
            windowManager.removeView(imageViewClose);
        }
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
    private ForegroundInfo createForeGroundInfo(){
        Notification notification = myAppsNotificationManager.getNotification(MainActivity.class,
                "CallbackWorker Running...",
                1,
                false,
                10);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new ForegroundInfo(10, notification, FOREGROUND_SERVICE_TYPE_LOCATION);
        }else{
            return new ForegroundInfo(10, notification);
        }
    }
}
