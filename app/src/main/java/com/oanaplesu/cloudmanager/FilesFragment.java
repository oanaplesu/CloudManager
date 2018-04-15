package com.oanaplesu.cloudmanager;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.DriveScopes;

import db.AppDatabase;
import utils.CloudResource;
import utils.CreateFolderCallback;
import utils.CreateFolderDropboxTask;
import utils.CreateFolderGoogleDriveTask;
import utils.DropboxUniqueFolderNameException;
import utils.FileAction;
import utils.FilesAdapter;
import utils.GetFilesCallback;
import utils.GetFilesFromDropboxTask;
import utils.GetFilesFromGoogleDriveTask;
import utils.UploadFileCallback;
import utils.UploadFileDropboxTask;
import utils.UploadFileGoogleDriveTask;
import utils.UriHelpers;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class FilesFragment extends Fragment {
    private View inflatedView;
    private FilesAdapter mFilesAdapter;
    private int accountType;
    private String accountEmail;
    private String folderId;
    private final static int GOOGLE_ACCOUNT = 100;
    private final static int DROPBOX_ACCOUNT = 200;
    private static final int PICKFILE_REQUEST_CODE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_files, container, false);
        setHasOptionsMenu(true);


        loadFiles();

        RecyclerView filesList = (RecyclerView) inflatedView.findViewById(R.id.files_list);
        mFilesAdapter = new FilesAdapter(new FilesAdapter.Callback() {
            @Override
            public void onFolderClicked(CloudResource folder) {
                Fragment fragment = new FilesFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("accountType", accountType);
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

        FloatingActionButton fab = (FloatingActionButton) inflatedView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performWithPermissions(FileAction.UPLOAD);
            }
        });

        return inflatedView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        accountType = bundle.getInt("accountType");
        accountEmail = bundle.getString("accountEmail");
        folderId = bundle.getString("folderId");

        Log.i("info", accountEmail);
        Log.i("info", folderId);
    }

    private void loadFiles() {
        ProgressDialog dialog = new ProgressDialog(getContext());

        GetFilesCallback callback = new GetFilesCallback() {
            @Override
            public void onComplete(List<CloudResource> files) {
                mFilesAdapter.setFiles(files);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getActivity(), "Failed to load files", Toast.LENGTH_LONG).show();
            }
        };

        if (accountType == GOOGLE_ACCOUNT) {
            new GetFilesFromGoogleDriveTask(
                    getGoogleAccountCredential(),
                    dialog, callback).execute(accountEmail, folderId);
        } else if (accountType == DROPBOX_ACCOUNT) {
            new GetFilesFromDropboxTask(
                    getDatabase(),
                    dialog, callback).execute(accountEmail, folderId);
        }
    }

    private GoogleAccountCredential getGoogleAccountCredential() {
        return GoogleAccountCredential.usingOAuth2(
                getContext(), Collections.singleton(DriveScopes.DRIVE));
    }

    private AppDatabase getDatabase() {
        return AppDatabase.getDatabase(getContext());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.file_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.delete_file) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.files_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.create_folder) {
            createNewFolder();

            return true;
        } else if(id == R.id.refresh_button) {
            refreshFrament();
        }

        return super.onOptionsItemSelected(item);
    }

    public void createNewFolder() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("New Folder Name");

        final EditText input = new EditText(getContext());
        alert.setView(input);

        final CreateFolderCallback callback = new CreateFolderCallback() {
            @Override
            public void onComplete() {
                refreshFrament();
            }

            @Override
            public void onError(Exception e) {
                if(e instanceof DropboxUniqueFolderNameException) {
                    Toast.makeText(getActivity(), "A folder with this name already exists",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "Failed to create folder",
                            Toast.LENGTH_LONG).show();
                }
            }
        };

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();

                if (accountType == GOOGLE_ACCOUNT) {
                    new CreateFolderGoogleDriveTask(
                            getGoogleAccountCredential(),
                            callback).execute(accountEmail, folderId, value);
                } else if (accountType == DROPBOX_ACCOUNT) {
                    new CreateFolderDropboxTask(
                            getDatabase(),
                            callback).execute(accountEmail, folderId, value);
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    private void refreshFrament() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

    private void performWithPermissions(final FileAction action) {
        if (hasPermissionsForAction(action)) {
            performAction(action);
            return;
        }

        if (shouldDisplayRationaleForAction(action)) {
            new android.support.v7.app.AlertDialog.Builder(getContext())
                    .setMessage("This app requires storage access to download and upload files.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissionsForAction(action);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        } else {
            requestPermissionsForAction(action);
        }
    }

    private boolean hasPermissionsForAction(FileAction action) {
        for (String permission : action.getPermissions()) {
            int result = ContextCompat.checkSelfPermission(getActivity(), permission);
            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private boolean shouldDisplayRationaleForAction(FileAction action) {
        for (String permission : action.getPermissions()) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)) {
                return true;
            }
        }
        return false;
    }

    private void requestPermissionsForAction(FileAction action) {
        ActivityCompat.requestPermissions(
                getActivity(),
                action.getPermissions(),
                action.getCode()
        );
    }

    private void performAction(FileAction action) {
        switch(action) {
            case UPLOAD:
                launchFilePicker();
                break;
          /*  case DOWNLOAD:
                if (mSelectedFile != null) {
                    downloadFile(mSelectedFile);
                } else {
                    Log.e(TAG, "No file selected to download.");
                }
                break;*/

        }
    }

    private void launchFilePicker() {
        // Launch intent to pick file for upload
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICKFILE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKFILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                UploadFileCallback callback = new UploadFileCallback() {
                    @Override
                    public void onComplete() {
                        refreshFrament();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getActivity(), "Failed to upload file",
                                Toast.LENGTH_LONG).show();
                    }
                };

                String fileUri = data.getData().toString();
                File localFile = UriHelpers.getFileForUri(getContext(), Uri.parse(fileUri));
                ProgressDialog dialog = new ProgressDialog(getContext());

                if (accountType == GOOGLE_ACCOUNT) {
                    new UploadFileGoogleDriveTask(
                            getGoogleAccountCredential(),
                            localFile,
                            dialog,
                            callback).execute(accountEmail, folderId);
                } else if (accountType == DROPBOX_ACCOUNT) {
                    new UploadFileDropboxTask(
                            getDatabase(),
                            localFile,
                            dialog,
                            callback).execute(accountEmail, folderId);
                }
            }
        }
    }
}
