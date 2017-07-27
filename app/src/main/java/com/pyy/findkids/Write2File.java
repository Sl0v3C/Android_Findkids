package com.pyy.findkids;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

/**
 * Created by SNAS on 2016/10/22 0022.
 */

public class Write2File {
    static final String logTag = "[Findkids]";

    public void Write2File() {
        String filePath = "/sdcard/Findkids/";
        String fileName = "info.txt";
        //生成文件夹之后，再生成文件，不然会出错
        genFile(filePath, fileName);
    }

    // 将字符串写入到文本文件中
    public void writeTxtToFile(String strcontent, String filePath, String fileName) {
        String strFilePath = filePath+fileName;
        // 每次写入时，都换行写
        // String strContent = strcontent + "\r\n";
        FileOutputStream out = null;
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d(logTag, "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            out = new FileOutputStream(file);
            out.write(strcontent.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.e(logTag, "Error on write File:" + e);
        }
    }

    // 生成文件
    public File genFile(String filePath, String fileName) {
        File file = null;
        genDir(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // 生成文件夹
    public static void genDir(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.e(logTag + "error:", e + "");
        }
    }
}
