package utils;

import android.content.Context;
import android.os.AsyncTask;


import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GetFilesFromGoogleDriveTask extends AsyncTask<String, Void, List<CloudResource>> {
    private Context mContext;
    private final Callback mCallback;
    private Exception mException;
    private final static String GOOGLE_DRIVE_FOLDER_MIME_TYPE
            = "application/vnd.google-apps.folder";

    public GetFilesFromGoogleDriveTask(Context context, Callback callback) {
        this.mContext = context;
        this.mCallback = callback;
    }

    public interface Callback {
        void onComplete(List<CloudResource> files);
        void onError(Exception e);
    }

    @Override
    protected List<CloudResource> doInBackground(String... args) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                mContext, Collections.singleton(DriveScopes.DRIVE));
        credential.setSelectedAccountName(args[0]);

        Drive service = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
                new GsonFactory(), credential).build();

        try {
            String folderId = args[1].equals("") ? "root" : args[1];
            ChildList driveFiles = service.children().list(folderId).execute();
            List<CloudResource> files = new ArrayList<>();


            for(ChildReference fileReference : driveFiles.getItems()) {
                File file = service.files().get(fileReference.getId()).execute();

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

            File folder = service.files().get(folderId).execute();
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

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete(files);
        }
    }
}
