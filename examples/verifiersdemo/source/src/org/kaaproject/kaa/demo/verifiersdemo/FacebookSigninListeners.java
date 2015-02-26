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

import android.util.Log;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

public class FacebookSigninListeners implements LoginButton.UserInfoChangedCallback, Session.StatusCallback {
    private LoginsActivity parentActivity;
    private static final String TAG = "MY-Facebook";
    private boolean isClicked;

    public FacebookSigninListeners(LoginsActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    // is called after call()
    @Override
    public void onUserInfoFetched(GraphUser user) {
        if (user != null && isClicked) {
            String accessToken = Session.getActiveSession().getAccessToken();
            String userId = user.getId();
            String userName = user.getFirstName();

            Log.i(TAG, "Token: " +  accessToken);
            Log.i(TAG, "User id: " + userId);
            Log.i(TAG, "User name: " + user.getFirstName());

            parentActivity.updateUI(userName, userId, accessToken, LoginsActivity.AccountType.FACEBOOK);
        }

        isClicked = false;
    }

    @Override
    public void call(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.i(TAG, "Logged in...");
            isClicked = true;
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
        }
    }
}
