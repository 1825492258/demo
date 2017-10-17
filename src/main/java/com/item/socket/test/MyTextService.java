package com.item.socket.test;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.item.socket.one.ConnectUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * 服务
 * Created by Administrator on 2017/10/17.
 */

public class MyTextService extends Service {
    private Socket socket;
    private BufferedReader in = null;
    private BufferedWriter out = null;
    private boolean close = true;
    private Thread readThread;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onHello(Msg message) {
        if (message.getI() == 2) {
            if (socket.isConnected() && out != null) {
                try {
                    out.write(message.getMsg());
                    out.flush();
                } catch (IOException e) {
                    Log.d("jiejie", "ddddddddddddddddd" + e);
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        close = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                for (; ; ) {
                    try {
                        count++;
                        SocketAddress socketAddress = new InetSocketAddress(ConnectUtils.HOST, ConnectUtils.POST);
                        socket = new Socket();
                        socket.connect(socketAddress, 10000);
                        if (socket.isConnected()) {
                            System.out.println("socket 连接成功");
                            Log.d("jiejie", "socket 连接成功");
                            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                            EventBus.getDefault().post("Socket 连接成功");
//                            //发送一个消息
//                            out.write("*AppUserRegister,1,1#");
//                            out.flush();
                            //开启一个线程来读取服务器发来的消息
                            readThread.start();
                            break;
                        }
                    } catch (Exception e) {
                        if (count > ConnectUtils.IDLE_TIME) {
                            break;
                        } else {
                            SystemClock.sleep(5000);
                        }
                    }
                }
            }
        }).start();
        readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (close) {
                    String readtext = readText(socket);
                    if (readtext != null) {
                        Log.d("jiejie", "服务器发来的消息：" + readtext);
                        EventBus.getDefault().post(readtext);
                    }
                }
            }
        });
    }

    private String readText(Socket socket) {
        String string = null;
        if (socket.isConnected()) {
            try {
                char[] buffer = new char[8000];
                int count = 0;
                if ((count = in.read(buffer)) > 0) {
                    char[] temp = new char[count];
                    for (int i = 0; i < count; i++) {
                        temp[i] = buffer[i];
                    }
                    string = new String(temp);
                }
            } catch (Exception e) {

                string = null;
            }
        }
        return string;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        close = false;
        if (socket != null) {
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        EventBus.getDefault().unregister(this);
    }
}
