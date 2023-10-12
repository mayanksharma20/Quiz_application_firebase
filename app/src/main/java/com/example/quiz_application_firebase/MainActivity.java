package com.example.quiz_application_firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.quiz_application_firebase.Models.CategoryModel;
import com.example.quiz_application_firebase.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.ktx.FirebaseStorageKtxRegistrar;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FirebaseDatabase database;
    FirebaseStorage storage;
    DatabaseReference reference;


    EditText   inputCategoryName;
    ImageView   categoryImage;
    Button uploadCategory;
    View fetchImage;
    Dialog dialog;

    Uri imageUri;

    int i=0;

    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onResume() {
        super.onResume();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }



        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog = new Dialog ( this);
        dialog.setContentView (R.layout.item_add_category_dialog);
        if (dialog.getWindow() !=null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCancelable(true);
        }

        progressDialog= new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        progressDialog.setMessage("Please Wait");


        uploadCategory = dialog.findViewById(R.id.uploadCategory);
        inputCategoryName = dialog.findViewById(R.id.inputCategoryName);
        categoryImage = dialog.findViewById(R.id.categoryImage);
        fetchImage = dialog.findViewById(R.id.fetchimage);

        binding.addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog.show();
            }
        });

        fetchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });

        uploadCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String categoryname=inputCategoryName.getText().toString();
                if(imageUri==null){
                    Toast.makeText(MainActivity.this, "Please upload image", Toast.LENGTH_SHORT).show();
                }else if(categoryname.isEmpty()){
                    inputCategoryName.setError("Enter Category Name");
                }else{
                    progressDialog.show();
                    uploadData();
                }

            }
        });

    }

    private void uploadData() {
//        final StorageReference reference =storage.getReference().child("category").child(new Date().getTime()+" ");
        final StorageReference reference =storage.getReference().child("category").child(new Date().getTime()+" ");

        reference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Only set the category image if the URL is not null.
                        if (uri != null) {
                            CategoryModel categoryModel = new CategoryModel();
                            categoryModel.setCategoryName(inputCategoryName.getText().toString());
                            categoryModel.setSetNum(0);
                            categoryModel.setCategoryImage(uri.toString());

                            database.getReference().child("category").child("category" + i++).setValue(categoryModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(MainActivity.this, "Data Uploaded", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            });
                        }
                    }
                });
            }
        });

//        reference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                    @Override
//                    public void onSuccess(Uri uri) {
//                        CategoryModel categoryModel=new CategoryModel();
//                        categoryModel.setCategoryName(inputCategoryName.getText().toString());
//                        categoryModel.setSetNum(0);
//                        categoryModel.setCategoryImage(imageUri.toString());
//
//                        database.getReference().child("category").child("category"+i++).setValue(categoryModel).addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void unused) {
//                                Toast.makeText(MainActivity.this, "Data Uploaded", Toast.LENGTH_SHORT).show();
//                                progressDialog.dismiss();
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                                progressDialog.dismiss();
//                            }
//                        });
//
//
//                    }
//                });
//            }
//        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(data!=null){
                imageUri=data.getData();
                categoryImage.setImageURI(imageUri);

            }
        }
    }
}