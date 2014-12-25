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

package org.kaaproject.kaa.server.admin.client.mvp.view;

import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.server.admin.shared.logs.LogAppenderInfoDto;
import org.kaaproject.kaa.server.common.avro.ui.shared.RecordField;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ValueListBox;

public interface LogAppenderView extends BaseDetailsView {

    ValueListBox<Integer> getMinSchemaVersion();

    ValueListBox<Integer> getMaxSchemaVersion();
    
    HasValue<Boolean> getConfirmDelivery();
    
    HasValue<String> getName();

    ValueListBox<LogAppenderInfoDto> getAppenderInfo();

    HasValue<String> getDescription();

    HasValue<String> getCreatedDateTime();

    HasValue<String> getCreatedUsername();

    void setMetadataListBox(List<LogHeaderStructureDto> header);

    List<LogHeaderStructureDto> getHeader();

    HasValue<RecordField> getConfiguration();

    void setSchemaVersions(List<Integer> schemaVersions);

}
