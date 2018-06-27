package utils.services;

import android.app.Activity;
import android.content.Context;

import java.util.Objects;

import utils.cloud.AccountType;

public class CloudManager {
    private static CloudService mInstance = null;
    private static int mHash;

    private static CloudService createService(Context context,
                                              int accountType,
                                              String accountEmail,
                                              Activity activity) {
        if(AccountType.GOOGLE_DRIVE.ordinal() == accountType) {
            mInstance = new GoogleDriveService(context, accountEmail);
        } else if(AccountType.DROPBOX.ordinal() == accountType) {
            mInstance = new DropboxService(context, accountEmail);
        } else if(AccountType.ONEDRIVE.ordinal() == accountType) {
            mInstance = new OneDriveService(context, accountEmail, activity);
        }

        mHash = Objects.hash(accountType, accountEmail);

        return mInstance;
    }

    public static CloudService getService(Context context, int accountType,
                                   String accountEmail, Activity activity) {
        if(mInstance != null && mHash == Objects.hash(accountType, accountEmail)) {
            return mInstance;
        }

        return createService(context, accountType, accountEmail, activity);
    }

    private CloudManager() {
    }

}
