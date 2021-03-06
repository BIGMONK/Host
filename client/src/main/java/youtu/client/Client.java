package youtu.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import youtu.mylibrary.DeviceValue;
import youtu.mylibrary.LogUtils;
import youtu.mylibrary.MsgPackDecode;
import youtu.mylibrary.MsgPackEncode;

/**
 * Description:
 * Author:Giousa
 * Date:2017/2/9
 * Email:65489469@qq.com
 */
public class Client {
    private String TAG = this.getClass().getSimpleName();
    private NioEventLoopGroup workGroup = new NioEventLoopGroup(4);
    private Channel channel;
    private Bootstrap bootstrap;
    private String hostIp = "127.0.0.1";
    private final static String SP_NAME = "CONFIG_IP";
    private ChannelChangeListener mChannelChangeListener;

    public Client() {

    }

    public interface ChannelChangeListener {
        void onChannelChangeListener(int resistance);
    }

    public void setChannelChangeListener(ChannelChangeListener channelChangeListener) {
        mChannelChangeListener = channelChangeListener;
    }

    public void sendData(DeviceValue deviceValue) throws Exception {
        if (channel != null && channel.isActive()) {

            channel.writeAndFlush(deviceValue);
        }
    }

    public void start() {

        //TODO 在设备上运行时，打开
        String string = SharedPreferencesUtil.getString(UIUtils.getContext(), SP_NAME, "");
        if (string != null && !string.equals("")) {
            hostIp = string;
        }

        try {
            bootstrap = new Bootstrap();
            bootstrap
                    .group(workGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline p = socketChannel.pipeline();
                            ClientHandler clientHandler = new ClientHandler(Client.this);
                            p.addLast(new IdleStateHandler(0, 0, 5));
                            p.addLast(new MsgPackDecode());
                            p.addLast(new MsgPackEncode());
                            p.addLast(clientHandler);
                            clientHandler.setChannelValueChangeListener(new ClientHandler
                                    .ChannelValueChangeListener() {
                                @Override
                                public void onChannelValueChangeListener(int resistance) {
                                    LogUtils.d(TAG, "client resistance = " + resistance);
                                    if (mChannelChangeListener != null) {
                                        mChannelChangeListener.onChannelChangeListener(resistance);
                                    }
                                }
                            });
                        }
                    });
            doConnect();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void doConnect() {
        if (channel != null && channel.isActive()) {
            return;
        }

        LogUtils.d(TAG, "here  hostIp=" + hostIp);
        ChannelFuture future = bootstrap.connect(hostIp, 12345);
//        ChannelFuture future = bootstrap.connect("127.0.0.1", 12345);
//        ChannelFuture future = bootstrap.connect("192.168.0.109", 12345);

        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture futureListener) throws Exception {
                if (futureListener.isSuccess()) {
                    channel = futureListener.channel();
                    LogUtils.d(TAG, "Connect to server successfully!");
                } else {
                    LogUtils.d(TAG, "Failed to connect to server, try connect after 10s");
                    futureListener.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            doConnect();
                        }
                    }, 2, TimeUnit.SECONDS);
                }
            }
        });
    }

    private void achieveHostIP() {
        LogUtils.d(TAG, "achieveHostIP");
        new Thread(new Runnable() {
            @Override
            public void run() {
                int port = 9527;
                DatagramSocket ds = null;
                DatagramPacket dp = null;
                byte[] buf = new byte[1024];
                StringBuffer sbuf = new StringBuffer();
                try {
                    ds = new DatagramSocket(port);
                    dp = new DatagramPacket(buf, buf.length);
                    LogUtils.d(TAG, "监听广播端口打开：");

                    while (true) {
                        ds.receive(dp);
                        int i;
                        for (i = 0; i < 1024; i++) {
                            if (buf[i] == 0) {
                                break;
                            }
                            sbuf.append((char) buf[i]);
                        }

                        if (sbuf != null) {
                            String mConfigServerIP = sbuf.toString();
                            LogUtils.d(TAG, "收到广播: " + mConfigServerIP);
                            hostIp = mConfigServerIP;
                            if (!mConfigServerIP.equals(hostIp) && !mConfigServerIP.isEmpty()) {
                                SharedPreferencesUtil.saveString(UIUtils.getContext(), SP_NAME,
                                        mConfigServerIP);

                            }
                        }
                    }

                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    ds.close();
                }
            }
        }).start();
    }

}
