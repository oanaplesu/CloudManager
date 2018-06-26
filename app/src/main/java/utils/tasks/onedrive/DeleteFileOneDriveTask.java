package utils.tasks.onedrive;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.dropbox.core.v2.DbxClientV2;
import com.facebook.stetho.inspector.console.CLog;
import com.onedrive.sdk.concurrency.ICallback;
import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.extensions.Folder;
import com.onedrive.sdk.extensions.IItemRequestBuilder;
import com.onedrive.sdk.extensions.Item;

import java.util.List;

import utils.cloud.AccountType;
import utils.cloud.CloudResource;
import utils.services.CloudService;
import utils.services.OneDriveService;
import utils.tasks.CloudRequestTask;

public class DeleteFileOneDriveTask extends OneDriveRequestTask {
    private ProgressDialog mDialog;
    private final CloudService.GenericCallback mCallback;

    public DeleteFileOneDriveTask(OneDriveService service,
                                  CloudService.GenericCallback callback) {
        this.mOnedriveService = service;
        this.mCallback = callback;
    }

    @Override
    protected void executeTaskInternal(String... args)  {
        String fileId = args[0];

        mClient.getItems(fileId)
                .buildRequest()
                .delete(new ICallback<Void>() {
                    @Override
                    public void success(Void aVoid) {
                        onPostExecute();
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
            mDialog.setMessage("Deleting file");
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
            mCallback.onComplete();
        }
    }
}
