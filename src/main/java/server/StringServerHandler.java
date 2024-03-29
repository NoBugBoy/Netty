package server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.log4j.Logger;

/**
 * 客户端的Handler
 */
public class StringServerHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger log = Logger.getLogger(MessageServerHandler.class);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Channel channel = ctx.channel();
        ChannelList.getAll().forEach(ch -> {
            if(channel !=ch){
                ch.writeAndFlush(channel.remoteAddress() +" 发送的消息:" +msg+" \n");
            }else{
                ch.writeAndFlush(" 自己"+msg +" \n");
            }
        });
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ChannelStringList.add(ctx.channel());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();

        System.out.println(channel.remoteAddress() +" 上线了");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        System.out.println(channel.remoteAddress() +" 下线了");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
        Channel channel = ctx.channel();
        ChannelStringList.remove(channel);
        ctx.close().sync();
    }
}
