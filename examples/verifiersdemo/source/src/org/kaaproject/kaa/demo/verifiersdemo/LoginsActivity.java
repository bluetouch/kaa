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
import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

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
	
    private static final String TAG = "MY";
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
    private ImageButton googleButton;
    private LoginButton facebookButton;
    private TwitterLoginButton twitterButton;
    
    // classes, handling each button's specific actions
    private GplusSigninListeners gplusSigninListeners;
    private FacebookSigninListeners facebookSigninListeners;
    private TwitterSigninListeners twitterSigninListeners;
    
    private GoogleApiClient mGoogleApiClient;
    
    private UiLifecycleHelper uiHelper;

    private EndpointRegistrationManager endpointRegistrationManager;

    public enum AccountType {GOOGLE, FACEBOOK, TWITTER};
    private final String GOOGLE_VERIFIER_ID = "73378476517438627673";
    private final String FACEBOOK_VERIFIER_ID = "12299156890256483944";
    private final String TWITTER_VERIFIER_ID = "19302337015611025768";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logins);
        greetingTextView = (TextView) findViewById(R.id.greeting);
        idTextView = (TextView) findViewById(R.id.idText);
        infoTextView = (TextView) findViewById(R.id.infoText);
        
        if (savedInstanceState != null) {
	       	curUserName = savedInstanceState.getCharSequence(USER_NAME);
	        curUserId = savedInstanceState.getCharSequence(USER_ID);
	        curUserInfo = savedInstanceState.getCharSequence(USER_INFO);
	        updateTextViews();
        }
        
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        twitterButton = (TwitterLoginButton) findViewById(R.id.twitter_sign_in_button);
        twitterButton.setEnabled(true);
        twitterSigninListeners = new TwitterSigninListeners(this);
        twitterButton.setCallback(twitterSigninListeners);
        twitterButton.setOnClickListener(twitterSigninListeners);
        
        // create listeners class for google+
        gplusSigninListeners = new GplusSigninListeners(this);
        
        googleButton = (ImageButton) findViewById(R.id.gplus_sign_in_button);
        googleButton.setOnClickListener(gplusSigninListeners);
        
        // Google API client, which is capable of making requests for tokens, etc.
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
        
        Log.i(TAG, "5");
        
        KaaClientPlatformContext platformContext = new AndroidKaaPlatformContext(this);
    	KaaClient kaaClient = Kaa.newClient(platformContext);
    	Log.i(TAG, "6");
    	kaaClient.start();
    	Log.i(TAG, "7");
    	
    	endpointRegistrationManager = kaaClient.getEndpointRegistrationManager();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
        String verifierId = null;
        switch (type) {
            case GOOGLE:
                verifierId = GOOGLE_VERIFIER_ID;
                // log out from Facebook
                Session.getActiveSession().closeAndClearTokenInformation();
                userName = "Hi to " + userName + " from Google";
                break;
            case FACEBOOK:
                verifierId = FACEBOOK_VERIFIER_ID;
                userName = "Hi to " + userName + " from Facebook";
                break;
            case TWITTER:
                verifierId = TWITTER_VERIFIER_ID;
                // log out from Facebook
                Session.getActiveSession().closeAndClearTokenInformation();
                userName = "Hi to " + userName + " from Twitter";
                break;
            default:
                break;
        }
        
        curUserName = userName;
        curUserId = userId;
        
        Log.i(TAG, "Attaching user...");
        endpointRegistrationManager.attachUser(verifierId, userId, token,
                new UserAttachCallback() {
            @Override
            public void onAttachResult(UserAttachResponse userAttachResponse) {
            	Log.i(TAG, userAttachResponse.toString());
            	
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
        Log.i(TAG, "User was attached...");
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
