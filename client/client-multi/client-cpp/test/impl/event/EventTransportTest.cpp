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

#include <boost/test/unit_test.hpp>

#include <kaa/event/EventTransport.hpp>
#include <kaa/event/IEventDataProcessor.hpp>

#include "headers/channel/MockChannelManager.hpp"
#include "headers/MockKaaClientStateStorage.hpp"

namespace kaa {

class TestKaaClientStateStorage : public MockKaaClientStateStorage {
public:
    virtual std::int32_t getEventSequenceNumber() const {
        return sn_;
    }

    virtual void setEventSequenceNumber(std::int32_t sn) {
        sn_ = sn;
    }

private:
    std::int32_t sn_;
};

class TestEventDataProcessor : public IEventDataProcessor
{
public:
    virtual std::list<Event> getPendingEvents() {
        return events_;
    }

    void setPendingEvents(const std::list<Event>& newEvents) {
        events_ = newEvents;
    }

    virtual std::map<std::int32_t, std::list<std::string> > getPendingListenerRequests() {
        static std::map<std::int32_t, std::list<std::string> > mock;
        return mock;
    }

    virtual void onEventsReceived(const EventSyncResponse::events_t& events) {}
    virtual void onEventListenersReceived(const EventSyncResponse::eventListenersResponses_t& listeners) {}

private:
    std::list<Event> events_;
};

BOOST_AUTO_TEST_SUITE(EventTransportTestSuite)

BOOST_AUTO_TEST_CASE(EventTransportSequenceNumberRequestTest)
{
    IKaaClientStateStoragePtr clientState;
    MockChannelManager channelManager;
    TestEventDataProcessor processor;
    EventTransport transport(processor, channelManager, clientState);

    std::list<Event> events = { Event(), Event() };
    processor.setPendingEvents(events);

    std::int32_t requestId = 1;
    std::shared_ptr<EventSyncRequest> eventSyncRequest1 =
                        transport.createEventRequest(requestId++);

    BOOST_CHECK(eventSyncRequest1);
    BOOST_CHECK(!eventSyncRequest1->eventSequenceNumberRequest.is_null());
    BOOST_CHECK(eventSyncRequest1->events.is_null());

    std::shared_ptr<EventSyncRequest> eventSyncRequest2 =
                        transport.createEventRequest(requestId++);

    BOOST_CHECK(eventSyncRequest2);
    BOOST_CHECK(!eventSyncRequest2->eventSequenceNumberRequest.is_null());
    BOOST_CHECK(eventSyncRequest2->events.is_null());
}

Event createEvent(const std::int32_t& sn) {
    Event e;
    e.seqNum = sn;
    return e;
}

BOOST_AUTO_TEST_CASE(SychronizedEventSequenceNumberTest)
{
    std::int32_t sn = 10;
    IKaaClientStateStoragePtr clientState(new TestKaaClientStateStorage);
    clientState->setEventSequenceNumber(sn);

    MockChannelManager channelManager;
    TestEventDataProcessor processor;
    EventTransport transport(processor, channelManager, clientState);

    std::list<Event> events = { createEvent(sn + 1)
                              , createEvent(sn)};
    processor.setPendingEvents(events);

    std::int32_t requestId = 1;
    transport.createEventRequest(requestId++);

    EventSyncResponse eventResponse1;
    EventSequenceNumberResponse esnr;
    esnr.seqNum = sn - 1;

    eventResponse1.eventSequenceNumberResponse.set_EventSequenceNumberResponse(esnr);
    eventResponse1.eventListenersResponses.set_null();
    eventResponse1.events.set_null();

    transport.onEventResponse(eventResponse1);

    std::shared_ptr<EventSyncRequest> eventSyncRequest =
                        transport.createEventRequest(requestId++);

    BOOST_CHECK(eventSyncRequest);
    BOOST_CHECK(eventSyncRequest->eventSequenceNumberRequest.is_null());
    BOOST_CHECK(!eventSyncRequest->events.is_null());

    const auto& sendingEvents = eventSyncRequest->events.get_array();
    for (const auto& e : sendingEvents) {
        if (e.seqNum != sn++) {
            BOOST_CHECK(false);
        }
    }
}

BOOST_AUTO_TEST_CASE(UnsychronizedEventSequenceNumberTest)
{
    std::int32_t restored_sn = 5;
    std::int32_t expected_sn = 10;
    IKaaClientStateStoragePtr clientState(new TestKaaClientStateStorage);
    clientState->setEventSequenceNumber(restored_sn);

    MockChannelManager channelManager;
    TestEventDataProcessor processor;
    EventTransport transport(processor, channelManager, clientState);

    std::list<Event> events = { createEvent(restored_sn + 1)
                              , createEvent(restored_sn + 2)
                              , createEvent(restored_sn)};
    processor.setPendingEvents(events);

    std::int32_t requestId = 1;
    transport.createEventRequest(requestId++);

    EventSyncResponse eventResponse1;
    EventSequenceNumberResponse esnr;
    esnr.seqNum = expected_sn - 1;

    eventResponse1.eventSequenceNumberResponse.set_EventSequenceNumberResponse(esnr);
    eventResponse1.eventListenersResponses.set_null();
    eventResponse1.events.set_null();

    transport.onEventResponse(eventResponse1);

    std::shared_ptr<EventSyncRequest> eventSyncRequest =
                        transport.createEventRequest(requestId++);

    BOOST_CHECK(eventSyncRequest);
    BOOST_CHECK(eventSyncRequest->eventSequenceNumberRequest.is_null());
    BOOST_CHECK(!eventSyncRequest->events.is_null());

    const auto& sendingEvents = eventSyncRequest->events.get_array();
    for (const auto& e : sendingEvents) {
        if (e.seqNum != expected_sn++) {
            BOOST_CHECK(false);
        }
    }
}

BOOST_AUTO_TEST_SUITE_END()

}
