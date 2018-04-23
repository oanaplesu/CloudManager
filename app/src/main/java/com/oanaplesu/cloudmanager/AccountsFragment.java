package com.oanaplesu.cloudmanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import utils.adapters.AccountAdapter;
import utils.db.AppDatabase;
import utils.cloud.CloudAccount;
import utils.tasks.DeleteAccountTask;


public class AccountsFragment extends Fragment {
    private AccountAdapter mAccountsAdapter;

    private final static int GOOGLE_ACCOUNT = 100;
    private final static int DROPBOX_ACCOUNT = 200;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mInflatedView = inflater.inflate(R.layout.fragment_accounts, container, false);

        RecyclerView filesList = (RecyclerView) mInflatedView.findViewById(R.id.accounts_list);

        mAccountsAdapter = new AccountAdapter(new AccountAdapter.Callback() {
            @Override
            public void onAccountClicked(CloudAccount account) {
                Fragment fragment = null;
                Bundle bundle = new Bundle();

                if (account.getProvider() == CloudAccount.Provider.GOOGLE_DRIVE) {
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.GET_ACCOUNTS},
                                MainActivity.MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);
                    } else {
                        bundle.putInt("accountType", GOOGLE_ACCOUNT);
                        bundle.putString("accountEmail", account.getEmail());
                        bundle.putString("folderId", "");
                        fragment = new FilesFragment();
                        fragment.setArguments(bundle);
                    }
                } else if (account.getProvider() == CloudAccount.Provider.DROPBOX) {
                    bundle.putInt("accountType", DROPBOX_ACCOUNT);
                    bundle.putString("accountEmail", account.getEmail());
                    bundle.putString("folderId", "");
                    fragment = new FilesFragment();
                    fragment.setArguments(bundle);
                }

                if (fragment != null) {
                    ((MainActivity)getActivity()).checkSelectedMenuItem(account.getEmail());
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.contentFrame, fragment);
                    ft.commit();
                }
            }

            @Override
            public void onDeleteAccountClicked(CloudAccount account) {
                Log.i("aici", "delete clicked");

                String provider = null;

                if(account.getProvider() == CloudAccount.Provider.GOOGLE_DRIVE) {
                    provider = "google";
                } else if(account.getProvider() == CloudAccount.Provider.DROPBOX) {
                    provider = "dropbox";
                }

                new DeleteAccountTask(AppDatabase.getDatabase(getContext()),
                    new DeleteAccountTask.Callback() {
                        @Override
                        public void OnComplete() {
                            ((MainActivity)getActivity()).UpdateNavigationMenu();
                            loadAccounts();
                            Toast.makeText(getContext(), "Account deleted successfully",
                                    Toast.LENGTH_LONG).show();
                            refreshFragment();
                        }
                    }
                ).execute(provider, account.getEmail());
            }
        });

        filesList.setLayoutManager(new LinearLayoutManager(getActivity()));
        filesList.setAdapter(mAccountsAdapter);

        loadAccounts();

        return mInflatedView;
    }

    void loadAccounts() {
        new LoadAccountsTask().execute();
    }

    class LoadAccountsTask extends AsyncTask<Void, Void, Void> {
        private AppDatabase database;
        List<String> googleAccounts;
        List<String> dropboxAccounts;

        @Override
        protected Void doInBackground(Void... args) {
            database = AppDatabase.getDatabase(getContext());
            googleAccounts = database.googleDriveUserDao().getAllAccounts();
            dropboxAccounts = database.dropboxUserDao().getAllAccounts();

            return null;
        }

        @Override
        protected void onPostExecute(Void arg) {
            ArrayList<CloudAccount> accountsList = new ArrayList<>();

            for(String accountName : googleAccounts) {
                accountsList.add(new CloudAccount(accountName,
                        CloudAccount.Provider.GOOGLE_DRIVE));
            }

            for(String accountName : dropboxAccounts) {
                accountsList.add(new CloudAccount(accountName,
                        CloudAccount.Provider.DROPBOX));
            }

            mAccountsAdapter.setAccounts(accountsList);
        }
    }

    void refreshFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

}
