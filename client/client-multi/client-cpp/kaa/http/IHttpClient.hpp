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

#ifndef IHTTPCLIENT_HPP_
#define IHTTPCLIENT_HPP_

#include "kaa/KaaDefaults.hpp"

#if defined(KAA_DEFAULT_BOOTSTRAP_HTTP_CHANNEL) || \
    defined(KAA_DEFAULT_OPERATION_HTTP_CHANNEL) || \
    defined(KAA_DEFAULT_LONG_POLL_CHANNEL)

#include <memory>
#include "kaa/http/IHttpResponse.hpp"
#include "kaa/http/IHttpRequest.hpp"

namespace kaa {

class IHttpClient
{
public:
    virtual std::shared_ptr<IHttpResponse> sendRequest(const IHttpRequest& request) = 0;
    virtual void closeConnection() = 0;

    virtual ~IHttpClient() { }
};

}

#endif

#endif /* IHTTPCLIENT_HPP_ */
