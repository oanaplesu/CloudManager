package utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.facebook.stetho.inspector.console.CLog;
import com.google.api.services.drive.model.File;

import java.util.ArrayList;
import java.util.List;

import db.AppDatabase;


public class GetFilesFromDropboxTask extends AsyncTask<String, Void, ArrayList<CloudResource>> {
    private ProgressDialog mDialog;
    private AppDatabase mDatabase;
    private final GetFilesCallback mCallback;
    private Exception mException;


    public GetFilesFromDropboxTask(AppDatabase database, ProgressDialog dialog,
                                   GetFilesCallback callback) {
        this.mCallback = callback;
        this.mDatabase = database;
        this.mDialog = dialog;
    }

    private DbxClientV2 getDbxClient(String accesToken) {
        DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("dropbox")
                .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                .build();
        return new DbxClientV2(requestConfig, accesToken);
    }

    @Override
    protected void onPreExecute() {
        mDialog.setMessage("Loading files");
        mDialog.show();
    }

    @Override
    protected ArrayList<CloudResource> doInBackground(String... args) {
        String accountEmail = args[0];
        String folderId = args[1];

        String token = mDatabase.dropboxUserDao().getTokenForAccount(accountEmail);
        DbxClientV2 dbxClient = getDbxClient(token);

        try {
            ListFolderResult dropboxFiles = dbxClient.files().listFolder(folderId);
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
}
