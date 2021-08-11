package me.xuan.bdocr.sdk.exception;

/**
 * Author: xuan
 * Created on 2019/10/23 14:48.
 * <p>
 * Describe:
 */
public class SDKError extends OCRError {

    public SDKError(int errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
        this.cause = cause;
        this.errorCode = errorCode;
    }

    public SDKError(int errorCode, String message) {
        super(errorCode, message);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public SDKError(String message) {
        super(message);
    }

    public SDKError() {
    }

    public interface ErrorCode {
        int NETWORK_REQUEST_ERROR = 283504;
        int ACCESS_TOKEN_DATA_ERROR = 283505;
        int LOAD_JNI_LIBRARY_ERROR = 283506;
    }
}
