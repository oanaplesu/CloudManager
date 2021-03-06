package utils.tasks.dropbox;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;

import java.io.InputStream;

import utils.services.CloudService;
import utils.tasks.CloudRequestTask;


public class GetAccountDetailsDropboxTask extends AsyncTask<String, Void, Void>
        implements CloudRequestTask {
    private DbxClientV2 mDbxClient;
    private CloudService.GetAccountDetailsCallback mCallback;
    private Exception mException;
    private CloudService.AccountDetails mDetails;
    private ProgressDialog mDialog;


    public GetAccountDetailsDropboxTask(DbxClientV2 dbxClient,
                                        ProgressDialog dialog,
                                        CloudService.GetAccountDetailsCallback callback) {
        this.mDbxClient = dbxClient;
        this.mCallback = callback;
        this.mDetails = new CloudService.AccountDetails();
        this.mDialog = dialog;
    }

    @Override
    protected Void doInBackground(String... args) {
        try {
            mDetails.name = mDbxClient.users().getCurrentAccount().getName().getDisplayName();
            mDetails.totalStorage = mDbxClient.users().getSpaceUsage()
                   .getAllocation().getIndividualValue().getAllocated();
            mDetails.usedStorage = mDbxClient.users().getSpaceUsage().getUsed();

            String photoUrl = mDbxClient.users().getCurrentAccount().getProfilePhotoUrl();
            if(photoUrl != null) {
                InputStream in = new java.net.URL(photoUrl).openStream();
                mDetails.photo = BitmapFactory.decodeStream(in);
            }
        } catch (DbxException e) {
            mException = e;
        } catch (Exception e) {
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
    protected void onPreExecute() {
        if(mDialog != null) {
            mDialog.setMessage("Requesting informations");
            mDialog.show();
        }

        super.onPreExecute();
    }

    @Override
    public void executeTask(String... args) {
        execute(args);
    }
}
