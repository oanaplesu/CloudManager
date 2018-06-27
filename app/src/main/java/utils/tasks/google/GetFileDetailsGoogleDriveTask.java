package utils.tasks.google;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import utils.services.CloudService;
import utils.tasks.CloudRequestTask;


public class GetFileDetailsGoogleDriveTask extends AsyncTask<String, Void, Void>
        implements CloudRequestTask {
    private Drive mService;
    private CloudService.GetFileDetailsCallback mCallback;
    private Exception mException;
    private CloudService.FileDetails mDetails;
    private ProgressDialog mDialog;
    private final static String GOOGLE_DRIVE_FOLDER_MIME_TYPE
            = "application/vnd.google-apps.folder";


    public GetFileDetailsGoogleDriveTask(Drive service, ProgressDialog dialog,
                                         CloudService.GetFileDetailsCallback callback) {
        this.mService = service;
        this.mCallback = callback;
        this.mDetails = new CloudService.FileDetails();
        this.mDialog = dialog;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if(mDialog != null) {
            mDialog.setMessage("Requesting details");
            mDialog.show();
        }
    }

    @Override
    protected Void doInBackground(String... args) {
        String fileId = args[0];

        try {
            File file = mService.files().get(fileId).execute();

            mDetails.title = file.getTitle();
            mDetails.type = file.getMimeType();
            mDetails.dateCreated = getFormatedDate(file.getCreatedDate().toString());
            mDetails.dateModified = getFormatedDate(file.getModifiedDate().toString());
            if (!file.getMimeType().equals(GOOGLE_DRIVE_FOLDER_MIME_TYPE)) {
                mDetails.size = file.getFileSize();
            } else {
                mDetails.type = "folder";
            }
        } catch (IOException e) {
            mException = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void voids) {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete(mDetails);
        }
    }

    @Override
    public void executeTask(String... args) {
        execute(args);
    }

    private static String getFormatedDate(String dateString) {
        DateTimeFormatter format = DateTimeFormatter.ISO_INSTANT;
        Instant dateInstant = Instant.from(format.parse(dateString));
        LocalDateTime date = LocalDateTime.ofInstant(dateInstant, ZoneId.of(ZoneOffset.UTC.getId()));
        String formatDateTime = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        return formatDateTime;
    }
}
