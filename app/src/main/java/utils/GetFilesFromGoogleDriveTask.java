package utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;


import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;
import com.oanaplesu.cloudmanager.MainActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GetFilesFromGoogleDriveTask extends AsyncTask<String, Void, List<CloudResource>> {
    private ProgressDialog mDialog;
    private Drive mService;
    private final GetFilesCallback mCallback;
    private Exception mException;

    private final static String GOOGLE_DRIVE_FOLDER_MIME_TYPE
            = "application/vnd.google-apps.folder";
    private final static String GOOGLE_DRIVE_ROOT_FOLDER = "root";


    public GetFilesFromGoogleDriveTask(Drive service,
                                       ProgressDialog dialog, GetFilesCallback callback) {
        this.mService = service;
        this.mCallback = callback;
        this.mDialog = dialog;
    }

    @Override
    protected void onPreExecute() {
        mDialog.setMessage("Loading files");
        mDialog.show();
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
                            CloudResource.Provider.GOOGLE_DRIVE,
                            CloudResource.Type.FOLDER,
                            file.getTitle(),
                            file.getMimeType(),
                            file.getId()
                            )
                    );
                } else {
                    files.add(new CloudResource(
                            CloudResource.Provider.GOOGLE_DRIVE,
                            CloudResource.Type.FILE,
                            file.getTitle(),
                            file.getMimeType(),
                            file.getId()
                            )
                    );
                }
            }

            File folder = mService.files().get(folderId).execute();
            String parentFolderId = folder.getParents().isEmpty()
                    ? "" :folder.getParents().get(0).getId();

            files.add(0, new CloudResource(
                            CloudResource.Provider.GOOGLE_DRIVE,
                            CloudResource.Type.FOLDER,
                            "...",
                            "",
                            parentFolderId
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

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete(files);
        }
    }
}
