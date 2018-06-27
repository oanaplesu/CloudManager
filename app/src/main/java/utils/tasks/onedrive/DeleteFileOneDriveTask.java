package utils.tasks.onedrive;

import android.app.ProgressDialog;

import com.onedrive.sdk.concurrency.ICallback;
import com.onedrive.sdk.core.ClientException;

import utils.services.CloudService;
import utils.services.OneDriveService;

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
