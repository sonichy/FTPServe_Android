package com.hty.ftpserve;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.util.ArrayList;
import java.util.List;

public class FtpService extends Service {

    private FtpServer server;
    private String username;
    private String password;
    private int port;
    private static String rootPath;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        port = intent.getIntExtra("port", 2121);
        username = intent.getStringExtra("username");
        if (username.equals("")) username = "anonymous";
        password = intent.getStringExtra("password");
        Log.e(Thread.currentThread().getStackTrace()[2] + "", "port(" + port + "), username(" + username + "), password(" + password + ")");
        try {
            init();
            Toast.makeText(this, "启动FTP服务成功", Toast.LENGTH_SHORT).show();
        } catch (FtpException e) {
            e.printStackTrace();
            Toast.makeText(this, "启动FTP服务失败", Toast.LENGTH_SHORT).show();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
        Toast.makeText(this, "关闭FTP服务", Toast.LENGTH_SHORT).show();
    }

    public void init() throws FtpException {
        release();
        startFtp();
    }

    private void startFtp() throws FtpException {
        FtpServerFactory serverFactory = new FtpServerFactory();

        //设置访问的用户名、密码、主目录
        BaseUser baseUser = new BaseUser();
        baseUser.setName(username);
        baseUser.setPassword(password);
        baseUser.setHomeDirectory(rootPath);

        List<Authority> authorities = new ArrayList<Authority>();
        authorities.add(new WritePermission());
        baseUser.setAuthorities(authorities);
        serverFactory.getUserManager().save(baseUser);

        ListenerFactory factory = new ListenerFactory();
        factory.setPort(port); //设置端口号，非ROOT不可使用1024以下的端口
        serverFactory.addListener("default", factory.createListener());

        server = serverFactory.createServer();
        server.start();
    }

    /**
     * 释放资源
     */
    public void release() {
        stopFtp();
    }

    private void stopFtp() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

}