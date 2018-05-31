package com.example.catchdemo;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.catchdemo.gallery.DetailsFragment;
import com.example.catchdemo.gallery.ExteriorFragment;
import com.example.catchdemo.gallery.InteriorFragment;
import com.google.android.cameraview.AspectRatio;
import com.google.android.cameraview.CameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        AspectRatioFragment.Listener, View.OnClickListener,
        GalleryFragmentDemo.OnFragmentInteractionListener,
        ExteriorFragment.OnFragmentInteractionListener,
        InteriorFragment.OnFragmentInteractionListener,
        DetailsFragment.OnFragmentInteractionListener {

    private static final String TAG = "CameraActivity";

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_READ_STORAGE_PERMISSION = 2;
    private static final int REQUEST_WRITE_STORAGE_PERMISSION = 3;

    public static final int CAMERA_SCALE_WIDTH = 896;
    public static final int CAMERA_SCALE_HEIGHT = 504;

    private PictureFocusType mPictureFocusType;

    private FrameLayout galleryFrame;
    private GalleryFragmentDemo galleryFragmentDemo;

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private SensorEventListener sensorEventListener;

    TextView x, y, z;

    private boolean galleryOpened;

    // Interior, Exterior, Details
    private AppCompatImageButton exteriorButton;
    private AppCompatImageButton interiorButton;
    private AppCompatImageButton detailsButton;

    private AppCompatImageButton galleryButton;

    private AppCompatImageButton fab;

    private ProgressDialog dialog;
    private ProgressBar mProgressBar;

    private String pathOfImage;

    private static final String FRAGMENT_DIALOG = "dialog";

    private static final int[] FLASH_OPTIONS = {
            CameraView.FLASH_AUTO,
            CameraView.FLASH_OFF,
            CameraView.FLASH_ON,
    };

    private static final int[] FLASH_ICONS = {
            R.drawable.ic_flash_auto,
            R.drawable.ic_flash_off,
            R.drawable.ic_flash_on,
    };

    private static final int[] FLASH_TITLES = {
            R.string.flash_auto,
            R.string.flash_off,
            R.string.flash_on,
    };

    private int mCurrentFlash;

    private CameraView mCameraView;

    private Handler mBackgroundHandler;

    public File picturesFolder;
    public File exteriorFolder;
    public File interiorFolder;
    public File detailsFolder;
    public File sessionFolder;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.take_picture:
                    if (mCameraView != null) {
                        mCameraView.takePicture();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);


        mCameraView = (CameraView) findViewById(R.id.camera);
        if (mCameraView != null) {
            mCameraView.addCallback(mCallback);
        }

        galleryOpened = false;


        fab = findViewById(R.id.take_picture);
        if (fab != null) {
            fab.setOnClickListener(mOnClickListener);
            fab.setClickable(true);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }

        mPictureFocusType = new PictureFocusType(PictureType.EXTERIOR, 0);
        if(getIntent().getExtras() != null && getIntent().getExtras().containsKey(getString(R.string.ge_picture_type))) {
            mPictureFocusType.setPictureType(
                    (PictureType) getIntent().getExtras().getSerializable(getResources().getString(R.string.ge_picture_type)));
        } else {
            mPictureFocusType.setPictureType(PictureType.EXTERIOR);
        }

        galleryFrame = findViewById(R.id.frame_layout);

        exteriorButton = (AppCompatImageButton) findViewById(R.id.camera_option_exterior);
        interiorButton = (AppCompatImageButton) findViewById(R.id.camera_option_interior);
        detailsButton  = (AppCompatImageButton) findViewById(R.id.camera_option_details);
        galleryButton  = (AppCompatImageButton) findViewById(R.id.btn_gallery);

        exteriorButton.setOnClickListener(this);
        interiorButton.setOnClickListener(this);
        detailsButton.setOnClickListener(this);
        galleryButton.setOnClickListener(this);


        Log.i(TAG, "--------------- directory_pictures = " + Environment.DIRECTORY_PICTURES);
        picturesFolder = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        // Get from previous activity VIN number
        String vin = "demo";
        if(getIntent().getExtras() != null && getIntent().getExtras().containsKey(getString(R.string.extra_message_vin_session))) {
            vin = getIntent().getExtras().getString(getResources().getString(R.string.extra_message_vin_session));
        }

        sessionFolder = new File(picturesFolder + File.separator + vin);
        if(!sessionFolder.exists()) sessionFolder.mkdirs();
        exteriorFolder = new File(sessionFolder  + File.separator + "exterior");
        interiorFolder = new File(sessionFolder  + File.separator + "interior");
        detailsFolder = new File(sessionFolder  + File.separator + "details");

        if(!exteriorFolder.exists()) exteriorFolder.mkdirs();
        if(!interiorFolder.exists()) interiorFolder.mkdirs();
        if(!detailsFolder.exists()) detailsFolder.mkdirs();

        // Set starting picture focus
        changePictureTypeFocus(mPictureFocusType.getPictureType());

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        x = findViewById(R.id.x);
        y = findViewById(R.id.y);
        z = findViewById(R.id.z);
        if(accelerometerSensor != null) {
            sensorEventListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    x.setText("x: " + String.format("%.2f", event.values[0]));
                    y.setText("y: " + String.format("%.2f", event.values[1]));
                    z.setText("z: " + String.format("%.2f", event.values[2]));
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };
        } else {
            Toast.makeText(this, "The device has no Gyroscope", Toast.LENGTH_LONG).show();
        }



    }

    @Override
    protected void onResume() {
        System.out.println(TAG + " ---------------------------- onResume()");
        super.onResume();
        galleryFrame.setClickable(false);
        galleryFrame.setFocusable(false);

        galleryOpened = false;
        sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            mCameraView.start();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ConfirmationDialogFragment
                    .newInstance(R.string.camera_permission_confirmation,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION,
                            R.string.camera_permission_not_granted)
                    .show(getSupportFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }


        /* Request WRITE_EXTERNAL_STORAGE  */

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, " ---------------------- WRITE_EXTERNAL_STORAGE = PERMISSION_GRANTED");
            // mCameraView.start();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.i(TAG, " ---------------------- WRITE_EXTERNAL_STORAGE = shouldShowRequestPermissionRationale");
            ConfirmationDialogFragment
                    .newInstance(R.string.write_storage_permission_confirmation,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_WRITE_STORAGE_PERMISSION,
                            R.string.write_storage_permission_not_granted)
                    .show(getSupportFragmentManager(), FRAGMENT_DIALOG);
        } else {
            Log.i(TAG, " ---------------------- WRITE_EXTERNAL_STORAGE = PERMISSION_DENIED");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE_PERMISSION);
        }
        if (fab != null) {
            fab.setClickable(true);
        }
    }

    @Override
    protected void onPause() {
        System.out.println(TAG + " ---------------------------- onResume()");
        closeGallery();
        mCameraView.stop();
        sensorManager.unregisterListener(sensorEventListener);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBackgroundHandler != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackgroundHandler.getLooper().quitSafely();
            } else {
                mBackgroundHandler.getLooper().quit();
            }
            mBackgroundHandler = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (permissions.length != 1 || grantResults.length != 1) {
                    throw new RuntimeException("Error on requesting camera permission.");
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.camera_permission_not_granted,
                            Toast.LENGTH_SHORT).show();
                }
                // No need to start camera here; it is handled by onResume
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.aspect_ratio:
                FragmentManager fragmentManager = getSupportFragmentManager();
                if (mCameraView != null
                        && fragmentManager.findFragmentByTag(FRAGMENT_DIALOG) == null) {
                    final Set<AspectRatio> ratios = mCameraView.getSupportedAspectRatios();
                    final AspectRatio currentRatio = mCameraView.getAspectRatio();
                    AspectRatioFragment.newInstance(ratios, currentRatio)
                            .show(fragmentManager, FRAGMENT_DIALOG);
                }
                return true;*/
            case R.id.switch_flash:
                if (mCameraView != null) {
                    mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
                    item.setTitle(FLASH_TITLES[mCurrentFlash]);
                    item.setIcon(FLASH_ICONS[mCurrentFlash]);
                    mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
                }
                return true;
            /*case R.id.switch_camera:
                if (mCameraView != null) {
                    int facing = mCameraView.getFacing();
                    mCameraView.setFacing(facing == CameraView.FACING_FRONT ?
                            CameraView.FACING_BACK : CameraView.FACING_FRONT);
                }
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAspectRatioSelected(@NonNull AspectRatio ratio) {
        if (mCameraView != null) {
            Toast.makeText(this, ratio.toString(), Toast.LENGTH_SHORT).show();
            mCameraView.setAspectRatio(ratio);
        }
    }

    private Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }



    private CameraView.Callback mCallback
            = new CameraView.Callback() {


        @Override
        public void onCameraOpened(CameraView cameraView) {
            Log.d(TAG, "onCameraOpened");
            if (mCameraView != null) {
                mCameraView.setAspectRatio(AspectRatio.of(16,9));
                mCameraView.setAdjustViewBounds(true);
                mCameraView.setFocusable(true);
                mCameraView.setFocusableInTouchMode(true);
                mCameraView.setFitsSystemWindows(true);
            }
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            Log.i(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            Log.i(TAG, "onPictureTaken " + data.length);
            toast("Picture Taken");
            // Hide Take Picture Button
            fab.setClickable(false);

            getBackgroundHandler().post(new Runnable() {
                @Override
                public void run() {

                    File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            "picture.jpg");
                    String pic_name = "pic_" + System.currentTimeMillis() + ".jpg";
                    File file2 = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), pic_name);


                    switch (mPictureFocusType.getPictureType()) {
                        case EXTERIOR:
                            file2 = new File(exteriorFolder, pic_name);
                            break;
                        case INTERIOR:
                            file2 = new File(interiorFolder, pic_name);
                            break;
                        case DETAILS:
                            file2 = new File(detailsFolder, pic_name);
                            break;
                    }

                    OutputStream os = null;
                    try {
                        os = new FileOutputStream(file);
                        os.write(data);
                        os.close();

                        Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Bitmap scaled = Bitmap.createScaledBitmap(b, CAMERA_SCALE_WIDTH, CAMERA_SCALE_HEIGHT, false);


                        FileOutputStream fOut = new FileOutputStream(file2);

                        scaled.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                        fOut.flush();
                        fOut.close();

                    } catch (IOException e) {
                        Log.w(TAG, "Cannot write to " + file, e);
                    } finally {
                        if (os != null) {
                            try {
                                os.close();
                                // Start AfterActivity

                                //getMaskFromServer(file2.getAbsolutePath());
                                toast("Picture Saved!");
                                fab.setClickable(true);
                                /*
                                Intent i = new Intent(CameraActivity.this, AfterActivityDemo.class);
                                i.putExtra("pathMask", file2.getAbsolutePath());
                                i.putExtra("pathImage", file2.getAbsolutePath());
                                startActivity(i);
                                */

                            } catch (IOException e) {
                                // Ignore
                            }
                        }
                    }
                }
            });


        }


        private void saveImage(Bitmap finalBitmap, String image_name) {

            if(isExternalStorageWritable() && finalBitmap!= null) {
                Log.i(TAG, "----------------- isExternalStorageWritable() = true");
                // MediaStore.Images.Media.insertImage(getContentResolver(), finalBitmap, image_name , "This is a description");
                CapturePhotoUtils.insertImage(getContentResolver(), finalBitmap, image_name, "Catch demo app image compressed");
            }
        }

        /* Checks if external storage is available for read and write */
        public boolean isExternalStorageWritable() {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                return true;
            }
            return false;
        }

        /* Checks if external storage is available to at least read */
        public boolean isExternalStorageReadable() {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state) ||
                    Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                return true;
            }
            return false;
        }

        public File getAlbumStorageDir(Context context, String albumName) {
            // Get the directory for the app's private pictures directory.
            File file = new File(context.getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES), albumName);
            if (!file.mkdirs()) {
                Log.e(TAG, "Directory not created");
            }
            return file;
        }
    };


    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void getMaskFromServer(String pathOfImage) {

        Bitmap picture = null;
        File imgFile = new  File(pathOfImage);

        if(imgFile.exists()){
            Log.i(TAG, "---------------------- Picture file exists");
            picture = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            //imagePreview.setImageBitmap(picture);
        } else {
            Log.i(TAG, "---------------------- Picture file NOT exists");
        }

        Bitmap res = null;

        AsyncTaskRunner runner = new AsyncTaskRunner(this);
        try {
            res = runner.execute(picture).get();
        } catch (InterruptedException e) {
            Log.i(TAG, "---------------------- InterruptedException()");
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.i(TAG, "---------------------- ExecutionException()");
            e.printStackTrace();
        } finally {
            if(res != null) {

                String pathOfMask = saveToInternalStorage(res, pathOfImage);

                Intent i = new Intent(CameraActivity.this, AfterActivityDemo.class);
                i.putExtra("pathMask", pathOfMask);
                i.putExtra("pathImage", pathOfImage);
                startActivity(i);
            } else {
                Toast.makeText(this, "Error with comunication", Toast.LENGTH_LONG).show();
            }
        }
        // mProgressBar.setVisibility(View.INVISIBLE);
    }

    private String saveToInternalStorage(Bitmap bitmapImage, String pathOfImage){

        String pathOfMask = pathOfImage.substring(0, pathOfImage.lastIndexOf('.'));
        pathOfMask = pathOfMask + "_mask.jpg";
        File file = new File(pathOfMask);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return (pathOfMask);
    }

    private void changePictureTypeFocus(PictureType pictureType) {
        this.mPictureFocusType.setPictureType(pictureType);

        exteriorButton.setImageResource(R.drawable.image_button_exterior_selected);
        interiorButton.setImageResource(R.drawable.image_button_interior);
        detailsButton.setImageResource(R.drawable.image_button_details);

        switch(pictureType){
            case EXTERIOR:
                Toast.makeText(this,"Changed to EXTERIOR", Toast.LENGTH_LONG).show();
                exteriorButton.setImageResource(R.drawable.image_button_exterior_selected);
                break;
            case INTERIOR:
                Toast.makeText(this,"Changed to INTERIOR", Toast.LENGTH_LONG).show();
                interiorButton.setImageResource(R.drawable.image_button_interior);
                break;
            case DETAILS:
                Toast.makeText(this,"Changed to DETAILS", Toast.LENGTH_LONG).show();
                detailsButton.setImageResource(R.drawable.image_button_details);
                break;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.camera_option_exterior:
                changePictureTypeFocus(PictureType.EXTERIOR);
                break;
            case R.id.camera_option_interior:
                changePictureTypeFocus(PictureType.INTERIOR);
                break;
            case R.id.camera_option_details:
                changePictureTypeFocus(PictureType.DETAILS);
                break;
            case R.id.btn_gallery:
                testGallery();
                break;
        }
    }

    public void testGallery() {
        galleryOpened = true;
        galleryFrame.setClickable(true);
        galleryFrame.setFocusable(true);


        mCameraView.stop();
        sensorManager.unregisterListener(sensorEventListener);

        galleryFragmentDemo = new GalleryFragmentDemo();
        // Finally , add the fragment
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frame_layout,galleryFragmentDemo)
                .commit();
    }

    public void closeGallery() {
        if(galleryOpened) {

            sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                mCameraView.start();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                ConfirmationDialogFragment
                        .newInstance(R.string.camera_permission_confirmation,
                                new String[]{Manifest.permission.CAMERA},
                                REQUEST_CAMERA_PERMISSION,
                                R.string.camera_permission_not_granted)
                        .show(getSupportFragmentManager(), FRAGMENT_DIALOG);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_PERMISSION);
            }

            galleryOpened = false;
            galleryFrame.setFocusable(false);
            galleryFrame.setClickable(false);
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(galleryFragmentDemo).commit();
        }
    }

    @Override
    public void onBackPressed() {
        if(galleryOpened) {
            closeGallery();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.i(TAG, "------------ uri: " + uri.toString());
    }

    public static class ConfirmationDialogFragment extends DialogFragment {

        private static final String ARG_MESSAGE = "message";
        private static final String ARG_PERMISSIONS = "permissions";
        private static final String ARG_REQUEST_CODE = "request_code";
        private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";

        public static ConfirmationDialogFragment newInstance(@StringRes int message,
                String[] permissions, int requestCode, @StringRes int notGrantedMessage) {
            ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_MESSAGE, message);
            args.putStringArray(ARG_PERMISSIONS, permissions);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage);
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle args = getArguments();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(args.getInt(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String[] permissions = args.getStringArray(ARG_PERMISSIONS);
                                    if (permissions == null) {
                                        throw new IllegalArgumentException();
                                    }
                                    ActivityCompat.requestPermissions(getActivity(),
                                            permissions, args.getInt(ARG_REQUEST_CODE));
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getActivity(),
                                            args.getInt(ARG_NOT_GRANTED_MESSAGE),
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                    .create();
        }

    }

}