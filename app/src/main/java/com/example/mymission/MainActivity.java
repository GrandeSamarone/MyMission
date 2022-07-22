package com.example.mymission;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    WorkManager workManager;
    private OneTimeWorkRequest workRequestOne;
    private Button buttonStart, buttonStop,buttonLogin;
    private  TextView txtLogin;
    Intent myServiceItent;
    private FirebaseAuth mAuth;
    UUID id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkLocationPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}, 200);
        }

        getPermission();

        mAuth = FirebaseAuth.getInstance();
        myServiceItent=new Intent(getApplicationContext(), ReceiverService.class);

        txtLogin=findViewById(R.id.txtLogin);
        buttonStart = (Button) findViewById(R.id.buttonStarter);
        buttonLogin = (Button) findViewById(R.id.buttonLOGIN);
        buttonStop = (Button) findViewById(R.id.buttonStop);

           if(mAuth.getCurrentUser()!=null){
               txtLogin.setText(mAuth.getCurrentUser().getEmail());
           }else{
               txtLogin.setText("click login");
           }

        buttonStart.setOnClickListener(this);
        buttonLogin.setOnClickListener(this);
        buttonStop.setOnClickListener(this);




        workManager = WorkManager.getInstance(getApplicationContext());

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                //.setRequiresBatteryNotLow(true)
                .build();

        workRequestOne =new  OneTimeWorkRequest.Builder(CallbackWorker.class)
                .addTag("ON")
                .setConstraints(constraints)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build();


        workManager.getWorkInfoByIdLiveData(workRequestOne.getId()).observe(this,
                new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        Log.d("oaksasaa","OnChanged: W Status:"+workInfo.getState());
                        Log.d("oaksasaa","OnChanged: W ID:"+workInfo.getId());
                        id=workInfo.getId();
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonLOGIN:
                Login();
                break;case R.id.buttonStarter:
                    if(mAuth.getCurrentUser()!=null){
                     //   startService(myServiceItent);

                        workManager.enqueueUniqueWork(
                                "MotoboyON",
                                ExistingWorkPolicy.REPLACE,
                                workRequestOne);
                    }else{
                        Toast.makeText(MainActivity.this, "SignIN",
                                Toast.LENGTH_SHORT).show();

                    }
                break;
            case R.id.buttonStop:
              //  stopService(myServiceItent);
                workManager.cancelUniqueWork("MotoboyON");
                break;
        }
    }

    void Login(){
        mAuth.signInWithEmailAndPassword("patixa@gmail.com","111111")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d("asdsadsd", "signInWithEmailAndPassword:success");
                            Log.d("asdsadsd", String.valueOf(user));
                            txtLogin.setText(user.getEmail());
                            Toast.makeText(MainActivity.this, "Authentication Sucess.",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            Log.w("asdsadsd", "signInWithEmail:failure", task.getException());
                        }
                    }
                });
    }


    private boolean checkLocationPermission() {
        int result3 = ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION);
        int result4 = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION);
        return result3 == PackageManager.PERMISSION_GRANTED &&
                result4 == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (grantResults.length > 0) {
                boolean coarseLocation = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean fineLocation = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (coarseLocation && fineLocation)
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void getPermission(){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)){

            Intent intent=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:"+getPackageName()));
            startActivityForResult(intent,1);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1){
            if(!Settings.canDrawOverlays(MainActivity.this)){
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }

        }
    }

}