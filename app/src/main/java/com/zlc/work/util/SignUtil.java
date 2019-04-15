package com.zlc.work.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * author: liuchun
 * date: 2018/11/30
 */
public class SignUtil {

    public static byte[] getSign(Context context, String pkgName) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES
                    | PackageManager.GET_SIGNING_CERTIFICATES);
            return pi.signatures[0].toByteArray();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getPublicKey(byte[] signature) {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream bis = new ByteArrayInputStream(signature);
            X509Certificate cert = (X509Certificate)factory.generateCertificate(bis);
            return cert.getPublicKey().toString();
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        return null;
    }
}
