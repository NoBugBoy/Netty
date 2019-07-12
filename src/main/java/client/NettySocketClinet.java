package client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class NettySocketClinet {
    public static void main(String[] args) {
        SocketClient socketClient = new SocketClient();
        socketClient.run();
    }
    public static class SocketClient{
        private static Channel client;
        public  void run(){
            EventLoopGroup workGroup=new NioEventLoopGroup();
            try {
                Bootstrap bootstrap=new Bootstrap();
                bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
                bootstrap.group(workGroup)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ChannelPipeline pipeline = ch.pipeline();
                                pipeline.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));
                                pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
                                pipeline.addLast("handler",new MessageHandler());
                            }
                        });
                client = bootstrap.connect("localhost",8089).sync().channel();


            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {

            }
        }
        public static void sendString(String str){
            client.writeAndFlush(str+"\r\n");
        }

    }
}
