package me.xuan.bdocr.sdk.utils;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * Author: xuan
 * Created on 2019/10/23 14:31.
 * <p>
 * Describe:
 */
public class CrashReporterHandler implements UncaughtExceptionHandler {

    private static final String EMPTY_STR = "";
    private static final String REPORT_FILENAME = "bd_aip_crashreport_file";
    private static final String HTTP_HEADER_CONTENTTYPE = "application/x-www-form-urlencoded;charset=utf-8";
    private static final String REPORT_TIME_FORMATTER = "yyyy-MM-dd HH:mm:ss";
    private static final String HTTP_URL = "https://verify.baidubce.com/verify/1.0/sdk/report";
    private static CrashReporterHandler instance;
    private static UncaughtExceptionHandler defaultHandler = null;
    private Context ctx;
    private Set<Class> sourceClassSet = new HashSet();

    public CrashReporterHandler(Context context) {
        this.ctx = context;
    }

    public static CrashReporterHandler init(Context ctx) {
        if (instance == null) {
            instance = new CrashReporterHandler(ctx);
        }

        if (defaultHandler == null) {
            defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        }

        Thread.setDefaultUncaughtExceptionHandler(instance);
        instance.sendPreviousReport();
        return instance;
    }

    public void release() {
        this.ctx = null;
    }

    public CrashReporterHandler addSourceClass(Class clazz) {
        this.sourceClassSet.add(clazz);
        return instance;
    }

    public void resolveException(Throwable e) {
        boolean isCause = false;

        String detailContent;
        try {
            isCause = this.isCauseBySource(e);
        } catch (Throwable var6) {
            detailContent = this.exDetailContent(var6);
            String fileString = this.createReport(detailContent);
            if (!this.writeFile(fileString)) {
                this.postBody(fileString);
            }

            return;
        }

        if (isCause) {
            detailContent = this.exDetailContent(e);
            String fileString = this.createReport(detailContent);
            if (!this.writeFile(fileString)) {
                this.postBody(fileString);
            }
        }

    }

    private String createReport(String detailContent) {
        JSONObject obj = new JSONObject();

        String jsonString;
        try {
            obj.put("id", DeviceUtil.getDeviceId(this.ctx));
            obj.put("time", this.getCurrentTime());
            obj.put("system", DeviceUtil.getDeviceInfo(this.ctx));
            obj.put("sdkver", "1_4_4");
            obj.put("appnm", this.getPackageName());
            obj.put("detail", detailContent);
            jsonString = obj.toString();
        } catch (JSONException var5) {
            jsonString = var5.toString();
        }

        return jsonString;
    }

    public void sendPreviousReport() {
        String report = this.readFile();
        if (report != null && report != "") {
            this.postBody(report);
        }

    }

    private boolean writeFile(String fileString) {
        try {
            FileOutputStream outStream = this.ctx.openFileOutput("bd_aip_crashreport_file", 0);
            outStream.write(fileString.getBytes("utf8"));
            outStream.close();
            return true;
        } catch (Exception var3) {
            return false;
        }
    }

    private String readFile() {
        StringBuffer sb = new StringBuffer();

        try {
            FileInputStream stream = this.ctx.openFileInput("bd_aip_crashreport_file");

            int ch;
            while((ch = stream.read()) != -1) {
                sb.append((char)ch);
            }

            stream.close();
            return sb.toString();
        } catch (Exception var4) {
            return null;
        }
    }

    private String genContent(Throwable e) {
        String lineBreaker = "|";
        StringBuffer sb = new StringBuffer();
        sb.append("!" + e.getMessage());
        sb.append(lineBreaker);
        StackTraceElement[] var4 = e.getStackTrace();
        int var5 = var4.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            StackTraceElement elem = var4[var6];
            sb.append(elem.getClassName() + " [" + elem.getMethodName() + ": " + elem.getLineNumber() + "] ");
            sb.append(lineBreaker);
        }

        return sb.toString();
    }

    private String exDetailContent(Throwable e) {
        return e.getCause() != null ? this.genContent(e) + this.exDetailContent(e.getCause()) : this.genContent(e);
    }

    private boolean isCauseBySource(Throwable e) {
        if (this.isStackCauseBySource(e.getStackTrace())) {
            return true;
        } else {
            return e.getCause() == null ? false : this.isCauseBySource(e.getCause());
        }
    }

    private boolean isStackCauseBySource(StackTraceElement[] elems) {
        StackTraceElement[] var2 = elems;
        int var3 = elems.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            StackTraceElement elem = var2[var4];

            try {
                Class clazz = Class.forName(elem.getClassName());
                if (this.sourceClassSet.contains(clazz)) {
                    return true;
                }
            } catch (ClassNotFoundException var7) {
            }
        }

        return false;
    }

    private String getCurrentTime() {
        Date date = new Date();
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return time.format(date);
    }

    private String getPackageName() {
        return this.ctx.getPackageName();
    }

    public void postBody(String fileString) {
        HttpsClient cl = new HttpsClient();
        HttpsClient.RequestBody body = new HttpsClient.RequestBody();
        body.setBody(fileString);
        HttpsClient.RequestInfo reqiInfo = new HttpsClient.RequestInfo("https://verify.baidubce.com/verify/1.0/sdk/report", body);
        reqiInfo.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        reqiInfo.build();
        cl.newCall(reqiInfo).enqueue(new HttpsClient.Callback() {
            public void onFailure(Throwable e) {
            }

            public void onResponse(String resultStr) {
                CrashReporterHandler.this.writeFile("");
            }
        });
    }

    public void uncaughtException(Thread t, Throwable e) {
        try {
            this.resolveException(e);
        } catch (Throwable var4) {
        }

        defaultHandler.uncaughtException(t, e);
    }
}
