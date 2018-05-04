package utils.services;


import android.app.ProgressDialog;
import android.content.Context;

import java.io.File;
import java.util.List;

import utils.cloud.CloudResource;
import utils.tasks.CloudRequestTask;
import utils.tasks.MoveFilesTask;

public interface CloudService {
    interface GenericCallback {
        void onComplete();
        void onError(Exception e);
    }

    interface GetFilesCallback {
        void onComplete(List<CloudResource> files);
        void onError(Exception e);
    }

    interface DownloadFileCallback {
        void onComplete(File file);
        void onError(Exception e);
    }

    interface CreateFolderCallback {
        void onComplete(CloudResource createdFolder);
        void onError(Exception e);
    }

    interface MoveFilesCallback {
        void onComplete(MoveFilesTask.Statistics stats);
    }

    CloudRequestTask getFilesTask(ProgressDialog dialog, GetFilesCallback callback);
    CloudRequestTask createFolderTask(CreateFolderCallback callback);
    CloudRequestTask deleteFileTask(GenericCallback callback);
    CloudRequestTask uploadFileTask(File file, ProgressDialog dialog, GenericCallback callback);
    CloudRequestTask downloadFileTask(ProgressDialog dialog, boolean saveTmp, DownloadFileCallback callback);
    CloudRequestTask moveFilesTask(CloudResource sourceFile, Context context, boolean deleteOriginal, MoveFilesCallback callback);
}
