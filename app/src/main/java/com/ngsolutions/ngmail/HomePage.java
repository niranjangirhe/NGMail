package com.ngsolutions.ngmail;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class HomePage extends AppCompatActivity {

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private FirebaseFirestore db;
    private EditText EditPatnerNo;
    private TextView Status;
    private Button BtnAddPartner;
    private Uri mImageUri = null;
    private Bitmap mbitmap;
    private ImageView DP;
    private ProgressBar progressBar;
    private FirebaseUser Auth;
    private String Phone;
    private StorageReference mStoreRef;
    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        db = FirebaseFirestore.getInstance();
        mStoreRef = FirebaseStorage.getInstance().getReference("DP");

        Auth = FirebaseAuth.getInstance().getCurrentUser();
        Phone = Auth.getPhoneNumber();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int from = extras.getInt("from",0);
            if(from==0) {
                if (Auth.getDisplayName().isEmpty() || Auth.getPhotoUrl().toString().isEmpty()) {
                    AddDetailsPopUp();
                }
                else
                {
                    TextView dname = findViewById(R.id.dname);
                    dname.setText(Auth.getDisplayName().toString());
                    TextView dphone = findViewById(R.id.dphone);
                    dphone.setText(Auth.getPhoneNumber());
                    ImageView ddp = findViewById(R.id.ddp);
                    Picasso.with(getApplicationContext()).load(Uri.parse("https://firebasestorage.googleapis.com/v0/b/n-75ec9.appspot.com/o/DP%2F%2B911234567890.jpg?alt=media&token=45224e01-44b7-4b3d-a25e-63f52438fef5")).fit().centerCrop().into(ddp);
                }
            }
            else
                {
                    AddDetailsPopUp();
                }
            //The key argument here must match that used in the other activity
        }

//      AddDetailsPopUp();
    }

    private void AddPartnerPopUp() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View AddPartner = getLayoutInflater().inflate(R.layout.addpatner,null);



        //binding
        EditPatnerNo = AddPartner.findViewById(R.id.editpartnerno);
        Status = AddPartner.findViewById(R.id.textpartnerstatus);
        Status = AddPartner.findViewById(R.id.textpartnerstatus);
        BtnAddPartner = AddPartner.findViewById(R.id.btnaddpartner);

        dialogBuilder.setView(AddPartner);
        dialog =dialogBuilder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        EditPatnerNo.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                search(s.toString());
            }
        });

        BtnAddPartner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(EditPatnerNo.getText().toString().trim().length()!=10)
                {
                    Toast.makeText(HomePage.this, "Please enter Valid Number", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    String SPatnerNo = EditPatnerNo.getText().toString();
                    search(SPatnerNo);
                }
            }
        });
    }

    private void AddDetailsPopUp() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View AddDetails = getLayoutInflater().inflate(R.layout.add_details,null);

        EditText Name;
        Button upload,submit;


        //binding
        Name = AddDetails.findViewById(R.id.editname);
        upload = AddDetails.findViewById(R.id.btnuploadimg);
        submit = AddDetails.findViewById(R.id.btnsubmitform);
        DP = AddDetails.findViewById(R.id.imgdppreview);
        progressBar = AddDetails.findViewById(R.id.pbform);

        dialogBuilder.setView(AddDetails);
        dialog =dialogBuilder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Name.getText().toString().trim().isEmpty() || mImageUri==null)
                {
                    Toast.makeText(HomePage.this, "Please Enter all details", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    sendMessage(Name.getText().toString());
                }
            }
        });
        upload.setOnClickListener(v -> {
            openFileChooser();
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    private void search(String sPatnerNo) {
        if(sPatnerNo.length()!=10) {
            BtnAddPartner.setEnabled(false);
            return;
        }
        DocumentReference addname = db.collection("users").document(sPatnerNo);
        addname.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                BtnAddPartner.setEnabled(true);
                BtnAddPartner.setText("Add Partner");
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Status.setText("User Exits");
                        BtnAddPartner.setEnabled(true);
                    } else {
                        Status.setText("User Not Found");
                        BtnAddPartner.setEnabled(false);
                    }
                } else {
                    Status.setText("Something Went Wrong, Retry");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode == RESULT_OK
                && data!=null && data.getData()!=null)
        {
            mImageUri = data.getData();
            BackgroundImageResize backgroundImageResize = new BackgroundImageResize();
            backgroundImageResize.execute(mImageUri);
        }
    }
    public class BackgroundImageResize extends AsyncTask<Uri,Integer,byte[]>
    {
        public BackgroundImageResize() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Toast.makeText(ChatActivity.this,getString(R.string.compressing_image), Toast.LENGTH_SHORT).show();
            progressBar.setIndeterminate(true);
        }

        @Override
        protected byte[] doInBackground(Uri... uris) {
            try {
                mbitmap = MediaStore.Images.Media.getBitmap(HomePage.this.getContentResolver(), uris[0]);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mbitmap = getResizedBitmap(mbitmap,1000);
                mImageUri = getUriFromBitmap(mbitmap,100,HomePage.this);
            }
            catch (Exception e)
            {
                Toast.makeText(getApplicationContext(), "Error In compression", Toast.LENGTH_SHORT).show();
            }
            return null;
        }
        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            progressBar.setIndeterminate(false);
            Picasso.with(HomePage.this).load( mImageUri ).fit().centerCrop().into(DP);
        }

    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private static Uri getUriFromBitmap(Bitmap bitmap, int quality, Context context)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),bitmap,"DP", null);
        return Uri.parse(path);
    }
    private String getFileExt(Uri uri)
    {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }
    private void sendMessage(String Name) {

        DocumentReference documentReference = db.collection("users").document(Phone);
        documentReference.addSnapshotListener(HomePage.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (mImageUri != null) {
                    StorageReference fileReference = mStoreRef.child(Phone + "." + getFileExt(mImageUri));
                    mUploadTask = fileReference.putFile(mImageUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Handler handler =  new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setProgress(0);
                                        }
                                    },1000);
                                    File file = new File(mImageUri.getPath());
                                    file.delete();
                                    taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(
                                            new OnCompleteListener<Uri>() {

                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    String fileLink = task.getResult().toString();
                                                    Map<String, Object> user = new HashMap<>();
                                                    user.put("DP", fileLink);
                                                    user.put("Name", Name);
                                                    mImageUri=null;
                                                    db.collection("users").document(Phone)
                                                            .update(user)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Toast.makeText(getApplicationContext(), "Details Added", Toast.LENGTH_SHORT).show();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(getApplicationContext(), "Failed to add Details", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(),"Image Upload Failed", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                                    progressBar.setProgress((int) progress);
                                }
                            });
                }
            }
        });
    }
}