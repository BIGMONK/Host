package youtu.client;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import youtu.mylibrary.CustomHeartbeatHandler;
import youtu.mylibrary.DeviceValue;
import youtu.mylibrary.TypeData;

/**
 * Description:
 * Author:Giousa
 * Date:2017/2/9
 * Email:65489469@qq.com
 */
public class ClientHandler extends CustomHeartbeatHandler {
    private Client client;
    public ClientHandler(Client client) {
        super("client");
        this.client = client;
    }

    public interface ChannelValueChangeListener{
        void onChannelValueChangeListener(int resistance);
    }

    private ChannelValueChangeListener mChannelValueChangeListener;

    public void setChannelValueChangeListener(ChannelValueChangeListener channelValueChangeListener) {
        mChannelValueChangeListener = channelValueChangeListener;
    }

    @Override
    protected void handleData(ChannelHandlerContext channelHandlerContext, Object msg) {
        System.out.println(name+"  handleData:"+msg);
        List<DeviceValue> deviceValues = (List<DeviceValue>) msg;
        int resistance = Integer.parseInt(String.valueOf(deviceValues.get(3)));
        System.out.println("resistance = "+resistance);
        int seat = Integer.parseInt(String.valueOf(deviceValues.get(2)));
        System.out.println("seat = "+seat);
        if(seat == TypeData.SERVER_RESISTANT){
            if(mChannelValueChangeListener != null){
                mChannelValueChangeListener.onChannelValueChangeListener(resistance);
            }
        }
    }

    @Override
    protected void handleAllIdle(ChannelHandlerContext ctx) {
        super.handleAllIdle(ctx);
        sendPingMsg(ctx);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        client.doConnect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(name + " exception"+cause.toString());

    }
}