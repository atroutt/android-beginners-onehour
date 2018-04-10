package com.audreytroutt.androidbeginners.firstapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final String TAG = "MainActivity";

    private DrawerLayout mDrawer;
    private FloatingActionButton fab;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        try {
            if (haveSelfieImageLocally()) {
                updateMainImageFromFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (haveSelfieImageLocally()) {
                        shareAction();
                    } else {
                        captureImage();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setUserInfoInDrawer();
    }

    private void setUserInfoInDrawer() {
        // TODO Project 3: Get user info and display it at the top of the drawer
    }

    private void unsetUserInfoInDrawer() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView currentUserEmail = (TextView) headerView.findViewById(R.id.current_user_email);
        currentUserEmail.setText(R.string.user_email_placeholder_text);
        TextView currentUserName = (TextView) headerView.findViewById(R.id.current_user_name);
        currentUserName.setText(R.string.user_name_placeholder_text);
        ImageView currentUserImage = (ImageView) headerView.findViewById(R.id.current_user_photo);
        Picasso.with(this).load(R.mipmap.ic_launcher).into(currentUserImage);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_sign_out) {
            // TODO Project 3: implement sign out
        } else if (id == R.id.action_delete_photo) {
            deleteSavedSelfieImage();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteSavedSelfieImage() {
        File savedImage = null;
        try {
            savedImage = getSelfieImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (savedImage != null && savedImage.exists()) {
            if (!isStoragePermissionGranted()) {
                requestWriteExternalStoragePermission();
            }
            savedImage.delete();
            Toast.makeText(this, "Photo deleted", Toast.LENGTH_LONG).show();

            resetMainImageToInitialState();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // TODO Project 2: call the captureImage() method
        } else if (id == R.id.nav_list) {
            // TODO Project 2: create an intent for the PaintingListActivity
        } else if (id == R.id.nav_grid) {
            // TODO Project 2: create an intent for the PaintingGridActivity
        } else if (id == R.id.nav_web) {
            // TODO Project 2: create an intent to open a url
        } else if (id == R.id.nav_share) {
            // TODO Project 2: call the shareAction() method
        } else if (id == R.id.nav_send) {
            // TODO Project 2: create an intent to send an email
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void captureImage() {
        if (!isStoragePermissionGranted()) {
            requestWriteExternalStoragePermission();
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = getTempImageFile();
            } catch (IOException e) {
                // Error occurred while creating the File
                e.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.audreytroutt.androidbeginners.firstapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            } else {
                Toast.makeText(this, "There was an error accessing the camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void shareAction() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "I just made my first Android app! #androidbeginner #womenintech #WITSNE");
        shareIntent.setType("text/plain");
        try {
            if (haveSelfieImageLocally()) {
                shareIntent.putExtra(Intent.EXTRA_STREAM, getSelfieImageUri());
                shareIntent.setType("*/*");
            }
            startActivity(shareIntent);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error accessing photo to share", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            // A picture was just taken, let's display that in our image view
            editImage();
            updateMainImageFromFile();
        }
    }

    // ----------------------------------
    // Image-related methods
    // ----------------------------------

    private Uri getTempImageUri() {
        Uri photoURI;
        photoURI = FileProvider.getUriForFile(this,
                "com.audreytroutt.androidbeginners.firstapp.fileprovider",
                new File(mCurrentPhotoPath));

        return photoURI;
    }

    private Uri getSelfieImageUri() throws IOException {
        Uri photoURI;
        photoURI = FileProvider.getUriForFile(this,
                "com.audreytroutt.androidbeginners.firstapp.fileprovider",
                getSelfieImageFile());

        return photoURI;
    }

    private boolean haveSelfieImageLocally() throws IOException {
        return getSelfieImageFile().exists();
    }

    /** Create a File for saving an image or video */
    private File getTempImageFile() throws IOException {
        File mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                "tempImage",  /* prefix */
                ".jpg",         /* suffix */
                mediaStorageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private File getSelfieImageFile() throws IOException {
        File mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(mediaStorageDir, "selfieImage.jpg");
    }

    private void updateMainImageFromFile() {
        try {
            ImageView imageView = (ImageView) findViewById(R.id.camera_image);
            Bitmap bitmap = BitmapFactory.decodeFile(getSelfieImageFile().getPath(), null);
            imageView.setImageBitmap(bitmap);

            ((TextView) findViewById(R.id.welcome_message)).setText(R.string.main_screen_welcom_message_if_image_set);

            // Hide the instructions for taking a photo
            findViewById(R.id.initial_arrow_image).setVisibility(View.INVISIBLE);
            findViewById(R.id.initial_instructions).setVisibility(View.INVISIBLE);

            // Switch the icon on the FAB to share
            fab.setImageResource(R.drawable.ic_share);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading selfie image", Toast.LENGTH_SHORT).show();
        }
    }

    // this is the opposite of updateMainImageFromFile
    private void resetMainImageToInitialState() {
        ImageView imageView = (ImageView)findViewById(R.id.camera_image);
        imageView.setImageBitmap(null);

        ((TextView)findViewById(R.id.welcome_message)).setText(R.string.main_screen_welcome_message_if_no_image);

        // Show the instructions for taking a photo
        findViewById(R.id.initial_arrow_image).setVisibility(View.VISIBLE);
        findViewById(R.id.initial_instructions).setVisibility(View.VISIBLE);

        // Switch the icon on the FAB to camera
        fab.setImageResource(R.drawable.ic_camera_alt);
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {
                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk < 23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    private void requestWriteExternalStoragePermission() {
        int requestCodeIgnoredForNow = 0;
        ActivityCompat.requestPermissions(this, new String[]{ WRITE_EXTERNAL_STORAGE }, requestCodeIgnoredForNow);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // continue editing image now that we have permission
            editImage();
        }
    }

    private void editImage() {
        if (!isStoragePermissionGranted()) {
            requestWriteExternalStoragePermission();
        }

        // Load the image into memory from the file
        Bitmap bmp = BitmapFactory.decodeFile(mCurrentPhotoPath, null);

        // Square up the image from the camera
        int croppedImageSize = (int)Math.min(bmp.getWidth(), bmp.getHeight());
        Bitmap cropped = centerCropBitmapToSquareSize(bmp, croppedImageSize);

        // TODO Project 4: Draw text on the cropped image

        // Finally, save the edited image back to the file
        saveBitmapToFile(cropped);
    }

    private Bitmap centerCropBitmapToSquareSize(Bitmap bmp, int cropSize) {
        int cropStartX = (int)Math.max(0, (int)(bmp.getWidth() / 2) - (int)(cropSize / 2));
        int cropStartY = (int)Math.max(0, (int)(bmp.getHeight() / 2) - (int)(cropSize / 2));
        Matrix matrix = new Matrix();
        // I rotate the image or your selfies (from front camera) will be sideways! This does mean that rear camera shots are usually upside-down.
        matrix.postRotate(270);
        return Bitmap.createBitmap(bmp, cropStartX, cropStartY, cropSize, cropSize, matrix, true);
    }

    private void saveBitmapToFile(Bitmap bitmap) {
        FileOutputStream out = null;
        try {
            // overwrite the file
            out = new FileOutputStream(getSelfieImageFile());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        } catch (Exception e) {
            Log.e(TAG, "save edited image failed", e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "close stream failed", e);
            }
        }
    }
}
