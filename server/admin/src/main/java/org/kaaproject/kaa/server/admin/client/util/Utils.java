/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.server.admin.client.util;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.kaaproject.avro.ui.gwt.client.AvroUiResources;
import org.kaaproject.avro.ui.gwt.client.AvroUiResources.AvroUiStyle;
import org.kaaproject.avro.ui.gwt.client.widget.AlertPanel;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.server.admin.client.KaaAdminResources;
import org.kaaproject.kaa.server.admin.client.KaaAdminResources.KaaAdminStyle;
import org.kaaproject.kaa.server.admin.client.i18n.KaaAdminConstants;
import org.kaaproject.kaa.server.admin.client.i18n.KaaAdminMessages;
import org.kaaproject.kaa.server.admin.client.mvp.view.dialog.MessageDialog;
import org.kaaproject.kaa.server.admin.client.mvp.view.dialog.UnauthorizedSessionDialog;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.StatusCodeException;

public class Utils {

    public static final KaaAdminResources resources = GWT
            .create(KaaAdminResources.class);

    public static final KaaAdminConstants constants = GWT
            .create(KaaAdminConstants.class);

    public static final KaaAdminMessages messages = GWT
            .create(KaaAdminMessages.class);
    
    public static final AvroUiResources avroUiResources = 
            GWT.create(AvroUiResources.class);
    
    public static final KaaAdminStyle kaaAdminStyle = 
            resources.kaaAdminStyle();
    
    public static final AvroUiStyle avroUiStyle =
            avroUiResources.avroUiStyle();
    
    private static final DateTimeFormat simpleDateFormat = DateTimeFormat
            .getFormat("MM/dd/yyyy");
    
    private static final DateTimeFormat simpleDateTimeFormat = DateTimeFormat
            .getFormat("MM/dd/yyyy h:mm a");

    private static final int INCORRECT_IDX = -1;

    private static UnauthorizedSessionDialog unauthorizedSessionDialog;
    
    public static void injectKaaStyles() {
        kaaAdminStyle.ensureInjected();
        avroUiStyle.ensureInjected();
    }

    public static void handleException(Throwable caught,
            HasErrorMessage hasErrorMessage) {
        handleException(caught, hasErrorMessage, null);
    }

    public static void handleException(Throwable caught,
            HasErrorMessage hasErrorMessage,
            ErrorMessageCustomizer errorMessageCustomizer) {
        boolean handled = false;
        if (caught instanceof StatusCodeException) {
            StatusCodeException sce = (StatusCodeException) caught;
            if (sce.getStatusCode() == Response.SC_UNAUTHORIZED) {
                onUnauthorized();
                handled = true;
            } else if (sce.getStatusCode() == 0) {
                handleNetworkConnectionError();
                handled = true;
            }
        } else if (caught instanceof IncompatibleRemoteServiceException) {
            MessageDialog.showMessageDialog(AlertPanel.Type.ERROR, constants.incompatibleRemoteService(), messages.incompatibleRemoteService());
            handled = true;
        } 
        if (!handled) {
            String message = parseErrorMessage(caught, errorMessageCustomizer);
            hasErrorMessage.setErrorMessage(message);
        }
    }
    
    public static void handleNetworkConnectionError() {
        MessageDialog.showMessageDialog(AlertPanel.Type.ERROR, constants.serverIsUnreachable(), messages.serverIsUnreacheableMessage());
    }
 
    public static String parseErrorMessage(Throwable caught) {
        return parseErrorMessage(caught, null);
    }

    private static String parseErrorMessage(Throwable caught,
            ErrorMessageCustomizer errorMessageCustomizer) {
        if (caught instanceof KaaAdminServiceException) {
            ServiceErrorCode errorCode = ((KaaAdminServiceException) caught)
                    .getErrorCode();
            String message = constants.getString(errorCode.getResKey());
            if (errorCode.showErrorMessage()) {
                message += caught.getLocalizedMessage();
            }
            return message;
        } else if (errorMessageCustomizer != null) {
            return errorMessageCustomizer.customizeErrorMessage(caught);
        } else {
            return caught.getLocalizedMessage();
        }
    }

    private static void onUnauthorized() {
        if (unauthorizedSessionDialog == null
                || !unauthorizedSessionDialog.isShowing()) {
            unauthorizedSessionDialog = new UnauthorizedSessionDialog(
                    new UnauthorizedSessionDialog.Listener() {
                        @Override
                        public void onLogin() {
                            Window.open(Window.Location.getPath(), "_blank", "");
                        }

                        @Override
                        public void onIgnore() {
                            // do nothing
                        }
                    });
            unauthorizedSessionDialog.center();
            unauthorizedSessionDialog.show();
        }
    }

    public static String millisecondsToDateString(long millis) {
        return simpleDateFormat.format(new Date(millis));
    }

    public static String millisecondsToDateTimeString(long millis) {
        return simpleDateTimeFormat.format(new Date(millis));
    }

    public static boolean validateEmail(String mail) {
        boolean result = false;
        if (mail != null && mail.length() != 0) {
            if (mail.indexOf('@') != INCORRECT_IDX
                    && mail.indexOf('.') != INCORRECT_IDX) {
                result = true;
            }
        }
        return result;
    }

    public static SchemaDto getMaxSchemaVersions(List<SchemaDto> schemas) {
        SchemaDto maxLogSchema = null;
        if (schemas != null && !schemas.isEmpty()) {
            Collections.sort(schemas, Collections.reverseOrder());
            maxLogSchema = schemas.get(0);
        }
        return maxLogSchema;
    }

    public static boolean isNotBlank(String string) {
        return string != null && string.length() > 0;
    }

    public static boolean isBlank(String string) {
        return string == null || string.length() == 0;
    }
}
