package utils;


public interface UploadFileCallback {
    void onComplete();
    void onError(Exception e);
}
