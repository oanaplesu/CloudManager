package utils.services;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;

import java.io.File;

import utils.cloud.CloudResource;
import utils.db.AppDatabase;
import utils.tasks.CloudRequestTask;
import utils.tasks.MoveFilesTask;
import utils.tasks.dropbox.CreateFolderDropboxTask;
import utils.tasks.dropbox.DeleteFileDropboxTask;
import utils.tasks.dropbox.DownloadFileDropboxTask;
import utils.tasks.dropbox.GetAccountDetailsDropboxTask;
import utils.tasks.dropbox.GetFileDetailsDropboxTask;
import utils.tasks.dropbox.GetFilesFromDropboxTask;
import utils.tasks.dropbox.UploadFileDropboxTask;
import utils.tasks.google.GetAccountDetailsGoogleDriveTask;


public class DropboxService implements CloudService {
    private DbxClientV2 mService;
    private String mAccountEmail;

    public DropboxService(Context context, String accountEmail) {
        mAccountEmail = accountEmail;
        AppDatabase database = AppDatabase.getDatabase(context);

        String token = database.dropboxUserDao().getTokenForAccount(accountEmail);

        DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("dropbox")
                .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                .build();
        mService = new DbxClientV2(requestConfig, token);
    }

    @Override
    public CloudRequestTask getFilesTask(ProgressDialog dialog, GetFilesCallback callback) {
        return new GetFilesFromDropboxTask(mService, mAccountEmail, dialog, callback);
    }

    @Override
    public CloudRequestTask createFolderTask(CreateFolderCallback callback) {
        return new CreateFolderDropboxTask(mService, mAccountEmail, callback);
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
    public CloudRequestTask downloadFileTask(ProgressDialog dialog, boolean saveTmp, DownloadFileCallback callback) {
        return new DownloadFileDropboxTask(mService, dialog, saveTmp, callback);
    }

    @Override
    public CloudRequestTask moveFilesTask(CloudResource sourceFile, Context context,
                                          boolean moveOriginal, MoveFilesCallback callback) {
        return new MoveFilesTask(this, sourceFile, moveOriginal, context, callback);
    }

    @Override
    public CloudRequestTask getAccountDetailsTask(GetAccountDetailsCallback callback) {
        return new GetAccountDetailsDropboxTask(mService, callback);
    }

    @Override
    public CloudRequestTask getFileDetailsTask(ProgressDialog dialog, GetFileDetailsCallback callback) {
        return new GetFileDetailsDropboxTask(mService, dialog, callback);
    }
}
