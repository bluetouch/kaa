/*
 * Copyright 2015 CyberVision, Inc.
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

package org.kaaproject.kaa.demo.verifiersdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import org.kaaproject.kaa.client.AndroidKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.KaaClientPlatformContext;
import org.kaaproject.kaa.client.event.registration.EndpointRegistrationManager;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.gen.UserAttachResponse;

import io.fabric.sdk.android.Fabric;

public class LoginsActivity extends FragmentActivity {
    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "01Y9gbsMeGPetye1w9kkNvNMi";
    private static final String TWITTER_SECRET = "g4Pwh51o7SQlhd3RL6inNF3VxixBURAJDZc494uSISF7yOyJjc";

    private static final String TAG = "Example-LoginsActivity";
    private static final String USER_NAME = "userName";
    private static final String USER_ID = "userId";
    private static final String USER_INFO = "userInfo";

    // these values are used to describe current connection status on UI
    private CharSequence curUserName;
    private CharSequence curUserId;
    private CharSequence curUserInfo;

    // text views, where connection status is shown
    private TextView greetingTextView;
    private TextView idTextView;
    private TextView infoTextView;

    // buttons used to connect to corresponding social networks
    private SignInButton googleButton;
    private LoginButton facebookButton;
    private TwitterLoginButton twitterButton;

    // classes, handling each button's specific actions
    private GplusSigninListeners gplusSigninListeners;
    private FacebookSigninListeners facebookSigninListeners;
    private TwitterSigninListeners twitterSigninListeners;

    // Google API client, which is used to establish connection with Google
    // and access its API
    private GoogleApiClient mGoogleApiClient;

    // Facebook UI helper class, used for managing login UI
    private UiLifecycleHelper uiHelper;

    // Kaa endpoint registration manager, responsible for attaching users to
    // endpoints
    private EndpointRegistrationManager endpointRegistrationManager;

    public enum AccountType {GOOGLE, FACEBOOK, TWITTER};

    // Configuration, which consists of three default Kaa verifiers tokens:
    // for Google, Facebook and Twitter (Kaa Configuration is used)
    private KaaVerifiersTokens verifiersTokens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logins);
        greetingTextView = (TextView) findViewById(R.id.greeting);
        idTextView = (TextView) findViewById(R.id.idText);
        infoTextView = (TextView) findViewById(R.id.infoText);

        // Resume saved state (i.e. after screen rotation), if any
        if (savedInstanceState != null) {
            curUserName = savedInstanceState.getCharSequence(USER_NAME);
            curUserId = savedInstanceState.getCharSequence(USER_ID);
            curUserInfo = savedInstanceState.getCharSequence(USER_INFO);
            updateTextViews();
        }

        // Twitter authConfig for Twitter credentials verification
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);

        // Register application with the help of Fabric plug-in for managing Twitter 
        // apps (and library dependencies) register application
        Fabric.with(this, new Twitter(authConfig));

        // Create Twitter button
        twitterButton = (TwitterLoginButton) findViewById(R.id.twitter_sign_in_button);

        // Enable button, even if a user is signed-in
        twitterButton.setEnabled(true);
        twitterSigninListeners = new TwitterSigninListeners(this);

        // Attach listeners needed to keep track of connection
        twitterButton.setCallback(twitterSigninListeners);
        twitterButton.setOnClickListener(twitterSigninListeners);

        // create listeners class for Google+
        gplusSigninListeners = new GplusSigninListeners(this);

        googleButton = (SignInButton) findViewById(R.id.gplus_sign_in_button);
        googleButton.setSize(SignInButton.SIZE_WIDE);
        googleButton.setOnClickListener(gplusSigninListeners);

        // Google API client, which is capable of making requests for tokens, user info etc.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(gplusSigninListeners)
                .addOnConnectionFailedListener(gplusSigninListeners)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
        gplusSigninListeners.setClient(mGoogleApiClient);

        // create listeners class for Facebook
        facebookSigninListeners = new FacebookSigninListeners(this);
        facebookButton = (LoginButton) findViewById(R.id.facebook_sign_in_button);

        facebookButton.setUserInfoChangedCallback(facebookSigninListeners);
        // UI helper is used for
        uiHelper = new UiLifecycleHelper(this, facebookSigninListeners);
        uiHelper.onCreate(savedInstanceState);

        KaaClientPlatformContext platformContext = new AndroidKaaPlatformContext(this);
        KaaClient kaaClient = Kaa.newClient(platformContext);
        kaaClient.start();

        verifiersTokens = kaaClient.getConfiguration();
        Log.i(TAG, "Verifiers tokens: " + verifiersTokens.toString());

        endpointRegistrationManager = kaaClient.getEndpointRegistrationManager();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // call corresponding onActivityResult methods for Facebook, Twitter and Google
        // respectively
        uiHelper.onActivityResult(requestCode, resultCode, data);
        twitterButton.onActivityResult(requestCode, resultCode, data);
        gplusSigninListeners.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current state of the UI (for Facebook)
        uiHelper.onSaveInstanceState(outState);
        outState.putCharSequence(USER_NAME, greetingTextView.getText());
        outState.putCharSequence(USER_ID, idTextView.getText());
        outState.putCharSequence(USER_INFO, infoTextView.getText());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        gplusSigninListeners.onStop();
    }

    public void updateUI(String userName, String userId, String token, AccountType type) {
        String kaaVerifierToken = null;
        switch (type) {
            case GOOGLE:
                kaaVerifierToken = verifiersTokens.getGoogleKaaVerifierToken();
                // Log out from Facebook (to make Log out button disappear)
                Session.getActiveSession().closeAndClearTokenInformation();
                userName = "Hi to " + userName + " from Google";
                break;
            case FACEBOOK:
                kaaVerifierToken = verifiersTokens.getFacebookKaaVerifierToken();
                userName = "Hi to " + userName + " from Facebook";
                break;
            case TWITTER:
                kaaVerifierToken = verifiersTokens.getTwitterKaaVerifierToken();
                // Log out from Facebook (to make Log out button disappear)
                Session.getActiveSession().closeAndClearTokenInformation();
                userName = "Hi to " + userName + " from Twitter";
                break;
            default:
                break;
        }

        // Update  userName and userId shown on UI
        curUserName = userName;
        curUserId = userId;
        curUserInfo = "Waiting for Kaa response...";
        updateTextViews();

        Log.i(TAG, "Attaching user...");
        endpointRegistrationManager.attachUser(kaaVerifierToken, userId, token,
                new UserAttachCallback() {
                    @Override
                    public void onAttachResult(UserAttachResponse userAttachResponse) {
                        Log.i(TAG, "User was attached... " + userAttachResponse.toString());

                        if (userAttachResponse.getResult() == SyncResponseResultType.SUCCESS) {
                            Log.i(TAG, "Successful Kaa verification");
                            Log.i(TAG, userAttachResponse.toString());
                            curUserInfo = "Successful Kaa verification";
                            updateTextViews();
                        } else {
                            Log.i(TAG, "Kaa verification failure: " + userAttachResponse.getErrorCode());
                            curUserInfo = "Kaa verification failure: " + userAttachResponse.getErrorCode();
                            updateTextViews();
                        }
                    }
                });
    }

    private void updateTextViews() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                greetingTextView.setText(LoginsActivity.this.curUserName);
                idTextView.setText(LoginsActivity.this.curUserId);
                infoTextView.setText(LoginsActivity.this.curUserInfo);
            }
        });
    }
}
