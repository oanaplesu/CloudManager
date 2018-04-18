package utils.tasks.dropbox;

import android.os.AsyncTask;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;

import utils.exceptions.DropboxUniqueFolderNameException;
import utils.services.CloudService;
import utils.tasks.CloudRequestTask;

public class CreateFolderDropboxTask extends AsyncTask<String, Void, Void>
        implements CloudRequestTask {
    private CloudService.GenericCallback mCallback;
    private Exception mException;
    private DbxClientV2 mDbxClient;


    public CreateFolderDropboxTask(DbxClientV2 dbxClient,
                                   CloudService.GenericCallback callback) {
        this.mDbxClient = dbxClient;
        this.mCallback = callback;
    }

    @Override
    protected Void doInBackground(String... args) {
        String folderId = args[0];
        String newFolderName = args[1];

        try {
            mDbxClient.files().createFolderV2(folderId + "/" + newFolderName);
        } catch (CreateFolderErrorException err) {
            if (err.errorValue.isPath() && err.errorValue.getPathValue().isConflict()) {
                mException = new DropboxUniqueFolderNameException();
            } else {
                mException = err;
            }
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
