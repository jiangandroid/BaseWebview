package com.jiang.baseWebview;

import android.content.Context;
import android.net.http.SslCertificate;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Created by xiyou on 2019/1/21
 */
public class KitWebviewUtil {

    public static File getExternalFilesDir(Context context, @Nullable String type) {
        if (type != null) {
            File f = context.getExternalFilesDir(type);
            if (f != null) {
                return f;
            }
        }

        String dir = "/Android/data/" + context.getPackageName() + "/files";
        File extFile = Environment.getExternalStorageDirectory();
        return new File(extFile.getPath() + dir);
    }

    //SSL签名校验
    public static boolean checkMySSLCNCert(String signature, SslCertificate cert) {
        Bundle bundle = SslCertificate.saveState(cert);
        byte[] bytes = bundle.getByteArray("x509-certificate");
        if (bytes != null) {
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                Certificate ca = cf.generateCertificate(new ByteArrayInputStream(bytes));
                return signature.equals(KitWebviewUtil.byte2hex(((X509Certificate) ca).getSignature()));
            } catch (Exception Ex) {

            }
        }
        return false;
    }

    /**
     * bytes转换成十六进制字符串
     *
     * @param b
     * @return
     */
    public static String byte2hex(byte[] b) {
        StringBuilder hs = new StringBuilder();

        for (int n = 0; b != null && n < b.length; ++n) {
            String stmp = Integer.toHexString(b[n] & 255);
            if (stmp.length() == 1) {
                hs.append('0');
            }

            hs.append(stmp);
        }

        return hs.toString().toUpperCase();
    }

    public static String exceptionToString(Throwable e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }
}
