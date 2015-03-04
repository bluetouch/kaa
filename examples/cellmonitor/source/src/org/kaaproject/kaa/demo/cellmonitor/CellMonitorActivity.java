/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.demo.cellmonitor;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class CellMonitorActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cell_monitor);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new CellMonitorFragment()).commit();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();

        /*
         * Notify application about background state.
         */

        getCellMonitorApplication().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
         * Notify application about foreground state.
         */

        getCellMonitorApplication().resume();
    }
    
    public CellMonitorApplication getCellMonitorApplication() {
        return (CellMonitorApplication) getApplication();
    }
}
