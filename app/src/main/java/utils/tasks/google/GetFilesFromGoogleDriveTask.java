package utils.tasks.google;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.cloud.AccountType;
import utils.cloud.CloudResource;
import utils.services.CloudService;
import utils.tasks.CloudRequestTask;


public class GetFilesFromGoogleDriveTask extends AsyncTask<String, Void, List<CloudResource>>
        implements CloudRequestTask {
    private ProgressDialog mDialog;
    private Drive mService;
    private final CloudService.GetFilesCallback mCallback;
    private Exception mException;
    private String mAccountEmail;

    private final static String GOOGLE_DRIVE_FOLDER_MIME_TYPE
            = "application/vnd.google-apps.folder";
    private final static String GOOGLE_DRIVE_ROOT_FOLDER = "root";


    public GetFilesFromGoogleDriveTask(Drive service,
                                       String accountEmail,
                                       ProgressDialog dialog,
                                       CloudService.GetFilesCallback callback) {
        this.mService = service;
        this.mCallback = callback;
        this.mDialog = dialog;
        this.mAccountEmail = accountEmail;
    }

    @Override
    protected void onPreExecute() {
        if(mDialog != null) {
            mDialog.setMessage("Loading files");
            mDialog.show();
        }
    }

    @Override
    protected List<CloudResource> doInBackground(String... args) {
        String folderId = args[0].equals("") ? GOOGLE_DRIVE_ROOT_FOLDER : args[0];

        try {
            ChildList driveFiles = mService.children().list(folderId).execute();
            ArrayList<CloudResource> files = new ArrayList<>();

            for(ChildReference fileReference : driveFiles.getItems()) {
                File file = mService.files().get(fileReference.getId()).execute();

                if (file.getMimeType().equals(GOOGLE_DRIVE_FOLDER_MIME_TYPE)) {
                    files.add(0, new CloudResource(
                            AccountType.GOOGLE_DRIVE,
                            CloudResource.Type.FOLDER,
                            file.getTitle(),
                            file.getMimeType(),
                            file.getId(),
                            mAccountEmail
                            )
                    );
                } else {
                    files.add(new CloudResource(
                            AccountType.GOOGLE_DRIVE,
                            CloudResource.Type.FILE,
                            file.getTitle(),
                            file.getMimeType(),
                            file.getId(),
                            mAccountEmail
                            )
                    );
                }
            }

            File folder = mService.files().get(folderId).execute();
            String parentFolderId = folder.getParents().isEmpty()
                    ? "" :folder.getParents().get(0).getId();

            files.add(0, new CloudResource(
                            AccountType.GOOGLE_DRIVE,
                            CloudResource.Type.FOLDER,
                            "...",
                            "",
                            parentFolderId,
                            mAccountEmail
                    )
            );

            return files;
        } catch (IOException e) {
            mException = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<CloudResource> files) {
        super.onPostExecute(files);

        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete(files);
        }
    }

    @Override
    public void executeTask(String... args) {
        execute(args);
    }
}
