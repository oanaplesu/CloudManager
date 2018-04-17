package utils;


import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class DeleteFileGoogleDriveTask extends AsyncTask<String, Void, Void> {
    private GoogleAccountCredential mCredential;
    private DeleteFileCallback mCallback;
    private Exception mException;

    private final static String GOOGLE_DRIVE_FOLDER_MIME_TYPE
            = "application/vnd.google-apps.folder";
    private final static String GOOGLE_DRIVE_ROOT_FOLDER = "root";


    public DeleteFileGoogleDriveTask(GoogleAccountCredential credential,
                                     DeleteFileCallback callback) {
        this.mCredential = credential;
        this.mCallback = callback;
    }

    @Override
    protected Void doInBackground(String... args) {
        String accountEmail = args[0];
        String fileId = args[1];

        mCredential.setSelectedAccountName(accountEmail);
        Drive service = new Drive.Builder(AndroidHttp.newCompatibleTransport(),
                new GsonFactory(), mCredential).build();

        try {
            service.files().delete(fileId).execute();
        } catch (IOException e) {
            mException = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void voids) {
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete();
        }
    }
}
