package utils.services;


import android.app.ProgressDialog;
import android.content.Context;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;

import java.io.File;

import utils.db.AppDatabase;
import utils.tasks.CloudRequestTask;
import utils.tasks.dropbox.CreateFolderDropboxTask;
import utils.tasks.dropbox.DeleteFileDropboxTask;
import utils.tasks.dropbox.DownloadFileDropboxTask;
import utils.tasks.dropbox.GetFilesFromDropboxTask;
import utils.tasks.dropbox.UploadFileDropboxTask;


public class DropboxService implements CloudService {
    DbxClientV2 mService;

    public DropboxService(Context context, String accountEmail) {
        AppDatabase database = AppDatabase.getDatabase(context);

        String token = database.dropboxUserDao().getTokenForAccount(accountEmail);

        DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("dropbox")
                .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                .build();
        mService = new DbxClientV2(requestConfig, token);
    }

    @Override
    public CloudRequestTask getFilesTask(ProgressDialog dialog, GetFilesCallback callback) {
        return new GetFilesFromDropboxTask(mService, dialog, callback);
    }

    @Override
    public CloudRequestTask createFolderTask(GenericCallback callback) {
        return new CreateFolderDropboxTask(mService, callback);
    }

    @Override
    public CloudRequestTask deleteFileTask(GenericCallback callback) {
        return new DeleteFileDropboxTask(mService, callback);
    }

    @Override
    public CloudRequestTask uploadFileTask(File file, ProgressDialog dialog, GenericCallback callback) {
        return new UploadFileDropboxTask(mService, file, dialog, callback);
    }

    @Override
    public CloudRequestTask downloadFileTask(ProgressDialog dialog, DownloadFileCallback callback) {
        return new DownloadFileDropboxTask(mService, dialog, callback);
    }
}
