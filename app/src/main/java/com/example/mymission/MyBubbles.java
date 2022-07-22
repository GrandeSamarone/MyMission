package com.example.mymission;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
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
import android.widget.ImageView;
import android.widget.TextView;

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
    TextView txtWidget;
    float height,width;
    boolean mostrar;
    WorkManager workManager;
    private OneTimeWorkRequest workRequestOne;

    @SuppressLint("ClickableViewAccessibility")
    public MyBubbles(Context base) {
        super(base);
         mostrar=false;
        workManager = WorkManager.getInstance(getApplicationContext());

        workManager
                .getWorkInfosByTagLiveData("ON").
                observeForever(new Observer<List<WorkInfo>>() {
                    @Override
                    public void onChanged(@Nullable List<WorkInfo> workInfos) {
                        Log.d("DEBUG", "onChanged()");
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
        mFloatingview= LayoutInflater.from(base).inflate(R.layout.layout_widget,null);

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

        windowManager=(WindowManager)base.getSystemService(WINDOW_SERVICE);

        imageViewClose=new ImageView(base);
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
                        if(mostrar){
                            imageViewClose.setVisibility(View.VISIBLE);
                        }


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
                            // Toast.makeText(this, "Time"+txtWidget.getText().toString(), Toast.LENGTH_SHORT).show();
                        }
                        return true;

                    case MotionEvent.ACTION_MOVE:

                        //calcular cordenadas da view
                        layoutParams.x=initialX+(int)(initialTouchX-motionEvent.getRawX());
                        layoutParams.y=initialY+(int)(motionEvent.getRawY()-initialTouchY);

                        //Atualizar layout
                        windowManager.updateViewLayout(mFloatingview,layoutParams);


                        if(mostrar){
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

                        return true;

                }
                if(mostrar){
                    if(mFloatingview!=null){
                        windowManager.removeView(mFloatingview);
                    }
                    if(imageViewClose!=null){
                        windowManager.removeView(imageViewClose);
                    }
                }


                return false;
            }
        });

    }

    void closed(){

    }
}
