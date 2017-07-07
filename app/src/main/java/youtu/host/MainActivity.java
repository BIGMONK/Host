package youtu.host;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import io.netty.channel.ChannelHandlerContext;
import youtu.mylibrary.DeviceValue;
import youtu.mylibrary.LogUtils;
import youtu.mylibrary.TypeData;

public class MainActivity extends AppCompatActivity implements SerialServer.ChannelChangeListener {
    private String TAG = this.getClass().getSimpleName();
    private String mHostIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHostIP = NetworkUtils.getIp(this);
        LogUtils.d(TAG, "onCreate  mHostIP=" + mHostIP);

        CountDownTimer countDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                sendIPToClient(mHostIP);
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "CountDownTimer onFinish ");
                this.start();

            }
        };
        countDownTimer.start();

//
//
//        SerialServer  mSerialServer = new SerialServer();
//        mSerialServer.setChannelChangeListener(this);
//        mSerialServer.startServer();


    }

    private void sendIPToClient(final String mHostIP) {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                String host = "255.255.255.255";
                String host = "192.168.1.121";
                int port = 9527;
                try {
                    InetAddress adds = InetAddress.getByName(host);
                    DatagramSocket ds = new DatagramSocket();
                    DatagramPacket dp = new DatagramPacket(mHostIP.getBytes(),
                            mHostIP.length(), adds, port);

                    LogUtils.d(TAG, "sendIPToClient:" + mHostIP+"  getSocketAddress"+dp.getSocketAddress());
                    ds.send(dp);
                    ds.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
    private Set<ChannelHandlerContext> mChannelHandlers= new HashSet<>();

    @Override
    public void onChannelChangeListener(ChannelHandlerContext ch, int seatId, int speed, int
            angle) {
        mChannelHandlers.add(ch);

        if(mChannelHandlers != null && mChannelHandlers.size() > 0){
            Iterator<ChannelHandlerContext> iterator = mChannelHandlers.iterator();
            while (iterator.hasNext()){
                sendMessageToClient(iterator.next(),123);
            }
        }
    }
    private void sendMessageToClient(ChannelHandlerContext ch, int currentResistant) {
        DeviceValue s = new DeviceValue();
        s.setType(TypeData.CUSTOME);
        s.setSpeed(currentResistant);
        s.setAngle(15);
        s.setSeatId(TypeData.SERVER_RESISTANT);
        ch.channel().writeAndFlush(s);
    }
}
