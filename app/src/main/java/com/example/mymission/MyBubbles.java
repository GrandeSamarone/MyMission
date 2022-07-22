package com.example.mymission;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MyBubbles extends ContextWrapper{

    int LAYOUT_FLAG;
    View mFloatingview;
    WindowManager windowManager;
    ImageView imageViewClose;
    int MAX_CLICK_DURATION=200;
    float height,width;
    boolean mostrar;
    WorkManager workManager;
    ImageView imageclick;
    WindowManager.LayoutParams layoutParams;
    private OneTimeWorkRequest workRequestOne;

    @SuppressLint("ClickableViewAccessibility")
    public MyBubbles(Context context) {
        super(context);
         mostrar=false;
        workManager = WorkManager.getInstance(getApplicationContext());

        workManager
                .getWorkInfosByTagLiveData("ON").
                observeForever(new Observer<List<WorkInfo>>() {
                    @Override
                    public void onChanged(@Nullable List<WorkInfo> workInfos) {
                        Log.d("DEBUG", "onChanged()");
                        if (workInfos != null && (!(workInfos.isEmpty()))) {
                            for (WorkInfo wI: workInfos) {
                                Log.d("DEBUG","OnChanged: W Status:"+wI.getState());
                                if (wI.getState() == WorkInfo.State.CANCELLED) {
                                     mostrar=true;

                                }
                            }
                        }
                    }
                });

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            LAYOUT_FLAG= WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else{
            LAYOUT_FLAG= WindowManager.LayoutParams.TYPE_PHONE;
        }


        ///INFLA
        mFloatingview= LayoutInflater.from(context).inflate(R.layout.layout_widget,null);

      layoutParams= new
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

        windowManager=(WindowManager)context.getSystemService(WINDOW_SERVICE);

        imageViewClose=new ImageView(context);
        imageViewClose.setImageResource(R.drawable.close);
        imageViewClose.setVisibility(View.INVISIBLE);
        windowManager.addView(imageViewClose,imageparams);
        windowManager.addView(mFloatingview,layoutParams);
        mFloatingview.setVisibility(View.VISIBLE);

        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        height= metrics.heightPixels;
        width =metrics.widthPixels;
        imageclick=mFloatingview.findViewById(R.id.bubble_background);
        //movimentar
        imageclick.setOnTouchListener(new View.OnTouchListener() {

            int initialX,initialY;
            float initialTouchX,initialTouchY;
            long startclickTime;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()){

                    case  MotionEvent.ACTION_DOWN:
                        startclickTime= Calendar.getInstance().getTimeInMillis();
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

                        Log.d("oskdsodkds","CLICK DURATION::"+clickDuration);
                        if(clickDuration<MAX_CLICK_DURATION){
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP); // You need this if starting
                            startActivity(intent);
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        mostrar(mostrar);
                        //calcular cordenadas da view
                        layoutParams.x=initialX+(int)(initialTouchX-motionEvent.getRawX());
                        layoutParams.y=initialY+(int)(motionEvent.getRawY()-initialTouchY);

                        //Atualizar layout
                        windowManager.updateViewLayout(mFloatingview,layoutParams);


                        return true;

                    case MotionEvent.ACTION_BUTTON_PRESS:

                        return true;

                }



                return false;
            }
        });

    }
    void bringToFront(){

        ActivityManager activtyManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activtyManager.getRunningTasks(3);
        for (ActivityManager.RunningTaskInfo runningTaskInfo : runningTaskInfos)
        {
            if (this.getPackageName().equals(runningTaskInfo.topActivity.getPackageName()))
            {
                activtyManager.moveTaskToFront(runningTaskInfo.id, ActivityManager.MOVE_TASK_WITH_HOME);
                return;
            }
        }
    }
    protected  void mostrar(boolean valor){


        if(valor){
            imageViewClose.setVisibility(View.VISIBLE);

            if(layoutParams.y>(height*0.6)){
                imageViewClose.setImageResource(R.drawable.close);

                if(mFloatingview!=null && imageViewClose!=null){
                    windowManager.removeView(mFloatingview);
                    windowManager.removeView(imageViewClose);
                }
            }else{
                imageViewClose.setImageResource(R.drawable.close_white);
            }

        }

    }

    protected void moveParaFrente() {
        if (Build.VERSION.SDK_INT >= 11) { // honeycomb
            final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            final List<ActivityManager.RunningTaskInfo> recentTasks = activityManager
                    .getRunningTasks(Integer.MAX_VALUE);

            for (int i = 0; i < recentTasks.size(); i++) {
//                Log.d("Executed app", "Application executed : "
//                        + recentTasks.get(i).baseActivity.toShortString()
//                        + "\t\t ID: " + recentTasks.get(i).id + "");
                // bring to front
                if (recentTasks.get(i).baseActivity.toShortString().contains(BuildConfig.APPLICATION_ID)) {
                    activityManager.moveTaskToFront(recentTasks.get(i).id, ActivityManager.MOVE_TASK_WITH_HOME);

                }
            }
        }
    }
}
