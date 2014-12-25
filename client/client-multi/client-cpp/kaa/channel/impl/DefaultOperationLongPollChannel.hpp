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

#ifndef DEFAULTOPERATIONLONGPOLLCHANNEL_HPP_
#define DEFAULTOPERATIONLONGPOLLCHANNEL_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_DEFAULT_LONG_POLL_CHANNEL

#include "kaa/channel/IDataChannel.hpp"
#include "kaa/channel/server/HttpLPServerInfo.hpp"
#include "kaa/http/HttpClient.hpp"
#include <boost/asio.hpp>
#include "kaa/security/KeyUtils.hpp"
#include "kaa/transport/HttpDataProcessor.hpp"
#include "kaa/channel/IKaaChannelManager.hpp"
#include "kaa/KaaThread.hpp"

#include <cstdint>
#include <thread>

namespace kaa {

class DefaultOperationLongPollChannel : public IDataChannel {
public:
    DefaultOperationLongPollChannel(IKaaChannelManager *channelManager, const KeyPair& clientKeys);
    virtual ~DefaultOperationLongPollChannel();

    virtual void sync(TransportType type);
    virtual void syncAll();
    virtual void syncAck(TransportType type);
    virtual const std::string& getId() const { return CHANNEL_ID; }
    virtual ChannelType getChannelType() const { return ChannelType::HTTP_LP; }
    virtual ServerType getServerType() const { return ServerType::OPERATIONS; }

    virtual void setMultiplexer(IKaaDataMultiplexer *multiplexer);
    virtual void setDemultiplexer(IKaaDataDemultiplexer *demultiplexer);
    virtual void setServer(IServerInfoPtr server);

    virtual IServerInfoPtr getServer() {
        return currentServer_;
    }

    virtual const std::map<TransportType, ChannelDirection>& getSupportedTransportTypes() const { return SUPPORTED_TYPES; }

    virtual void shutdown();
    virtual void pause();
    virtual void resume();

    virtual void setConnectivityChecker(ConnectivityCheckerPtr checker) {}

private:
    void startPoll();
    void stopPoll();
    void postTask();
    void executeTask();
    void doShutdown();

private:
    static const std::string CHANNEL_ID;
    static const std::map<TransportType, ChannelDirection> SUPPORTED_TYPES;

    KeyPair clientKeys_;

    boost::asio::io_service io_;
    boost::asio::io_service::work work_;
    std::thread pollThread_;
    bool stopped_;
    bool isShutdown_;
    bool isPaused_;
    bool connectionInProgress_;
    bool taskPosted_;
    bool firstStart_;
    IKaaDataMultiplexer *multiplexer_;
    IKaaDataDemultiplexer *demultiplexer_;
    IKaaChannelManager *channelManager_;
    OperationServerLongPollInfoPtr currentServer_;
    HttpDataProcessor httpDataProcessor_;
    HttpClient httpClient_;
    KAA_CONDITION_VARIABLE_DECLARE(waitCondition_);
    KAA_MUTEX_DECLARE(conditionMutex_);
    KAA_MUTEX_DECLARE(channelGuard_);
};

}

#endif

#endif /* DEFAULTOPERATIONLONGPOLLCHANNEL_HPP_ */
