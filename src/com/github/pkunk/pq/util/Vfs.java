package com.github.pkunk.pq.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * User: pkunk
 * Date: 2012-01-31
 */
public class Vfs {
    private static final String TAG = Vfs.class.getCanonicalName();

    private static final String UTF8 = "UTF-8";

    public static final String SETTINGS = "Settings";
    public static final String PLAYER_ID = "playerId";
    public static final String TAB_STATE = "tabState";

    public static final String EQ = "=";
    public static final String SEPARATOR = ";";

    private static final String FILE_NAME_SEPARATOR = ".";
    private static final String ZIP_EXT = FILE_NAME_SEPARATOR + "zip";

    private static final int MAX_SAVE_FILES = 5;

    public static void setPlayerId(Context context, String playerId) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PLAYER_ID, playerId);
        editor.commit();
    }

    public static String getPlayerId(Context context) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return settings.getString(PLAYER_ID, null);
    }

    public static void setTabState(Context context, int tabState) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(TAB_STATE, tabState);
        editor.commit();
    }

    public static int getTabState(Context context) {
        SharedPreferences settings = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
        return settings.getInt(TAB_STATE, 0);
    }
    
    public static void writeLastAlarmTime(Context context)
    {
    	SharedPreferences preferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
    	SharedPreferences.Editor editor = preferences.edit();
    	
    	editor.putLong("LAST_ALARM_TIME", System.currentTimeMillis());
    	editor.commit();
    }
    
    public static long readLastAlarmTime(Context context)
    {
    	SharedPreferences preferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
    	return preferences.getLong("LAST_ALARM_TIME", System.currentTimeMillis());
    }

    public static void writePlayerToFile(Context context, String playerId, Map<String, List<String>> dataMap) throws IOException {
        String fileName = playerId + FILE_NAME_SEPARATOR + PqUtils.getShortTimestamp() + ZIP_EXT;
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

        String[] playerSaveFiles = getPlayerSaveFiles(context, playerId);
        filterSaveFiles(context, playerSaveFiles, playerId);
    }


    public static Map<String, List<String>> readPlayerFromFile(Context context, String playerId) throws IOException {

        String[] playerSaveFiles = getPlayerSaveFiles(context, playerId);
        String fileName = filterSaveFiles(context, playerSaveFiles, playerId);

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

    public static String[] getPlayersSaveFiles(Context context) {
        String[] saveFiles = getAllSaveFiles(context);

        Map<String, List<String>> playersMap = new HashMap<String, List<String>>();
        for (String saveFileName : saveFiles) {
            String playerId = saveFileName.substring(0, saveFileName.indexOf(FILE_NAME_SEPARATOR));

            List<String> playerFileList;
            if (playersMap.keySet().contains(playerId)) {
                playerFileList = playersMap.get(playerId);
            } else {
                playerFileList = new ArrayList<String>();
                playersMap.put(playerId, playerFileList);
            }
            playerFileList.add(saveFileName);
        }

        List<String> result = new ArrayList<String>();

        for (String playerId : playersMap.keySet()) {
            List<String> playerSaveFiles = playersMap.get(playerId);
            String fileName = filterSaveFiles(context, playerSaveFiles.toArray(new String[playerSaveFiles.size()]), playerId);
            if (fileName.length() > 0) {
                result.add(fileName);
            }
        }

        return result.toArray(new String[result.size()]);
    }

    private static boolean checkSaveFile(String playerId, List<String> annotations) {
        if (annotations != null) {

            String pattern = PLAYER_ID + Vfs.EQ + playerId;
            for (String entry : annotations) {
                if  (pattern.equals(entry)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String[] getAllSaveFiles(Context context) {
        File saveDir = context.getFilesDir();
        String[] saveFiles = saveDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(ZIP_EXT)) {
                    return true;
                }
                return false;
            }
        });
        return saveFiles;
    }

    private static String[] getPlayerSaveFiles(Context context, final String playerId) {
        File saveDir = context.getFilesDir();
        String[] saveFiles = saveDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                if (filename.endsWith(ZIP_EXT)
                    && filename.startsWith(playerId)
                )
                {
                    return true;
                }
                return false;
            }
        });
        return saveFiles;
    }
    
    private static String filterSaveFiles(Context context, String[] saveFiles, String playerId) {
        List<String> validSaveFileList = new LinkedList<String>();

        Map<String, List<String>> playerAnnotationMap = Vfs.readEntryFromFiles(context, saveFiles, "Annotation");

        for (String saveFile : playerAnnotationMap.keySet()) {
            if (checkSaveFile(playerId, playerAnnotationMap.get(saveFile))) {
                validSaveFileList.add(saveFile);
            } else {
                deleteFile(context, saveFile);
            }
        }
        
        Collections.sort(validSaveFileList, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                int diff = rhs.length() - lhs.length();
                if (diff != 0) {
                    return diff;
                } else {
                    return rhs.compareTo(lhs);
                }
            }
        });
        
        for (int i = MAX_SAVE_FILES; i<validSaveFileList.size(); i++) {
            deleteFile(context, validSaveFileList.get(i));
        }

        if (validSaveFileList.size() > 0) {
            return validSaveFileList.get(0);
        } else {
            return "";
        }
    }

    public static Map<String, List<String>> readEntryFromFiles(Context context, String[] fileNames, String entry) {
        Map<String, List<String>> result = new HashMap<String, List<String>>(fileNames.length);
        File saveDir = context.getFilesDir();
        for (String fileName : fileNames) {
            try {
                File file = new File(saveDir, fileName);
                ZipFile zipFile = new ZipFile(file);
                ZipEntry zipEntry = zipFile.getEntry(entry);
                InputStream is = new BufferedInputStream(zipFile.getInputStream(zipEntry));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count;
                while ((count = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                }
                List<String> entryData = fromByteArray(baos.toByteArray());
                result.put(fileName, entryData);
            } catch (IOException ioe) {
                Log.e(TAG, ioe.getMessage());
                ioe.printStackTrace();
            }
        }
        return result;
    }

    public static void deletePlayerFiles(Context context, String playerId) {
        String[] playerSaveFiles = getPlayerSaveFiles(context, playerId);
        for (String saveFile : playerSaveFiles) {
            deleteFile(context, saveFile);
        }
    }

    private static boolean deleteFile(Context context, String fileName) {
        File saveDir = context.getFilesDir();
        File file = new File(saveDir, fileName);
        return file.delete();
    }

    private static List<String> fromByteArray(byte[] array) {
        String read = null;
        try {
            read = new String(array, UTF8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        List<String> result = Arrays.asList(read.split("\n"));
        return result;
    }

    private static byte[] toByteArray(List<String> strings) {
        StringBuilder builder = new StringBuilder();
        for (String s : strings) {
            builder.append(s).append("\n");
        }
        String text = builder.toString();
        byte[] result = null;
        try {
            result = text.getBytes(UTF8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public static String sanitizeString(String string) {
        String result = string;
        result = result.replace(Vfs.EQ, "-");
        result = result.replace(Vfs.SEPARATOR, ":");
        result = result.replace("\n", " ");
        result = result.replace("\r", "");
        result = result.trim();
        return result;
    }
}
