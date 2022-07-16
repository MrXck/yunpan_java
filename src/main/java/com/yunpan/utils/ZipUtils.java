package com.yunpan.utils;


import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {
    public static void toZip(List<File> srcFiles, File zipFile) throws IOException{
        if(zipFile == null){
            return;
        }
        if(!zipFile.getName().endsWith(".zip")){
            return;
        }
        ZipOutputStream zos = null;
        FileOutputStream out = new FileOutputStream(zipFile);
        try{
            zos = new ZipOutputStream(out);
            for (File srcFile : srcFiles) {
                byte[] buf = new byte[1024];
                zos.putNextEntry(new ZipEntry(srcFile.getName()));
                int len;
                FileInputStream in = new FileInputStream(srcFile);
                while ((len = in.read(buf)) != -1){
                    zos.write(buf, 0, len);
                    zos.flush();
                }
                in.close();
            }
        }catch (Exception e){

        }finally {
            if(zos != null){
                zos.close();
            }
        }
    }

    public static void downloadZip(OutputStream outputStream, List<com.yunpan.entity.File> fileList){
        ZipOutputStream zipOutputStream = null;
        try {
            zipOutputStream = new ZipOutputStream(outputStream);
            for (com.yunpan.entity.File file : fileList) {
                ZipEntry zipEntry = new ZipEntry(file.getFilename());
                zipOutputStream.putNextEntry(zipEntry);
                byte[] buf = new byte[1024];
                int len;
                FileInputStream in = new FileInputStream(file.getFilePath().replace("\\u202a", ""));
                while ((len = in.read(buf)) != -1){
                    zipOutputStream.write(buf, 0, len);
                    zipOutputStream.flush();
                }
            }
            zipOutputStream.flush();
            zipOutputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(zipOutputStream != null){
                    zipOutputStream.close();
                }
                if(outputStream != null){
                    outputStream.close();
                }
            }catch (Exception e){

            }
        }
    }
}
