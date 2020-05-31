package com.star.monitor.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;


@RestController
public class FileUploadController {

    @PostMapping("/upload")
    public void upload(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        String realPath = "/usr/local/server/tomcat/webapps/video";
        File saveDir = new File(realPath);
        if(!saveDir.exists() || !saveDir.isDirectory()){
            saveDir.mkdirs();
        }

        Collection<Part> parts = request.getParts();
        for (Part part : parts){
            String header = part.getHeader("content-disposition");
            System.out.println(header);
            String fileName = getFileName(header);
            System.out.println(fileName);
            part.write(realPath + File.separator + fileName);
        }
        PrintWriter writer = response.getWriter();
        writer.println("upload success");
        writer.flush();
        writer.close();
    }

    public String getFileName(String header){
        String[] str1 = header.split(";");
        String[] str2 = str1[2].split("=");
        String fileName = str2[1].substring(str2[1].lastIndexOf("\\") + 1).replace("\"", "");
        return fileName;
    }
}
