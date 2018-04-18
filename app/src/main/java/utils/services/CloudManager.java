package utils.services;

import android.content.Context;
import java.util.Objects;

public class CloudManager {
    private static CloudService mInstance = null;
    private static int mHash;

    private final static int GOOGLE_ACCOUNT = 100;
    private final static int DROPBOX_ACCOUNT = 200;


    private static CloudService createService(Context context,
                                              int accountType,
                                              String accountEmail) {
        switch(accountType) {
            case GOOGLE_ACCOUNT:
                mInstance = new GoogleDriveService(context, accountEmail);
                break;
            case DROPBOX_ACCOUNT:
                mInstance = new DropboxService(context, accountEmail);
                break;
        }

        mHash = Objects.hash(accountType, accountEmail);

        return mInstance;
    }

    public static CloudService getService(Context context, int accountType,
                                   String accountEmail) {
        if(mInstance != null && accountEmail.hashCode()
                == Objects.hash(accountType, accountEmail)) {
            return mInstance;
        }

        return createService(context, accountType, accountEmail);
    }

    private CloudManager() {
    }

}
