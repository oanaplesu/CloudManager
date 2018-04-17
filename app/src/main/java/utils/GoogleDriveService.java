package utils;


import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.common.hash.HashCode;

import java.util.Collections;

public class GoogleDriveService {
    private static Drive mInstance = null;
    private static int mHash;

    private static Drive createDriveService(Context context, String accountEmail) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context, Collections.singleton(DriveScopes.DRIVE));
        credential.setSelectedAccountName(accountEmail);
        mHash = accountEmail.hashCode();

        return new Drive.Builder(AndroidHttp.newCompatibleTransport(),
                new GsonFactory(), credential).build();
    }

    public static Drive get(Context context, String accountEmail) {
        if(mInstance != null && accountEmail.hashCode() == mHash) {
            return mInstance;
        }

        return createDriveService(context, accountEmail);
    }

    private GoogleDriveService() {
    }
}
