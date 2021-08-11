package me.xuan.bdocr.sdk.exception;

/**
 * Author: xuan
 * Created on 2019/10/23 14:40.
 * <p>
 * Describe:
 */
public class OCRError extends Exception {

    protected int errorCode;
    protected long logId;
    protected String errorMessage;
    protected Throwable cause;

    public OCRError(int errorCode, String message, Throwable cause) {
        super(genMessage(errorCode, message), cause);
        this.cause = cause;
        this.errorCode = errorCode;
    }

    public OCRError(int errorCode, String message) {
        super(genMessage(errorCode, message));
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public OCRError(String message) {
        super(message);
    }

    public OCRError() {
    }

    private static String genMessage(int code, String message) {
        return "[" + code + "] " + message;
    }

    public Throwable getCause() {
        return this.cause;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public long getLogId() {
        return this.logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public interface ErrorCode {
        int SERVICE_DATA_ERROR = 283505;
    }
}
