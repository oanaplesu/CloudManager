package utils.tasks.dropbox;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.util.ArrayList;

import utils.cloud.CloudResource;
import utils.services.CloudService;
import utils.tasks.CloudRequestTask;


public class GetFilesFromDropboxTask extends AsyncTask<String, Void, ArrayList<CloudResource>>
        implements CloudRequestTask {
    private ProgressDialog mDialog;
    private DbxClientV2 mDbxClient;
    private final CloudService.GetFilesCallback mCallback;
    private Exception mException;


    public GetFilesFromDropboxTask(DbxClientV2 dbxClient, ProgressDialog dialog,
                                   CloudService.GetFilesCallback callback) {
        this.mCallback = callback;
        this.mDbxClient = dbxClient;
        this.mDialog = dialog;
    }

    @Override
    protected void onPreExecute() {
        mDialog.setMessage("Loading files");
        mDialog.show();
    }

    @Override
    protected ArrayList<CloudResource> doInBackground(String... args) {
        String folderId = args[0];

        try {
            ListFolderResult dropboxFiles = mDbxClient.files().listFolder(folderId);
            ArrayList<CloudResource> files = new ArrayList<>();

            for(Metadata file : dropboxFiles.getEntries()) {
                String mimeType = CloudResource.getMimeTypeFromFileName(file.getName());

                if (file instanceof FolderMetadata) {
                    files.add(0, new CloudResource(
                                    CloudResource.Provider.DROPBOX,
                                    CloudResource.Type.FOLDER,
                                    file.getName(),
                                    mimeType,
                                    folderId + "/" + file.getName()
                            )
                    );
                } else {
                    files.add(new CloudResource(
                            CloudResource.Provider.DROPBOX,
                            CloudResource.Type.FILE,
                            file.getName(),
                            mimeType,
                            folderId + "/" + file.getName()
                            )
                    );
                }
            }

            int index = folderId.lastIndexOf("/");
            String parentFolderId = index == -1
                    ? "" : folderId.substring(0, index);

            files.add(0, new CloudResource(
                            CloudResource.Provider.GOOGLE_DRIVE,
                            CloudResource.Type.FOLDER,
                            "...",
                            "",
                            parentFolderId
                    )
            );

            return files;
        } catch (ListFolderErrorException e) {
            mException = e;
        } catch (DbxException e) {
            mException = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<CloudResource> files) {
        super.onPostExecute(files);

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete(files);
        }
    }

    @Override
    public void executeTask(String... args) {
        execute(args);
    }
}
