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
import utils.cloud.AccountType;
import utils.db.AppDatabase;
import utils.cloud.CloudAccount;
import utils.tasks.DeleteAccountTask;


public class AccountsFragment extends Fragment {
    private AccountAdapter mAccountsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mInflatedView = inflater.inflate(R.layout.fragment_accounts, container, false);

        RecyclerView filesList = (RecyclerView) mInflatedView.findViewById(R.id.accounts_list);

        mAccountsAdapter = new AccountAdapter(new AccountAdapter.Callback() {
            @Override
            public void onAccountClicked(CloudAccount account) {
                Fragment fragment = new AccountDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("accountType", account.getProvider().ordinal());
                bundle.putString("accountEmail", account.getEmail());
                fragment.setArguments(bundle);

                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.contentFrame, fragment);
                ft.commit();
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
        List<String> onedriveAccounts;

        @Override
        protected Void doInBackground(Void... args) {
            database = AppDatabase.getDatabase(getContext());
            googleAccounts = database.googleDriveUserDao().getAllAccounts();
            dropboxAccounts = database.dropboxUserDao().getAllAccounts();
            onedriveAccounts = database.oneDriveUserDao().getAllAccounts();

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

            for(String accountName : onedriveAccounts) {
                accountsList.add(new CloudAccount(accountName,
                        CloudAccount.Provider.ONEDRIVE));
            }

            if(accountsList.isEmpty()) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.contentFrame, new AddNewAccountFragment());
                ft.commit();
            }

            mAccountsAdapter.setAccounts(accountsList);
        }
    }

    void refreshFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

}
