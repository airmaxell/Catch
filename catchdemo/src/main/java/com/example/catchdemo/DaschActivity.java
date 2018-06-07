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
import android.animation.ValueAnimator;
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
import android.widget.FrameLayout;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dasch);



        requests = new ArrayList<>();

        requests.add(new CarItem("BMW x5 fndas jksnf", "KJFJNS83JNFD83JD"));
        requests.add(new CarItem("BMW 530i jdsf ank", "KF93MFJ38FJ389FJ"));
        requests.add(new CarItem("BMW 530i jdsf ank", "PR98EHV02M67DGR6"));
        requests.add(new CarItem("BMW 530i jdsf ank", "OFH47SRAU47WTV75"));

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

        Intent cameraActivity = new Intent(DaschActivity.this, CameraActivity.class);
        cameraActivity.putExtra(getString(R.string.extra_message_vin_session), vin);
        startActivity(cameraActivity);
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

                    // AnimationUtilities
                    final AppCompatButton accept = v.findViewById(R.id.button_accept);
                    final AppCompatButton decline = v.findViewById(R.id.button_decline);

                    accept.setVisibility(View.VISIBLE);
                    decline.setVisibility(View.VISIBLE);

                    getToggleAnimation(accept, 0, v.getHeight(), 500, 0).start();
                    getToggleAnimation(decline, 0, v.getHeight(), 500, 0).start();

                    getToggleAnimation(accept, v.getHeight(), 0, 500, 2500).start();
                    getToggleAnimation(decline, v.getHeight(), 0, 500, 2500).start();



                    accept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i(TAG, "--------- Clicked on Accept - " + carItem.getVin());

                            startNewSession(carItem.getVin());

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



        private ValueAnimator getToggleAnimation(final AppCompatButton view , int startHeight , int endHeight, int duration, int startDelay) {
            ValueAnimator animatorHeight = ValueAnimator.ofInt(startHeight,endHeight);
            animatorHeight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    view.getLayoutParams().height = (int)animation.getAnimatedValue();
                    view.requestLayout();
                }
            });
            //A duration for the whole animation, this can easily become a function parameter if needed.
            animatorHeight.setDuration(duration);
            animatorHeight.setStartDelay(startDelay);
            return animatorHeight;
        }
    }


}
