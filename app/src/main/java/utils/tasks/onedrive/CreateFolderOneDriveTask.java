package utils.tasks.onedrive;

import android.app.ProgressDialog;

import com.onedrive.sdk.concurrency.ICallback;
import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.extensions.Folder;
import com.onedrive.sdk.extensions.IItemRequestBuilder;
import com.onedrive.sdk.extensions.Item;

import utils.cloud.AccountType;
import utils.cloud.CloudResource;
import utils.services.CloudService;
import utils.services.OneDriveService;

public class CreateFolderOneDriveTask extends OneDriveRequestTask {
    private ProgressDialog mDialog;
    private final CloudService.CreateFolderCallback mCallback;
    private String mAccountEmail;
    CloudResource mCreatedFolder;


    public CreateFolderOneDriveTask(OneDriveService service,
                                    String accountEmail,
                                    CloudService.CreateFolderCallback callback) {
        this.mOnedriveService = service;
        this.mCallback = callback;
        this.mAccountEmail = accountEmail;
    }

    @Override
    protected void executeTaskInternal(String... args)  {
        String folderId = args[0];
        String newFolderName = args[1];

        IItemRequestBuilder folder;

        if(folderId.isEmpty()) {
            folder = mClient.getRoot();
        } else {
            folder = mClient.getItems(folderId);
        }

        final Item folderToCreate = new Item();
        folderToCreate.name = newFolderName;
        folderToCreate.folder = new Folder();

        folder.getChildren()
                .buildRequest()
                .create(folderToCreate, new ICallback<Item>() {
                    @Override
                    public void success(Item item) {
                        String mimeType = CloudResource.getMimeTypeFromFileName(item.name);

                        mCreatedFolder = new CloudResource(
                                AccountType.DROPBOX,
                                CloudResource.Type.FILE,
                                item.name,
                                mimeType,
                                item.id,
                                mAccountEmail
                        );
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
            mDialog.setMessage("Creating new folder");
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
            mCallback.onComplete(mCreatedFolder);
        }
    }
}
