package server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;


public class NioWebSocketChannelInit extends ChannelInitializer<SocketChannel>{

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        //-----------web-------------
        ch.pipeline().addLast("logging",new LoggingHandler("DEBUG"));
        ch.pipeline().addLast("http-codec",new HttpServerCodec());
        ch.pipeline().addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));
        ch.pipeline().addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
        ch.pipeline().addLast("aggregator",new HttpObjectAggregator(102400));
        ch.pipeline().addLast("http-chunked",new ChunkedWriteHandler());
        ch.pipeline().addLast("handler",new MessageServerHandler());
        //-----------客户端--------------
        //ch.pipeline().addLast("framer", new DelimiterBasedFrameDecoder(1024, Delimiters.lineDelimiter()));
        //ch.pipeline().addLast("web-compress",new WebSocketServerCompressionHandler());
        //ch.pipeline().addLast(new WebSocketServerProtocolHandler("/websocket"));
        //ch.pipeline().addLast("handler",new StringServerHandler());
    }
}
