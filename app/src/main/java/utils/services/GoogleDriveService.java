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

import utils.tasks.google.DeleteFileGoogleDriveTask;
import utils.tasks.google.GetFilesFromGoogleDriveTask;
import utils.tasks.CloudRequestTask;
import utils.tasks.google.CreateFolderGoogleDriveTask;
import utils.tasks.google.UploadFileGoogleDriveTask;

public class GoogleDriveService implements CloudService {
    private Drive mService;

    public GoogleDriveService(Context context, String accountEmail) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE));
        credential.setSelectedAccountName(accountEmail);

        mService = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
                new GsonFactory(), credential).build();
    }

    @Override
    public CloudRequestTask getFilesTask(ProgressDialog dialog, GetFilesCallback callback) {
        return new GetFilesFromGoogleDriveTask(mService, dialog, callback);
    }

    @Override
    public CloudRequestTask createFolderTask(GenericCallback callback) {
        return new CreateFolderGoogleDriveTask(mService, callback);
    }

    @Override
    public CloudRequestTask deleteFileTask(GenericCallback callback) {
        return new DeleteFileGoogleDriveTask(mService, callback);
    }

    @Override
    public CloudRequestTask uploadFileTask(File file, ProgressDialog dialog, GenericCallback callback) {
        return new UploadFileGoogleDriveTask(mService, file, dialog, callback);
    }
}
