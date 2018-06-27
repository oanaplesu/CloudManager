package utils.tasks;

import android.os.AsyncTask;

import utils.db.AppDatabase;

public class DeleteAccountTask extends AsyncTask<String, Void, Void> {
    private Callback mCallback;
    private AppDatabase mDb;

    public interface Callback {
        void OnComplete();
    }

    public DeleteAccountTask(AppDatabase db, Callback callback) {
        this.mCallback = callback;
        this.mDb = db;
    }

    @Override
    protected Void doInBackground(String... strings) {
        String provider = strings[0];
        String email = strings[1];

        if(provider.equals("google")) {
            mDb.googleDriveUserDao().removeUser(email);
        } else if(provider.equals("dropbox")) {
            mDb.dropboxUserDao().removeUser(email);
        } else if(provider.equals("onedrive")) {
            mDb.oneDriveUserDao().removeUser(email);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void voids) {
        mCallback.OnComplete();
    }
}
