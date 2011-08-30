
package me.openphoto.android.test.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    public static void writeToFile(InputStream inputStream, File file) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        byte buf[] = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) > 0)
            out.write(buf, 0, len);
        out.close();
        inputStream.close();
    }
}
