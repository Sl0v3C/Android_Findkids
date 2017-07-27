package com.pyy.findkids;

/**
 * Created by SNAS on 2016/10/22 0022.
 */

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FTP {
    static final String logTag = "[Findkids]";
    /**
     * 服务器名.
     */
    private String hostName;

    /**
     * 端口号
     */
    private int serverPort;

    /**
     * 用户名.
     */
    private String userName;

    /**
     * 密码.
     */
    private String password;

    /**
     * FTP连接.
     */
    private FTPClient ftpClient;

    public FTP() {
        this.hostName = "";  // FTP hostname
        this.serverPort = 21;
        this.userName = "";  // FTP username
        this.password = "";  // FTP password
        this.ftpClient = new FTPClient();
    }

    // -------------------------------------------------------文件上传方法------------------------------------------------

    /**
     * 上传单个文件.
     *
     * @param singleFile
     *            本地文件
     * @param remotePath
     *            FTP目录
     * @throws IOException
     */
    public void uploadSingleFile(File singleFile, String remotePath) throws IOException {
        // 上传之前初始化
        this.uploadBeforeOperate(remotePath);
        boolean flag;
        flag = uploadingSingle(singleFile);
        if (flag) {
            Log.i(logTag, "上传成功");
        } else {
            Log.e(logTag, "上传失败");
        }
        // 上传完成之后关闭连接
        this.uploadAfterOperate();
    }

    /**
     * 上传单个文件.
     *
     * @param localFile
     *            本地文件
     * @return true上传成功, false上传失败
     * @throws IOException
     */
    private boolean uploadingSingle(File localFile) throws IOException {
        boolean flag = false;
        // 不带进度的方式
        // 创建输入流
        InputStream inputStream = new FileInputStream(localFile);
        // 上传单个文件
        flag = ftpClient.storeFile(localFile.getName(), inputStream);
        // 关闭文件流
        inputStream.close();

        return flag;
    }

    /**
     * 上传文件之前初始化相关参数
     *
     * @param remotePath
     *            FTP目录
     * @throws IOException
     */
    private void uploadBeforeOperate(String remotePath) throws IOException {

        // 打开FTP服务
        try {
            this.openConnect();
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        // 设置模式
        ftpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.STREAM_TRANSFER_MODE);
        //Log.e(logTag, "set Mode");
        // FTP下创建文件夹
        ftpClient.makeDirectory(remotePath);
        //Log.e(logTag, "mkdir");
        // 改变FTP目录
        ftpClient.changeWorkingDirectory(remotePath);
        //Log.e(logTag, "change to new dir");
        // 上传单个文件

    }

    /**
     * 上传完成之后关闭连接
     *
     * @throws IOException
     */
    private void uploadAfterOperate()
            throws IOException {
        this.closeConnect();
    }

    // -------------------------------------------------------打开关闭连接------------------------------------------------

    /**
     * 打开FTP服务.
     *
     * @throws IOException
     */
    public void openConnect() throws IOException {
        // 中文转码
        ftpClient.setControlEncoding("UTF-8");
        int reply; // 服务器响应值
        // 连接至服务器
        ftpClient.connect(hostName, serverPort);
        // 获取响应值
        reply = ftpClient.getReplyCode();
        //Log.e(logTag, "Connect reply is " + reply);
        if (!FTPReply.isPositiveCompletion(reply)) {
            // 断开连接
            ftpClient.disconnect();
            throw new IOException(logTag + "connect fail: " + reply);
        }
        // 登录到服务器
        ftpClient.login(userName, password);
        // 获取响应值
        reply = ftpClient.getReplyCode();
        //Log.e(logTag, "Login reply is " + reply);
        if (!FTPReply.isPositiveCompletion(reply)) {
            // 断开连接
            ftpClient.disconnect();
            throw new IOException(logTag + "connect fail: " + reply);
        } else {
            // 获取登录信息
            FTPClientConfig config = new FTPClientConfig(ftpClient
                    .getSystemType().split(" ")[0]);
            config.setServerLanguageCode("zh");
            ftpClient.configure(config);
            // 使用被动模式设为默认
            ftpClient.enterLocalPassiveMode();
            // 二进制文件支持
            ftpClient
                    .setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
        }
    }

    /**
     * 关闭FTP服务.
     *
     * @throws IOException
     */
    public void closeConnect() throws IOException {
        if (ftpClient != null) {
            // 退出FTP
            ftpClient.logout();
            // 断开连接
            ftpClient.disconnect();
        }
    }

}
