package com.item.socket.one;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * 使用MINA框架实现socket通信
 * 由于接受消息会阻塞Android线程，所以开在子线程中（同时将其放在Service中，让其在后台运行）
 */
public class MyServiceTwo extends Service {
    private String TAG = "jiejie";
    private IoSession session = null;
    private MinaClientHandler minaClientHandler;
    private NioSocketConnector connector = null;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        minaClientHandler = new MinaClientHandler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //创建连接客户端
                connector = new NioSocketConnector();
                //设置连接超时时间
                connector.setConnectTimeoutMillis(30000);
                //添加过滤器
                connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"), LineDelimiter.WINDOWS.getValue(), LineDelimiter.WINDOWS.getValue())));
                //自定义的编解码器
                //connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new CharsetCodecFactory()));
                //设置接收缓冲区大小
                connector.getSessionConfig().setReadBufferSize(1024);
                //设置处理器
                connector.setHandler(minaClientHandler);
                //设置默认方位地址
                connector.setDefaultRemoteAddress(new InetSocketAddress(ConnectUtils.HOST, ConnectUtils.POST));
                //添加重连监听
                connector.addListener(new IoListener() {
                    @Override
                    public void sessionDestroyed(IoSession ioSession) throws Exception {
                        int count = 0;
                        for (; ; ) {
                            try {
                                count++;
                                ConnectFuture future = connector.connect();
                                future.awaitUninterruptibly();//等待连接创建完成
                                session = future.getSession();//获取session
                                if (session.isConnected()) {
                                    System.out.println("断线了 但是又重连了");
                                    count = 0;
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
                });
                for (; ; ) {
                    try {
                        ConnectFuture future = connector.connect();
                        future.awaitUninterruptibly();
                        System.out.println("socket 连接成功");
                        Log.d(TAG, "socket 连接成功");
                        session = future.getSession();
                        session.write("*AppUserRegister,1,1#");
                        break;
                    } catch (Exception e) {
                        SystemClock.sleep(5000);
                    }
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        connector.dispose();
    }

    private class MinaClientHandler extends IoHandlerAdapter {
        @Override
        public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
            super.exceptionCaught(session, cause);
            Log.d(TAG, "exceptionCaught--------" + cause);
        }

        @Override
        public void messageReceived(IoSession session, Object message) throws Exception {
            super.messageReceived(session, message);
            Log.d(TAG, "messageReceived---------" + message);
        }

        @Override
        public void messageSent(IoSession session, Object message) throws Exception {
            super.messageSent(session, message);
            Log.d(TAG, "messageSent-------" + message);
        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {
            super.sessionClosed(session);
            Log.d(TAG, "sessionClosed-------");
        }

        @Override
        public void sessionCreated(IoSession session) throws Exception {
            super.sessionCreated(session);
            Log.d(TAG, "sessionCreated------" + session);
        }

        @Override
        public void sessionOpened(IoSession session) throws Exception {
            super.sessionOpened(session);
            Log.d(TAG, "sessionOpened---------");
        }
    }

    /**
     * 创建一个监听器实现mina的IoServiceListener接口，里面的方法可以不用实现
     */
    private class IoListener implements IoServiceListener {

        @Override
        public void serviceActivated(IoService ioService) throws Exception {

        }

        @Override
        public void serviceIdle(IoService ioService, IdleStatus idleStatus) throws Exception {

        }

        @Override
        public void serviceDeactivated(IoService ioService) throws Exception {

        }

        @Override
        public void sessionCreated(IoSession ioSession) throws Exception {

        }

        @Override
        public void sessionClosed(IoSession ioSession) throws Exception {

        }

        @Override
        public void sessionDestroyed(IoSession ioSession) throws Exception {

        }
    }
}
