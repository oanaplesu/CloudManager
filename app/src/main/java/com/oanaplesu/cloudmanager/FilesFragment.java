package com.oanaplesu.cloudmanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import utils.CloudResource;
import utils.FilesAdapter;
import utils.GetFilesFromDropboxTask;
import utils.GetFilesFromGoogleDriveTask;

import java.io.File;
import java.util.List;


public class FilesFragment extends Fragment {
    private View inflatedView;
    private FilesAdapter mFilesAdapter;
    private String accountType;
    private String accountEmail;
    private String folderId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_files, container, false);
        loadFiles();

        RecyclerView filesList = (RecyclerView) inflatedView.findViewById(R.id.files_list);
        mFilesAdapter = new FilesAdapter(new FilesAdapter.Callback() {
            @Override
            public void onFolderClicked(CloudResource folder) {
                Fragment fragment = new FilesFragment();
                Bundle bundle = new Bundle();
                bundle.putString("accountType", accountType);
                bundle.putString("accountEmail", accountEmail);
                bundle.putString("folderId", folder.getId());

                fragment.setArguments(bundle);

                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.contentFrame, fragment);
                ft.commit();
            }

            @Override
            public void onFileClicked(final CloudResource file) {
                // mSelectedFile = file;
                //  performWithPermissions(FileAction.DOWNLOAD);
            }
        });

        filesList.setLayoutManager(new LinearLayoutManager(getActivity()));
        filesList.setAdapter(mFilesAdapter);
        registerForContextMenu(filesList);

        return inflatedView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            accountType = bundle.getString("accountType");
            accountEmail = bundle.getString("accountEmail");
            folderId = bundle.getString("folderId");

            Log.i("info", accountType);
            Log.i("info", accountEmail);
            Log.i("info", folderId);
        }
    }

    private void loadFiles() {
        if (accountType.equals("google")) {
            new GetFilesFromGoogleDriveTask(getContext(), new GetFilesFromGoogleDriveTask.Callback() {
                @Override
                public void onComplete(List<CloudResource> files) {
                    mFilesAdapter.setFiles(files);
                }

                @Override
                public void onError(Exception e) {
                    Log.i("test", "no");
                }
            }).execute(accountEmail, folderId);
        } else if (accountType.equals("dropbox")) {
            new GetFilesFromDropboxTask(getContext(), new GetFilesFromDropboxTask.Callback() {
                @Override
                public void onComplete(List<CloudResource> files) {
                    mFilesAdapter.setFiles(files);
                }

                @Override
                public void onError(Exception e) {
                    Log.i("test", "no");
                }
            }).execute(accountEmail, folderId);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.delete_file) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
