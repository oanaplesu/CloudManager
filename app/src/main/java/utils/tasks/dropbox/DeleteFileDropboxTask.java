package utils.tasks.dropbox;

import android.os.AsyncTask;

import com.dropbox.core.v2.DbxClientV2;

import utils.services.CloudService;
import utils.tasks.CloudRequestTask;

public class DeleteFileDropboxTask extends AsyncTask<String, Void, Void>
        implements CloudRequestTask {
        private DbxClientV2 mDbxClient;
        private CloudService.GenericCallback mCallback;
        private Exception mException;

        public DeleteFileDropboxTask(DbxClientV2 dbxClient,
                                     CloudService.GenericCallback callback) {
            this.mDbxClient = dbxClient;
            this.mCallback = callback;
        }

        @Override
        protected Void doInBackground(String... args) {
            String fileId = args[0];

            try {
                mDbxClient.files().deleteV2(fileId);
            } catch (Exception err) {
                mException = err;
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
