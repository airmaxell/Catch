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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class AfterActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "AfterActivity";

    RecyclerView horizontal_recycler_view;
    HorizontalAdapter horizontalAdapter;
    private List<Data> data;

    private ProgressDialog dialog;

    TextView tv;
    AppCompatImageView imagePreview;

    Bitmap picture;
    Bitmap masked_image;
    Bitmap mask;
    Bitmap to_preview;

    AppCompatButton btn_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after);

        btn_save = (AppCompatButton) findViewById(R.id.btn_save_image);
        btn_save.setOnClickListener(this);
        horizontal_recycler_view= (RecyclerView) findViewById(R.id.horizontal_recycler_view);

        data = fill_with_data();
        imagePreview = (AppCompatImageView) findViewById(R.id.image_preview);

        horizontalAdapter = new HorizontalAdapter(data, getApplication());

        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(AfterActivity.this, LinearLayoutManager.HORIZONTAL, false);
        horizontal_recycler_view.setLayoutManager(horizontalLayoutManager);
        horizontal_recycler_view.setAdapter(horizontalAdapter);



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
            Log.i(TAG, "---------------------- res != null");
            masked_image = maskingImage(picture, mask);
            imagePreview.setImageBitmap(masked_image);
        } else {
            imagePreview.setImageBitmap(picture);
        }


    }

    private Bitmap loadImageFromStorage(String path)
    {
        Bitmap img = null;
        try {
            File f=new File(path);
            img = BitmapFactory.decodeStream(new FileInputStream(f));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return img;
    }

    private Bitmap maskingImage(Bitmap picture, Bitmap mask) {

        Log.i(TAG, "--------------- mask resolution: " + mask.getWidth() + " x " + mask.getHeight());
        Log.i(TAG, "--------------- picture resolution: " + picture.getWidth() + " x " + picture.getHeight());
        Bitmap result = Bitmap.createBitmap(picture.getWidth(), picture.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mCanvas.drawBitmap(picture, 0, 0, null);
        mCanvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(null);
        return result;
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

    public List<Data> fill_with_data() {

        List<Data> data = new ArrayList<>();

        data.add(new Data( R.drawable.denzel_mini, "denzel"));
        data.add(new Data( R.drawable.garaza_mini, "garaza"));
        data.add(new Data( R.drawable.grafiti_mini, "grafiti"));
        data.add(new Data( R.drawable.himalaji_mini, "himalaji"));
        data.add(new Data( R.drawable.more_mini, "more"));
        data.add(new Data( R.drawable.salon_mini, "salon"));

        return data;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_save_image:
                saveImageToGallery(to_preview);
                break;
        }
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

    public class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.MyViewHolder> {


        List<Data> horizontalList = Collections.emptyList();
        Context context;


        public HorizontalAdapter(List<Data> horizontalList, Context context) {
            this.horizontalList = horizontalList;
            this.context = context;
        }


        public class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView imageView;
            TextView txtview;
            public MyViewHolder(View view) {
                super(view);
                imageView=(ImageView) view.findViewById(R.id.imageview);
                txtview=(TextView) view.findViewById(R.id.txtview);
            }
        }



        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.vertical_menu, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {

            holder.imageView.setImageResource(horizontalList.get(position).imageId);
            holder.txtview.setText(horizontalList.get(position).txt);

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override

                public void onClick(View v) {
                    String list = horizontalList.get(position).txt.toString();
                    Toast.makeText(AfterActivity.this, list, Toast.LENGTH_SHORT).show();


                    int id = context.getResources().getIdentifier(list, "drawable", context.getPackageName());
                    Bitmap bg = BitmapFactory.decodeResource(context.getResources(), id);
                    to_preview = mergeBitmaps(masked_image, bg);

                    imagePreview.setImageBitmap(to_preview);

                }

            });

        }

        @Override
        public int getItemCount()
        {
            return horizontalList.size();
        }
    }
}
