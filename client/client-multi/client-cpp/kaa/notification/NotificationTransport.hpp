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

#ifndef DEFAULTNOTIFICATIONTRANSPORT_HPP_
#define DEFAULTNOTIFICATIONTRANSPORT_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_NOTIFICATIONS

#include <map>
#include <set>
#include <string>

#include "kaa/channel/transport/IKaaTransport.hpp"
#include "kaa/channel/transport/INotificationTransport.hpp"
#include "kaa/channel/transport/AbstractKaaTransport.hpp"
#include "kaa/IKaaClientStateStorage.hpp"
#include "kaa/notification/INotificationProcessor.hpp"

namespace kaa {

class IKaaChannelManager;

class NotificationTransport: public AbstractKaaTransport<TransportType::NOTIFICATION>,
                             public INotificationTransport
{
public:
    NotificationTransport(IKaaClientStateStoragePtr status, IKaaChannelManager& manager)
        : AbstractKaaTransport(manager), notificationProcessor_(nullptr)
    {
        setClientState(status);
    }

    virtual NotificationSyncRequestPtr createEmptyNotificationRequest();

    virtual NotificationSyncRequestPtr createNotificationRequest();

    virtual void onNotificationResponse(const NotificationSyncResponse& response);

    virtual void onSubscriptionChanged(const SubscriptionCommands& commands);

    virtual void setNotificationProcessor(INotificationProcessor* processor) {
        if (processor != nullptr) {
            notificationProcessor_ = processor;
        }
    }

    virtual void sync() {
        syncByType(type_);
    }
private:
    Notifications getUnicastNotifications(const Notifications & notifications);
    Notifications getMulticastNotifications(const Notifications & notifications);

private:
    INotificationProcessor*   notificationProcessor_;

    std::set<std::string>                    acceptedUnicastNotificationIds_;
    std::map<std::string, std::int32_t>    notificationSubscriptions_;
    SubscriptionCommands                     subscriptions_;
};

} /* namespace kaa */

#endif

#endif /* DEFAULTNOTIFICATIONTRANSPORT_HPP_ */
