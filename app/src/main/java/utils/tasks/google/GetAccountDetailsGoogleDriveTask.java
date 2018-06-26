package utils.tasks.google;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.User;

import java.io.IOException;
import java.io.InputStream;

import utils.services.CloudService;
import utils.tasks.CloudRequestTask;


public class GetAccountDetailsGoogleDriveTask extends AsyncTask<String, Void, Void>
        implements CloudRequestTask {
    private Drive mService;
    private CloudService.GetAccountDetailsCallback mCallback;
    private Exception mException;
    private CloudService.AccountDetails mDetails;
    private ProgressDialog mDialog;


    public GetAccountDetailsGoogleDriveTask(Drive service,
                                            ProgressDialog dialog,
                                            CloudService.GetAccountDetailsCallback callback) {
        this.mService = service;
        this.mCallback = callback;
        this.mDetails = new CloudService.AccountDetails();
        this.mDialog = dialog;
    }

    @Override
    protected Void doInBackground(String... args) {
        About accountDetails = null;

        try {
            accountDetails = mService.about().get().execute();
            mDetails.name = accountDetails.getUser().getDisplayName();
            mDetails.totalStorage = accountDetails.getQuotaBytesTotal();
            mDetails.usedStorage = accountDetails.getQuotaBytesUsed();

            User.Picture picture = accountDetails.getUser().getPicture();
            if(picture != null) {
                String photoUrl = picture.getUrl();
                InputStream in = new java.net.URL(photoUrl).openStream();
                mDetails.photo = BitmapFactory.decodeStream(in);
            }
        } catch (Exception e) {
            mException = e;
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if(mDialog != null) {
            mDialog.setMessage("Requesting informations");
            mDialog.show();
        }
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
}
