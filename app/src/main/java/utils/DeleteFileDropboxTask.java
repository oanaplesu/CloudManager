package utils;

import android.os.AsyncTask;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;

import db.AppDatabase;

public class DeleteFileDropboxTask extends AsyncTask<String, Void, Void> {
        private DeleteFileCallback mCallback;
        private AppDatabase mDatabase;
        private Exception mException;

        public DeleteFileDropboxTask(AppDatabase database,
                                       DeleteFileCallback callback) {
            this.mDatabase = database;
            this.mCallback = callback;
        }

        private DbxClientV2 getDbxClient(String accesToken) {
            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("dropbox")
                    .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                    .build();
            return new DbxClientV2(requestConfig, accesToken);
        }

        @Override
        protected Void doInBackground(String... args) {
            String accountEmail = args[0];
            String fileId = args[1];

            String token = mDatabase.dropboxUserDao().getTokenForAccount(accountEmail);
            DbxClientV2 dbxClient = getDbxClient(token);

            try {
                dbxClient.files().deleteV2(fileId);
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
}
