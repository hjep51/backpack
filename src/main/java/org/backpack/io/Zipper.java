/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.backpack.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author hjep
 */
public class Zipper {
    private List<String> fileList = new ArrayList<>();
    
    public void compressDirectory(String dir, String zipFile) {
        File directory = new File(dir);
        getFileList(directory);

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (String filePath : fileList) {
                // Creates a zip entry.
                String name = filePath.substring(
                    directory.getAbsolutePath().length() + 1,
                    filePath.length());

                ZipEntry zipEntry = new ZipEntry(name);
                zos.putNextEntry(zipEntry);

                // Read file content and write to zip output stream.
                try (FileInputStream fis = new FileInputStream(filePath)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }

                    // Close the zip entry.
                    zos.closeEntry();
                } catch (Exception e) {
                    Logger.getLogger(Zipper.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        } catch (IOException e) {
            Logger.getLogger(Zipper.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void getFileList(File directory) {
        File[] files = directory.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile()) {
                    fileList.add(file.getAbsolutePath());
                } else {
                    getFileList(file);
                }
            }
        }

    }
}
