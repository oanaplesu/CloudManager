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
    private Drive mService;
    private DeleteFileCallback mCallback;
    private Exception mException;


    public DeleteFileGoogleDriveTask(Drive service,
                                     DeleteFileCallback callback) {
        this.mService = service;
        this.mCallback = callback;
    }

    @Override
    protected Void doInBackground(String... args) {
        String fileId = args[0];

        try {
            mService.files().delete(fileId).execute();
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
