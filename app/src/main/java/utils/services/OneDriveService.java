package utils.services;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;

import com.onedrive.sdk.authentication.MSAAuthenticator;
import com.onedrive.sdk.concurrency.ICallback;
import com.onedrive.sdk.core.ClientException;
import com.onedrive.sdk.core.DefaultClientConfig;
import com.onedrive.sdk.core.IClientConfig;
import com.onedrive.sdk.extensions.IDriveRequestBuilder;
import com.onedrive.sdk.extensions.IOneDriveClient;
import com.onedrive.sdk.extensions.OneDriveClient;

import java.io.File;

import utils.cloud.CloudResource;
import utils.db.AppDatabase;
import utils.tasks.CloudRequestTask;
import utils.tasks.onedrive.GetFilesFromOneDriveTask;

import static android.content.Context.MODE_PRIVATE;

public class OneDriveService implements CloudService {
    //to replace
    public static final String REFRESH_TOKEN_KEY = "refresh_token";
    public static final String USER_KEY = "userId";
    public static final String SHARED_PREFERENCES_ONEDRIVE_FILE = "MSAAuthenticatorPrefs";
    public static final String SHARED_PREFERENCES_TOKEN_FILE = "com.microsoft.live";

    private IDriveRequestBuilder mClient;
    private String mAccountEmail;
	private Context mContext;
    private Activity mActivity;
    private boolean mInitialized;


    public OneDriveService(Context context, String accountEmail, Activity activity) {
		mContext = context;
        mAccountEmail = accountEmail;
        mActivity = activity;
        mInitialized = false;
    }

    public boolean isInitialized() {
        return mInitialized;
    }

    public interface OnCreateCallback {
        void onComplete();
        void onError(Exception e);
    }

    public void createService(OnCreateCallback callback) {
        AppDatabase database = AppDatabase.getDatabase(mContext);
        String token = database.oneDriveUserDao().getTokenForAccount(mAccountEmail);

        SharedPreferences onedrivePreferences = mContext.getSharedPreferences(
                OneDriveService.SHARED_PREFERENCES_ONEDRIVE_FILE, MODE_PRIVATE);
        SharedPreferences tokenPreferences = mContext.getSharedPreferences(
                OneDriveService.SHARED_PREFERENCES_TOKEN_FILE, MODE_PRIVATE);

        onedrivePreferences.edit().putString(USER_KEY, mAccountEmail).commit();
        tokenPreferences.edit().putString(REFRESH_TOKEN_KEY, token).commit();

        ICallback<IOneDriveClient> authCallback = new ICallback<IOneDriveClient>() {
            @Override
            public void success(final IOneDriveClient result) {
                mClient = result.getDrive();
                mInitialized = true;
                callback.onComplete();
            }

            @Override
            public void failure(final ClientException error) {
                mClient = null;
                mInitialized = true;
                callback.onError(error);
            }
        };

        MSAAuthenticator auth = OneDriveService.getMSAAuthenticatorOneDrive();
        IClientConfig oneDriveConfig = DefaultClientConfig.createWithAuthenticator(auth);

        new OneDriveClient.Builder()
                .fromConfig(oneDriveConfig)
                .loginAndBuildClient(mActivity, authCallback);

    }

    public IDriveRequestBuilder getClient() {
        return mClient;
    }

    public static MSAAuthenticator getMSAAuthenticatorOneDrive() {
        return new MSAAuthenticator() {
            @Override
            public String getClientId() {
                return "df52c9ee-82ad-4207-8d2a-7a0ea8861616";
            }

            @Override
            public String[] getScopes() {
                return new String[] { "onedrive.readwrite", "onedrive.appfolder",
                        "wl.offline_access", "wl.emails"};
            }
        };
    }

	private void updateToken() {
		AppDatabase.getDatabase(mContext).oneDriveUserDao().updateUser(
			mAccountEmail,
			mContext.getSharedPreferences(OneDriveService.SHARED_PREFERENCES_TOKEN_FILE, MODE_PRIVATE)
				.getString(OneDriveService.REFRESH_TOKEN_KEY, "")		
		);
	}


    @Override
    public CloudRequestTask getFilesTask(ProgressDialog dialog, GetFilesCallback callback) {
        return new GetFilesFromOneDriveTask(this, mAccountEmail, dialog, callback);
        }

    @Override
    public CloudRequestTask createFolderTask(CreateFolderCallback callback) {
        return null;
        //return new CreateFolderOneDriveTask(mClient, mAccountEmail, callback);
    }

    @Override
    public CloudRequestTask deleteFileTask(GenericCallback callback) {
	    return null;
        //return new DeleteFileOneDriveTask(mClient, callback);
    }

    @Override
    public CloudRequestTask uploadFileTask(File file, ProgressDialog dialog, GenericCallback callback) {
		return null;
       // return new UploadFileOneDriveTask(mClient, file, dialog, callback);
    }

    @Override
    public CloudRequestTask downloadFileTask(ProgressDialog dialog, boolean saveTmp, DownloadFileCallback callback) {
        return null;
       // return new DownloadFileOneDriveTask(mClient, dialog, saveTmp, callback);
    }

    @Override
    public CloudRequestTask moveFilesTask(CloudResource sourceFile, Context context, Activity activity, boolean deleteOriginal, MoveFilesCallback callback) {
        return null;
    }

    @Override
    public CloudRequestTask getAccountDetailsTask(GetAccountDetailsCallback callback) {
        return null;
        //return new GetAccountDetailsOneDriveTask(mClient, callback);
    }

    @Override
    public CloudRequestTask getFileDetailsTask(ProgressDialog dialog, GetFileDetailsCallback callback) {
		return null;
        //return new GetFileDetailsDropboxTask(mClient, dialog, callback);
    }
}