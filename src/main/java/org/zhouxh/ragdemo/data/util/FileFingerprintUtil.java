package org.zhouxh.ragdemo.data.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

/**
 * 文件指纹
 */
public class FileFingerprintUtil {

    // 生产推荐：SHA-256
    public static String getFileSha256(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    public static String getFileSha256(FileInputStream file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (file) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = file.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    public static String getFileSha256(java.io.InputStream inputStream) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (inputStream) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
        }
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}