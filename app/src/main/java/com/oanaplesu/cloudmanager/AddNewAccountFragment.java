package com.oanaplesu.cloudmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.Scope;
import com.google.api.services.drive.DriveScopes;

import db.AppDatabase;
import db.GoogleDriveUser;


public class AddNewAccountFragment extends Fragment {
    final private int REQUEST_CODE_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_new_account, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        signIn();
    }

    private void signIn() {
        mGoogleSignInClient = buildGoogleSignInClient();
        mGoogleSignInClient.signOut();
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
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

        if (requestCode == REQUEST_CODE_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                result.getSignInAccount().getAccount();

                GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(getActivity());
                new AddNewAccountTask().execute(googleAccount.getEmail());
            } else {
                Toast.makeText(getActivity(), "Adding account failed. Try again", Toast.LENGTH_LONG).show();
            }
        }
    }

    class AddNewAccountTask extends AsyncTask<String, Void, Boolean> {
        private AppDatabase database;

        @Override
        protected Boolean doInBackground(String... args) {
            database = AppDatabase.getDatabase(getActivity());

            try {
                database.googleDriveUserDao().addUser(new GoogleDriveUser(args[0]));
                ((MainActivity)getActivity()).UpdateNavigationMenu();
            } catch( android.database.sqlite.SQLiteConstraintException ex ) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean added) {
            super.onPostExecute(added);

            if (added) {
                Toast.makeText(getActivity(), "Account added succesfully", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Account already added", Toast.LENGTH_LONG).show();
            }
        }
    }
}
