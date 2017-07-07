package youtu.host;

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
public class ServerHandler extends CustomHeartbeatHandler {

    public ServerHandler() {
        super("server");
    }

    public interface ChannelValueChangeListener{
        void onChannelValueChangeListener(ChannelHandlerContext ch, int seatId, int speed, int angle);
    }

    private ChannelValueChangeListener mChannelValueChangeListener;

    public void setChannelValueChangeListener(ChannelValueChangeListener channelValueChangeListener) {
        mChannelValueChangeListener = channelValueChangeListener;
    }

    @Override
    protected void handleData(ChannelHandlerContext channelHandlerContext, Object msg) {
        System.out.println(name+"  handleData:"+msg);

        List<DeviceValue> deviceValues = (List<DeviceValue>) msg;

        int angle =  Integer.parseInt(String.valueOf(deviceValues.get(0)));
        System.out.println("angle = "+angle);
        int seat = Integer.parseInt(String.valueOf(deviceValues.get(1)));
        System.out.println("seat = "+seat);
        int speed = Integer.parseInt(String.valueOf(deviceValues.get(2)));
        System.out.println("speed = "+speed);
        int type = Integer.parseInt(String.valueOf(deviceValues.get(3)));
        System.out.println("type = "+type);

        if(mChannelValueChangeListener != null){
            mChannelValueChangeListener.onChannelValueChangeListener(channelHandlerContext,seat,speed,angle);
        }


        DeviceValue s = new DeviceValue();
        s.setType(TypeData.CUSTOME);
        s.setSpeed(0);
        s.setAngle(15);
        s.setSeatId(TypeData.SERVER_RESPONSE);
        channelHandlerContext.writeAndFlush(s);
    }


    @Override
    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        super.handleReaderIdle(ctx);
        System.err.println("---client " + ctx.channel().remoteAddress().toString() + " reader timeout, close it---");
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(name+" exception"+cause.toString());
    }
}