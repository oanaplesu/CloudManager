package utils.services;


import android.app.ProgressDialog;

import java.io.File;
import java.util.List;

import utils.misc.CloudResource;
import utils.tasks.CloudRequestTask;

public interface CloudService {
    interface GenericCallback {
        void onComplete();
        void onError(Exception e);
    }

    interface GetFilesCallback {
        void onComplete(List<CloudResource> files);
        void onError(Exception e);
    }

    CloudRequestTask getFilesTask(ProgressDialog dialog, GetFilesCallback callback);
    CloudRequestTask createFolderTask(GenericCallback callback);
    CloudRequestTask deleteFileTask(GenericCallback callback);
    CloudRequestTask uploadFileTask(File file, ProgressDialog dialog, GenericCallback callback);
}
