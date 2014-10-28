#
# Copyright 2014 CyberVision, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

cmake_minimum_required(VERSION 2.8.8)

project (Kaa-c)
enable_language(C)

SET(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fPIC -std=c1x -Wall")

if("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Clang")
    SET(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -undefined dynamic_lookup")
endif()

if (CMAKE_COMPILER_IS_GNUCC)
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Os")
endif ()

if (KAA_WITHOUT_EVENTS)
set (CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DKAA_DISABLE_FEATURE_EVENTS")
endif()

find_package (OpenSSL REQUIRED)

include_directories (${CMAKE_CURRENT_SOURCE_DIR}/src
)

set (KAA_SOURCE_FILES
                       src/avro_src/io.c
                       src/avro_src/encoding_binary.c
                       src/kaa.c
                       src/kaa_context.c
                       src/kaa_profile.c
                       src/kaa_user.c
                       src/kaa_event.c
                       src/kaa_bootstrap.c
                       src/kaa_status.c
                       src/kaa_list.c
                       src/kaa_channel_manager.c
                       src/kaa_common_schema.c
                       src/kaa_mem.c
                       src/kaa_common.c
                       src/gen/kaa_endpoint_gen.c
                       src/gen/kaa_profile_gen.c
)

add_library(kaac_o OBJECT ${KAA_SOURCE_FILES})

add_library (kaac SHARED ./src $<TARGET_OBJECTS:kaac_o>)

target_link_libraries(kaac ssl crypto)

add_library (kaac_s STATIC ./src $<TARGET_OBJECTS:kaac_o>)

if (KAA_UNITTESTS_COMPILE)
#set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DKAA_TRACE_MEMORY_ALLOCATIONS")
add_executable  (test_context
                    test/test_kaa_context.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_context kaac crypto)

add_executable  (test_event
                    test/test_kaa_event.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_event kaac crypto)

add_executable  (test_status
                    test/test_kaa_status.c
                )
target_link_libraries(test_status kaac crypto)

add_executable  (test_bootstrap
                    test/test_kaa_bootstrap.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_bootstrap kaac crypto)

add_executable  (test_profile
                    test/test_kaa_profile.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_profile kaac crypto)

add_executable  (test_common
                    test/test_kaa_common.c
                    test/kaa_test_external.c
                )
target_link_libraries(test_common kaac crypto)

endif ()

install (TARGETS kaac kaac_s
    LIBRARY DESTINATION lib
    ARCHIVE DESTINATION lib
    RUNTIME DESTINATION lib)

install (DIRECTORY src/ DESTINATION include/kaac
    FILES_MATCHING PATTERN *.h)