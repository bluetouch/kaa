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
package org.kaaproject.kaa.server.operations.service.akka.actors.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.PingResponse;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.Base64Util;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEvent;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.operations.pojo.SyncResponseHolder;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.ChannelMap.ChannelMetaData;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointStopMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.SyncRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogDeliveryMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogEventPackMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.notification.ThriftNotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.ActorTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.ChannelTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.RequestTimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.TimeoutMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.topic.NotificationMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.topic.TopicUnsubscriptionMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.topic.TopicSubscriptionMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventDeliveryMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventDeliveryMessage.EventDeliveryStatus;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventReceiveMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointEventSendMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserActionMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserAttachMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserDetachMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserDisconnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.verification.UserVerificationRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.verification.UserVerificationResponseMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.response.NettySessionResponseMessage;
import org.kaaproject.kaa.server.operations.service.akka.utils.EntityConvertUtils;
import org.kaaproject.kaa.server.operations.service.event.EventClassFamilyVersion;
import org.kaaproject.kaa.server.sync.ClientSync;
import org.kaaproject.kaa.server.sync.ConfigurationClientSync;
import org.kaaproject.kaa.server.sync.EndpointAttachResponse;
import org.kaaproject.kaa.server.sync.EndpointDetachRequest;
import org.kaaproject.kaa.server.sync.EndpointDetachResponse;
import org.kaaproject.kaa.server.sync.Event;
import org.kaaproject.kaa.server.sync.EventClientSync;
import org.kaaproject.kaa.server.sync.EventSequenceNumberResponse;
import org.kaaproject.kaa.server.sync.EventServerSync;
import org.kaaproject.kaa.server.sync.LogClientSync;
import org.kaaproject.kaa.server.sync.LogEntry;
import org.kaaproject.kaa.server.sync.NotificationClientSync;
import org.kaaproject.kaa.server.sync.ServerSync;
import org.kaaproject.kaa.server.sync.SyncStatus;
import org.kaaproject.kaa.server.sync.UserAttachNotification;
import org.kaaproject.kaa.server.sync.UserAttachRequest;
import org.kaaproject.kaa.server.sync.UserClientSync;
import org.kaaproject.kaa.server.sync.UserDetachNotification;
import org.kaaproject.kaa.server.sync.UserServerSync;
import org.kaaproject.kaa.server.transport.channel.ChannelAware;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.concurrent.duration.Duration;
import akka.actor.ActorContext;
import akka.actor.ActorRef;

