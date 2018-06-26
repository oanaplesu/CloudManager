package utils.tasks.onedrive;

import android.app.ProgressDialog;
import android.util.Log;

import com.onedrive.sdk.concurrency.ICallback;
import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.extensions.IDriveRequestBuilder;
import com.onedrive.sdk.extensions.IItemCollectionPage;
import com.onedrive.sdk.extensions.IItemRequestBuilder;
import com.onedrive.sdk.extensions.Item;

import java.util.ArrayList;
import java.util.List;

import utils.cloud.AccountType;
import utils.cloud.CloudResource;
import utils.services.CloudService;
import utils.services.OneDriveService;
import utils.tasks.CloudRequestTask;

public class GetFilesFromOneDriveTask extends OneDriveRequestTask {
    private ProgressDialog mDialog;
    private final CloudService.GetFilesCallback mCallback;
    private String mAccountEmail;
    private List<CloudResource> mFiles;


    public GetFilesFromOneDriveTask(OneDriveService onedriveService,
								    String accountEmail,
								    ProgressDialog dialog,
								    CloudService.GetFilesCallback callback) {
        this.mOnedriveService = onedriveService;
        this.mCallback = callback;
        this.mDialog = dialog;
        this.mAccountEmail = accountEmail;
    }

    @Override
    protected void onPreExecute() {
        if(mDialog != null) {
            mDialog.setMessage("Loading files");
            mDialog.show();
        }

        mFiles = new ArrayList<>();
    }

    @Override
    protected void executeTaskInternal(String... args) {
        String folderId = args[0];

		IItemRequestBuilder folder;

		if(folderId.isEmpty()) {
			folder = mClient.getRoot();
		} else {
			folder = mClient.getItems(folderId);
		}

		folder.getChildren()
				.buildRequest()
				.get(new ICallback<IItemCollectionPage>() {
					@Override
					public void success(final IItemCollectionPage result) {
						if (result == null) {
							return;
						}

						for (final Item file : result.getCurrentPage()) {
                            String mimeType = CloudResource.getMimeTypeFromFileName(file.name);

                            if(file.folder != null) {
								  mFiles.add(0, new CloudResource(
									AccountType.ONEDRIVE,
									CloudResource.Type.FOLDER,
									file.name,
									mimeType,
									file.id,
									mAccountEmail
									)
								);
							} else {
								mFiles.add(new CloudResource(
									AccountType.ONEDRIVE,
									CloudResource.Type.FILE,
									file.name,
									mimeType,
									file.id,
									mAccountEmail
									)
								);
							}
						}

                        folder.buildRequest().get(new ICallback<Item>() {
                            @Override
                            public void success(Item item) {
                                String parentFolderId = item.parentReference.id;
                                if(parentFolderId == null) {
                                    parentFolderId = "";
                                }

                                mFiles.add(0, new CloudResource(
                                                AccountType.GOOGLE_DRIVE,
                                                CloudResource.Type.FOLDER,
                                                "...",
                                                "",
                                                parentFolderId,
                                                mAccountEmail
                                        )
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
					public void failure(ClientException ex) {
						mException = ex;
						onPostExecute();
					}
				});
    }

    @Override
    protected void onPostExecute() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete(mFiles);
        }
    }
}