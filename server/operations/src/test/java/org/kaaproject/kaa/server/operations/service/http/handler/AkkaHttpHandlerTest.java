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

package org.kaaproject.kaa.server.operations.service.http.handler;

import io.netty.util.concurrent.EventExecutorGroup;

import java.util.UUID;

import org.junit.Test;
import org.kaaproject.kaa.server.operations.service.akka.AkkaService;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyHttpSyncMessage;
import org.kaaproject.kaa.server.operations.service.http.commands.AbstractHttpSyncCommand;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.mockito.Mockito;

public class AkkaHttpHandlerTest {

    @Test
    public void testFlow() throws Exception{
        UUID uuid = UUID.randomUUID();
        AkkaService akkaService = Mockito.mock(AkkaService.class);
        EventExecutorGroup executor = Mockito.mock(EventExecutorGroup.class);
        AbstractHttpSyncCommand commandMock = Mockito.mock(AbstractHttpSyncCommand.class);
        Mockito.when(commandMock.getChannelType()).thenReturn(ChannelType.HTTP);
        AkkaHttpHandler handler = new AkkaHttpHandler(uuid, akkaService, executor);
        handler.channelRead0(null, commandMock);
        Mockito.verify(akkaService).process(Mockito.any(NettyHttpSyncMessage.class));
    }
}
