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

package org.kaaproject.kaa.server.operations.service.profile;

import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EventClassFamilyVersionStateDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.operations.pojo.RegisterProfileRequest;
import org.kaaproject.kaa.server.operations.pojo.UpdateProfileRequest;
import org.kaaproject.kaa.server.operations.service.cache.AppSeqNumber;
import org.kaaproject.kaa.server.operations.service.cache.AppVersionKey;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.cache.EventClassFamilyIdKey;
import org.kaaproject.kaa.server.sync.EndpointVersionInfo;
import org.kaaproject.kaa.server.sync.EventClassFamilyVersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Class DefaultProfileService is a default implementation of
 * {@link ProfileService ProfileService}.
 *
 * @author ashvayka
 */
public class DefaultProfileService implements ProfileService {

    /** The LOG constant. */
    private static final Logger LOG = LoggerFactory
            .getLogger(DefaultProfileService.class);

    /** The application service. */
    @Autowired
    private ApplicationService applicationService;

    /** The endpoint service. */
    @Autowired
    private EndpointService endpointService;

    /** The profile service. */
    @Autowired
    private org.kaaproject.kaa.server.common.dao.ProfileService profileService;

    /** The endpoint service. */
    @Autowired
    private CacheService cacheService;


    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.operations.service.profile.ProfileService#getProfile
     * (org.kaaproject.kaa.common.hash.EndpointObjectHash)
     */
    @Override
    public EndpointProfileDto getProfile(EndpointObjectHash endpointKey) {
        return endpointService.findEndpointProfileByKeyHash(endpointKey.getData());
    }

    @Override
    public EndpointProfileDto updateProfile(EndpointProfileDto profile) {
        LOG.debug("Updating profile {} ", profile);
        return endpointService.saveEndpointProfile(profile);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.profile.ProfileService#
     * registerProfile
     * (org.kaaproject.kaa.server.operations.pojo.RegisterProfileRequest)
     */
    @Override
    public EndpointProfileDto registerProfile(RegisterProfileRequest request) {
        LOG.debug("Registering Profile for {}", request.getEndpointKey());
        LOG.trace("Lookup application by token: {}", request.getAppToken());

        AppSeqNumber appSeqNumber = cacheService.getAppSeqNumber(request.getAppToken());
        LOG.trace("Application by token: {} found: {}", request.getAppToken(), appSeqNumber);

        String profileJson = decodeProfile(request.getProfile(), appSeqNumber.getAppToken(), request.getVersionInfo().getProfileVersion());

        EndpointProfileDto dto = new EndpointProfileDto();
        dto.setApplicationId(appSeqNumber.getAppId());
        dto.setEndpointKey(request.getEndpointKey());
        dto.setEndpointKeyHash(EndpointObjectHash.fromSHA1(request.getEndpointKey()).getData());
        dto.setProfile(profileJson);
        dto.setProfileHash(EndpointObjectHash.fromSHA1(request.getProfile()).getData());

        populateVersionStates(appSeqNumber.getTenantId(), dto, request.getVersionInfo());

        if(request.getAccessToken() != null){
            dto.setAccessToken(request.getAccessToken());
        }

        dto.setCfSequenceNumber(0);
        dto.setNfSequenceNumber(0);
        dto.setChangedFlag(Boolean.FALSE);

        return endpointService.saveEndpointProfile(dto);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.operations.service.profile.ProfileService#
     * updateProfile
     * (org.kaaproject.kaa.server.operations.pojo.UpdateProfileRequest)
     */
    @Override
    public EndpointProfileDto updateProfile(UpdateProfileRequest request) {
        LOG.debug("Updating Profile for {}", request.getEndpointKeyHash());

        EndpointProfileDto dto = endpointService.findEndpointProfileByKeyHash(request.getEndpointKeyHash().getData());
        
        AppSeqNumber appSeqNumber = cacheService.getAppSeqNumber(request.getApplicationToken());

        String profileJson = decodeProfile(request.getProfile(), appSeqNumber.getAppToken(), request.getVersionInfo().getProfileVersion());

        if(request.getAccessToken() != null){
            dto.setAccessToken(request.getAccessToken());
        }
        dto.setProfile(profileJson);
        dto.setProfileHash(EndpointObjectHash.fromSHA1(request.getProfile()).getData());

        populateVersionStates(appSeqNumber.getTenantId(), dto, request.getVersionInfo());

        List<EndpointGroupStateDto> egsList = new ArrayList<>();
        dto.setCfGroupStates(egsList);
        dto.setCfSequenceNumber(0);
        dto.setNfGroupStates(egsList);
        dto.setNfSequenceNumber(0);
        return endpointService.saveEndpointProfile(dto);
    }

    protected void populateVersionStates(String tenantId, EndpointProfileDto dto, EndpointVersionInfo evInfo) {
        dto.setProfileVersion(evInfo.getProfileVersion());
        dto.setConfigurationVersion(evInfo.getConfigVersion());
        dto.setSystemNfVersion(evInfo.getSystemNfVersion());
        dto.setUserNfVersion(evInfo.getUserNfVersion());
        dto.setLogSchemaVersion(evInfo.getLogSchemaVersion());
        if(evInfo.getEventFamilyVersions() != null){
            List<EventClassFamilyVersionStateDto> ecfVersionStates = new ArrayList<>(evInfo.getEventFamilyVersions().size());
            for(EventClassFamilyVersionInfo ecfVersionInfo : evInfo.getEventFamilyVersions()){
                EventClassFamilyVersionStateDto ecfVersionDto = new EventClassFamilyVersionStateDto();
                String ecfId = cacheService.getEventClassFamilyIdByName(new EventClassFamilyIdKey(tenantId, ecfVersionInfo.getName()));
                if(ecfId != null){
                    ecfVersionDto.setEcfId(ecfId);
                    ecfVersionDto.setVersion(ecfVersionInfo.getVersion());
                    ecfVersionStates.add(ecfVersionDto);
                }else{
                    LOG.warn("Failed to add ecf version state for ecf name {} and version {}", ecfVersionInfo.getName(), ecfVersionInfo.getVersion());
                }
            }
            dto.setEcfVersionStates(ecfVersionStates);
        }
    }

    /**
     * Decode profile.
     *
     * @param profileRaw
     *            the profile raw
     * @param appId
     *            the app id
     * @param schemaVersion
     *            the schema version
     * @return the string
     */
    private String decodeProfile(byte[] profileRaw, String appToken, int schemaVersion) {
        LOG.trace("Lookup profileSchema by appToken: {} and version: {}", appToken, schemaVersion);

        ProfileSchemaDto profileSchemaDto = cacheService.getProfileSchemaByAppAndVersion(new AppVersionKey(appToken, schemaVersion));
        String profileSchema = profileSchemaDto.getSchema();

        LOG.trace("ProfileSchema by appToken: {} and version: {} found: {}", appToken, schemaVersion, profileSchema);

        String profileJson = GenericAvroConverter.toJson(profileRaw, profileSchema);
        LOG.trace("Profile json : {} ", profileJson);

        return profileJson;
    }
}
