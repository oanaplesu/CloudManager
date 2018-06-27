package utils.tasks.onedrive;


import android.app.ProgressDialog;

import com.google.android.gms.common.util.IOUtils;
import com.onedrive.sdk.concurrency.ICallback;
import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.extensions.IItemRequestBuilder;
import com.onedrive.sdk.extensions.Item;

import java.io.File;
import java.io.IOException;

import utils.services.CloudService;
import utils.services.OneDriveService;

public class UploadFileOneDriveTask extends OneDriveRequestTask {
    private ProgressDialog mDialog;
    private final CloudService.GenericCallback mCallback;
    private File mFile;

    public UploadFileOneDriveTask(OneDriveService service,
                                  java.io.File file, ProgressDialog dialog,
                                  CloudService.GenericCallback callback) {
        this.mOnedriveService = service;
        this.mFile = file;
        this.mCallback = callback;
    }

    @Override
    protected void executeTaskInternal(String... args)  {
        String folderId = args[0];
        String fileName = args[1];

        if (mFile == null) {
            mException = new Exception("error");
            onPostExecute();
        }

        IItemRequestBuilder folder;

        if(folderId.isEmpty()) {
            folder = mClient.getRoot();
        } else {
            folder = mClient.getItems(folderId);
        }

        byte[] fileBytes = null;

        try {
            fileBytes = IOUtils.toByteArray(mFile);
        } catch (IOException e) {
            mException = e;
            onPostExecute();
        }

        folder.getChildren()
                .byId(fileName)
                .getContent()
                .buildRequest()
                .put(fileBytes, new ICallback<Item>() {
                    @Override
                    public void success(Item item) {
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
            mDialog.setMessage("Uploading file");
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