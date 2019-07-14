package server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.apache.log4j.Logger;

import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * web端的Handler
 */
public class MessageServerHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger log = Logger.getLogger(MessageServerHandler.class);
    private WebSocketServerHandshaker handshaker;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof FullHttpRequest){
            log.info("http握手:"+msg);
            //第一次请求进来 http握手
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        }else if(msg instanceof WebSocketFrame){
            if(msg instanceof TextWebSocketFrame){
                textWebSocketFrame(ctx,(TextWebSocketFrame)msg);
            }else{
                handlerWebSocketFrame(ctx, (WebSocketFrame) msg);
            }
        }else if(msg instanceof BinaryWebSocketFrame){
            log.info("二进制websocket msg :"+msg);
            handlerBinaryWebSocketFrame(ctx, (BinaryWebSocketFrame) msg);
        }else{
            log.info("unknow msg ...");
        }
    }
    public void sendList(){
        StringBuilder stringBuilder = new StringBuilder("list,");
        for (Channel channel : ChannelList.getAll()) {
            stringBuilder.append(channel.remoteAddress().toString()+"_"+channel.id().asShortText());
            stringBuilder.append(",");
        }
        ChannelList.sendAll(new TextWebSocketFrame(stringBuilder.toString()));
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        String s = socketAddress.toString();
        log.info("客户端加入连接："+ctx.channel().id()+"ip:"+s);
        ChannelList.add(ctx.channel());
        ChannelList.sendAll(new TextWebSocketFrame("全网公布： 欢迎"+ctx.channel().remoteAddress().toString()+"加入!\r\n当前在线人数："+ChannelList.size()));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端断开连接："+ctx.channel().id());
        ChannelList.sendAll(new TextWebSocketFrame("全网公布： "+ctx.channel().remoteAddress().toString()+"断开了连接！"));
        ChannelList.remove(ctx.channel());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        sendList();
        log.info("读取完毕");
        ctx.flush();

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        log.info("异常:"+cause);
    }

    private void handlerBinaryWebSocketFrame(ChannelHandlerContext ctx, BinaryWebSocketFrame msg){
        log.info("服务器接收到二进制消息.");
        ByteBuf content = msg.content();
        content.markReaderIndex();
        content.resetReaderIndex();

        ByteBuf byteBuf = Unpooled.directBuffer(msg.content().capacity());
        byteBuf.writeBytes(msg.content());

        ctx.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
    }
    public void textWebSocketFrame(ChannelHandlerContext ctx,TextWebSocketFrame frame){
        String text =  frame.text();
        log.info("服务器收到消息为:"+text);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

        if(text.indexOf("_")>0 && text.split("_").length>0){
            ChannelList.getAll().forEach(channel -> {
                if(channel == ctx.channel()){
                    TextWebSocketFrame mytws = new TextWebSocketFrame(simpleDateFormat.format(new Date())+"   "+"自己"+" : " + text.split("_")[1]);
                    channel.writeAndFlush(mytws);
                }else{
                    if(channel.id().asShortText().equals(text.split("_")[0])){
                        TextWebSocketFrame tws = new TextWebSocketFrame(simpleDateFormat.format(new Date())+"   "+ctx.channel().remoteAddress().toString()+" : " + text.split("_")[1]);
                        channel.writeAndFlush(tws);
                    }

                }
            });
//                if(text.split("_")[0].equals(ctx.channel().id().asShortText())){
//                    TextWebSocketFrame tws = new TextWebSocketFrame(simpleDateFormat.format(new Date())+"   "+"自己"+" : " + text.split("_")[1]);
//                    ChannelList.sendToId(tws,text.split("_")[0]);
//                }else{
//                    TextWebSocketFrame tws = new TextWebSocketFrame(simpleDateFormat.format(new Date())+"   "+ctx.channel().remoteAddress().toString()+" : " + text.split("_")[1]);
//                    ChannelList.sendToId(tws,text.split("_")[0]);
//
//                }
        }else{
            if("myip".equals(text)){
                TextWebSocketFrame tws = new TextWebSocketFrame("myip_"+ctx.channel().remoteAddress().toString());
                ctx.writeAndFlush(tws);
            }else{
                ChannelList.getAll().forEach(channel -> {
                    if(channel == ctx.channel()){
                        TextWebSocketFrame mytws = new TextWebSocketFrame(simpleDateFormat.format(new Date())+"   "+"自己"+" : " + text);
                        channel.writeAndFlush(mytws);
                    }else{
                        TextWebSocketFrame tws = new TextWebSocketFrame(simpleDateFormat.format(new Date())+"   "+ctx.channel().remoteAddress().toString()+" : " + text);
                        channel.writeAndFlush(tws);
                    }
                });
//                    String msg = simpleDateFormat.format(new Date())+"   "+ctx.channel().remoteAddress().toString()+" : " + text;
//                    ChannelList.sendAllButMe(msg,ctx.channel().id().asShortText());
//                    TextWebSocketFrame tt = new TextWebSocketFrame(simpleDateFormat.format(new Date())+"   "+"自己"+" : " + text);
//                    ChannelList.sendToId(tt,ctx.channel().id().asShortText());
            }
        }}
    private void handlerWebSocketFrame(ChannelHandlerContext ctx,WebSocketFrame frame){

        if(frame instanceof CloseWebSocketFrame){
            log.info("服务器收到CloseWebSocketFrame......");
            //关闭
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if(frame instanceof PingWebSocketFrame) {
            log.info("服务器收到PingWebSocketFrame......");
            //ping消息
            ctx.channel().write(new PongWebSocketFrame(frame.content()).retain());
            return;
        }

    }
    private void handleHttpRequest(ChannelHandlerContext ctx,FullHttpRequest req){
        if(!req.decoderResult().isSuccess() || !"websocket".equals(req.headers().get("Upgrade"))){
            sendHttpResponse(ctx,req,new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        WebSocketServerHandshakerFactory wsf = new WebSocketServerHandshakerFactory("ws://localhost:8080/websocket",null,true,10485760);
        handshaker = wsf.newHandshaker(req);
        if(handshaker == null){
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        }else{
            handshaker.handshake(ctx.channel(),req);
        }
    }
    private static void sendHttpResponse(ChannelHandlerContext chx, FullHttpRequest req, DefaultFullHttpResponse res){
        if(res.status().code() != 200){
            log.info("######发现非法连接#######");
            HttpMethod method = req.method();
            String uri = req.uri();
            log.info("######发现非法连接方法名："+method.name());
            log.info("######发现非法连接URI名："+uri);
            HttpHeaders headers = req.headers();
            headers.forEach(x -> {
                if(x!=null && x.getKey()!=null && x.getValue()!=null){
                    log.info("KEY:"+x.getKey()+"VALUE:"+x.getValue());
                }
            });

            ByteBuf byteBuf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(byteBuf);
            byteBuf.release();
        }
        ChannelFuture channelFuture = chx.channel().writeAndFlush(res);
        if(!HttpUtil.isKeepAlive(req)){
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
