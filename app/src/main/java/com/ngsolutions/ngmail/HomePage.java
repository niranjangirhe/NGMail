package com.ngsolutions.ngmail;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;

public class HomePage extends AppCompatActivity {

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        dialogBuilder = new AlertDialog.Builder(this);
        final View AddPartner = getLayoutInflater().inflate(R.layout.addpatner,null);
        userNameEditText = namePopupView.findViewById(R.id.UserNameText);
        saveName = namePopupView.findViewById(R.id.SaveLangBtn);
        dialogBuilder.setView(namePopupView);
        dialog =dialogBuilder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        saveName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userNameEditText.getText().toString().trim().isEmpty())
                {
                    Toast.makeText(ChatMainScreenActivity.this, "Please enter your name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    userName = userNameEditText.getText().toString();
                    Map<String, Object> user = new HashMap<>();
                    user.put("userName",userName);
                    DocumentReference addname = fstore.collection("users").document(userID);
                    if(datafound) {
                        addname.update(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ChatMainScreenActivity.this, "Name saved to your profile", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else
                    {
                        user.put("cropNum", "000000");
                        user.put("crop0", "");
                        user.put("crop1", "");
                        user.put("crop2", "");
                        user.put("crop3", "");
                        user.put("crop4", "");
                        user.put("crop5", "");
                        user.put("token", FirebaseInstanceId.getInstance().getToken());
                        addname.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ChatMainScreenActivity.this, "Name saved to your profile", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    gotName =true;
                    dialog.dismiss();
                }
            }
        });
    }
}