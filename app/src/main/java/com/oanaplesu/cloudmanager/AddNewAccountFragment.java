package com.oanaplesu.cloudmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;

import utils.db.AppDatabase;
import utils.db.DropboxUser;
import utils.db.GoogleDriveUser;


public class AddNewAccountFragment extends Fragment {
    final private int REQUEST_CODE_GOOGLE_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;
    private View inflatedView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_add_new_account, container, false);

        final Button googleSignInButton = inflatedView.findViewById(R.id.googleSignInButton);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signInGoogle();
            }
        });

        final Button dropboxSignInButton = inflatedView.findViewById(R.id.dropboxSignInButton);
        dropboxSignInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signInDropbox();
            }
        });

        return inflatedView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void signInGoogle() {
        mGoogleSignInClient = buildGoogleSignInClient();
        mGoogleSignInClient.signOut();
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    private void signInDropbox() {
        com.dropbox.core.android.Auth.startOAuth2Authentication(
                getActivity(), getString(R.string.app_key));
    }

    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(new Scope(DriveScopes.DRIVE))
                        .requestEmail()
                        .build();
        return GoogleSignIn.getClient(getActivity(), signInOptions);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                result.getSignInAccount().getAccount();

                GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(getActivity());
                new AddNewGoogleAccountTask().execute(googleAccount.getEmail());
            } else {
                Toast.makeText(getActivity(), "Adding account failed. Try again", Toast.LENGTH_LONG).show();
            }
        }
    }

    class AddNewGoogleAccountTask extends AsyncTask<String, Void, Boolean> {
        private AppDatabase database;

        @Override
        protected Boolean doInBackground(String... args) {
            database = AppDatabase.getDatabase(getActivity());

            try {
                database.googleDriveUserDao().addUser(new GoogleDriveUser(args[0]));
            } catch (android.database.sqlite.SQLiteConstraintException ex) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean added) {
            super.onPostExecute(added);

            if (added) {
                ((MainActivity)getActivity()).UpdateNavigationMenu();
                Toast.makeText(getActivity(), "Account added succesfully", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Account already added", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        String accessToken = com.dropbox.core.android.Auth.getOAuth2Token();

        if(accessToken != null) {
            new AddNewDropboxAccountTask(accessToken).execute();
        } else {
            Log.i("info", "fail");
        }
    }

    class AddNewDropboxAccountTask extends AsyncTask<Void, Void, Boolean> {
        private AppDatabase mDatabase;
        private String mAccesToken;
        DbxClientV2 mDbxClient;

        AddNewDropboxAccountTask(String accessToken) {
            mAccesToken = accessToken;
            mDatabase = AppDatabase.getDatabase(getActivity());
            setDbxClient();
        }

        private void setDbxClient() {
            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("dropbox")
                    .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                    .build();
            mDbxClient = new DbxClientV2(requestConfig, mAccesToken);
        }

        @Override
        protected void onPostExecute(Boolean added) {
            super.onPostExecute(added);

            if (added) {
                ((MainActivity)getActivity()).UpdateNavigationMenu();
                Toast.makeText(getActivity(), "Account added succesfully", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Account already added", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            FullAccount account;

            try {
                account = mDbxClient.users().getCurrentAccount();
            } catch (DbxException e) {
                return false;
            }

            DropboxUser dropboxUser = new DropboxUser(account.getEmail(), mAccesToken);

            try {
                mDatabase.dropboxUserDao().addUser(dropboxUser);
            } catch (android.database.sqlite.SQLiteConstraintException ex) {
                return false;
            }

            return true;
        }
    }
}
