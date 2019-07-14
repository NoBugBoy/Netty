package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {
    public static void main(String[] args) {
        try {
            new NettyServer(8080).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int port;

    public NettyServer(int port) {
        this.port = port;
    }
    public void start() throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        System.out.println("运行端口："+port);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NioWebSocketChannelInit())
            .option(ChannelOption.SO_BACKLOG,128)
            .childOption(ChannelOption.TCP_NODELAY,true)
            .childOption(ChannelOption.SO_KEEPALIVE,true).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10 * 1000);

            ChannelFuture f = serverBootstrap.bind(port).sync();
            f.channel().closeFuture().sync();
        }catch (Exception ex){
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }
}
