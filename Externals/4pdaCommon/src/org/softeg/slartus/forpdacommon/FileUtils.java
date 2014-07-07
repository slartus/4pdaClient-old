package org.softeg.slartus.forpdacommon;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * User: slinkin
 * Date: 09.11.11
 * Time: 7:31
 */
public class FileUtils {
    public static byte[] toByteArray(File file) throws IOException {

        InputStream input_stream = new BufferedInputStream(new FileInputStream(file));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[16384]; // 16K
        int bytes_read;
        while ((bytes_read = input_stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytes_read);
        }
        input_stream.close();
        return buffer.toByteArray();
    }

    public static void CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1)
                    break;
                os.write(bytes, 0, count);
            }
        } catch (Exception ex) {
        }
    }

    private static final char[] ILLEGAL_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};

    /*
    * Нормализует(уберает иллегальные символы)
     */
    public static String normalize(String fileName) {
        for (char illegalChar : ILLEGAL_CHARACTERS) {
            fileName = fileName.replace(illegalChar, '_');
        }
        return fileName;
    }

    public static String getFileNameFromUrl(String url) throws UnsupportedEncodingException {
        String decodedUrl = UrlExtensions.decodeUrl(url).toString();
        int index = decodedUrl.lastIndexOf("/");

        return normalize(decodedUrl.substring(index + 1, decodedUrl.length()));
    }

    public static String getDirPath(String filePath) {

        return filePath.substring(0, filePath.lastIndexOf(File.separator));
    }

    public static String fileExt(String url) {
        String ext = url.substring(url.lastIndexOf("."));
        if (ext.indexOf("?") > -1) {
            ext = ext.substring(0, ext.indexOf("?"));
        }
        if (ext.indexOf("%") > -1) {
            ext = ext.substring(0, ext.indexOf("%"));
        }
        return ext;
    }

    public static String combine(String path1, String path2) {

        if (!path1.endsWith(File.separator))
            path1 += File.separator;
        return path1 + path2;
    }

    public static String getUniqueFilePath(String dirPath, String fileName) {
        String name = fileName;
        String ext = "";
        int ind = fileName.lastIndexOf(".");
        if (ind != -1) {
            name = fileName.substring(0, ind);
            ext = fileName.substring(ind, fileName.length());
        }
        if (!dirPath.endsWith(File.separator))
            dirPath += File.separator;
        String suffix = "";
        int c = 0;
        while (new File(dirPath + name + suffix + ext).exists() || new File(dirPath + name + suffix + ext + "_download").exists()) {
            suffix = "(" + c + ")";
            c++;
        }
        return dirPath + name + suffix + ext;
    }

    public static Boolean mkDirs(String filePath) {
        //int startind=1;
        String dirPath = new File(filePath).getParentFile().getAbsolutePath() + File.separator;

        File dir = new File(dirPath.replace("/", File.separator));
        return dir.exists() || dir.mkdirs();
//        while(true){
//             if(startind>=dirPath.length()||startind==-1)
//                return true;
//            int slashInd=dirPath.indexOf(File.separator,startind);
//            if(slashInd==-1)return true;
//            String subPath=dirPath.substring(0,slashInd);
//            File f=new File(subPath);
//            if(!f.exists()&&!f.mkdir()){
//                return false;
//            }
//            startind=subPath.length()+1;
//
//        }

    }

    static public boolean hasStorage(String dirPath, boolean requireWriteAccess) throws NotReportException {
        //TODO: After fix the bug,  add "if (VERBOSE)" before logging errors.
        String state = Environment.getExternalStorageState();
       // Log.v(TAG, "storage state is " + state);
        if(Environment.MEDIA_REMOVED.equals(state))
            throw new NotReportException("Карта памяти не подключена: "+dirPath);
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (requireWriteAccess) {
                boolean writable = checkFsWritable(dirPath);
            //    Log.v(TAG, "storage writable is " + writable);
                return writable;
            } else {
                return true;
            }
        } else if (!requireWriteAccess && Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private static boolean checkFsWritable(String dirPath) {
        // Create a temporary file to see whether a volume is really writeable.
        // It's important not to put it in the root directory which may have a
        // limit on the number of files.

        File directory = new File(dirPath);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                return false;
            }
        }
        return directory.canWrite();
    }
}
