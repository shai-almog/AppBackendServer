package com.codename1.app.backend.service;

import com.codename1.app.backend.data.RestaurantEntity;
import com.codename1.app.backend.data.RestaurantRepository;
import com.codename1.app.backend.webservices.FileResponseService;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class BuildAppAsync {
    @Autowired
    private RestaurantRepository restRepository;

    @Async
    public void buildApp(String secret, String pushKey, String targetType) {
        try {
            RestaurantEntity re = restRepository.findBySecret(secret);
            if(re == null) {
                return;
            }

            File tempDirectory = File.createTempFile("tempDir", "ending");
            tempDirectory.delete();
            tempDirectory.mkdirs();

            unzip(getClass().getResourceAsStream("/MyRestaurant.zip"), tempDirectory);

            String uuid = UUID.randomUUID().toString();
            FileResponseService.FILES_DIRECTORY.mkdirs();
            File tempFileName = new File(FileResponseService.FILES_DIRECTORY, uuid + ".zip");
            //tempFileName.deleteOnExit();

            zipDir(tempFileName.getAbsolutePath(), tempDirectory.getAbsolutePath());
        } catch(IOException err) {
            throw new RuntimeException(err);
        }
    }    

    private void unzip(InputStream sourceZip, File destDir) throws IOException {
        BufferedOutputStream dest = null;
        ZipInputStream zis = new ZipInputStream(sourceZip);
        ZipEntry entry;
        byte[] data = new byte[8192];
        while ((entry = zis.getNextEntry()) != null) {
            String entryName = entry.getName();

            if (entry.isDirectory()) {
                File dir = new File(destDir, entryName);
                dir.mkdirs();
                continue;
            }

            int count;

            // write the files to the disk
            File destFile = new File(destDir, entryName);
            destFile.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(destFile);
            dest = new BufferedOutputStream(fos, data.length);
            while ((count = zis.read(data, 0, data.length)) != -1) {
                dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
        }
        zis.close();
        sourceZip.close();
    }

    private static void zipDir(String zipFileName, String dir) throws IOException {
        File dirObj = new File(dir);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
        addDir(dirObj, dirObj, out);
        out.close();
    }

    static void addDir(File baseDir, File dirObj, ZipOutputStream out) throws IOException {
        File[] files = dirObj.listFiles();
        byte[] tmpBuf = new byte[8192];

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                boolean found = false;
                if (!found) {
                    addDir(baseDir, files[i], out);
                }
                continue;
            }
            FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
            //System.out.println(" Adding: " + files[i].getAbsolutePath());
            out.putNextEntry(new ZipEntry(files[i].getAbsolutePath().substring(baseDir.getAbsolutePath().length() + 1).replace('\\', '/')));
            int len;
            while ((len = in.read(tmpBuf)) > 0) {
                out.write(tmpBuf, 0, len);
            }
            out.closeEntry();
            in.close();
        }
    }
}
