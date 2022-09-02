package org.andre.strimzi.oauth.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil {
    private IOUtil(){}
    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[4096];
        int rc;
        try {
            while ((rc = in.read(buf)) != -1) {
                out.write(buf, 0, rc);
            }
        } finally {
            try {
                in.close();
            } catch (Exception e) {

            }
        }
    }
}
