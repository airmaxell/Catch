package com.example.catchdemo.gallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.AppCompatImageView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.catchdemo.CameraActivity;
import com.example.catchdemo.R;
import com.example.catchdemo.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExteriorFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ExteriorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExteriorFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private LinearLayout layoutImages;
    private AppCompatImageView selectedImage;
    private List<File> exteriorImagesList;

    public ExteriorFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ExteriorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExteriorFragment newInstance(String param1, String param2) {
        ExteriorFragment fragment = new ExteriorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exterior, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutImages = (LinearLayout) view.findViewById(R.id.exterior_images);
        selectedImage = (AppCompatImageView) view.findViewById(R.id.exterior_image_selected);

        CameraActivity cameraActivity = (CameraActivity)getActivity();
        exteriorImagesList = Utils.getListFiles(cameraActivity.exteriorFolder);
        for(File image : exteriorImagesList) {
            layoutImages.addView(getViewFromFile(image));
        }
        if(!exteriorImagesList.isEmpty()) {
            selectImageFromPath(exteriorImagesList.get(0).getAbsolutePath());
        }

    }

    private View getViewFromFile(File image) {

        final String path = image.getAbsolutePath();
        Bitmap bm = BitmapFactory.decodeFile(image.getAbsolutePath());

        LinearLayout layout = new LinearLayout(getActivity().getApplicationContext());
        layout.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
        layout.setGravity(Gravity.CENTER);

        ImageView imageView = new ImageView(getActivity().getApplicationContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(180, 180));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(bm);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageFromPath(path);
            }
        });

        layout.addView(imageView);
        return layout;
    }


    private void selectImageFromPath(String path) {
        Bitmap bmp = BitmapFactory.decodeFile(path);
        selectedImage.setImageBitmap(bmp);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}
