package utils.tasks.onedrive;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.MenuItem;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.google.api.client.util.IOUtils;
import com.onedrive.sdk.concurrency.ICallback;
import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.extensions.IItemRequestBuilder;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import utils.services.CloudService;
import utils.services.OneDriveService;
import utils.tasks.CloudRequestTask;


public class DownloadFileOneDriveTask extends OneDriveRequestTask {
    private ProgressDialog mDialog;
    private final CloudService.DownloadFileCallback mCallback;
    private boolean mSaveTmp;
    private File mFile;

    public DownloadFileOneDriveTask(OneDriveService service,
                                    ProgressDialog dialog,
                                    boolean saveTmp,
                                    CloudService.DownloadFileCallback callback) {
        this.mOnedriveService = service;
        this.mCallback = callback;
        this.mSaveTmp = saveTmp;
    }

    @Override
    protected void executeTaskInternal(String... args)  {
        String fileId = args[0];
        String fileName = args[1];

        if(mSaveTmp) {
            try {
                String cacheDirPath = args[2];
                mFile = File.createTempFile(fileName, null, new File(cacheDirPath));
            } catch (IOException e) {
                mException = e;
                onPostExecute();
            }
        } else {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            mFile = new File(path, fileName);

            if (!path.exists()) {
                if (!path.mkdirs()) {
                    mException = new RuntimeException("Unable to create directory: " + path);
                    onPostExecute();
                }
            } else if (!path.isDirectory()) {
                mException = new IllegalStateException("Download path is not a directory: " + path);
                onPostExecute();
            }
        }

        mClient.getItems(fileId)
                .getContent()
                .buildRequest()
                .get(new ICallback<InputStream>() {
                    @Override
                    public void success(InputStream inputStream) {
                        new ConvertFileTask(inputStream, new CloudService.GenericCallback() {
                            @Override
                            public void onComplete() {
                                onPostExecute();
                            }

                            @Override
                            public void onError(Exception e) {
                                mException = e;
                                onPostExecute();
                            }
                        }).execute();
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
            mCallback.onComplete(mFile);
        }
    }

    private class ConvertFileTask extends AsyncTask<Void, Void, Void> {
        private CloudService.GenericCallback mCallback;
        private InputStream mInput;

        public ConvertFileTask(InputStream input, CloudService.GenericCallback callback) {
            this.mCallback = callback;
            this.mInput = input;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                FileUtils.copyInputStreamToFile(mInput, mFile);
                mCallback.onComplete();
            } catch (IOException e) {
                mCallback.onError(e);
            }

            return null;
        }
    }
}