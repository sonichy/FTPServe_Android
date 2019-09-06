package com.hty.ftpserve;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    private EditText editText_ip, editText_port, editText_username, editText_password;
    private TextView textView_info;
    private Button button_start;
    boolean isStop = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText_ip = (EditText) findViewById(R.id.editText_ip);
        editText_port = (EditText) findViewById(R.id.editText_port);
        editText_username = (EditText) findViewById(R.id.editText_username);
        editText_password = (EditText) findViewById(R.id.editText_password);
        button_start = (Button) findViewById(R.id.button_start);
        button_start.setOnClickListener(new ClickListener());
        textView_info = (TextView) findViewById(R.id.textView_info);

        String ip = getIp();
        if(TextUtils.isEmpty(ip)){
            textView_info.setText("获取不到IP，请连接网络");
        }else{
            editText_ip.setText(ip);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, FtpService.class));
    }

    public String getIp(){
        /*
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            //wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        return ip;
        */

        //https://blog.csdn.net/u011068702/article/details/77870152
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("get IpAddress fail", ex.toString());
            Log.e(Thread.currentThread().getStackTrace()[2] + "get IpAddress fail", ex.toString());
            return "";
        }
        return "";
    }

    private String intToIp(int i) {
        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }

    class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.button_start:
                    if(isStop){
                        String ip = getIp();
                        if(TextUtils.isEmpty(ip)){
                            textView_info.setText("获取不到IP，请连接网络");
                        }else {
                            editText_ip.setText(ip);
                            Intent intent = new Intent(MainActivity.this, FtpService.class);
                            intent.putExtra("ip", editText_ip.getText().toString());
                            intent.putExtra("port", Integer.parseInt(editText_port.getText().toString()));
                            intent.putExtra("username", editText_username.getText().toString());
                            intent.putExtra("password", editText_password.getText().toString());
                            startService(intent);
                            isStop = false;
                            //editText_ip.setEnabled(false);
                            editText_port.setEnabled(false);
                            editText_username.setEnabled(false);
                            editText_password.setEnabled(false);
                            button_start.setText("停止服务");
                            String str = "在浏览器上输入网址访问FTP服务\n" +
                                    "ftp://" + editText_ip.getText().toString() + ":" + editText_port.getText().toString() + "\n" +
                                    "用户名：" + editText_username.getText().toString() + "\n" +
                                    "密码：" + editText_password.getText().toString();
                            textView_info.setText(str);
                        }
                    }else{
                        stopService(new Intent(MainActivity.this, FtpService.class));
                        isStop = true;
                        //editText_ip.setEnabled(true);
                        editText_port.setEnabled(true);
                        editText_username.setEnabled(true);
                        editText_password.setEnabled(true);
                        button_start.setText("开始服务");
                        textView_info.setText("");
                    }
                    break;
            }
        }
    }

}