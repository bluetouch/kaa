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

#ifndef EVENTFAMILYFACTORY_HPP_
#define EVENTFAMILYFACTORY_HPP_

/*
 *
 * NOTE: THIS FILE IS AUTOGENERATED.
 *
 */

#ifdef KAA_USE_EVENTS

#include <map>
#include <set>
#include <memory>

#include "kaa/KaaDefaults.hpp"
#include "kaa/event/IEventFamily.hpp"
#include "kaa/event/IEventManager.hpp"
#include "kaa/transact/ITransactable.hpp"

namespace kaa {

class EventFamilyFactory {
public:
    EventFamilyFactory(IEventManager& manager, ITransactable &transactionManager)
        : eventManager_(manager), transactionManager_(transactionManager) {}

    TransactionIdPtr startEventsBlock() {
        return transactionManager_.beginTransaction();
    }

    void submitEventsBlock(TransactionIdPtr trxId) {
        transactionManager_.commit(trxId);
    }

    void removeEventsBlock(TransactionIdPtr trxId) {
        transactionManager_.rollback(trxId);
    }

private:
    IEventManager& eventManager_;
    ITransactable& transactionManager_;
    std::set<std::string> efcNames_;
    std::map<std::string, std::shared_ptr<IEventFamily> > eventFamilies_;
    std::map<std::string, FQNList > supportedFQNLists_;

    std::shared_ptr<IEventFamily> getEventFamilyByName(const std::string& efcName) {
        auto it = eventFamilies_.find(efcName);
        if (it != eventFamilies_.end()) {
            return it->second;
        }
        return std::shared_ptr<IEventFamily>();
    }

    void addEventFamilyByName(const std::string& efcName, std::shared_ptr<IEventFamily> eventFamily) {
        eventManager_.registerEventFamily(eventFamily.get());
        eventFamilies_[efcName] = eventFamily;
    }

    const FQNList& getSupportedFQNsByFamilyName(const std::string& efcName) {
        auto it = supportedFQNLists_.find(efcName);
        if (it != supportedFQNLists_.end()) {
            return it->second;
        }
        static const FQNList empty;
        return empty;
    }

    const std::set<std::string> &getEventFamilyClassNames() {
        return efcNames_;
    }
};

} /* namespace kaa */

#endif

#endif /* EVENTFAMILYFACTORY_HPP_ */
