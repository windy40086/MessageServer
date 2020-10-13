package com.windy.messageserver.ui.login;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.windy.messageserver.R;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class LoginActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        final TextView ipText = findViewById(R.id.ipText);
        final EditText usernameEditText = findViewById(R.id.username);
        final Button clearButton = findViewById(R.id.clearbutton);
        final Button pasteButton = findViewById(R.id.pasteButton);
        final int PORT = 2333;
        String ADDRESS = null;

        try {
            for (Enumeration<NetworkInterface> enNetI = NetworkInterface
                    .getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
                NetworkInterface netI = enNetI.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = netI
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        ipText.setText("          服务器目前开放在：" + inetAddress.getHostAddress() + ":" + PORT + "\n"
                                + "可使用浏览器访问，如无法显示请更换Edge最新版"
                        );
                        ADDRESS = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ipText.setText("无法获取IP地址");
        }

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usernameEditText.setText("");
            }
        });

        pasteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) LoginActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                // 返回数据
                ClipData clipData = clipboard.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    // 从数据集中获取（粘贴）第一条文本数据
                    usernameEditText.setText(clipData.getItemAt(0).getText().toString());
                }
            }
        });


        ActivityCompat.requestPermissions(LoginActivity.this,
                new String[]{Manifest.permission.INTERNET}, 0);

        final String finalADDRESS = ADDRESS;
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("开启服务");
                ServerSocket server = null;
                Socket socket;
                try {
                    server = new ServerSocket(PORT);
                    System.out.println("启动服务成功" + "port:" + PORT);
                } catch (IOException e) {
                    System.out.println("启动server失败，错误原因：" + e.getMessage());
                }

                try {
                    while (true) {
                        // 进入等待环节
                        System.out.println("等待设备的连接... ... ");
                        assert server != null;
                        socket = server.accept();
                        // 获取手机连接的地址及端口号
                        final String address = socket.getRemoteSocketAddress().toString();
                        System.out.println("连接成功，连接的设备为：" + address);

                        try {
                            StringBuilder value = new StringBuilder("");
                            OutputStream os = socket.getOutputStream();

                            os.write("HTTP/1.1 200 OK\r\n".getBytes());
                            os.write("Content-Type:text/html\r\n".getBytes());
                            os.write("\r\n".getBytes());

                            value.append("<html><head><meta charset=\"UTF-8\"></head><body>");
                            value.append(new String(("这是返回的内容:'" + usernameEditText.getText().toString() + "'<br />").getBytes(), "utf-8"));
//                            value.append("<a href=\"http://" + finalADDRESS + ":" + PORT + "\">刷新</a></body>");
                            value.append("</body>");

                            os.write(value.toString().getBytes());
                            os.close();
                            socket.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}