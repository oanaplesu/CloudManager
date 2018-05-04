package utils.tasks.google;


import android.os.AsyncTask;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;

import java.io.IOException;
import java.util.Collections;

import utils.cloud.AccountType;
import utils.cloud.CloudResource;
import utils.services.CloudService;
import utils.tasks.CloudRequestTask;

public class CreateFolderGoogleDriveTask extends AsyncTask<String, Void, CloudResource>
        implements CloudRequestTask {
    private Drive mService;
    private CloudService.CreateFolderCallback mCallback;
    private Exception mException;
    private String mAccountEmail;

    private final static String GOOGLE_DRIVE_FOLDER_MIME_TYPE
            = "application/vnd.google-apps.folder";
    private final static String GOOGLE_DRIVE_ROOT_FOLDER = "root";


    public CreateFolderGoogleDriveTask(Drive service, String accountEmail,
                                       CloudService.CreateFolderCallback callback) {
        this.mService = service;
        this.mCallback = callback;
        this.mAccountEmail = accountEmail;
    }

    @Override
    protected CloudResource doInBackground(String... args) {
        String folderId = args[0].equals("") ? GOOGLE_DRIVE_ROOT_FOLDER : args[0];
        String newFolderName = args[1];

        File fileMetadata = new File();
        fileMetadata.setTitle(newFolderName);
        fileMetadata.setMimeType(GOOGLE_DRIVE_FOLDER_MIME_TYPE);
        fileMetadata.setParents(Collections.singletonList(
            new ParentReference().setId(folderId)));

        try {
            File file = mService.files().insert(fileMetadata)
                    .setFields("id, parents")
                    .execute();

            return new CloudResource(
                    AccountType.GOOGLE_DRIVE,
                    CloudResource.Type.FILE,
                    file.getTitle(),
                    file.getMimeType(),
                    file.getId(),
                    mAccountEmail
            );
        } catch (IOException e) {
            mException = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(CloudResource createdFolder) {
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete(createdFolder);
        }
    }

    @Override
    public void executeTask(String... args) {
        execute(args);
    }
}
