package com.example.android.travelmantics_firebase;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class DealActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int IMAGE_GALLARD_REQUEST = 21;
    public static final String LOG_TAG = "LOG_TAG";
    EditText txtTitle;
    EditText txtPrice;
    EditText txtDescription;
    TextView mTvNoImage;
    Button mBtnSelectImage;
    ImageView mSelectedImage;

    private DatabaseReference mDatabaseReference;

    private TravelDeal mDeal;
    private Uri mImageUri;
    private int mPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

//        FirebaseUtil.openFirebaseReference("traveldeals", this);
        mDatabaseReference = FirebaseUtil.sDatabaseReference;

        mSelectedImage = findViewById(R.id.iv_deal);
        mBtnSelectImage = findViewById(R.id.btn_select_image);
        mBtnSelectImage.setOnClickListener(this);
        txtTitle = findViewById(R.id.txtTitle);
        txtDescription = findViewById(R.id.txtDescription);
        txtPrice = findViewById(R.id.txtPrice);
        mTvNoImage = findViewById(R.id.tv_no_image);

        Intent intent = getIntent();
//        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        mPosition = intent.getIntExtra("position", -1);

        TravelDeal deal;

        if (mPosition == -1) {
            deal = new TravelDeal();
        } else {
            deal = FirebaseUtil.sDeals.get(mPosition);
        }

        mDeal = deal;
        txtTitle.setText(mDeal.getTitle());
        txtDescription.setText(mDeal.getDescription());
        txtPrice.setText(mDeal.getPrice());
        if (mDeal.getImageUrl() != null && !TextUtils.isEmpty(mDeal.getImageUrl())) {
            Picasso.get()
                    .load(mDeal.getImageUrl())
                    .into(mSelectedImage);
            mTvNoImage.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.save_menu:
                saveDeal();
                Toast.makeText(this, "Deal saved", Toast.LENGTH_SHORT).show();
                clean();
                backToList();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                clean();
                Toast.makeText(this, "Deal deleted", Toast.LENGTH_SHORT).show();
                backToList();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void clean() {
        txtTitle.setText("");
        txtDescription.setText("");
        txtPrice.setText("");
        txtTitle.requestFocus();
    }

    private void saveDeal() {
        mDeal.setTitle(txtTitle.getText().toString());
        mDeal.setDescription(txtDescription.getText().toString());
        mDeal.setPrice(txtPrice.getText().toString());
        if (mImageUri == null || mImageUri.getLastPathSegment() == null) {
            updateFirebaseWithDeal();
            return;
        }

        // saving the picture into firebase
        Log.d(LOG_TAG, "Image not null");
        final StorageReference storageRef = FirebaseUtil.sStorageReference.child(mImageUri.getLastPathSegment());
        storageRef.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String url = uri.toString();
                        mDeal.setImageUrl(uri.toString());
                        Log.d(LOG_TAG, "URI: " + url);
                        String imageName = storageRef.getName();
                        mDeal.setImageName(imageName);
                        Log.d(LOG_TAG, "the storageRef is: " + imageName);

                        updateFirebaseWithDeal();
                    }
                });
            }
        });
    }

    void updateFirebaseWithDeal() {
        if (mDeal.getId() == null) {
            // insert new deal into Firebase
            mDatabaseReference.push().setValue(mDeal);

        } else {
            // update current deal
            mDatabaseReference.child(mDeal.getId()).setValue(mDeal);
        }
    }

    private void deleteDeal() {
        if (mDeal == null) {
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabaseReference.child((mDeal).getId()).removeValue();

        if (mDeal.getImageName() != null && !TextUtils.isEmpty(mDeal.getImageName())) {

            StorageReference picRef = FirebaseUtil.sStorageReference.child(mDeal.getImageName());
            // deleting the image of the deal from firebase
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(LOG_TAG, "Image Deleted successfully");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(LOG_TAG, "Error Deleting Image: " + e.getMessage());
                }
            });
        }
    }

    private void backToList() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_select_image) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);

            // where to find data
            File pictureDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            String pictureDirPath = pictureDir.getPath();

            // get URI representation
            Uri data = Uri.parse(pictureDirPath);

            // get all image types
            photoPickerIntent.setDataAndType(data, "image/*");

            startActivityForResult(photoPickerIntent, IMAGE_GALLARD_REQUEST);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(LOG_TAG, "onActivityResult()");
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_GALLARD_REQUEST) {

                // the address of the image on the SD card
                if (data != null) {
                    mImageUri = data.getData();
                }

                try {
                    InputStream inputStream = getContentResolver().openInputStream(mImageUri);

                    Bitmap image = BitmapFactory.decodeStream(inputStream);
                    mSelectedImage.setImageBitmap(image);

                    mTvNoImage.setVisibility(View.GONE);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to open the image", Toast.LENGTH_LONG).show();
                }


            }
        }
    }
}
