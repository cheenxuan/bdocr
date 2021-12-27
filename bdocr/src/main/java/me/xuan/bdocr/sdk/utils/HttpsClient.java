package me.xuan.bdocr.sdk.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

/**
 * Author: xuan
 * Created on 2019/10/23 14:37.
 * <p>
 * Describe:
 */
public class HttpsClient {
    public HttpsClient() {
    }

    public HttpsClient.Call newCall(HttpsClient.RequestInfo requestInfo) {
        HttpsClient.Call call = new HttpsClient.Call(requestInfo);
        return call;
    }

    public static class Call implements Runnable {
        private HttpsClient.RequestInfo requestInfo;
        private Thread thread;
        private HttpsClient.Callback callback;

        public Call(HttpsClient.RequestInfo requestInfo) {
            this.requestInfo = requestInfo;
        }

        public HttpsClient.Call enqueue(HttpsClient.Callback callback) {
            this.callback = callback;
            this.thread = new Thread(this);
            this.thread.start();
            return this;
        }

        private void setHeaders(HttpsURLConnection con, Map<String, String> headers) {
            Iterator var3 = headers.entrySet().iterator();

            while (var3.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry) var3.next();
                con.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
            }

        }

        public void run() {
            HttpsClient.RequestInfo requestInfo = this.requestInfo;
            HttpsURLConnection con = null;
            Exception buildException;
            if ((buildException = requestInfo.getBuildException()) != null) {
                this.callback.onFailure(buildException);
            } else {
                try {
                    URL url = requestInfo.getURL();
                    byte[] body = requestInfo.getBody();
                    con = (HttpsURLConnection) url.openConnection();
                    this.setHeaders(con, requestInfo.getHeaders());
                    con.setRequestMethod("POST");
                    con.setConnectTimeout(requestInfo.getConTimeout());
                    con.setReadTimeout(requestInfo.getReadTimeout());
                    con.setDoOutput(true);
                    OutputStream out = con.getOutputStream();
                    out.write(body);
                    this.writeResp(con);
                } catch (Throwable var10) {
                    var10.printStackTrace();
                    this.callback.onFailure(var10);
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }

                }

            }
        }

        public void writeResp(HttpsURLConnection con) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuffer sb = new StringBuffer();
                char[] cs = new char[512];
                boolean var5 = false;

                int readedNumber;
                while ((readedNumber = br.read(cs)) != -1) {
                    sb.append(new String(cs, 0, readedNumber));
                }

                this.callback.onResponse(sb.toString());
                br.close();
            } catch (IOException var6) {
                this.callback.onFailure(var6);
            }

        }
    }

    public static class RequestInfo {
        private String urlStr;
        private URL url;
        private Map<String, String> headers;
        private HttpsClient.RequestBody body;
        private Exception ex;
        private int conTimeout;
        private int readTimeout;

        public Exception getBuildException() {
            return this.ex;
        }

        public int getConTimeout() {
            return this.conTimeout;
        }

        public int getReadTimeout() {
            return this.readTimeout;
        }

        public RequestInfo(String urlStr, HttpsClient.RequestBody body) {
            this.urlStr = urlStr;
            this.body = body;
            this.headers = new HashMap();
            this.ex = null;
            this.conTimeout = HttpUtil.getOptions().getConnectionTimeoutInMillis();
            this.readTimeout = HttpUtil.getOptions().getSocketTimeoutInMillis();
        }

        public Map<String, String> getHeaders() {
            return this.headers;
        }

        public void setHeader(String key, String value) {
            this.headers.put(key, value);
        }

        public void build() {
            try {
                this.url = new URL(this.urlStr);
            } catch (Exception var2) {
                this.ex = var2;
            }

        }

        public URL getURL() {
            return this.url;
        }

        public byte[] getBody() {
            return this.body.getBytes();
        }
    }

    public static class RequestBody {
        private StringBuffer stringBuffer = new StringBuffer();
        private int paramNumber = 0;
        private static String UTF8 = "UTF-8";
        private static FileBase64Encoder encoder = new FileBase64Encoder();

        public RequestBody() {
        }

        public void setBody(String body) {
            this.stringBuffer.append(body);
        }

        public void setStrParams(Map<String, String> params) {
            if (params != null) {
                Iterator<Entry<String, String>> it = params.entrySet().iterator();

                while (it.hasNext()) {
                    Entry entry = (Entry) it.next();
                    if (this.paramNumber > 0) {
                        this.stringBuffer.append("&");
                    }

                    String key = (String) entry.getKey();
                    String value = (String) entry.getValue();

                    try {
                        key = URLEncoder.encode(key, UTF8);
                        value = URLEncoder.encode(value, UTF8);
                        this.stringBuffer.append(key + "=" + value);
                        ++this.paramNumber;
                    } catch (UnsupportedEncodingException var7) {
                        var7.printStackTrace();
                    }
                }

            }
        }

        public void setFileParams(Map<String, File> params) {
            StringBuffer sb = new StringBuffer();
            if (params != null) {
                Iterator iterator = params.entrySet().iterator();

                while (iterator.hasNext()) {
                    Entry<String, File> entry = (Entry) iterator.next();
                    if (this.paramNumber > 0) {
                        this.stringBuffer.append("&");
                    }

                    String key = entry.getKey();
                    File file = entry.getValue();

                    try {
                        key = URLEncoder.encode(key, UTF8);
                        encoder.setInputFile(file);
                        this.stringBuffer.append(key + "=");

                        byte[] encoded;
                        while ((encoded = encoder.encode()) != null) {
                            sb.append(new String(encoded));
                            this.stringBuffer.append(URLEncoder.encode(new String(encoded), UTF8));
                        }

                        ++this.paramNumber;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        public byte[] getBytes() {
            byte[] bytes = new byte[0];

            try {
                bytes = String.valueOf(this.stringBuffer).getBytes("UTF-8");
                return bytes;
            } catch (UnsupportedEncodingException var3) {
                var3.printStackTrace();
                return bytes;
            }
        }
    }

    public interface Callback {
        void onFailure(Throwable var1);

        void onResponse(String var1);
    }
}
