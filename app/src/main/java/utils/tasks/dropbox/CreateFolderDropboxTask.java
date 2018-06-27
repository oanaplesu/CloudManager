package utils.tasks.dropbox;

import android.os.AsyncTask;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.CreateFolderResult;
import com.dropbox.core.v2.files.Metadata;

import utils.cloud.AccountType;
import utils.cloud.CloudResource;
import utils.exceptions.DropboxUniqueFolderNameException;
import utils.services.CloudService;
import utils.tasks.CloudRequestTask;

public class CreateFolderDropboxTask extends AsyncTask<String, Void, CloudResource>
        implements CloudRequestTask {
    private CloudService.CreateFolderCallback mCallback;
    private Exception mException;
    private DbxClientV2 mDbxClient;
    private String mAccountEmail;


    public CreateFolderDropboxTask(DbxClientV2 dbxClient,
                                   String accountEmail,
                                   CloudService.CreateFolderCallback callback) {
        this.mDbxClient = dbxClient;
        this.mCallback = callback;
        this.mAccountEmail = accountEmail;
    }

    @Override
    protected CloudResource doInBackground(String... args) {
        String folderId = args[0];
        String newFolderName = args[1];

        try {
            CreateFolderResult result =  mDbxClient.files().createFolderV2(folderId + "/" + newFolderName);
            Metadata file = result.getMetadata();
            String mimeType = CloudResource.getMimeTypeFromFileName(file.getName());

            return new CloudResource(
                    AccountType.DROPBOX,
                    CloudResource.Type.FILE,
                    file.getName(),
                    mimeType,
                    folderId + "/" + file.getName(),
                    mAccountEmail
            );
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
    protected void onPostExecute(CloudResource createdFolder) {
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete(createdFolder);
        }
    }

    @Override
    public void executeTask(String... args) {
        execute(args);
    }
}
