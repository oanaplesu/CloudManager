package utils;


public interface DeleteFileCallback {
    void onComplete();
    void onError(Exception e);
}
