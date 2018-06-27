package utils.tasks.onedrive;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.onedrive.sdk.concurrency.ICallback;
import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.extensions.Drive;

import utils.db.AppDatabase;
import utils.services.CloudService;
import utils.services.OneDriveService;


public class GetAccountDetailsOneDriveTask extends OneDriveRequestTask {
    private ProgressDialog mDialog;
    private final CloudService.GetAccountDetailsCallback mCallback;
    private CloudService.AccountDetails mDetails;
    private AppDatabase mDatabase;
    private String mEmail;


    public GetAccountDetailsOneDriveTask(OneDriveService service,
                                         Context context,
                                         String email,
                                         ProgressDialog dialog,
                                         CloudService.GetAccountDetailsCallback callback) {
        this.mOnedriveService = service;
        this.mCallback = callback;
        this.mDetails = new CloudService.AccountDetails();
        this.mDatabase = AppDatabase.getDatabase(context);
        this.mEmail = email;
        this.mDialog = dialog;
    }

    @Override
    protected void executeTaskInternal(String... args)  {
        mClient.buildRequest().get(new ICallback<Drive>() {
            @Override
            public void success(Drive drive) {
                mDetails.totalStorage = drive.quota.total;
                mDetails.usedStorage = drive.quota.used;

                new GetDetailsTask(mDatabase, mEmail, new GetDetailsTask.Callback() {
                    @Override
                    public void onComplete(String name) {
                        mDetails.name = name;
                        onPostExecute();
                    }
                }).execute();
            }

            @Override
            public void failure(ClientException ex) {
                mException = ex;
                onPostExecute();
            }
        });
    }

    @Override
    protected void onPreExecute() {
        if(mDialog != null) {
            mDialog.setMessage("Requesting informations");
            mDialog.show();
        }
    }

    @Override
    protected void onPostExecute() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete(mDetails);
        }
    }

    private static class GetDetailsTask extends AsyncTask<Void, Void, Void> {
        private AppDatabase db;
        private String email;
        private String name;
        private Callback mCallback;

        public interface Callback {
            void onComplete(String name);
        }

        public GetDetailsTask(AppDatabase db, String email, Callback callback) {
            this.db = db;
            this.mCallback = callback;
            this.email = email;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            name = db.oneDriveUserDao().getNameForAccount(email);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mCallback.onComplete(name);
        }
    }
}
