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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DaschActivity extends AppCompatActivity {

    private final String TAG = "DaschActivity";
    private ArrayList<CarItem> requests;
    private ArrayAdapter<CarItem> mArrayAdapter;

    private File picturesFolder;
    private File exteriorFolder;
    private File interiorFolder;
    private File detailsFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dasch);

        picturesFolder = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        exteriorFolder = new File(picturesFolder  + File.separator + "exterior");
        interiorFolder = new File(picturesFolder  + File.separator + "interior");
        detailsFolder = new File(picturesFolder  + File.separator + "details");
        if(!exteriorFolder.exists()) exteriorFolder.mkdirs();
        if(!interiorFolder.exists()) interiorFolder.mkdirs();
        if(!detailsFolder.exists()) detailsFolder.mkdirs();

        requests = new ArrayList<>();

        requests.add(new CarItem("BMW x5 fndas jksnf", "KJFJNS83JNFD83JD"));
        requests.add(new CarItem("BMW 530i jdsf ank", "KF93MFJ38FJ389FJ"));
        requests.add(new CarItem("BMW 530i jdsf ank", "123"));
        requests.add(new CarItem("BMW 530i jdsf ank", "test_milos"));

        GetRequests getRequestsAsyncTask = new GetRequests();
        getRequestsAsyncTask.execute((Void)null);

        mArrayAdapter = new CarItemArrayAdapter(this, requests);


        final ListView listview = findViewById(R.id.listview);
        listview.setAdapter(mArrayAdapter);

        listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "--------------------- Clicked on item: " + position);
            }
        });


    }


    public void startNewSession(String vin){

        // delete previous files
        if (exteriorFolder.isDirectory() && interiorFolder.isDirectory() && detailsFolder.isDirectory())
        {
            String[] children = exteriorFolder.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(exteriorFolder, children[i]).delete();
            }
            children = interiorFolder.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(interiorFolder, children[i]).delete();
            }
            children = detailsFolder.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(detailsFolder, children[i]).delete();
            }
        }
    }

    private class GetRequests extends AsyncTask<Void, Void, Void> {

        public GetRequests() {}

        @Override
        protected Void doInBackground(Void... voids) {
            JSONArray response = null;
            String link = getResources().getString(R.string.api_server) + "/get_all_requests";
            try {
                // Simulate network access.
                response = new JSONArray(Utils.getResponseText(link));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i("DaschActivity", "--------------- link: " + link);
            Log.i("DaschActivity", "--------------- response: " + response.toString());
            Log.i("DaschActivity", "--------------- response: " + response.length());

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private class CarItemArrayAdapter extends ArrayAdapter<CarItem> {

        private Context mContext;
        private List<CarItem> requestsList;
        private final String TAG = "CarItemArrayAdapter";

        public CarItemArrayAdapter(@NonNull Context context,
                ArrayList<CarItem> objects) {
            super(context,R.layout.list_view_item, objects);

            this.mContext = context;
            this.requestsList = objects;
        }


        @NonNull
        public View getView(final int position, View convertView, @NonNull final ViewGroup parent) {

            final int pos = position;
            final CarItem carItem = requestsList.get(position);

            //LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View listItem = convertView;
            if(listItem == null) {
                listItem = LayoutInflater.from(mContext).inflate(R.layout.list_view_item,parent,false);
            }

            listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "--------------------- Clicked on item: " + carItem.getVin());

                    // Animation
                    final AppCompatButton accept = v.findViewById(R.id.button_accept);
                    final AppCompatButton decline = v.findViewById(R.id.button_decline);

                    accept.setVisibility(View.VISIBLE);
                    decline.setVisibility(View.VISIBLE);

                    accept.animate()
                            .scaleY(1.0f)
                            .setDuration(2000)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    accept.setVisibility(View.GONE);
                                    accept.setScaleY(1.0f);
                                }
                            });
                    decline.animate()
                            .scaleY(1.0f)
                            .setDuration(2000)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    decline.setVisibility(View.GONE);
                                    decline.setScaleY(1.0f);
                                }
                            });

                    accept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i(TAG, "--------- Clicked on Accept - " + carItem.getVin());

                            startNewSession(carItem.getVin());

                            Intent cameraActivity = new Intent(DaschActivity.this, CameraActivity.class);
                            cameraActivity.putExtra(getString(R.string.extra_message_vin_session), carItem.getVin());
                            startActivity(cameraActivity);
                        }
                    });
                    decline.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i(TAG, "--------- Clicked on Decline - " + carItem.getVin());
                            //parent.removeViewAt(position);
                            mArrayAdapter.remove(carItem);
                            mArrayAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });

            TextView title = listItem.findViewById(R.id.title);
            TextView vin   = listItem.findViewById(R.id.vin);

            title.setText(carItem.getTitle());
            vin.setText(carItem.getVin());

            return listItem;
        }
    }


}
