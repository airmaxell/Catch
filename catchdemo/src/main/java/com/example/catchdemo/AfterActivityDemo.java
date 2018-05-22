/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.catchdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Random;

public class AfterActivityDemo extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AfterActivvityDemo";

    private LinearLayout myGallery;
    private AppCompatButton btn_save;
    private AppCompatImageView imagePreview;

    private Bitmap picture;
    private Bitmap masked_image;
    private Bitmap mask;
    private Bitmap to_preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_demo);

        myGallery = findViewById(R.id.images_galllery);
        int[] files = {R.drawable.denzel, R.drawable.garaza, R.drawable.grafiti, R.drawable.himalaji, R.drawable.more, R.drawable.salon};
        for (int id : files){
            myGallery.addView(insertPhotoDrawable(id));
        }

        btn_save = (AppCompatButton) findViewById(R.id.btn_save_image);
        btn_save.setOnClickListener(this);

        imagePreview = (AppCompatImageView) findViewById(R.id.image_preview);

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        Bitmap mask = null;

        // Get path of mask from CameraActivity
        Intent intent = getIntent();
        String maskPath = intent.getExtras().getString("pathMask");
        String imagePath = intent.getExtras().getString("pathImage");
        Log.i(TAG, "---------------------- path = " + maskPath);

        picture = loadImageFromStorage(imagePath);
        mask = loadImageFromStorage(maskPath);

        if(mask != null) {
            Log.i(TAG, "---------------------- mask != null");
            masked_image = maskingImage(picture, mask);
            imagePreview.setImageBitmap(masked_image);
        } else {
            imagePreview.setImageBitmap(picture);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        applyBackground(R.drawable.denzel);

    }

    private Bitmap loadImageFromStorage(String path)
    {
        Bitmap img = null;
        try {
            File f=new File(path);
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inMutable = true;
            img = BitmapFactory.decodeStream(new FileInputStream(f), null, opt);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return img;
    }



    private Bitmap maskingImage(Bitmap picture, Bitmap mask) {
        long time_enter = System.currentTimeMillis();

        //mask.reconfigure(mask.getWidth(), mask.getHeight(), Bitmap.Config.ALPHA_8);
        //testImage(mask);
        Log.i(TAG, " --------- get in" + System.currentTimeMillis() );
        Bitmap result = Bitmap.createBitmap(picture.getWidth(), picture.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(result);

        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        float[] src = {
                0, 0, 0, 0, 255,
                0, 0, 0, 0, 255,
                0, 0, 0, 0, 255,
                1, 1, 1, -1, 0,
        };
        ColorMatrix cm = new ColorMatrix(src);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(filter);
        mCanvas.drawBitmap(picture, 0, 0, null);
        mCanvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(null);
        Log.i(TAG, "------- time spent: " + (System.currentTimeMillis() - time_enter) + " milliseconds.");
        return result;
    }

    void testImage(Bitmap image) {
        int black = 0;
        int white = 0;
        int other = 0;
        for(int i = 0; i < image.getWidth(); i++) {
            for(int j = 0; j < image.getHeight(); j++) {
                if(image.getPixel(i, j) == Color.BLACK) {
                    black++;
                } else if(image.getPixel(i,j) == Color.WHITE) {
                    white++;
                } else if(image.getPixel(i, j) == Color.TRANSPARENT) {
                    other++;
                }

            }
        }

        Log.i(TAG, "------------------ Test Results");
        Log.i(TAG, "------------------ black = " + black);
        Log.i(TAG, "------------------ white = " + white);
        Log.i(TAG, "------------------ transparent = " + other);
    }

    private View insertPhotoDrawable(final int id) {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), id);

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
        layout.setGravity(Gravity.CENTER);

        ImageView imageView = new ImageView(getApplicationContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(180, 180));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(bm);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyBackground(id);
            }
        });

        layout.addView(imageView);
        return layout;
    }

    public void applyBackground(int id) {
        Bitmap bg = BitmapFactory.decodeResource(getResources(), id);
        to_preview = mergeBitmaps(masked_image, bg);

        imagePreview.setImageBitmap(to_preview);
    }

    public Bitmap mergeBitmaps(Bitmap picture, Bitmap background) {
        Bitmap bmOverlay = Bitmap.createBitmap(picture.getWidth(), picture.getHeight(), picture.getConfig());
        System.out.println("---------------------- picture resolution: " + picture.getWidth() + " x " + picture.getHeight());
        System.out.println("---------------------- background resolution: " + background.getWidth() + " x " + background.getHeight());
        Canvas canvas = new Canvas(bmOverlay);
        Bitmap bg = getResizedBitmap(background, CameraActivity.CAMERA_SCALE_WIDTH, CameraActivity.CAMERA_SCALE_HEIGHT);
        System.out.println("---------------------- background resolution: " + bg.getWidth() + " x " + bg.getHeight());
        canvas.drawBitmap(bg, 0, 0, null);
        canvas.drawBitmap(picture, 0, 0, null);
        return bmOverlay;
    }


    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }



    public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth, int reqHeight) {
        Bitmap bm = null;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(path, options);

        return bm;
    }

    public int calculateInSampleSize(

            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float)height / (float)reqHeight);
            } else {
                inSampleSize = Math.round((float)width / (float)reqWidth);
            }
        }

        return inSampleSize;
    }

    private void saveImageToGallery(Bitmap finalBitmap) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/saved_images_1");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Toast.makeText(this,"Image saved to gallery.", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            Toast.makeText(this,"Error while saving image.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }


        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_save_image:
                saveImageToGallery(to_preview);
                break;
        }
    }
}
