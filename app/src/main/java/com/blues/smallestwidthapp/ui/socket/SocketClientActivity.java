package com.blues.smallestwidthapp.ui.socket;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import com.blues.smallestwidthapp.R;
import com.blues.smallestwidthapp.ui.CodePop;
import com.blues.smallestwidthapp.ui.utils.GlideEngine;
import com.huantansheng.easyphotos.EasyPhotos;
import com.huantansheng.easyphotos.callback.SelectCallback;
import com.huantansheng.easyphotos.constant.Type;
import com.huantansheng.easyphotos.models.album.entity.Photo;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;
import com.yzq.zxinglibrary.encode.CodeCreator;

public class SocketClientActivity extends AppCompatActivity {
    private static final String TAG = "SocketClientActivity";
    private Button sentMessageButton;
    private EditText contentEdit;
    private Socket clientSocket;
    private Handler mHandler;
    private Intent serverIntent;
    private TextView contentShow;
    private Button startServerButton, bindServerButton, bindRemoteServer, showIp, scan, getFile;
    private ConstraintLayout mainLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_socket);
        initView();
    }

    private void initView() {
        contentEdit = findViewById(R.id.message_from_client_edit);
        sentMessageButton = findViewById(R.id.sent_message_button);
        contentShow = findViewById(R.id.chart_content_show);
        startServerButton = findViewById(R.id.start_server);
        bindServerButton = findViewById(R.id.bind_server);
        bindRemoteServer = findViewById(R.id.bind_remote_server);
        showIp = findViewById(R.id.show_ip);
        mainLayout = findViewById(R.id.main_layout);
        scan = findViewById(R.id.scan);
        getFile = findViewById(R.id.get_file);
        setViewListener();
        initRes();
    }

    private void initRes() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 10066);
        mHandler = new Handler(getMainLooper());
        setTitle("Socket聊天室");
    }

    private void setViewListener() {
        sentMessageButton.setOnClickListener((View view) -> setMessage());
        startServerButton.setOnClickListener(view -> startServer());
        bindServerButton.setOnClickListener(view -> connectServer(getIPAddressForNetwork()));
        bindRemoteServer.setOnClickListener(view -> connectServer(contentEdit.getText().toString()));
        showIp.setOnClickListener(view -> {
            String ipStr = getIPAddressForNetwork();
            Bitmap bitmap = CodeCreator.createQRCode(ipStr, 500, 500, null);
            CodePop codePop = new CodePop(SocketClientActivity.this);
            codePop.setCodeText(ipStr);
            codePop.setImageBitmap(bitmap);
            codePop.showPop(mainLayout);
        });
        scan.setOnClickListener(view -> {
            Intent intent = new Intent(this, CaptureActivity.class);
            /*ZxingConfig是配置类
             *可以设置是否显示底部布局，闪光灯，相册，
             * 是否播放提示音  震动
             * 设置扫描框颜色等
             * 也可以不传这个参数
             * */
            ZxingConfig config = new ZxingConfig();
            config.setPlayBeep(true);//是否播放扫描声音 默认为true
            config.setShake(true);//是否震动  默认为true
            config.setDecodeBarCode(true);//是否扫描条形码 默认为true
            config.setReactColor(R.color.purple_200);//设置扫描框四个角的颜色 默认为白色
            config.setFrameLineColor(R.color.purple_500);//设置扫描框边框颜色 默认无色
            config.setScanLineColor(R.color.purple_700);//设置扫描线的颜色 默认白色
            config.setFullScreenScan(false);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
            intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
            startActivityForResult(intent, 10086);
        });
        getFile.setOnClickListener(view -> getFile());
    }

    private void getFile() {
        EasyPhotos.createAlbum(this, true, true, GlideEngine.getInstance())
                .setFileProviderAuthority("com.blues.smallestwidthapp")
                .setCount(1)
                .filter(Type.VIDEO)
                .start(new SelectCallback() {
                    @Override
                    public void onResult(ArrayList<Photo> photos, boolean isOriginal) {
                        if ((photos == null) || (photos.size() == 0)) {
                            return;
                        }
                        Photo photo = photos.get(0);
                        Log.d(TAG, "photo = " + photo.toString());
                        uploadFile(photo);
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    private void uploadFile(Photo photo) {
        new Thread(() -> uploadFileNew(photo)).start();
    }

    private void uploadFileNew(Photo photo) {
        if (clientSocket == null) {
            return;
        }
        InputStream inputStream = null;
        OutputStream outputStream = null;
        File uploadFile = new File(photo.path);
        Log.d(TAG, "uploadFile = " + uploadFile.getName() + " - n = " + photo.name + " - \n" + photo.path);
        long fileLength = uploadFile.length();
        long total = 0;
        int len;
        byte[] bytes = new byte[1024];
        try {
            inputStream = new FileInputStream(photo.path);
            outputStream = clientSocket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeUTF(uploadFile.getName());
            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                total += len;
                int progress = (int) ((total * 100) / fileLength);
                Log.d(TAG, "fileLength = " + fileLength + " - total = " + total + " - progress = " + progress + " - len = " + len + " - n = " + photo.name);
            }

        } catch (Exception e) {
            e.printStackTrace();
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 10086) && (resultCode == RESULT_OK)) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                Log.d(TAG, "content = " + content);
                connectServer(content);
            }
        }
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            service = ((ServerSocketService.MyServiceBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private ServerSocketService service;


    private void startServer() {
        serverIntent = new Intent(this, ServerSocketService.class);
        bindService(serverIntent, connection, Context.BIND_AUTO_CREATE);
        setTitle("Socket聊天室(" + getIPAddressForNetwork() + "/6688)");
    }

    private void stopServer() {
        try {
            stopService(serverIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connectServer(String ip) {
        new Thread(() -> {
            connect(ip);
        }).start();
    }

    private PrintStream ps = null;

    private void connect(String ip) {
        Log.d(TAG, "connect ip = " + ip);
        Socket socket = null;
        while (socket == null) {
            try {
                socket = new Socket(ip, 6688);
                clientSocket = socket;
                ps = new PrintStream(socket.getOutputStream());
                mHandler.post(() -> {
                    setTitle("Socket聊天室(" + getIPAddressForNetwork() + "/6688)");
                });
                new Thread(() -> {
                    listenerServer(clientSocket);
                }).start();
            } catch (IOException e) {
                SystemClock.sleep(1000);
                Log.d(TAG, "connect fail = " + e.getMessage());
                mHandler.post(() -> {
                    setTitle("Socket聊天室(连接失败)");
                });
            }
        }
    }

    String content = null;

    private void listenerServer(Socket socket) {
        if (socket != null) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                content = null;
                while ((content = br.readLine()) != null) {
                    Log.d(TAG, "content=" + content);
                    mHandler.post(() -> {
                        getMessageFromServer(content);
                    });
                }
            } catch (IOException i) {
                Log.d(TAG, "listener server fail");
            }
        }
    }

    private void getMessageFromServer(String msg) {
        if (msg.equals(contentEdit.getText().toString())) {
            contentShow.setText(contentShow.getText().toString() + "\n客户端:" + msg);
            contentEdit.setText("");
        } else {
            contentShow.setText(contentShow.getText().toString() + "\n服务器:" + msg);
        }
    }

    private void setMessage() {
        String msg = contentEdit.getText().toString();
        Log.d(TAG, "setMessage = " + msg + " - " + (service == null));
        if (service != null) {
            new Thread(() -> service.sendMsg(msg)).start();
        }
//        if (ps != null) {
//            if (!contentEdit.getText().toString().isEmpty()) {
//                new Thread(() -> {
//                    ps.println(contentEdit.getText().toString());
//                }).start();
//            } else {
//                Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
//            }
//        }
    }

    public static String getIPAddressForNetwork() {
        try {
            List<String> ipv4 = new ArrayList<>();
            Enumeration<NetworkInterface> enum1 = NetworkInterface.getNetworkInterfaces();
            if (enum1 != null) {
                List<NetworkInterface> nilist = Collections.list(enum1);
                for (NetworkInterface ni : nilist) {
                    List<InetAddress> ialist = Collections.list(ni.getInetAddresses());
                    Log.d(TAG, "address=" + ialist.size());
                    for (InetAddress address : ialist) {
                        if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                            ipv4.add(address.getHostAddress());
                            Log.d(TAG, "IPv4=" + ipv4);
                        }
                    }
                }
            }
            if (ipv4.size() > 0) return ipv4.get(ipv4.size() - 1);
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (clientSocket != null) clientSocket.close();
        } catch (IOException i) {
        }
        stopServer();
    }
}
