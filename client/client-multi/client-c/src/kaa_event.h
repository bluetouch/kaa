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

/**
 * @file kaa_event.h
 * @brief Kaa event subsystem API
 *
 * Supplies API for Kaa event subsystem
 */

#ifndef KAA_EVENT_H_
#define KAA_EVENT_H_

#ifdef __cplusplus
extern "C" {
#endif

#ifndef KAA_DISABLE_FEATURE_EVENTS

#include <stddef.h>
#include <stdint.h>
#include "kaa_error.h"

#define KAA_ENDPOINT_ID_LENGTH 20

typedef uint8_t        kaa_endpoint_id[KAA_ENDPOINT_ID_LENGTH];
typedef const uint8_t* kaa_endpoint_id_p;

typedef void (*kaa_event_callback_t)(const char *event_fqn, const char *event_data, size_t event_data_size, kaa_endpoint_id_p event_source);
typedef size_t kaa_event_block_id;

typedef struct kaa_event_manager_t kaa_event_manager_t;

/**
 * @brief Start a new event block.
 *
 * Returns a new id which must be used to add an event to the block.
 *
 * @param[in]       self                Valid pointer to the event manager instance.
 * @param[in,out]   trx_id              Pointer to the @link kaa_event_block_id @endlink instance which will be fulfilled with a corresponding ID.
 *
 * @return Error code.
 */
kaa_error_t kaa_event_create_transaction(kaa_event_manager_t *self, kaa_event_block_id *trx_id);

/**
 * @brief Send all the events from the event block at once.
 *
 * The event block is identified by the given trx_id.
 *
 * @param[in]       self                Valid pointer to the event manager instance.
 * @param[in]       trx_id              The ID of the event block to be sent.
 *
 * @return Error code.
 */
kaa_error_t kaa_event_finish_transaction(kaa_event_manager_t *self, kaa_event_block_id trx_id);

/**
 * @brief Removes the event block without sending events.
 *
 * @param[in]       self                Valid pointer to the event manager instance.
 * @param[in]       trx_id              The ID of the event block to be sent.
 *
 * @return Error code.
 */
kaa_error_t kaa_event_remove_transaction(kaa_event_manager_t *self, kaa_event_block_id trx_id);

/**
 * @brief Find class family name of the event by its fully-qualified name.
 *
 * @param[in]       fqn                 Fully-qualified name of the event (null-terminated string).
 *
 * @return Null-terminated string if corresponding event class family was found, @code NULL @endcode otherwise.
 */

const char *kaa_find_class_family_name(const char *fqn);

#endif

#ifdef __cplusplus
} // extern "C"
#endif
#endif /* KAA_EVENT_H_ */
