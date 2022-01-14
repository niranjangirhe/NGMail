package com.ngsolutions.ngmail;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button BtnGetOtp;
    private EditText EditGetPhone;
    FirebaseAuth Auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //binding
        BtnGetOtp = findViewById(R.id.btngetotp);
        EditGetPhone =  findViewById(R.id.editgetphone);

        Auth = FirebaseAuth.getInstance();
        FirebaseUser user = Auth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(MainActivity.this,HomePage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        BtnGetOtp.setOnClickListener(v -> {
            String Phone = EditGetPhone.getText().toString().trim();
            if(Phone.length()!=10)
                Toast.makeText(getApplicationContext(), "Invalid Phone Number", Toast.LENGTH_SHORT).show();
            else
            {
                Intent ToOtp = new Intent(MainActivity.this,EnterOTP.class);
                ToOtp.putExtra("PhoneNo",Phone);
                startActivity(ToOtp);
                finish();
            }
        });
    }
}