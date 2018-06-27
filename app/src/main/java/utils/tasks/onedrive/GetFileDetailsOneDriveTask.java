package utils.tasks.onedrive;

import android.app.ProgressDialog;

import com.onedrive.sdk.concurrency.ICallback;
import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.extensions.Item;

import java.text.SimpleDateFormat;
import java.util.Locale;

import utils.cloud.CloudResource;
import utils.services.CloudService;
import utils.services.OneDriveService;


public class GetFileDetailsOneDriveTask extends OneDriveRequestTask {
    private ProgressDialog mDialog;
    private final CloudService.GetFileDetailsCallback mCallback;
    private CloudService.FileDetails mDetails;

    public GetFileDetailsOneDriveTask(OneDriveService service, ProgressDialog dialog,
                                      CloudService.GetFileDetailsCallback callback) {
        this.mOnedriveService = service;
        this.mCallback = callback;
        this.mDialog = dialog;
        this.mDetails = new CloudService.FileDetails();
    }

    @Override
    protected void executeTaskInternal(String... args)  {
        String fileId = args[0];

        mClient.getItems(fileId)
                .buildRequest()
                .get(new ICallback<Item>() {
                    @Override
                    public void success(Item item) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);

                        mDetails.title = item.name;
                        mDetails.size = item.size;
                        mDetails.dateCreated = sdf.format(item.createdDateTime.getTime()); ;
                        mDetails.dateModified = sdf.format(item.lastModifiedDateTime.getTime());
                        mDetails.type = CloudResource.getMimeTypeFromFileName(mDetails.title);

                        if(item.folder != null) {
                            mDetails.type = "folder";
                        }

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
            mDialog.setMessage("Requesting details");
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
            mCallback.onComplete(mDetails);
        }
    }
}
