package org.kaaproject.kaa.server.transports.tcp.transport;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.kaaproject.kaa.server.common.server.AbstractNettyServer;
import org.kaaproject.kaa.server.transport.AbstractKaaTransport;
import org.kaaproject.kaa.server.transport.SpecificTransportContext;
import org.kaaproject.kaa.server.transport.TransportLifecycleException;
import org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig;
import org.kaaproject.kaa.server.transports.tcp.transport.commands.KaaTcpCommandFactory;
import org.kaaproject.kaa.server.transports.tcp.transport.netty.AbstractKaaTcpCommandProcessor;
import org.kaaproject.kaa.server.transports.tcp.transport.netty.AbstractKaaTcpServerInitializer;
import org.kaaproject.kaa.server.transports.tcp.transport.netty.KaaTcpDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpTransport extends AbstractKaaTransport<AvroTcpConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(TcpTransport.class);
    private static final String BIND_INTERFACE_PROP_NAME = "transport.bindInterface";
    private static final String LOCALHOST = "localhost";
    private static final int SUPPORTED_VERSION = 1;
    private SpecificTransportContext<AvroTcpConfig> context;
    private AbstractNettyServer netty;

    @Override
    protected void init(SpecificTransportContext<AvroTcpConfig> context) throws TransportLifecycleException {
        AvroTcpConfig configuration = context.getConfiguration();
        configuration.setBindInterface(replaceProperty(configuration.getBindInterface(), BIND_INTERFACE_PROP_NAME,
                context.getCommonProperties().getProperty(BIND_INTERFACE_PROP_NAME, LOCALHOST)));
        final KaaTcpCommandFactory factory = new KaaTcpCommandFactory();
        this.netty = new AbstractNettyServer(configuration.getBindInterface(), configuration.getBindPort()) {

            @Override
            protected ChannelInitializer<SocketChannel> configureInitializer() throws Exception {
                return new AbstractKaaTcpServerInitializer() {
                    @Override
                    protected SimpleChannelInboundHandler<AbstractKaaTcpCommandProcessor> getMainHandler(UUID uuid) {
                        return new TcpHandler(uuid, TcpTransport.this.handler);
                    }

                    @Override
                    protected KaaTcpDecoder getDecoder() {
                        return new KaaTcpDecoder(factory);
                    }
                };
            }
        };
    }

    @Override
    public void start() {
        LOG.info("Initializing netty");
        netty.init();
        LOG.info("Starting netty");
        netty.start();
    }

    @Override
    public void stop() {
        LOG.info("Stopping netty");
        netty.shutdown();
    }

    @Override
    public Class<AvroTcpConfig> getConfigurationClass() {
        return AvroTcpConfig.class;
    }

    @Override
    protected ByteBuffer getSerializedConnectionInfo() {
        byte[] interfaceData = toUTF8Bytes(context.getConfiguration().getBindInterface());
        byte[] publicKeyData = context.getServerKey().getEncoded();
        ByteBuffer buf = ByteBuffer.wrap(new byte[SIZE_OF_INT*3 + interfaceData.length + publicKeyData.length]);
        buf.putInt(publicKeyData.length);
        buf.put(publicKeyData);
        buf.putInt(interfaceData.length);
        buf.put(interfaceData);
        buf.putInt(context.getConfiguration().getBindPort());
        return buf;
    }

    @Override
    protected int getMinSupportedVersion() {
        return SUPPORTED_VERSION;
    }

    @Override
    protected int getMaxSupportedVersion() {
        return SUPPORTED_VERSION;
    }
}
