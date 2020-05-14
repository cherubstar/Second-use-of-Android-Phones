package com.star.monitor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlRequestUtil {

    public static String readParse(String urlPath){

        ByteArrayOutputStream outStream = null;
        InputStream inStream;

        try {
            outStream = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int len = 0;
            URL url = new URL(urlPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            inStream = conn.getInputStream();
            while ((len = inStream.read(data)) != -1) {
                outStream.write(data, 0, len);
            }

            inStream.close();
        } catch (Exception e){
            e.printStackTrace();
        }

        //通过out.Stream.toByteArray获取到写的数据
        return new String(outStream.toByteArray());
    }
}
