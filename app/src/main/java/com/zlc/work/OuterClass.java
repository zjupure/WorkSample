package com.zlc.work;

import android.content.Context;
import android.util.Log;

/**
 * author: liuchun
 * date: 2019-06-08
 */
public class OuterClass extends OuterClassParent {

    private InnerClass innerObj = new InnerClass();

    public void method1() {
        Log.i("OuterClass", "method1 called");
    }

    private static String getUrl(Context context, Object... params) {
        StringBuilder base = new StringBuilder("base");
        for (Object param : params) {
            base.append(param);
        }
        return base.toString();
    }

    private class InnerClass {

        public void imethod1() {
            //method1();
            //OuterClass.this.showToast();
            String url = getUrl(null, "", "", "");
            Log.i("tag", "url=" + url);
        }
    }


    private class InnerClass2 {

        public void imethod2() {
            innerObj.imethod1();
        }
    }
}
