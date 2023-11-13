package com.blues.smallestwidthapp.ui.socket;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;

public class ServerSocketService extends Service {
    private static final String TAG = "ServerSocketService";
    private boolean setWhile = true;
    private ServerSocket weChartServer;
    private Handler mHandler;
    private static List<Socket> socketList = Collections.synchronizedList(new ArrayList<>());
    private final MyServiceBinder binder = new MyServiceBinder();

    public class MyServiceBinder extends Binder {
        public ServerSocketService getService() {
            return ServerSocketService.this;
        }
    }

    public void sendMsg(String msg) {
        Log.d(TAG, "msg = " + msg + " - " + socketList.size());
        try {
            for (Socket socket : socketList) {
                PrintStream printStream = new PrintStream(socket.getOutputStream());
                printStream.println(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onCreate");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        mHandler = new Handler(getMainLooper());
        new Thread(ServerSocketRunnable).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setWhile = false;
        try {
            for (Socket socket : socketList) socket.close();
            if (weChartServer == null) {
                return;
            }
            weChartServer.close();
        } catch (IOException i) {
        }
        mHandler.post(() -> {
            Toast.makeText(ServerSocketService.this, "聊天室已关闭", Toast.LENGTH_SHORT).show();
        });
    }

    private Runnable ServerSocketRunnable = () -> {
        try {
            weChartServer = new ServerSocket(6688);
            mHandler.post(() -> {
                Toast.makeText(ServerSocketService.this, "聊天室已启动", Toast.LENGTH_SHORT).show();
            });
        } catch (IOException e) {
            Log.d(TAG, "build service fail = " + e.getMessage());
            mHandler.post(() -> {
                Toast.makeText(ServerSocketService.this, "聊天室启动失败", Toast.LENGTH_SHORT).show();
            });
            return;
        }
        dealWithClient();

    };

    private void dealWithClient() {
        while (setWhile) {
            try {
                final Socket socket = weChartServer.accept();
                socketList.add(socket);
                Log.d(TAG, "addSocket");
                new Thread(new SocketRunnable(socket)).start();
            } catch (IOException i) {
                Log.d(TAG, "IOException");
            }
        }
    }

    private class SocketRunnable implements Runnable {
        private Socket runnableSocket = null;
        private BufferedReader in;

        public SocketRunnable(Socket socket) {
            runnableSocket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException i) {
                Log.d(TAG, "read fail");
            }
        }

        @Override
        public void run() {
            String content = null;
            try {
//                Log.d(TAG, "line= try");
//                while ((content = readFromClient()) != null) {
//                    Log.d(TAG, "line=" + content);
//                    for (Socket socket : socketList) {
//                        PrintStream printStream = new PrintStream(socket.getOutputStream());
//                        printStream.println(content);
//                    }
//                }
//                Log.d(TAG, "line= finish");

                DataInputStream dataInputStream = new DataInputStream(runnableSocket.getInputStream());
                String fileName = dataInputStream.readUTF();
                Log.d(TAG, "fileName = " + fileName);
                File file = new File(Environment.getExternalStorageDirectory() + "/" + fileName);
                FileOutputStream outputStream = new FileOutputStream(file);
                InputStream inputStream = runnableSocket.getInputStream();
                byte[] bytes = new byte[1024];
                long total = 0;
                int len;
                while ((len = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, len);
                    total += len;
                    Log.d(TAG, "download total = " + total + " - len = " + len);
                }
                outputStream.close();
                inputStream.close();
            } catch (IOException i) {
                Log.d(TAG, "i = " + i.getMessage());
            }
        }

        private String readFromClient() {
            try {
                return in.readLine();
            } catch (IOException i) {
                socketList.remove(runnableSocket);
            }
            return null;
        }
    }
}
