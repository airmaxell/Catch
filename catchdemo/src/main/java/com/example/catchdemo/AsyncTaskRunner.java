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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Array;
import java.util.Iterator;

/**
 * Created by Maxell on 2/12/2018.
 */

class AsyncTaskRunner extends AsyncTask<Bitmap, Void, Bitmap> {

    private static String TAG = "AsyncTaskRunner";

    private String SERVER_ADDRESS = "";

    /** progress dialog to show user that the backup is processing. */
    private ProgressDialog dialog;
    /** application context. */
    private Activity activity;
    private String server_ip;

    public AsyncTaskRunner(Activity activity) {
        this.activity = activity;
        //dialog = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Context context = this.activity;
        SharedPreferences sp = context.getSharedPreferences(this.activity.getString(R.string.catch_server_sp), Context.MODE_PRIVATE);
        server_ip = sp.getString(this.activity.getString(R.string.sp_ip_address), "34.240.15.254");

        SERVER_ADDRESS = "http://" + server_ip + ":5000/uploader";

        //this.dialog.setTitle("Processing");
        //this.dialog.setMessage("Please wait...");
        //this.dialog.show();
    }

    @Override
    protected Bitmap doInBackground(Bitmap... arrays) {

        Bitmap picture = arrays[0];
        return getFromServer(picture);
        //return maskingImage(picture, getFromServer(picture));
    }


    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        //if (dialog.isShowing()) {
            //dialog.dismiss();
        //}
    }

    private Bitmap getFromServer(Bitmap picture) {
        Bitmap mask = null;
        long time_enter = System.currentTimeMillis();
        Log.i(TAG, "-------- Get in getFromServer()");
        try {
            Log.i("AsyncTaskRunner", "-------------------- SERVER_ADDRESS = " + SERVER_ADDRESS);
            URL url = new URL(SERVER_ADDRESS);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");

            MultipartEntity entity = new MultipartEntity(
                    HttpMultipartMode.BROWSER_COMPATIBLE);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            picture.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] data = bos.toByteArray();
            ByteArrayBody bab = new ByteArrayBody(data, "picture-compressed.jpg");
            entity.addPart("image", bab);

            //entity.addPart("someOtherStringToSend", new StringBody("your string here"));

            conn.addRequestProperty("Content-length", entity.getContentLength() + "");
            conn.addRequestProperty(entity.getContentType().getName(), entity.getContentType().getValue());

            OutputStream os = conn.getOutputStream();
            entity.writeTo(conn.getOutputStream());
            os.close();
            conn.connect();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inMutable = true;
                Bitmap newBitmap = BitmapFactory.decodeStream(conn.getInputStream());
                //newBitmap.eraseColor(Color.BLACK);

                //newBitmap.reconfigure(newBitmap.getWidth(), newBitmap.getHeight(), Bitmap.Config.ALPHA_8);
                mask = newBitmap;
                //mask = BlackToAlpha(newBitmap);
                //mask.eraseColor(Color.BLACK);
            }

            conn.disconnect();

        } catch (IOException e) {
            System.out.println("AsyncTask --------------------- IOException");
            e.printStackTrace();
        }

        Log.i(TAG, "------- time spent: " + ((System.currentTimeMillis() - time_enter)/1000) + " seconds.");
        return mask;
    }


    private Bitmap BlackToAlpha(Bitmap image) {
        Bitmap rgbImage;
        rgbImage =  Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        rgbImage.setHasAlpha(true);

        int black = 0;
        int white = 0;
        int other = 0;
        for(int i = 0; i < rgbImage.getWidth(); i++) {
            for(int j = 0; j < rgbImage.getHeight(); j++) {
                if(image.getPixel(i, j) == Color.BLACK) {
                    rgbImage.setPixel(i, j, Color.TRANSPARENT);
                    black++;
                } else if(image.getPixel(i,j) == Color.WHITE) {
                    white++;
                    rgbImage.setPixel(i, j, Color.WHITE);
                } else if(image.getPixel(i, j) == Color.TRANSPARENT) {
                    other++;
                }

            }
        }


        Log.i(TAG, "------------------ Izlazi iz BlackToAlpha()");
        Log.i(TAG, "------------------ black = " + black);
        Log.i(TAG, "------------------ white = " + white);
        Log.i(TAG, "------------------ other = " + other);

        return image;
    }




    private Bitmap maskingImage(Bitmap picture, Bitmap mask) {
        Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas mCanvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mCanvas.drawBitmap(picture, 0, 0, null);
        mCanvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(null);
        return result;
    }

    private String findLargest(JSONObject jsonObject) {
        String key_max = "0";
        int max_ones = 0;
        try {
            Iterator<String> temp = jsonObject.keys();
            while (temp.hasNext()) {
                String key = temp.next();
                String value = jsonObject.getString(key);
                System.out.println("AsyncTask ----------------------- temp.hasNext()  ----- value.length = " + value.length());
                int count = 0;
                for(int i = 0; i < value.length(); i++) {
                    if(value.charAt(i) == '1') {
                        count++;
                    }
                }
                if(count > max_ones) {
                    key_max = key;
                    max_ones = count;
                }
                System.out.println("AsyncTask ----------------------- number of '1' = " + count);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return key_max;
    }

    private String readStream(InputStream in) {

        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return builder.toString();
    }

    private Bitmap convertToBitmapMask(String mask_string, int width, int height) {
        Bitmap mask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mask.setHasAlpha(true);
        String[] lines = mask_string.split(";");
        System.out.println("AsyncTasj ------------------ convertToBitmapMask() ------ lines.length() = " + lines.length);
        System.out.println("AsyncTasj ------------------ mask = " + mask.getWidth() + " x " + mask.getHeight());
        int x = 0;
        int y = 0;
        for(String row : lines) {
            if(row.length() > 1) {
                String[] cols = row.split(",");
                //System.out.println("AsyncTask -------------------------- cols.length: " +cols.length);
                y = 0;
                for(String col : cols) {
                    if(col.length() > 0) {
                        if (col.equals("0")) {
                            mask.setPixel(y, x, Color.TRANSPARENT);
                            y++;
                        } else if(col.equals("1")) {
                            mask.setPixel(y, x, Color.WHITE);
                            y++;
                        }
                    }
                }
                x++;
            }
        }
        System.out.println("AsyncTask -------------------------- X,Y: " + x + ", " + y);

        return mask;
    }
}
