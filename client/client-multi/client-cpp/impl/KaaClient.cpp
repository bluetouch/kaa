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

#include <fstream>

#include "kaa/KaaClient.hpp"

#include "kaa/channel/connectivity/IPConnectivityChecker.hpp"
#include "kaa/bootstrap/BootstrapManager.hpp"
#include "kaa/KaaDefaults.hpp"

#include "kaa/bootstrap/BootstrapTransport.hpp"
#include "kaa/channel/MetaDataTransport.hpp"
#include "kaa/configuration/ConfigurationTransport.hpp"
#include "kaa/notification/NotificationTransport.hpp"
#include "kaa/profile/ProfileTransport.hpp"
#include "kaa/event/EventTransport.hpp"
#include "kaa/event/registration/UserTransport.hpp"
#include "kaa/log/LoggingTransport.hpp"
#include "kaa/channel/RedirectionTransport.hpp"

#include "kaa/channel/KaaChannelManager.hpp"

#include "kaa/logging/Log.hpp"

namespace kaa {

KaaClient::KaaClient()
    : status_(new ClientStatus(CLIENT_STATUS_FILE_LOCATION))
    , options_(0)
{

}

void KaaClient::init(int options /*= KAA_DEFAULT_OPTIONS*/)
{
    options_ = options;
    KAA_LOG_INFO(boost::format("Starting Kaa C++ sdk version %1%, commit hash %2%. Options: %3%")
        % BUILD_VERSION % BUILD_COMMIT_HASH % options);

    initClientKeys();

#ifdef KAA_USE_CONFIGURATION
    configurationProcessor_.reset(new ConfigurationProcessor);
    configurationManager_.reset(new ConfigurationManager);
#endif

    bootstrapManager_.reset(new BootstrapManager);
    channelManager_.reset(new KaaChannelManager(*bootstrapManager_, getBootstrapServers()));
#ifdef KAA_USE_EVENTS
    registrationManager_.reset(new EndpointRegistrationManager(status_));
    eventManager_.reset(new EventManager(status_));
    eventFamilyFactory_.reset(new EventFamilyFactory(*eventManager_, *eventManager_));
#endif

#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_.reset(new NotificationManager(status_));
#endif
    profileManager_.reset(new ProfileManager());

#ifdef KAA_USE_LOGGING
    logCollector_.reset(new LogCollector(channelManager_.get()));
#endif

    initKaaConfiguration();
    initKaaTransport();
}

void KaaClient::start()
{
#ifdef KAA_USE_CONFIGURATION
    auto configHash = configurationPersistenceManager_->getConfigurationHash().getHashDigest();
    if (configHash.empty()) {
        SequenceNumber sn = { 0, 0, 1 };
        status_->setAppSeqNumber(sn);
        setDefaultConfiguration();
    }
#endif
    bootstrapManager_->receiveOperationsServerList();
}

void KaaClient::stop()
{
    channelManager_->shutdown();
}

void KaaClient::pause()
{
    channelManager_->pause();
}

void KaaClient::resume()
{
    channelManager_->resume();
}

void KaaClient::initKaaConfiguration()
{
#ifdef KAA_USE_CONFIGURATION
    ConfigurationPersistenceManager *cpm = new ConfigurationPersistenceManager(status_);
    cpm->setConfigurationProcessor(configurationProcessor_.get());
    configurationPersistenceManager_.reset(cpm);

    configurationProcessor_->addOnProcessedObserver(*configurationManager_);
    configurationProcessor_->subscribeForUpdates(*configurationManager_);
    configurationManager_->subscribeForConfigurationChanges(*configurationPersistenceManager_);
#endif
}

void KaaClient::initKaaTransport()
{
    IBootstrapTransportPtr bootstrapTransport(new BootstrapTransport(*channelManager_, *bootstrapManager_));

    bootstrapManager_->setTransport(bootstrapTransport.get());
    bootstrapManager_->setChannelManager(channelManager_.get());

    EndpointObjectHash publicKeyHash(clientKeys_->getPublicKey().begin(), clientKeys_->getPublicKey().size());
    IMetaDataTransportPtr metaDataTransport(new MetaDataTransport(status_, publicKeyHash, 60000L));
    IProfileTransportPtr profileTransport(new ProfileTransport(*channelManager_, clientKeys_->getPublicKey()));
#ifdef KAA_USE_CONFIGURATION
    IConfigurationTransportPtr configurationTransport(new ConfigurationTransport(
            *channelManager_
            , configurationProcessor_.get()
            , configurationPersistenceManager_.get()
            , status_));
#endif
#ifdef KAA_USE_NOTIFICATIONS
    INotificationTransportPtr notificationTransport(new NotificationTransport(status_, *channelManager_));
#endif
#ifdef KAA_USE_EVENTS
    IUserTransportPtr userTransport(new UserTransport(*registrationManager_, *channelManager_));
    IEventTransportPtr eventTransport(new EventTransport(*eventManager_, *channelManager_, status_));
    dynamic_cast<EventTransport*>(eventTransport.get())->setClientState(status_);
#endif
#ifdef KAA_USE_LOGGING
    ILoggingTransportPtr loggingTransport(new LoggingTransport(*channelManager_, *logCollector_));
#endif
    IRedirectionTransportPtr redirectionTransport(new RedirectionTransport(*bootstrapManager_));

    profileTransport->setProfileManager(profileManager_.get());
    dynamic_cast<ProfileTransport*>(profileTransport.get())->setClientState(status_);
    profileManager_->setTransport(profileTransport);

    syncProcessor_.reset(
            new SyncDataProcessor(
              metaDataTransport
            , bootstrapTransport
            , profileTransport
#ifdef KAA_USE_CONFIGURATION
            , configurationTransport
#else
            , nullptr
#endif
#ifdef KAA_USE_NOTIFICATIONS
            , notificationTransport
#else
            , nullptr
#endif
#ifdef KAA_USE_EVENTS
            , userTransport
            , eventTransport
#else
            , nullptr
            , nullptr
#endif
#ifdef KAA_USE_LOGGING
            , loggingTransport
#else
            , nullptr
#endif
            , redirectionTransport
            , status_));

#ifdef KAA_USE_EVENTS
    eventManager_->setTransport(std::dynamic_pointer_cast<EventTransport, IEventTransport>(eventTransport).get());
    registrationManager_->setTransport(std::dynamic_pointer_cast<UserTransport, IUserTransport>(userTransport).get());
#endif
#ifdef KAA_USE_LOGGING
    logCollector_->setTransport(std::dynamic_pointer_cast<LoggingTransport, ILoggingTransport>(loggingTransport).get());
#endif
#ifdef KAA_USE_NOTIFICATIONS
    notificationManager_->setTransport(std::dynamic_pointer_cast<NotificationTransport, INotificationTransport>(notificationTransport));
#endif
#ifdef KAA_DEFAULT_BOOTSTRAP_HTTP_CHANNEL
    if (options_ & KaaOption::USE_DEFAULT_BOOTSTRAP_HTTP_CHANNEL) {
        bootstrapChannel_.reset(new DefaultBootstrapChannel(channelManager_.get(), *clientKeys_));
        bootstrapChannel_->setDemultiplexer(syncProcessor_.get());
        bootstrapChannel_->setMultiplexer(syncProcessor_.get());
        KAA_LOG_INFO(boost::format("Going to set default bootstrap channel: %1%") % bootstrapChannel_.get());
        channelManager_->addChannel(bootstrapChannel_.get());
    }
#endif
#ifdef KAA_DEFAULT_OPERATION_HTTP_CHANNEL
    if (options_ & KaaOption::USE_DEFAULT_OPERATION_HTTP_CHANNEL) {
        opsHttpChannel_.reset(new DefaultOperationHttpChannel(channelManager_.get(), *clientKeys_));
        opsHttpChannel_->setMultiplexer(syncProcessor_.get());
        opsHttpChannel_->setDemultiplexer(syncProcessor_.get());
        KAA_LOG_INFO(boost::format("Going to set default operations Kaa HTTP channel: %1%") % opsHttpChannel_.get());
        channelManager_->addChannel(opsHttpChannel_.get());
    }
#endif
#ifdef KAA_DEFAULT_LONG_POLL_CHANNEL
    if (options_ & KaaOption::USE_DEFAULT_OPERATION_LONG_POLL_CHANNEL) {
        opsLongPollChannel_.reset(new DefaultOperationLongPollChannel(channelManager_.get(), *clientKeys_));
        opsLongPollChannel_->setMultiplexer(syncProcessor_.get());
        opsLongPollChannel_->setDemultiplexer(syncProcessor_.get());
        KAA_LOG_INFO(boost::format("Going to set default operations Kaa HTTP Long Poll channel: %1%") % opsLongPollChannel_.get());
        channelManager_->addChannel(opsLongPollChannel_.get());
    }
#endif
#ifdef KAA_DEFAULT_TCP_CHANNEL
    if (options_ & KaaOption::USE_DEFAULT_OPERATION_KAATCP_CHANNEL) {
        opsTcpChannel_.reset(new DefaultOperationTcpChannel(channelManager_.get(), *clientKeys_));
        opsTcpChannel_->setDemultiplexer(syncProcessor_.get());
        opsTcpChannel_->setMultiplexer(syncProcessor_.get());
        KAA_LOG_INFO(boost::format("Going to set default operations Kaa TCP channel: %1%") % opsTcpChannel_.get());
        channelManager_->addChannel(opsTcpChannel_.get());
    }
#endif
#ifdef KAA_DEFAULT_CONNECTIVITY_CHECKER
    if (options_ & KaaOption::USE_DEFAULT_CONNECTIVITY_CHECKER) {
        ConnectivityCheckerPtr connectivityChecker(new IPConnectivityChecker(
                *static_cast<KaaChannelManager*>(channelManager_.get())));
        channelManager_->setConnectivityChecker(connectivityChecker);
    }
#endif
}

void KaaClient::initClientKeys()
{
    std::ifstream key(CLIENT_PUB_KEY_LOCATION);
    bool exists = key.good();
    key.close();
    if (exists) {
        clientKeys_.reset(new KeyPair(KeyUtils::loadKeyPair(CLIENT_PUB_KEY_LOCATION, CLIENT_PRIV_KEY_LOCATION)));
    } else {
        clientKeys_.reset(new KeyPair(KeyUtils().generateKeyPair(2048)));
        KeyUtils::saveKeyPair(*clientKeys_, CLIENT_PUB_KEY_LOCATION, CLIENT_PRIV_KEY_LOCATION);
    }

    EndpointObjectHash publicKeyHash(clientKeys_->getPublicKey().begin(), clientKeys_->getPublicKey().size());
    auto digest = publicKeyHash.getHashDigest();
    publicKeyHash_ = Botan::base64_encode(digest.data(), digest.size());

    status_->setEndpointKeyHash(publicKeyHash_);
    status_->save();

}

void KaaClient::setDefaultConfiguration()
{
#ifdef KAA_USE_CONFIGURATION
    const Botan::SecureVector<std::uint8_t>& config = getDefaultConfigData();
    if (!config.empty()) {
        configurationProcessor_->processConfigurationData(config.begin(), config.size(), true);
    }
#endif
}

}
