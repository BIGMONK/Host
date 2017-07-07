package youtu.host;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import youtu.mylibrary.MsgPackDecode;
import youtu.mylibrary.MsgPackEncode;

/**
 * Description:
 * Author:Giousa
 * Date:2017/2/10
 * Email:65489469@qq.com
 */
public class SerialServer implements ServerHandler.ChannelValueChangeListener {

    private ChannelChangeListener mChannelChangeListener;
    private ServerHandler mServerHandler;

    public SerialServer() {

    }

    @Override
    public void onChannelValueChangeListener(ChannelHandlerContext ch, int seatId, int speed, int angle) {
        System.out.println("SerialServer onChannelValueChangeListener");
        if(mChannelChangeListener != null){
            mChannelChangeListener.onChannelChangeListener(ch,seatId,speed,angle);
        }
    }

    public interface ChannelChangeListener{
        void onChannelChangeListener(ChannelHandlerContext ch, int seatId, int speed, int angle);
    }

    public void setChannelChangeListener(ChannelChangeListener channelChangeListener) {
        mChannelChangeListener = channelChangeListener;
    }

    public void startServer() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
                NioEventLoopGroup workGroup = new NioEventLoopGroup(4);
                try {
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap
                            .group(bossGroup, workGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                protected void initChannel(SocketChannel socketChannel) throws Exception {
                                    ChannelPipeline p = socketChannel.pipeline();
                                    mServerHandler = new ServerHandler();
                                    p.addLast(new IdleStateHandler(10, 0, 0));
                                    p.addLast(new MsgPackDecode());
                                    p.addLast(new MsgPackEncode());
                                    p.addLast(mServerHandler);
                                    mServerHandler.setChannelValueChangeListener(new ServerHandler.ChannelValueChangeListener() {
                                        @Override
                                        public void onChannelValueChangeListener(ChannelHandlerContext ch, int seatId, int speed, int angle) {
                                            System.out.println("initChannel onChannelValueChangeListener");
                                            if(mChannelChangeListener != null){
                                                mChannelChangeListener.onChannelChangeListener(ch,seatId,speed,angle);
                                            }
                                        }
                                    });
                                }
                            });

                    Channel ch = bootstrap.bind(12345).sync().channel();
                    System.out.println("------Server Start------");
                    ch.closeFuture().sync();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    bossGroup.shutdownGracefully();
                    workGroup.shutdownGracefully();
                }
            }
        }).start();

    }

}
