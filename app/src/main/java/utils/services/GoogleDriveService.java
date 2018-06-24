package utils.services;


import android.app.ProgressDialog;
import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.util.Collections;

import utils.cloud.CloudResource;
import utils.tasks.MoveFilesTask;
import utils.tasks.dropbox.GetFileDetailsDropboxTask;
import utils.tasks.google.DeleteFileGoogleDriveTask;
import utils.tasks.google.DownloadFileGoogleDriveTask;
import utils.tasks.google.GetAccountDetailsGoogleDriveTask;
import utils.tasks.google.GetFileDetailsGoogleDriveTask;
import utils.tasks.google.GetFilesFromGoogleDriveTask;
import utils.tasks.CloudRequestTask;
import utils.tasks.google.CreateFolderGoogleDriveTask;
import utils.tasks.google.UploadFileGoogleDriveTask;

public class GoogleDriveService implements CloudService {
    private Drive mService;
    private String mAccountEmail;

    public GoogleDriveService(Context context, String accountEmail) {
        this.mAccountEmail = accountEmail;
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE));
        credential.setSelectedAccountName(accountEmail);

        mService = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
                new GsonFactory(), credential).build();
    }

    @Override
    public CloudRequestTask getFilesTask(ProgressDialog dialog, GetFilesCallback callback) {
        return new GetFilesFromGoogleDriveTask(mService, mAccountEmail, dialog, callback);
    }

    @Override
    public CloudRequestTask createFolderTask(CreateFolderCallback callback) {
        return new CreateFolderGoogleDriveTask(mService, mAccountEmail, callback);
    }

    @Override
    public CloudRequestTask deleteFileTask(GenericCallback callback) {
        return new DeleteFileGoogleDriveTask(mService, callback);
    }

    @Override
    public CloudRequestTask uploadFileTask(File file, ProgressDialog dialog, GenericCallback callback) {
        return new UploadFileGoogleDriveTask(mService, file, dialog, callback);
    }

    @Override
    public CloudRequestTask downloadFileTask(ProgressDialog dialog, boolean saveTmp, DownloadFileCallback callback) {
        return new DownloadFileGoogleDriveTask(mService, dialog, saveTmp, callback);
    }

    @Override
    public CloudRequestTask moveFilesTask(CloudResource sourceFile, Context context, boolean deleteOriginal, MoveFilesCallback callback) {
        return new MoveFilesTask(this, sourceFile, deleteOriginal, context, callback);
    }

    @Override
    public CloudRequestTask getAccountDetailsTask(GetAccountDetailsCallback callback) {
        return new GetAccountDetailsGoogleDriveTask(mService, callback);
    }

    @Override
    public CloudRequestTask getFileDetailsTask(ProgressDialog dialog, GetFileDetailsCallback callback) {
        return new GetFileDetailsGoogleDriveTask(mService, dialog, callback);
    }
}
