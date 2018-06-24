package utils.tasks.dropbox;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import utils.cloud.CloudResource;
import utils.services.CloudService;
import utils.tasks.CloudRequestTask;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;


public class GetFileDetailsDropboxTask extends AsyncTask<String, Void, Void>
        implements CloudRequestTask {
    private DbxClientV2 mDbxClient;
    private CloudService.GetFileDetailsCallback mCallback;
    private Exception mException;
    private CloudService.FileDetails mDetails;
    private ProgressDialog mDialog;


    public GetFileDetailsDropboxTask(DbxClientV2 dbxClient, ProgressDialog dialog,
                                     CloudService.GetFileDetailsCallback callback) {
        this.mDbxClient = dbxClient;
        this.mCallback = callback;
        this.mDetails = new CloudService.FileDetails();
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
            Metadata metadata = mDbxClient.files().getMetadata(fileId);

            if(metadata instanceof FileMetadata) {
                FileMetadata data = (FileMetadata) metadata;
                mDetails.title = data.getName();
                mDetails.type = CloudResource.getMimeTypeFromFileName(mDetails.title);
                mDetails.dateCreated = null;

                Log.i("TEST", data.getClientModified().toString());
                if(data.getClientModified() != null) {
                    mDetails.dateModified = getFormatedDate(data.getClientModified().toString());
                }
                mDetails.size = data.getSize();
            } else {
                FolderMetadata data = (FolderMetadata) metadata;
                mDetails.title = data.getName();
                mDetails.type = "folder";
                mDetails.dateCreated = null;
                mDetails.dateModified = null;
                mDetails.size = 0;
            }

        } catch (DbxException e) {
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

    static String getFormatedDate(String dateString) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);

        LocalDateTime date = null;
        try{
            date = LocalDateTime.parse(dateString, format);
        }
        catch (Exception e) {
            return "";
        }

        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
