package utils.tasks.onedrive;

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


    public GetAccountDetailsDropboxTask(DbxClientV2 dbxClient,
                                        CloudService.GetAccountDetailsCallback callback) {
        this.mDbxClient = dbxClient;
        this.mCallback = callback;
        this.mDetails = new CloudService.AccountDetails();
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
