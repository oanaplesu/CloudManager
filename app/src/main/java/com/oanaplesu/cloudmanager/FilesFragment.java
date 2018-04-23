package com.oanaplesu.cloudmanager;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import utils.cloud.CloudResource;
import utils.services.CloudManager;
import utils.services.CloudService;
import utils.exceptions.DropboxUniqueFolderNameException;
import utils.misc.FileAction;
import utils.adapters.FilesAdapter;
import utils.misc.UriHelpers;

import java.io.File;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class FilesFragment extends Fragment {
    private View mInflatedView;
    private FilesAdapter mFilesAdapter;
    private int mAccountType;
    private String mAccountEmail;
    private String mFolderId;
    private CloudResource mSelectedFile;
    private static final int PICKFILE_REQUEST_CODE = 1;


    private CloudService getService() {
        return CloudManager.getService(getContext(), mAccountType, mAccountEmail);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflatedView = inflater.inflate(R.layout.fragment_files, container, false);
        setHasOptionsMenu(true);

        RecyclerView filesList = (RecyclerView) mInflatedView.findViewById(R.id.files_list);
        mFilesAdapter = new FilesAdapter(new FilesAdapter.Callback() {
            @Override
            public void onFolderClicked(CloudResource folder) {
                Fragment fragment = new FilesFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("accountType", mAccountType);
                bundle.putString("accountEmail", mAccountEmail);
                bundle.putString("folderId", folder.getId());

                fragment.setArguments(bundle);

                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.contentFrame, fragment);
                ft.commit();
            }
        });

        filesList.setLayoutManager(new LinearLayoutManager(getActivity()));
        filesList.setAdapter(mFilesAdapter);

        loadFiles("");

        registerForContextMenu(filesList);

        FloatingActionButton fab = (FloatingActionButton) mInflatedView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performWithPermissions(FileAction.UPLOAD);
            }
        });

        return mInflatedView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        mAccountType = bundle.getInt("accountType");
        mAccountEmail = bundle.getString("accountEmail");
        mFolderId = bundle.getString("folderId");
    }

    private void loadFiles(final String onCompleteMessage) {
        ProgressDialog dialog = new ProgressDialog(getContext());

        getService().getFilesTask(dialog, new CloudService.GetFilesCallback() {
            @Override
            public void onComplete(List<CloudResource> files) {
                mFilesAdapter.setFiles(files);

                if(!onCompleteMessage.isEmpty()) {
                    Toast.makeText(getActivity(), onCompleteMessage,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getActivity(), "Failed to load files",
                        Toast.LENGTH_LONG).show();
            }
        }).executeTask(mFolderId);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = mFilesAdapter.getPosition();
        CloudResource file = mFilesAdapter.getFile(position);
        int id = item.getItemId();

        if (id == R.id.delete_file) {
            getService().deleteFileTask(new CloudService.GenericCallback() {
                @Override
                public void onComplete() {
                    loadFiles("File deleted successfully");
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getActivity(), "Failed to delete file",
                            Toast.LENGTH_LONG).show();
                }
            }).executeTask(file.getId());

            return true;
        } else if(id == R.id.download_file) {
            mSelectedFile = file;
            performWithPermissions(FileAction.DOWNLOAD);

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
            loadFiles("");
        }

        return super.onOptionsItemSelected(item);
    }

    public void createNewFolder() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("New Folder Name");

        final EditText input = new EditText(getContext());
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();

                getService().createFolderTask(new CloudService.GenericCallback() {
                    @Override
                    public void onComplete() {
                        loadFiles("Folder created successfully");
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
                }).executeTask(mFolderId, value);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
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
            case DOWNLOAD:
                if (mSelectedFile != null) {
                    downloadFile(mSelectedFile);
                }
                break;

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
                String fileUri = data.getData().toString();
                File localFile = UriHelpers.getFileForUri(getContext(), Uri.parse(fileUri));
                ProgressDialog dialog = new ProgressDialog(getContext());

                getService().uploadFileTask(localFile, dialog, new CloudService.GenericCallback() {
                    @Override
                    public void onComplete() {
                        loadFiles("File uploaded successfully");
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getActivity(), "Failed to upload file",
                                Toast.LENGTH_LONG).show();
                    }
                }).executeTask(mFolderId);
            }
        }
    }

    private void viewFileInExternalApp(File result) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = result.getName().substring(result.getName().indexOf(".") + 1);
        String type = mime.getMimeTypeFromExtension(ext);

        Uri apkURI = FileProvider.getUriForFile(
                getActivity(),
                getContext()
                        .getPackageName() + ".provider", result);
        intent.setDataAndType(apkURI, type);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        PackageManager manager = getActivity().getPackageManager();
        List<ResolveInfo> resolveInfo = manager.queryIntentActivities(intent, 0);
        if (resolveInfo.size() > 0) {
            startActivity(intent);
        } else {
            intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
            startActivity(intent);
        }

    }

    private void downloadFile(CloudResource file) {
        ProgressDialog dialog = new ProgressDialog(getContext());

        getService().downloadFileTask(dialog, new CloudService.DownloadFileCallback() {
            @Override
            public void onComplete(final File file) {
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(file));
                getContext().sendBroadcast(intent);
                Snackbar mySnackbar = Snackbar.make(mInflatedView, "Download complete", Snackbar.LENGTH_INDEFINITE);
                mySnackbar.setAction("Open", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        viewFileInExternalApp(file);
                    }
                });
                mySnackbar.show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getActivity(), "Failed to download file",
                        Toast.LENGTH_LONG).show();
            }
        }).executeTask(file.getId(), file.getName());
    }

}
