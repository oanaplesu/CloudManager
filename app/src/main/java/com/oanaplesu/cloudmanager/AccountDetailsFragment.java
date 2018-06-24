package com.oanaplesu.cloudmanager;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import utils.cloud.AccountType;
import utils.cloud.CloudAccount;
import utils.db.AppDatabase;
import utils.services.CloudManager;
import utils.services.CloudService;
import utils.tasks.DeleteAccountTask;

public class AccountDetailsFragment extends Fragment {
    private int mAccountType;
    private String mAccountEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_account_details, container, false);

        TextView accountTypeTextView = inflatedView.findViewById(R.id.account_type);
        final TextView accountNameTextView = inflatedView.findViewById(R.id.account_name);
        TextView accountEmailTextView = inflatedView.findViewById(R.id.account_email);
        final ImageView accountImageView = inflatedView.findViewById(R.id.account_image);
        final ProgressBar progressBar = inflatedView.findViewById(R.id.progressBar);
        final TextView storageTextView = inflatedView.findViewById(R.id.storage);

        accountTypeTextView.setText(mAccountType == AccountType.DROPBOX.ordinal() ?
                "Dropbox" : "Google Drive");
        accountEmailTextView.setText(mAccountEmail);

        final Button deleteAccountButton = inflatedView.findViewById(R.id.delete_account_button);
        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String provider = "dropbox";

                if(mAccountType == CloudAccount.Provider.GOOGLE_DRIVE.ordinal()) {
                    provider = "google";
                }

                new DeleteAccountTask(AppDatabase.getDatabase(getContext()),
                        new DeleteAccountTask.Callback() {
                            @Override
                            public void OnComplete() {
                                ((MainActivity)getActivity()).UpdateNavigationMenu();
                                Toast.makeText(getContext(), "Account deleted successfully",
                                        Toast.LENGTH_LONG).show();

                                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                ft.replace(R.id.contentFrame, new AccountsFragment());
                                ft.commit();
                            }
                        }
                ).execute(provider, mAccountEmail);
            }
        });

        Button openAccountButton = inflatedView.findViewById(R.id.open_account_button);
        openAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = null;
                Bundle bundle = new Bundle();
                int groupId = R.id.other_options;

                if (mAccountType == CloudAccount.Provider.GOOGLE_DRIVE.ordinal()) {
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.GET_ACCOUNTS},
                                MainActivity.MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);
                    } else {
                        bundle.putInt("accountType",  AccountType.GOOGLE_DRIVE.ordinal());
                        bundle.putString("accountEmail", mAccountEmail);
                        bundle.putString("folderId", "");
                        fragment = new FilesFragment();
                        fragment.setArguments(bundle);
                        groupId = R.id.google_drive_accounts;
                    }
                } else if (mAccountType == CloudAccount.Provider.DROPBOX.ordinal()) {
                    bundle.putInt("accountType",  AccountType.DROPBOX.ordinal());
                    bundle.putString("accountEmail", mAccountEmail);
                    bundle.putString("folderId", "");
                    fragment = new FilesFragment();
                    fragment.setArguments(bundle);
                    groupId = R.id.dropbox_accounts;
                }

                ((MainActivity)getActivity()).checkSelectedMenuItem(mAccountEmail, groupId);

                if(fragment != null) {
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.contentFrame, fragment);
                    ft.commit();
                }
            }
        });

        CloudManager.getService(getContext(), mAccountType, mAccountEmail)
                .getAccountDetailsTask(new CloudService.GetAccountDetailsCallback() {
                    @Override
                    public void onComplete(CloudService.AccountDetails details) {
                        accountNameTextView.setText(details.name);
                        if(details.photo != null) {
                            accountImageView.setImageBitmap(details.photo);
                        }

                        int percent = (int) (details.usedStorage * 100 / details.totalStorage);
                        if(percent == 0) {
                            percent = 1;
                        }
                        progressBar.setProgress(percent, true);
                        String storageText = FileUtils.byteCountToDisplaySize(details.totalStorage - details.usedStorage)
                                + " out of " + FileUtils.byteCountToDisplaySize(details.totalStorage) + " free";
                        storageTextView.setText(storageText);
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getContext(), "Failed to load details for the " +
                                        "selected account",
                                Toast.LENGTH_LONG).show();

                        FragmentTransaction ft = getActivity().getSupportFragmentManager()
                                .beginTransaction();
                        ft.replace(R.id.contentFrame, new AccountsFragment());
                        ft.commit();
                    }
                }).executeTask();

        return inflatedView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        mAccountType = bundle.getInt(getString(R.string.account_type));
        mAccountEmail = bundle.getString(getString(R.string.account_email));
    }

}
