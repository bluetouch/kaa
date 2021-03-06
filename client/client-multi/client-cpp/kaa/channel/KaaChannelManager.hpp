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

#ifndef KAACHANNELMANAGER_HPP_
#define KAACHANNELMANAGER_HPP_

#include <map>
#include <set>
#include <list>

#include "kaa/KaaThread.hpp"
#include "kaa/KaaDefaults.hpp"
#include "kaa/common/TransportType.hpp"
#include "kaa/channel/IKaaChannelManager.hpp"
#include "kaa/channel/connectivity/IPingServerStorage.hpp"

namespace kaa {

class IBootstrapManager;

class KaaChannelManager: public IKaaChannelManager, public IPingServerStorage
{
public:
    KaaChannelManager(IBootstrapManager& manager, const BootstrapServers& servers);
    ~KaaChannelManager() { doShutdown(); }

    virtual void setChannel(TransportType type, IDataChannelPtr channel);
    virtual void addChannel(IDataChannelPtr channel);
    virtual void removeChannel(const std::string& id);
    virtual void removeChannel(IDataChannelPtr channel);

    virtual std::list<IDataChannelPtr> getChannels();

    virtual IDataChannelPtr getChannelByTransportType(TransportType type);
    virtual IDataChannelPtr getChannel(const std::string& channelId);

    virtual void onServerFailed(ITransportConnectionInfoPtr connectionInfo);
    virtual void onTransportConnectionInfoUpdated(ITransportConnectionInfoPtr connectionInfo);

    virtual void clearChannelList();

    virtual ITransportConnectionInfoPtr getPingServer() { return (*lastBSServers_.begin()).second; }

    virtual void setConnectivityChecker(ConnectivityCheckerPtr checker);

    void shutdown();
    void pause();
    void resume();

private:
    bool useChannelForType(const std::pair<TransportType, ChannelDirection>& type, IDataChannelPtr channel);
    void useNewChannel(IDataChannelPtr channel);
    void useNewChannelForType(TransportType type);
    void replaceChannel(IDataChannelPtr channel);

    bool addChannelToList(IDataChannelPtr channel);

    void doShutdown();

    ITransportConnectionInfoPtr getCurrentBootstrapServer(const TransportProtocolId& protocolId);
    ITransportConnectionInfoPtr getNextBootstrapServer(ITransportConnectionInfoPtr usedConnectionInfo);

private:
    IBootstrapManager&   bootstrapManager_;

    bool_type isShutdown_;
    bool_type isPaused_;

    std::map<TransportProtocolId, std::list<ITransportConnectionInfoPtr>> bootstrapServers_;

    KAA_MUTEX_DECLARE(lastOpsServersGuard_);
    std::map<TransportProtocolId, ITransportConnectionInfoPtr>    lastOpsServers_;

    std::map<TransportProtocolId, ITransportConnectionInfoPtr>    lastBSServers_;

    KAA_MUTEX_DECLARE(channelGuard_);
    std::set<IDataChannelPtr>                   channels_;

    KAA_R_MUTEX_DECLARE(mappedChannelGuard_);
    std::map<TransportType, IDataChannelPtr>    mappedChannels_;

    ConnectivityCheckerPtr connectivityChecker_;
};

} /* namespace kaa */

#endif /* KAACHANNELMANAGER_HPP_ */
