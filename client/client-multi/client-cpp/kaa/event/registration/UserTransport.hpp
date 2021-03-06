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

#ifndef USERTRANSPORT_HPP_
#define USERTRANSPORT_HPP_

#include "kaa/KaaDefaults.hpp"

#ifdef KAA_USE_EVENTS

#include "kaa/channel/transport/AbstractKaaTransport.hpp"
#include "kaa/channel/transport/IUserTransport.hpp"
#include "kaa/event/registration/IRegistrationProcessor.hpp"

namespace kaa {

class UserTransport : public AbstractKaaTransport<TransportType::USER>, public IUserTransport {
public:
    UserTransport(IRegistrationProcessor& manager, IKaaChannelManager& channelManager);
    std::shared_ptr<UserSyncRequest>     createUserRequest();
    void                onUserResponse(const UserSyncResponse& response);
    void                sync();
    void                syncProfile();
private:
    IRegistrationProcessor & manager_;

};

}  // namespace kaa

#endif

#endif /* USERTRANSPORT_HPP_ */
