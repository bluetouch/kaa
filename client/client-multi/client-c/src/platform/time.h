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


#ifndef TIME_H_
#define TIME_H_

#ifdef ECONAIS_PLATFORM
#include "../platform-impl/Econais/EC19D/econais_ec19d_time.h"
#else
#include "../platform-impl/posix/posix_time.h"
#endif

#endif /* TIME_H_ */
