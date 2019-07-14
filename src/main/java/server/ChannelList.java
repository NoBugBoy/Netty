package server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 用于web端
 */
public class ChannelList {
    private static ChannelGroup globalGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 添加channel
     * @param channel
     */
    public  static void add(Channel channel){
        globalGroup.add(channel);
    }
    /**
     * 移除channel
     * @param channel
     */
    public static void remove(Channel channel){
        globalGroup.remove(channel);
    }

    /**
     * 推给所有已建立连接的channel
     * @param tws
     */
    public static void sendAll(WebSocketFrame tws){
        globalGroup.writeAndFlush(tws);
    }
    /**
     * 推给某个ID
     * @param tws
     */
    public static void sendToId(TextWebSocketFrame tws,String id){
        for (Channel channel : globalGroup) {
            String s = channel.id().asShortText();
            if(s.equals(id)){
                channel.writeAndFlush(tws);
                break;
            }
        }
    }

    /**
     * 当前在线人数
     * @return
     */
    public static int size(){
        return globalGroup.size();
    }

    /**
     * 获得所有channel
     * @return
     */
    public static List<Channel>  getAll(){
        List<Channel> collect = globalGroup.stream().collect(Collectors.toList());
        return collect;
    }
}
