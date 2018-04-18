package utils.tasks.google;


import android.os.AsyncTask;

import com.google.api.services.drive.Drive;

import java.io.IOException;

import utils.services.CloudService;
import utils.tasks.CloudRequestTask;

public class DeleteFileGoogleDriveTask extends AsyncTask<String, Void, Void>
        implements CloudRequestTask{
    private Drive mService;
    private CloudService.GenericCallback mCallback;
    private Exception mException;


    public DeleteFileGoogleDriveTask(Drive service,
                                     CloudService.GenericCallback callback) {
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

    @Override
    public void executeTask(String... args) {
        execute(args);
    }
}
