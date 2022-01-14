package com.ngsolutions.ngmail;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EnterOTP extends AppCompatActivity {

    private EditText EditOtp;
    private String Phone;
    FirebaseAuth Auth;
    private Button BtnVerify;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String VerificationCode=null;
    private ImageButton BtnGoToPhone;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_otp);

        //binding
        EditOtp = findViewById(R.id.editotp);
        BtnVerify = findViewById(R.id.btnverify);
        BtnGoToPhone = findViewById(R.id.btngotophone);

        db = FirebaseFirestore.getInstance();

        //get bundle data
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Phone = extras.getString("PhoneNo","0");
            Toast.makeText(getApplicationContext(), Phone, Toast.LENGTH_SHORT).show();
            //The key argument here must match that used in the other activity
        }

        Auth = FirebaseAuth.getInstance();
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
                signIn(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(EnterOTP.this, e.getMessage() , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationCode, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationCode, forceResendingToken);
                Toast.makeText(EnterOTP.this, "OTP Sent", Toast.LENGTH_SHORT).show();
                VerificationCode = verificationCode;
            }
        };
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(Auth)
                        .setPhoneNumber("+91"+Phone)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        BtnVerify.setOnClickListener(v -> {
            if(VerificationCode!=null)
            {
                String code = EditOtp.getText().toString().trim();
                if(EditOtp.getText().toString().trim().length()==6) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(VerificationCode, code);
                    signIn(credential);
                }
                else
                    Toast.makeText(getApplicationContext(), "OTP format is wrong", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(getApplicationContext(), "OTP is not sent yet", Toast.LENGTH_SHORT).show();
        });
        BtnGoToPhone.setOnClickListener(v -> {
            Intent intent = new Intent(EnterOTP.this,MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
    private void signIn(PhoneAuthCredential credential){

        Auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    // Create a new user with a first and last name
                    Map<String, Object> user = new HashMap<>();
                    user.put("Phone", Phone);
                    user.put("UID", FirebaseAuth.getInstance().getUid());
                    db.collection("users")
                            .add(user)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("TAG", "Error adding document", e);
                                }
                            });
                    Intent intent = new Intent(EnterOTP.this,HomePage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Toast.makeText(EnterOTP.this, task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
