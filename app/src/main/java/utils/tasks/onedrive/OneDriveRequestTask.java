package utils.tasks.onedrive;

import com.onedrive.sdk.extensions.IDriveRequestBuilder;

import utils.services.CloudService;
import utils.services.OneDriveService;
import utils.tasks.CloudRequestTask;



public abstract class OneDriveRequestTask implements CloudRequestTask {
    protected OneDriveService mOnedriveService;
    protected IDriveRequestBuilder mClient;
    protected Exception mException;


    protected abstract void onPreExecute();

    protected abstract void onPostExecute();

    protected abstract void executeTaskInternal(String... args);

    @Override
    public void executeTask(String... args) {
        onPreExecute();

        if(!mOnedriveService.isClientInitialized()) {
            mOnedriveService.createClient(new CloudService.GenericCallback() {
                @Override
                public void onComplete() {
                    mClient =  mOnedriveService.getClient();
                    if(mClient != null) {
                        executeTaskInternal(args);
                    } else {
                        mException = new Exception("Error");
                        onPostExecute();
                    }
                }

                @Override
                public void onError(Exception e) {
                    mException = e;
                    onPostExecute();
                }
            });
        } else {
            mClient = mOnedriveService.getClient();
            executeTaskInternal(args);
        }
    }

}
