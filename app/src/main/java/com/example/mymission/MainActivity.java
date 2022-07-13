package com.example.mymission;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {



    private Button buttonStart, buttonStop,buttonLogin;
    private  TextView txtLogin;
    Intent myServiceItent;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkLocationPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}, 200);
        }



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

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonLOGIN:
                Login();
                break;case R.id.buttonStarter:
                    if(mAuth.getCurrentUser()!=null){
                        startService(myServiceItent);
                    }else{
                        Toast.makeText(MainActivity.this, "SignIN",
                                Toast.LENGTH_SHORT).show();

                    }
                break;
            case R.id.buttonStop:
                stopService(myServiceItent);
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

}