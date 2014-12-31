package org.kaaproject.kaa.server.operations.service.netty;

import io.netty.channel.ChannelHandlerContext;

import org.kaaproject.kaa.server.operations.service.akka.messages.io.ChannelContext;

public class NettyChannelContext implements ChannelContext{
    private final ChannelHandlerContext ctx;

    public NettyChannelContext(ChannelHandlerContext ctx) {
        super();
        this.ctx = ctx;
    }

    @Override
    public void writeAndFlush(Object msg) {
        ctx.writeAndFlush(msg);
    }

    @Override
    public void fireExceptionCaught(Exception e) {
        ctx.fireExceptionCaught(e);
    }

    @Override
    public void write(Object msg) {
        ctx.write(msg);
    }

    @Override
    public void flush() {
        ctx.flush();
    }
}