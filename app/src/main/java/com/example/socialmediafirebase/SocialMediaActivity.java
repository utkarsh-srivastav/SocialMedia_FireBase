package com.example.socialmediafirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class SocialMediaActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private FirebaseAuth mAuth;
    private ImageView postImageView;
    private Button btnCreatePost;
    private EditText edtDescription;
    private ListView usersListView;
    private Bitmap bitmap;
    private String imageIdentifier;
    private ArrayList<String> usernames, uids;
    private ArrayAdapter adapter;
    private String imageDownloadLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_media);

        mAuth = FirebaseAuth.getInstance();
        postImageView = findViewById(R.id.postImageView);
        btnCreatePost = findViewById(R.id.btnCreatePost);
        edtDescription = findViewById(R.id.edtDescription);
        usersListView = findViewById(R.id.usersListView);
        usersListView.setOnItemClickListener(this);
        usernames = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, usernames);
        uids = new ArrayList<>();

        postImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        btnCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImageToServer();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.itemLogout:
                logout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        logout();
    }

    private void logout(){
        mAuth.signOut();
        finish();
    }

    private void selectImage(){
        if (Build.VERSION.SDK_INT < 23){
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1000);
        } else if (Build.VERSION.SDK_INT >= 23){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1000);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            selectImage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == RESULT_OK && data != null){
            Uri chosenImageData = data.getData();

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), chosenImageData);
                postImageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void uploadImageToServer(){
        if (bitmap != null) {
            postImageView.setDrawingCacheEnabled(true);
            postImageView.buildDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            final byte[] data = baos.toByteArray();

            imageIdentifier = UUID.randomUUID() + ".png";   // for generating a unique value to our images

            UploadTask uploadTask = FirebaseStorage.getInstance().getReference().child("my_images").child(imageIdentifier).putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SocialMediaActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(SocialMediaActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    edtDescription.setVisibility(View.VISIBLE);

                    FirebaseDatabase.getInstance().getReference().child("my_users").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            uids.add(dataSnapshot.getKey());
                            String username = (String) dataSnapshot.child("username").getValue();
                            usernames.add(username);
                            adapter.notifyDataSetChanged();

                            usersListView.setAdapter(adapter);
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                imageDownloadLink = task.getResult().toString();
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        HashMap<String, String> dataMap = new HashMap<>();
        dataMap.put("fromWhom", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        dataMap.put("imageIdentifier", imageIdentifier);
        dataMap.put("imageLink", imageDownloadLink);
        dataMap.put("des", edtDescription.getText().toString());
        FirebaseDatabase.getInstance().getReference().child("my_users").child("received_posts").push().setValue(dataMap);
    }
}