public class EndpointActorMessageProcessor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EndpointActorMessageProcessor.class);

    private final EndpointActorState state;

    /** The operations service. */
    private final OperationsService operationsService;

    /** The app token. */
    private final String appToken;

    /** The key. */
    private final EndpointObjectHash key;

    /** The actor key. */
    private final String actorKey;

    /** The actor key. */
    private final String endpointKey;

    private final Map<Integer, LogDeliveryMessage> logUploadResponseMap;

    private final Map<UUID, UserVerificationResponseMessage> userAttachResponseMap;

    private final int inactivityTimeout;

    protected EndpointActorMessageProcessor(AkkaContext context, String appToken, EndpointObjectHash key, String actorKey) {
        super();
        this.operationsService = context.getOperationsService();
        this.inactivityTimeout = context.getInactivityTimeout();
        this.appToken = appToken;
        this.key = key;
        this.actorKey = actorKey;
        this.endpointKey = Base64Util.encode(key.getData());
        this.state = new EndpointActorState(endpointKey, actorKey);
        this.logUploadResponseMap = new HashMap<>();
        this.userAttachResponseMap = new LinkedHashMap<>();
    }

    public void processEndpointSync(ActorContext context, SyncRequestMessage message) {
        sync(context, message);
    }

    public void processEndpointEventReceiveMessage(ActorContext context, EndpointEventReceiveMessage message) {
        EndpointEventDeliveryMessage response;
        List<ChannelMetaData> eventChannels = state.getChannelsByType(TransportType.EVENT);
        if (!eventChannels.isEmpty()) {
            for (ChannelMetaData eventChannel : eventChannels) {
                addEventsAndReply(context, eventChannel, message);
            }
            response = new EndpointEventDeliveryMessage(message, EventDeliveryStatus.SUCCESS);
        } else {
            LOG.debug("[{}] Message ignored due to no channel contexts registered for events", actorKey, message);
            response = new EndpointEventDeliveryMessage(message, EventDeliveryStatus.FAILURE);
            state.setUserRegistrationPending(true);
        }
        tellParent(context, response);
    }

    protected void tellParent(ActorContext context, Object response) {
        context.parent().tell(response, context.self());
    }

    public void processThriftNotification(ActorContext context, ThriftNotificationMessage message) {
        Set<ChannelMetaData> channels = state.getChannelsByTypes(TransportType.CONFIGURATION, TransportType.NOTIFICATION);

        LOG.debug("[{}][{}] Processing thrift norification for {} channels", endpointKey, actorKey, channels.size());

        for (ChannelMetaData channel : channels) {
            ClientSync originalRequest = channel.getRequestMessage().getRequest();
            ServerSync syncResponse = channel.getResponseHolder().getResponse();

            ClientSync newRequest = new ClientSync();
            newRequest.setRequestId(originalRequest.getRequestId());
            newRequest.setClientSyncMetaData(originalRequest.getClientSyncMetaData());
            if (originalRequest.getConfigurationSync() != null) {
                ConfigurationClientSync configurationSyncRequest = originalRequest.getConfigurationSync();
                if (syncResponse.getConfigurationSync() != null) {
                    int newSeqNumber = syncResponse.getConfigurationSync().getAppStateSeqNumber();
                    LOG.debug("[{}][{}] Change original configuration request {} appSeqNumber from {} to {}", endpointKey, actorKey,
                            originalRequest, configurationSyncRequest.getAppStateSeqNumber(), newSeqNumber);
                    configurationSyncRequest.setAppStateSeqNumber(newSeqNumber);
                }
                newRequest.setConfigurationSync(configurationSyncRequest);
                originalRequest.setConfigurationSync(null);
            }
            if (originalRequest.getNotificationSync() != null) {
                NotificationClientSync notificationSyncRequest = originalRequest.getNotificationSync();
                if (syncResponse.getNotificationSync() != null) {
                    int newSeqNumber = syncResponse.getNotificationSync().getAppStateSeqNumber();
                    LOG.debug("[{}][{}] Change original notification request {} appSeqNumber from {} to {}", endpointKey, actorKey,
                            originalRequest, notificationSyncRequest.getAppStateSeqNumber(), newSeqNumber);
                    notificationSyncRequest.setAppStateSeqNumber(newSeqNumber);
                }
                newRequest.setNotificationSync(notificationSyncRequest);
                originalRequest.setNotificationSync(null);
            }
            LOG.debug("[{}][{}] Processing request {}", endpointKey, actorKey, originalRequest);
            sync(context, new SyncRequestMessage(channel.getRequestMessage().getSession(), newRequest, channel.getRequestMessage()
                    .getCommand(), channel.getRequestMessage().getOriginator()));
        }
    }

    public void processNotification(ActorContext context, NotificationMessage message) {
        LOG.debug("[{}][{}] Processing notification message {}", endpointKey, actorKey, message);

        List<ChannelMetaData> channels = state.getChannelsByType(TransportType.NOTIFICATION);
        if(channels.isEmpty()){
            LOG.debug("[{}][{}] No channels to process notification message", endpointKey, actorKey);
            return;
        }
        String unicastNotificationId = message.getUnicastNotificationId();
        List<NotificationDto> validNfs = state.filter(message.getNotifications());
        if(unicastNotificationId == null && validNfs.isEmpty()){
            LOG.debug("[{}][{}] message is no longer valid for current endpoint", endpointKey, actorKey);
            return;
        }
        for (ChannelMetaData channel : channels) {
            LOG.debug("[{}][{}] processing channel {} and response {}", endpointKey, actorKey, channel, channel.getResponseHolder()
                    .getResponse());
            ServerSync syncResponse = operationsService.updateSyncResponse(channel.getResponseHolder().getResponse(),
                    validNfs, unicastNotificationId);
            if (syncResponse != null) {
                LOG.debug("[{}][{}] processed channel {} and response {}", endpointKey, actorKey, channel, syncResponse);
                sendReply(context, channel.getRequestMessage(), syncResponse);
                if (!channel.getType().isAsync()) {
                    state.removeChannel(channel);
                }
            }
        }
    }

    public void processRequestTimeoutMessage(ActorContext context, RequestTimeoutMessage message) {
        ChannelMetaData channel = state.getChannelByRequestId(message.getRequestId());
        if (channel != null) {
            SyncResponseHolder response = channel.getResponseHolder();
            sendReply(context, channel.getRequestMessage(), response.getResponse());
            if (!channel.getType().isAsync()) {
                state.removeChannel(channel);
            }
        } else {
            LOG.debug("[{}][{}] Failed to find request by id [{}].", endpointKey, actorKey, message.getRequestId());
        }
    }

    public void processActorTimeoutMessage(ActorContext context, ActorTimeoutMessage message) {
        if (state.getLastActivityTime() <= message.getLastActivityTime()) {
            LOG.debug("[{}][{}] Request stop of endpoint actor due to inactivity timeout", endpointKey, actorKey);
            tellParent(context, new EndpointStopMessage(key, actorKey, context.self()));
        }
    }

    private void sync(ActorContext context, SyncRequestMessage requestMessage) {
        try {
            state.setLastActivityTime(System.currentTimeMillis());
            long start = state.getLastActivityTime();

            ChannelMetaData channel = initChannel(context, requestMessage);

            ClientSync request = buildRequestForChannel(requestMessage, channel);

            ChannelType channelType = channel.getType();
            LOG.debug("[{}][{}] Processing sync request {} from {} channel [{}]", endpointKey, actorKey, request, channelType,
                    requestMessage.getChannelUuid());

            SyncResponseHolder responseHolder = operationsService.sync(request, state.getProfile());

            state.setProfile(responseHolder.getEndpointProfile());

            if (state.getProfile() != null) {
                processLogUpload(context, request, responseHolder);
                processUserAttachRequest(context, request, responseHolder);
                processEvents(context, request, responseHolder);
                processUserAttachDetachResults(context, request, responseHolder);
            } else {
                LOG.warn("[{}][{}] Endpoint profile is not set after request processing!", endpointKey, actorKey);
            }

            LOG.debug("[{}][{}] SyncResponseHolder {}", endpointKey, actorKey, responseHolder);

            if (channelType.isAsync()) {
                LOG.debug("[{}][{}] Adding async request from channel [{}] to map ", endpointKey, actorKey, requestMessage.getChannelUuid());
                channel.update(responseHolder);
                updateSubscriptionsToTopics(context, responseHolder);
                sendReply(context, requestMessage, responseHolder.getResponse());
            } else {
                if (channelType.isLongPoll() && !responseHolder.requireImmediateReply()) {
                    LOG.debug("[{}][{}] Adding long poll request from channel [{}] to map ", endpointKey, actorKey,
                            requestMessage.getChannelUuid());
                    channel.update(responseHolder);
                    updateSubscriptionsToTopics(context, responseHolder);
                    scheduleTimeoutMessage(context, requestMessage.getChannelUuid(), getDelay(requestMessage, start));
                } else {
                    sendReply(context, requestMessage, responseHolder.getResponse());
                    state.removeChannel(channel);
                }
            }
        } catch (GetDeltaException e) {
            LOG.error("[{}][{}] processEndpointRequest", endpointKey, actorKey, e);
            sendReply(context, requestMessage, e);
        }
    }

    private ClientSync buildRequestForChannel(SyncRequestMessage requestMessage, ChannelMetaData channel) {
        ClientSync request;
        if (channel.getType().isAsync()) {
            if (channel.isFirstRequest()) {
                request = channel.getRequestMessage().getRequest();
            } else {
                LOG.debug("[{}][{}] Updating request for async channel {}", endpointKey, actorKey, channel);
                request = channel.mergeRequest(requestMessage);
                LOG.trace("[{}][{}] Updated request for async channel {} : {}", endpointKey, actorKey, channel, request);
            }
        } else {
            request = channel.getRequestMessage().getRequest();
        }
        return request;
    }

    private void processUserAttachRequest(ActorContext context, ClientSync syncRequest, SyncResponseHolder responseHolder) {
        UserClientSync request = syncRequest.getUserSync();
        if (request != null && request.getUserAttachRequest() != null) {
            UserAttachRequest aRequest = request.getUserAttachRequest();
            context.parent().tell(
                    new UserVerificationRequestMessage(context.self(), aRequest.getUserVerifierId(), aRequest.getUserExternalId(),
                            aRequest.getUserAccessToken()), context.self());
            LOG.debug("[{}][{}] received and forwarded user attach request {}", endpointKey, actorKey, request.getUserAttachRequest());

            if (userAttachResponseMap.size() > 0) {
                Entry<UUID, UserVerificationResponseMessage> entryToSend = userAttachResponseMap.entrySet().iterator().next();
                updateResponseWithUserAttachResults(responseHolder.getResponse(), entryToSend.getValue());
                userAttachResponseMap.remove(entryToSend.getKey());
            }
        }
    }

    private void updateResponseWithUserAttachResults(ServerSync response, UserVerificationResponseMessage message) {
        if (response.getUserSync() == null) {
            response.setUserSync(new UserServerSync());
        }
        response.getUserSync().setUserAttachResponse(EntityConvertUtils.convert(message));
    }

    private void processEvents(ActorContext context, ClientSync request, SyncResponseHolder responseHolder) {
        if (state.isValidForEvents()) {
            updateUserConnection(context);
            if (request.getEventSync() != null) {
                EventClientSync eventRequest = request.getEventSync();
                processSeqNumber(eventRequest, responseHolder);
                sendEventsIfPresent(context, eventRequest);
            }
        } else {
            LOG.debug(
                    "[{}][{}] Endpoint profile is not valid for send/receive events. Either no assigned user or no event families in sdk",
                    endpointKey, actorKey);
        }
    }

    private void processSeqNumber(EventClientSync request, SyncResponseHolder responseHolder) {
        if (request.isSeqNumberRequest()) {
            EventServerSync response = responseHolder.getResponse().getEventSync();
            if (response == null) {
                response = new EventServerSync();
                responseHolder.getResponse().setEventSync(response);
            }
            response.setEventSequenceNumberResponse(new EventSequenceNumberResponse(Math.max(state.getEventSeqNumber(), 0)));
        }
    }

    private void updateUserConnection(ActorContext context) {
        if (state.userIdMismatch()) {
            sendDisconnectFromOldUser(context, state.getProfile());
            state.setUserRegistrationPending(false);
        }
        if (!state.isUserRegistrationPending()) {
            state.setUserId(state.getProfileUserId());
            if (state.getUserId() != null) {
                sendConnectToNewUser(context, state.getProfile());
                state.setUserRegistrationPending(true);
            }
        } else {
            LOG.trace("[{}][{}] User registration request is already sent.", endpointKey, actorKey);
        }
    }

    private void processLogUpload(ActorContext context, ClientSync syncRequest, SyncResponseHolder responseHolder) {
        LogClientSync request = syncRequest.getLogSync();
        if (request != null) {
            if (request.getLogEntries() != null && request.getLogEntries().size() > 0) {
                LOG.debug("[{}][{}] Processing log upload request {}", endpointKey, actorKey, request.getLogEntries().size());
                LogEventPack logPack = new LogEventPack();
                logPack.setDateCreated(System.currentTimeMillis());
                logPack.setEndpointKey(Base64Util.encode(key.getData()));
                List<LogEvent> logEvents = new ArrayList<>(request.getLogEntries().size());
                for (LogEntry logEntry : request.getLogEntries()) {
                    LogEvent logEvent = new LogEvent();
                    logEvent.setLogData(logEntry.getData().array());
                    logEvents.add(logEvent);
                }
                logPack.setEvents(logEvents);
                logPack.setLogSchemaVersion(responseHolder.getEndpointProfile().getLogSchemaVersion());
                logPack.setUserId(state.getUserId());
                context.parent().tell(new LogEventPackMessage(request.getRequestId(), context.self(), logPack), context.self());
            }
            if (logUploadResponseMap.size() > 0) {
                responseHolder.getResponse().setLogSync(EntityConvertUtils.convert(logUploadResponseMap));
                logUploadResponseMap.clear();
            }
        }
    }

    private void sendConnectToNewUser(ActorContext context, EndpointProfileDto endpointProfile) {
        List<EventClassFamilyVersion> ecfVersions = EntityConvertUtils.convertToECFVersions(endpointProfile.getEcfVersionStates());
        EndpointUserConnectMessage userRegistrationMessage = new EndpointUserConnectMessage(state.getUserId(), key, ecfVersions, appToken,
                context.self());
        LOG.debug("[{}][{}] Sending user registration request {}", endpointKey, actorKey, userRegistrationMessage);
        context.parent().tell(userRegistrationMessage, context.self());
    }

    private void sendDisconnectFromOldUser(ActorContext context, EndpointProfileDto endpointProfile) {
        LOG.debug("[{}][{}] Detected user change from [{}] to [{}]", endpointKey, actorKey, state.getUserId(),
                endpointProfile.getEndpointUserId());
        EndpointUserDisconnectMessage userDisconnectMessage = new EndpointUserDisconnectMessage(state.getUserId(), key, appToken,
                context.self());
        context.parent().tell(userDisconnectMessage, context.self());
    }

    private long getDelay(SyncRequestMessage requestMessage, long start) {
        return requestMessage.getRequest().getClientSyncMetaData().getTimeout() - (System.currentTimeMillis() - start);
    }

    private ChannelMetaData initChannel(ActorContext context, SyncRequestMessage requestMessage) {
        ChannelMetaData channel = state.getChannelById(requestMessage.getChannelUuid());
        if (channel == null) {
            channel = new ChannelMetaData(requestMessage);

            if (!channel.getType().isAsync() && channel.getType().isLongPoll()) {
                LOG.debug("[{}][{}] Received request using long poll channel.", endpointKey, actorKey);
                // Probably old long poll channels lost connection. Sending
                // reply to them just in case
                List<ChannelMetaData> channels = state.getChannelsByType(TransportType.EVENT);
                for (ChannelMetaData oldChannel : channels) {
                    if (!oldChannel.getType().isAsync() && channel.getType().isLongPoll()) {
                        LOG.debug("[{}][{}] Closing old long poll channel [{}]", endpointKey, actorKey, oldChannel.getId());
                        sendReply(context, oldChannel.getRequestMessage(), oldChannel.getResponseHolder().getResponse());
                        state.removeChannel(oldChannel);
                    }
                }
            }

            long time = System.currentTimeMillis();

            channel.setLastActivityTime(time);

            if (channel.getType().isAsync() && channel.getKeepAlive() > 0) {
                scheduleKeepAliveCheck(context, channel);
            }

            state.addChannel(channel);
        }
        return channel;
    }

    private void scheduleKeepAliveCheck(ActorContext context, ChannelMetaData channel) {
        TimeoutMessage message = new ChannelTimeoutMessage(channel.getId(), channel.getLastActivityTime());
        LOG.debug("Scheduling channel timeout message: {} to timeout in {}", message, channel.getKeepAlive() * 1000);
        scheduleTimeoutMessage(context, message, channel.getKeepAlive() * 1000);
    }

    private void processUserAttachDetachResults(ActorContext context, ClientSync request, SyncResponseHolder responseHolder) {
        if (responseHolder.getResponse().getUserSync() != null) {
            List<EndpointAttachResponse> attachResponses = responseHolder.getResponse().getUserSync().getEndpointAttachResponses();
            if (attachResponses != null && !attachResponses.isEmpty()) {
                state.resetEventSeqNumber();
                for (EndpointAttachResponse response : attachResponses) {
                    if (response.getResult() != SyncStatus.SUCCESS) {
                        LOG.debug("[{}][{}] Skipped unsuccessful attach response [{}]", endpointKey, actorKey, response.getRequestId());
                        continue;
                    }
                    EndpointUserAttachMessage attachMessage = new EndpointUserAttachMessage(EndpointObjectHash.fromBytes(Base64Util
                            .decode(response.getEndpointKeyHash())), state.getUserId(), endpointKey);
                    context.parent().tell(attachMessage, context.self());
                    LOG.debug("[{}][{}] Notification to attached endpoint [{}] sent", endpointKey, actorKey, response.getEndpointKeyHash());
                }
            }

            List<EndpointDetachRequest> detachRequests = request.getUserSync() == null ? null : request.getUserSync()
                    .getEndpointDetachRequests();
            if (detachRequests != null && !detachRequests.isEmpty()) {
                state.resetEventSeqNumber();
                for (EndpointDetachRequest detachRequest : detachRequests) {
                    for (EndpointDetachResponse detachResponse : responseHolder.getResponse().getUserSync().getEndpointDetachResponses()) {
                        if (detachRequest.getRequestId() == detachResponse.getRequestId()) {
                            if (detachResponse.getResult() != SyncStatus.SUCCESS) {
                                LOG.debug("[{}][{}] Skipped unsuccessful detach response [{}]", endpointKey, actorKey,
                                        detachResponse.getRequestId());
                                continue;
                            }
                            EndpointUserDetachMessage attachMessage = new EndpointUserDetachMessage(EndpointObjectHash.fromBytes(Base64Util
                                    .decode(detachRequest.getEndpointKeyHash())), state.getUserId(), endpointKey);
                            context.parent().tell(attachMessage, context.self());
                            LOG.debug("[{}][{}] Notification to detached endpoint [{}] sent", endpointKey, actorKey,
                                    detachRequest.getEndpointKeyHash());
                        }
                    }
                }
            }
        }
    }

    protected void scheduleActorTimeout(ActorContext context) {
        if (state.isNoChannels()) {
            scheduleTimeoutMessage(context, new ActorTimeoutMessage(state.getLastActivityTime()), inactivityTimeout);
        }
    }

    /**
     * Subscribe to topics.
     *
     * @param response
     *            the response
     */
    private void updateSubscriptionsToTopics(ActorContext context, SyncResponseHolder response) {
        Map<String, Integer> newStates = response.getSubscriptionStates();
        if(newStates == null){
            return;
        }
        Map<String, Integer> currentStates = state.getSubscriptionStates();
        // detect and remove unsubscribed topics;
        Iterator<String> currentSubscriptionsIterator = currentStates.keySet().iterator();
        while (currentSubscriptionsIterator.hasNext()) {
            String subscribedTopic = currentSubscriptionsIterator.next();
            if (!newStates.containsKey(subscribedTopic)) {
                currentSubscriptionsIterator.remove();
                TopicUnsubscriptionMessage topicSubscriptionMessage = new TopicUnsubscriptionMessage(subscribedTopic,
                        appToken, key, context.self());
                context.parent().tell(topicSubscriptionMessage, context.self());
            }
        }
        // subscribe to new topics;
        for (Entry<String, Integer> entry : newStates.entrySet()) {
            if (!currentStates.containsKey(entry.getKey())) {
                TopicSubscriptionMessage topicSubscriptionMessage = new TopicSubscriptionMessage(entry.getKey(),
                        entry.getValue(), response.getSystemNfVersion(), response.getUserNfVersion(), appToken, key, context.self());
                context.parent().tell(topicSubscriptionMessage, context.self());
            }
        }
        state.setSubscriptionStates(newStates);
    }

    private void scheduleTimeoutMessage(ActorContext context, UUID requestId, long delay) {
        scheduleTimeoutMessage(context, new RequestTimeoutMessage(requestId), delay);
    }

    private void scheduleTimeoutMessage(ActorContext context, TimeoutMessage message, long delay) {
        context.system().scheduler()
                .scheduleOnce(Duration.create(delay, TimeUnit.MILLISECONDS), context.self(), message, context.dispatcher(), context.self());
    }

    private void addEventsAndReply(ActorContext context, ChannelMetaData channel, EndpointEventReceiveMessage message) {
        SyncRequestMessage pendingRequest = channel.getRequestMessage();
        SyncResponseHolder pendingResponse = channel.getResponseHolder();

        EventServerSync eventResponse = pendingResponse.getResponse().getEventSync();
        if (eventResponse == null) {
            eventResponse = new EventServerSync();
            pendingResponse.getResponse().setEventSync(eventResponse);
        }

        eventResponse.setEvents(message.getEvents());
        sendReply(context, pendingRequest, pendingResponse.getResponse());
        if (!channel.getType().isAsync()) {
            state.removeChannel(channel);
        }
    }

    private void sendReply(ActorContext context, SyncRequestMessage request, ServerSync syncResponse) {
        sendReply(context, request, null, syncResponse);
    }

    private void sendReply(ActorContext context, SyncRequestMessage request, GetDeltaException e) {
        sendReply(context, request, e, null);
    }

    private void sendReply(ActorContext context, SyncRequestMessage request, GetDeltaException e, ServerSync syncResponse) {
        LOG.debug("[{}] response: {}", actorKey, syncResponse);

        ServerSync copy = ServerSync.deepCopy(syncResponse);

        NettySessionResponseMessage response = new NettySessionResponseMessage(request.getSession(), copy, request.getCommand()
                .getMessageBuilder(), request.getCommand().getErrorBuilder());

        tellActor(context, request.getOriginator(), response);
        scheduleActorTimeout(context);
    }

    protected void tellActor(ActorContext context, ActorRef target, Object message) {
        target.tell(message, context.self());
    }

    protected void sendEventsIfPresent(ActorContext context, EventClientSync request) {
        List<Event> events = request.getEvents();
        if (state.getUserId() != null && events != null && !events.isEmpty()) {
            LOG.debug("[{}][{}] Processing events {} with seq number > {}", endpointKey, actorKey, events, state.getEventSeqNumber());
            List<Event> eventsToSend = new ArrayList<>(events.size());
            int maxSentEventSeqNum = state.getEventSeqNumber();
            for (Event event : events) {
                if (event.getSeqNum() > state.getEventSeqNumber()) {
                    event.setSource(endpointKey);
                    eventsToSend.add(event);
                    maxSentEventSeqNum = Math.max(event.getSeqNum(), maxSentEventSeqNum);
                } else {
                    LOG.debug("[{}][{}] Ignoring duplicate/old event {} due to seq number < {}", endpointKey, actorKey, events,
                            state.getEventSeqNumber());
                }
            }
            state.setEventSeqNumber(maxSentEventSeqNum);
            if (!eventsToSend.isEmpty()) {
                EndpointEventSendMessage message = new EndpointEventSendMessage(state.getUserId(), eventsToSend, key, appToken,
                        context.self());
                context.parent().tell(message, context.self());
            }
        }
    }

    public void processEndpointUserActionMessage(ActorContext context, EndpointUserActionMessage message) {
        Set<ChannelMetaData> eventChannels = state.getChannelsByTypes(TransportType.EVENT, TransportType.USER);
        LOG.debug("[{}][{}] Current Endpoint was attached/detached from user. Need to close all current event channels {}", endpointKey,
                actorKey, eventChannels.size());
        state.setUserRegistrationPending(false);

        if (message instanceof EndpointUserAttachMessage) {
            if (state.isProfileSet()) {
                state.setProfileUserId(message.getUserId());
            }
            LOG.debug("[{}][{}] Updating endpoint user id to {} in profile", endpointKey, actorKey, message.getUserId());
        } else if (message instanceof EndpointUserDetachMessage) {
            if (state.isProfileSet() && message.getUserId().equals(state.getProfileUserId())) {
                state.setProfileUserId(null);
            }
            LOG.debug("[{}][{}] Clanup endpoint user id in profile", endpointKey, actorKey, message.getUserId());
        }

        if (!eventChannels.isEmpty()) {
            updateUserConnection(context);
            for (ChannelMetaData channel : eventChannels) {
                SyncRequestMessage pendingRequest = channel.getRequestMessage();
                ServerSync pendingResponse = channel.getResponseHolder().getResponse();

                UserServerSync userSyncResponse = pendingResponse.getUserSync();

                if (userSyncResponse == null && pendingRequest.isValid(TransportType.USER)) {
                    userSyncResponse = new UserServerSync();
                    pendingResponse.setUserSync(userSyncResponse);
                }
                if (userSyncResponse != null) {
                    if (message instanceof EndpointUserAttachMessage) {
                        userSyncResponse
                                .setUserAttachNotification(new UserAttachNotification(message.getUserId(), message.getOriginator()));
                        LOG.debug("[{}][{}] Adding user attach notification", endpointKey, actorKey);
                    } else if (message instanceof EndpointUserDetachMessage) {
                        userSyncResponse.setUserDetachNotification(new UserDetachNotification(message.getOriginator()));
                        LOG.debug("[{}][{}] Adding user detach notification", endpointKey, actorKey);
                    }
                }

                LOG.debug("[{}][{}] sending reply to [{}] channel", endpointKey, actorKey, channel.getId());
                sendReply(context, pendingRequest, pendingResponse);
                if (!channel.getType().isAsync()) {
                    state.removeChannel(channel);
                }
            }
        } else {
            LOG.debug("[{}][{}] Message ignored due to no channel contexts registered for events", endpointKey, actorKey, message);
        }
    }

    public boolean processDisconnectMessage(ActorContext context, ChannelAware message) {
        LOG.debug("[{}][{}] Received disconnect message for channel [{}]", endpointKey, actorKey, message.getChannelUuid());
        ChannelMetaData channel = state.getChannelById(message.getChannelUuid());
        if (channel != null) {
            state.removeChannel(channel);
            return true;
        } else {
            LOG.debug("[{}][{}] Can't find channel by uuid [{}]", endpointKey, actorKey, message.getChannelUuid());
            return false;
        }
    }

    public boolean processPingMessage(ActorContext context, ChannelAware message) {
        LOG.debug("[{}][{}] Received ping message for channel [{}]", endpointKey, actorKey, message.getChannelUuid());
        ChannelMetaData channel = state.getChannelById(message.getChannelUuid());
        if (channel != null) {
            long lastActivityTime = System.currentTimeMillis();
            LOG.debug("[{}][{}] Updating last activity time for channel [{}] to ", endpointKey, actorKey, message.getChannelUuid(),
                    lastActivityTime);
            channel.setLastActivityTime(lastActivityTime);
            channel.getContext().writeAndFlush(new PingResponse());
            return true;
        } else {
            LOG.debug("[{}][{}] Can't find channel by uuid [{}]", endpointKey, actorKey, message.getChannelUuid());
            return false;
        }
    }

    public boolean processChannelTimeoutMessage(ActorContext context, ChannelTimeoutMessage message) {
        LOG.debug("[{}][{}] Received channel timeout message for channel [{}]", endpointKey, actorKey, message.getChannelUuid());
        ChannelMetaData channel = state.getChannelById(message.getChannelUuid());
        if (channel != null) {
            if (channel.getLastActivityTime() <= message.getLastActivityTime()) {
                LOG.debug("[{}][{}] Timeout message accepted for channel [{}]. Last activity time {} and timeout is {} ", endpointKey,
                        actorKey, message.getChannelUuid(), channel.getLastActivityTime(), message.getLastActivityTime());
                state.removeChannel(channel);
                return true;
            } else {
                LOG.debug("[{}][{}] Timeout message ignored for channel [{}]. Last activity time {} and timeout is {} ", endpointKey,
                        actorKey, message.getChannelUuid(), channel.getLastActivityTime(), message.getLastActivityTime());
                scheduleKeepAliveCheck(context, channel);
                return false;
            }
        } else {
            LOG.debug("[{}][{}] Can't find channel by uuid [{}]", endpointKey, actorKey, message.getChannelUuid());
            return false;
        }
    }

    public void processLogDeliveryMessage(ActorContext context, LogDeliveryMessage message) {
        LOG.debug("[{}][{}] Received log delivery message for request [{}] with status {}", endpointKey, actorKey, message.getRequestId(),
                message.isSuccess());
        logUploadResponseMap.put(message.getRequestId(), message);
        List<ChannelMetaData> channels = state.getChannelsByType(TransportType.LOGGING);
        for (ChannelMetaData channel : channels) {
            SyncRequestMessage pendingRequest = channel.getRequestMessage();
            ServerSync pendingResponse = channel.getResponseHolder().getResponse();

            pendingResponse.setLogSync(EntityConvertUtils.convert(logUploadResponseMap));

            LOG.debug("[{}][{}] sending reply to [{}] channel", endpointKey, actorKey, channel.getId());
            sendReply(context, pendingRequest, pendingResponse);
            if (!channel.getType().isAsync()) {
                state.removeChannel(channel);
            }
        }
        logUploadResponseMap.clear();
    }

    public void processUserVerificationMessage(ActorContext context, UserVerificationResponseMessage message) {
        LOG.debug("[{}][{}] Received user verification message for request [{}] with status {}", endpointKey, actorKey,
                message.getRequestId(), message.isSuccess());
        userAttachResponseMap.put(message.getRequestId(), message);
        List<ChannelMetaData> channels = state.getChannelsByType(TransportType.USER);
        Entry<UUID, UserVerificationResponseMessage> entryToSend = userAttachResponseMap.entrySet().iterator().next();
        for (ChannelMetaData channel : channels) {
            SyncRequestMessage pendingRequest = channel.getRequestMessage();
            ServerSync pendingResponse = channel.getResponseHolder().getResponse();

            updateResponseWithUserAttachResults(pendingResponse, entryToSend.getValue());

            LOG.debug("[{}][{}] sending reply to [{}] channel", endpointKey, actorKey, channel.getId());
            sendReply(context, pendingRequest, pendingResponse);
            if (!channel.getType().isAsync()) {
                state.removeChannel(channel);
            }
        }
        userAttachResponseMap.remove(entryToSend.getKey());
        if (message.isSuccess()) {
            operationsService.attachEndpointToUser(state.getProfile(), appToken, message.getUserId());
            updateUserConnection(context);
        }
    }
}
