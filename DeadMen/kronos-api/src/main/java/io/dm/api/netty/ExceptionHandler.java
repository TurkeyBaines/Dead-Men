package io.dm.api.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.dm.api.utils.ServerWrapper;

public class ExceptionHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if(!cause.getStackTrace()[0].getMethodName().equals("read0"))
            ServerWrapper.logError("Netty issue", cause);
        ctx.close();
    }

}