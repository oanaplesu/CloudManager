package utils.services;

import android.content.Context;
import java.util.Objects;

import utils.cloud.AccountType;
import utils.cloud.CloudResource;

import static utils.cloud.AccountType.*;

public class CloudManager {
    private static CloudService mInstance = null;
    private static int mHash;

    private static CloudService createService(Context context,
                                              int accountType,
                                              String accountEmail) {
        if(AccountType.GOOGLE_DRIVE.ordinal() == accountType) {
            mInstance = new GoogleDriveService(context, accountEmail);
        } else if(AccountType.DROPBOX.ordinal() == accountType) {
            mInstance = new DropboxService(context, accountEmail);
        }

        mHash = Objects.hash(accountType, accountEmail);

        return mInstance;
    }

    public static CloudService getService(Context context, int accountType,
                                   String accountEmail) {
        if(mInstance != null && mHash == Objects.hash(accountType, accountEmail)) {
            return mInstance;
        }

        return createService(context, accountType, accountEmail);
    }

    private CloudManager() {
    }

}
