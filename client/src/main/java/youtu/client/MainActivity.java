package youtu.client;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import youtu.mylibrary.LogUtils;

public class MainActivity extends AppCompatActivity implements Client.ChannelChangeListener {

    private String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogUtils.d(TAG, "onCreate");

//        Client mClient = new Client();
//        mClient.start();
//        mClient.setChannelChangeListener(this);
        achieveHostIP();

    }

    @Override
    public void onChannelChangeListener(int resistance) {
        LogUtils.d(TAG, "TSerivice resistance=" + resistance);
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
                            if (!TextUtils.isEmpty(mConfigServerIP)) {
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

    private final static String SP_NAME = "CONFIG_IP";

}
