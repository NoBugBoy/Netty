package server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ChannelStringList {
    private static ChannelGroup globalGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static Map<String,ChannelId> channelIdMap= new ConcurrentHashMap<>();
    public  static void add(Channel channel){
        globalGroup.add(channel);
        channelIdMap.put(channel.id().asShortText(),channel.id());
    }
    public static void remove(Channel channel){
        globalGroup.remove(channel);
        channelIdMap.remove(channel.id().asShortText());
    }
    public static Channel getChannel(String id){
        return globalGroup.find(channelIdMap.get(id));
    }
    public static void sendAll(String tws){
        globalGroup.writeAndFlush(tws);
    }
    public static void sendToId(String tws, String id){
        for (Channel channel : globalGroup) {
            String s = channel.id().asShortText();
            if(s.equals(id)){
                channel.writeAndFlush(tws);
                break;
            }
        }
    }
    public static int size(){
        return globalGroup.size();
    }
    public static List<Channel> getAll(){
        List<Channel> collect = globalGroup.stream().collect(Collectors.toList());
        return collect;
    }

    public static void sendAllButMe(String tws,String id) {
        channelIdMap.forEach((x,y) -> {
            if(!x.equals(id)){
                globalGroup.find(y).writeAndFlush(new TextWebSocketFrame(tws));
            }
        });
    }
}
