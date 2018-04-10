package utils;

import java.util.List;

public interface GetFilesCallback {
    void onComplete(List<CloudResource> files);
    void onError(Exception e);
}