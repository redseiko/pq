package com.github.pkunk.progressquest.util;

import android.content.Context;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * User: pkunk
 * Date: 2012-01-31
 */
public class Vfs {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static final String EQ = "=";
    public static final String SEPARATOR = ";";

    public static final String ZIP_EXT = ".zip";
    public static final String BAK_EXT = ".bak";

    public static void writeToFile(Context context, String fileName, Map<String, List<String>> dataMap) throws IOException {
        OutputStream os = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os));
        try {
            for (Map.Entry<String, List<String>> dataEntry : dataMap.entrySet()) {
                byte[] bytes = toByteArray(dataEntry.getValue());
                ZipEntry entry = new ZipEntry(dataEntry.getKey());
                zos.putNextEntry(entry);
                zos.write(bytes);
                zos.closeEntry();
            }
        } finally {
            try {
                zos.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }


    public static Map<String, List<String>> readFromFile(Context context, String fileName) throws IOException {

        Map<String, List<String>> result = new HashMap<String, List<String>>();

        InputStream is = context.openFileInput(fileName);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
        try {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count;
                while ((count = zis.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                }
                String entryName = ze.getName();
                List<String> entryData = fromByteArray(baos.toByteArray());
                result.put(entryName, entryData);
            }
        } finally {
            try {
                zis.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        return result;
    }

    private static List<String> fromByteArray(byte[] array) {
        String read = new String(array, UTF8);
        List<String> result = Arrays.asList(read.split("\n"));
        return result;
    }

    private static byte[] toByteArray(List<String> strings) {
        StringBuilder builder = new StringBuilder();
        for (String s : strings) {
            builder.append(s).append("\n");
        }
        String result = builder.toString();
        return result.getBytes(UTF8);
    }
}