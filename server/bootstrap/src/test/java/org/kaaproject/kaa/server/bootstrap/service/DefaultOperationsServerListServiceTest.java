package org.kaaproject.kaa.server.bootstrap.service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.server.common.zk.bootstrap.BootstrapNode;
import org.kaaproject.kaa.server.common.zk.gen.ConnectionInfo;
import org.kaaproject.kaa.server.common.zk.gen.OperationsNodeInfo;
import org.kaaproject.kaa.server.common.zk.gen.TransportMetaData;
import org.kaaproject.kaa.server.common.zk.gen.VersionConnectionInfoPair;
import org.kaaproject.kaa.server.sync.bootstrap.ProtocolConnectionData;
import org.kaaproject.kaa.server.sync.bootstrap.ProtocolVersionKey;
import org.mockito.Mockito;

public class DefaultOperationsServerListServiceTest {

    OperationsServerListService service;

    @Before
    public void before() {
        service = new DefaultOperationsServerListService();
        BootstrapNode zkNode = Mockito.mock(BootstrapNode.class);
        OperationsNodeInfo nodeInfo = new OperationsNodeInfo();
        nodeInfo.setConnectionInfo(new ConnectionInfo("localhost", 8000, ByteBuffer.wrap(new byte[0])));
        List<TransportMetaData> mdList = new ArrayList<TransportMetaData>();
        mdList.add(new TransportMetaData(1, 42, 42, Collections.singletonList(new VersionConnectionInfoPair(42, ByteBuffer.wrap("test"
                .getBytes())))));
        mdList.add(new TransportMetaData(2, 73, 73, Collections.singletonList(new VersionConnectionInfoPair(73, ByteBuffer.wrap("test"
                .getBytes())))));
        mdList.add(new TransportMetaData(3, 1, 3, Arrays.asList(new VersionConnectionInfoPair(1, ByteBuffer.wrap("test1".getBytes())),
                new VersionConnectionInfoPair(2, ByteBuffer.wrap("test2".getBytes())),
                new VersionConnectionInfoPair(3, ByteBuffer.wrap("test3".getBytes())))));
        nodeInfo.setTransports(mdList);

        Mockito.when(zkNode.getCurrentOperationServerNodes()).thenReturn(Arrays.asList(nodeInfo));
        service.init(zkNode);
    }

    @Test
    public void testFilterOneResult() {
        Set<ProtocolConnectionData> result = service.filter(Collections.singletonList(new ProtocolVersionKey(1, 42)));
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertNotNull(result.iterator().next());
        Assert.assertEquals(1, result.iterator().next().getProtocolId());
        Assert.assertEquals(42, result.iterator().next().getProtocolVersion());
    }

    @Test
    public void testFilterResultDeduplication() {
        Set<ProtocolConnectionData> result = service.filter(Arrays.asList(new ProtocolVersionKey(3, 1), new ProtocolVersionKey(3, 1)));
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertNotNull(result.iterator().next());
        Assert.assertEquals(3, result.iterator().next().getProtocolId());
        Assert.assertEquals(1, result.iterator().next().getProtocolVersion());
    }

    @Test
    public void testFilterMultipleResults() {
        Set<ProtocolConnectionData> result = service.filter(Arrays.asList(new ProtocolVersionKey(1, 42), new ProtocolVersionKey(2, 73)));
        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void testFilterNoResults() {
        Set<ProtocolConnectionData> result = service.filter(Arrays.asList(new ProtocolVersionKey(2, 42), new ProtocolVersionKey(1, 73)));
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }
}
