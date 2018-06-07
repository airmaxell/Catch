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
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.catchdemo.Utilities.AnimationUtilities;
import com.example.catchdemo.Utilities.CustomEditText;
import com.example.catchdemo.Utilities.DrawableClickListener;
import com.example.catchdemo.Utilities.HorizontalDottedProgress;

import java.io.IOException;

import static com.example.catchdemo.Utilities.AnimationUtilities.getAlphaAnimation;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity  {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    private SharedPreferences sharedPreferences;

    // UI references.
    private EditText mEmailView;
    private CustomEditText mPasswordView;
    private HorizontalDottedProgress mProgressView;
    private View mLoginFormView;
    private View mRegisterView;
    private Activity mActivity;

    private AppCompatImageButton mEmailSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mActivity = getActivity();
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);

        mPasswordView = (CustomEditText) findViewById(R.id.password);
        mPasswordView.setTransformationMethod(new AsteriskPasswordTransformationMethod());
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mPasswordView.setDrawableClickListener(new DrawableClickListener() {
            @Override
            public void onClick(DrawablePosition target) {
                switch (target) {
                    case RIGHT:
                        makeToast("Forgot Password");
                        break;
                    default:
                        break;
                }
            }
        });

        mEmailSignInButton = (AppCompatImageButton) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        sharedPreferences = getSharedPreferences(getString(R.string.shared_preference_user_info), Context.MODE_PRIVATE);
        mLoginFormView = findViewById(R.id.login_form);
        mRegisterView = findViewById(R.id.layout_register);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserExists();
    }

    private void checkUserExists() {

        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                showLogin();
            }
        }, 3000);


        /*
        boolean user = sharedPreferences.getBoolean(getString(R.string.key_bool_user_logged_in), false);
        if(user) {
            int user_id = sharedPreferences.getInt(getString(R.string.key_int_user_id), 0);
        } else {
            showLogin();
        }
        */
    }

    private void showLogin() {

        final LinearLayout login = findViewById(R.id.login_form);

        AppCompatImageView logo = findViewById(R.id.logo);

        final float scale = getResources().getDisplayMetrics().density;
        int dpWidthInPx  = (int) (160 * scale);
        int dpHeightInPx = (int) (168 * scale);
        int dpMarginTopInPx = (int) (47 * scale);

        AnimationUtilities.getHeightAnimation(logo, logo.getLayoutParams().height, dpHeightInPx, 500, 400).start();
        AnimationUtilities.getWidthAnimation(logo, logo.getLayoutParams().width, dpWidthInPx, 500, 400).start();
        AnimationUtilities.getMarginTopAnimation(logo, ((ViewGroup.MarginLayoutParams)logo.getLayoutParams()).topMargin, dpMarginTopInPx, 500, 400).start();

        final AppCompatImageView textCatch = findViewById(R.id.text_catch);
        getAlphaAnimation(textCatch, 0.0f, 100, 0).start();

        final com.example.catchdemo.Utilities.HorizontalDottedProgress loadings = findViewById(R.id.loading);
        ValueAnimator va = getAlphaAnimation(loadings, 0.0f, 100, 0);

        va.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                textCatch.setVisibility(View.GONE);
                loadings.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        va.start();

        AnimationUtilities.getScaleXAnimation(login, 1000.0f, 0.0f, 500, 1000).start();

        AnimationUtilities.getScaleYAnimation(mEmailSignInButton, 600.0f, 0.0f, 500, 1000).start();


        LinearLayout layoutRegister = findViewById(R.id.layout_register);

        AnimationUtilities.getScaleYAnimation(layoutRegister, 600.0f, 0.0f, 600, 1200).start();



    }

    public static void hideSoftKeyboard(Activity activity) {

        InputMethodManager inputMethodManager = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }



    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        hideSoftKeyboard(this);

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 3;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });

            mRegisterView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mRegisterView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });

            mEmailSignInButton.setVisibility(show ? View.GONE : View.VISIBLE);
            mEmailSignInButton.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mEmailSignInButton.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });


            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
            });

    }

    protected void storeId(String id) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_user_id), id);
        editor.apply();
    }

    public Activity getActivity() {
        return mActivity;
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;
        public String response = "";

        UserLoginTask(String email, String password) {
            mUsername = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String link = "http://api.catch-ai.com/signin?username=" + mUsername + "&password=" + mPassword;
            try {
                // Simulate network access.
                response = Utils.getResponseText(link);
            } catch (IOException e) {
                e.printStackTrace();
                makeToast(getString(R.string.toast_error_connection));
            }

            Log.i("LoginActivity", "--------------- link: " + link);
            Log.i("LoginActivity", "--------------- response: " + response);

            return Utils.isNumeric(response);

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                //finish();
                Intent daschActivity = new Intent(LoginActivity.this, DaschActivity.class);
                daschActivity.putExtra(getString(R.string.extra_message_user_id), response);
                startActivity(daschActivity);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);

            makeToast(getString(R.string.toast_error_connection));
        }


    }
    public void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public class AsteriskPasswordTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(source);
        }

        private class PasswordCharSequence implements CharSequence {
            private CharSequence mSource;
            public PasswordCharSequence(CharSequence source) {
                mSource = source; // Store char sequence
            }
            public char charAt(int index) {
                return '*'; // This is the important part
            }
            public int length() {
                return mSource.length(); // Return default
            }
            public CharSequence subSequence(int start, int end) {
                return mSource.subSequence(start, end); // Return default
            }
        }
    };


}

