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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import utils.cloud.CloudResource;
import utils.services.CloudManager;
import utils.services.CloudService;
import utils.exceptions.DropboxUniqueFolderNameException;
import utils.misc.FileAction;
import utils.adapters.FilesAdapter;
import utils.misc.UriHelpers;
import utils.tasks.MoveFilesTask;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static android.view.MenuItem.SHOW_AS_ACTION_ALWAYS;


public class FilesFragment extends Fragment {
    private View mInflatedView;
    private FilesAdapter mFilesAdapter;
    private int mAccountType;
    private String mAccountEmail;
    private String mFolderId;
    private CloudResource mSelectedFile;
    private static final int PICKFILE_REQUEST_CODE = 1;

    private class SavedFile {
        public CloudResource file;
        public boolean deleteOriginal;

        public SavedFile(CloudResource file, boolean deleteOriginal) {
            this.file = file;
            this.deleteOriginal = deleteOriginal;
        }
    }

    private static SavedFile mSavedFile = null;

    private CloudService getService() {
        return CloudManager.getService(getContext(), mAccountType, mAccountEmail, getActivity());
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
                bundle.putInt(getString(R.string.account_type), mAccountType);
                bundle.putString(getString(R.string.account_email), mAccountEmail);
                bundle.putString(getString(R.string.folder_id), folder.getId());

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
        mAccountType = bundle.getInt(getString(R.string.account_type));
        mAccountEmail = bundle.getString(getString(R.string.account_email));
        mFolderId = bundle.getString(getString(R.string.folder_id));
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
                Toast.makeText(getActivity(), R.string.load_files_failed,
                        Toast.LENGTH_LONG).show();
            }
        }).executeTask(mFolderId);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = mFilesAdapter.getPosition();
        final CloudResource file = mFilesAdapter.getFile(position);
        int id = item.getItemId();

        if (id == R.id.delete_file) {
            getService().deleteFileTask(new CloudService.GenericCallback() {
                @Override
                public void onComplete() {
                    loadFiles(getString(R.string.file_delete_success));
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getActivity(), R.string.file_delete_failed,
                            Toast.LENGTH_LONG).show();
                }
            }).executeTask(file.getId());

            return true;
        } else if(id == R.id.download_file) {
            mSelectedFile = file;
            performWithPermissions(FileAction.DOWNLOAD);

            return true;
        } else if(id == R.id.copy_file) {
            mSavedFile = new SavedFile(file, false);
            getActivity().invalidateOptionsMenu();
            return true;
        } else if(id == R.id.cut_file) {
            mSavedFile = new SavedFile(file, true);
            getActivity().invalidateOptionsMenu();
            return true;
        } else if(id == R.id.details_file) {
            ProgressDialog dialog = new ProgressDialog(getContext());

            getService().getFileDetailsTask(dialog, new CloudService.GetFileDetailsCallback() {
                @Override
                public void onComplete(CloudService.FileDetails details) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    builder.setTitle("File Details");
                    builder.setCancelable(true);

                    View layout = inflater.inflate(R.layout.file_details, null);
                    TextView fileTitleTextView = layout.findViewById(R.id.file_title);
                    TextView fileTypeTextView = layout.findViewById(R.id.file_mime_type);
                    TextView fileCreatedTextView = layout.findViewById(R.id.file_created);
                    TextView fileModifiedTextView = layout.findViewById(R.id.file_modified);
                    TextView fileSizeTextView = layout.findViewById(R.id.file_size);
                    TextView Created = layout.findViewById(R.id.Created);
                    TextView Modified = layout.findViewById(R.id.Modified);
                    TextView Size = layout.findViewById(R.id.Size);

                    fileTitleTextView.setText(details.title);
                    fileTypeTextView.setText(details.type);
                    fileCreatedTextView.setText(details.dateCreated);
                    fileModifiedTextView.setText(details.dateModified);
                    fileSizeTextView.setText(FileUtils.byteCountToDisplaySize(details.size));

                    if(details.dateCreated == null) {
                        fileCreatedTextView.setHeight(0);
                        Created.setHeight(0);
                    }

                    if(details.dateModified == null) {
                        fileModifiedTextView.setHeight(0);
                        Modified.setHeight(0);
                    }

                    if(details.size == 0) {
                        fileSizeTextView.setHeight(0);
                        Size.setHeight(0);
                    }

                    builder.setView(layout);
                    builder.setPositiveButton("Close", null);
                    builder.create();
                    builder.show();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getActivity(), "Failed to load file details",
                            Toast.LENGTH_LONG).show();
                }
            }).executeTask(file.getId());
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(Menu.NONE, R.id.refresh_files, 1, R.string.refresh)
                .setIcon(R.drawable.refresh_icon)
                .setShowAsAction(SHOW_AS_ACTION_ALWAYS);
        menu.add(Menu.NONE, R.id.create_folder, 2, R.string.create_folder);

        if(mSavedFile != null) {
            menu.add(Menu.NONE, R.id.paste_file, 2, R.string.paste_file);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.create_folder) {
            createNewFolder();
            return true;
        } else if(id == R.id.refresh_files) {
            loadFiles("");
            return true;
        } else if(id == R.id.paste_file) {
            moveSavedFile();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void moveSavedFile() {
        if(mSavedFile == null) {
            Toast.makeText(getActivity(), R.string.generic_error,
                    Toast.LENGTH_LONG).show();
            return;
        }

        getService().moveFilesTask(mSavedFile.file, getContext(), getActivity(),
                mSavedFile.deleteOriginal, new CloudService.MoveFilesCallback() {
            @Override
            public void onComplete(MoveFilesTask.Statistics stats) {
                if(stats.failed > 0 && stats.filesMoved == 0 && stats.foldersMoved == 0) {
                    Toast.makeText(getActivity(), "Failed to "
                            + (mSavedFile.deleteOriginal ? " move " : " copy ")
                            + "files. Try again",
                            Toast.LENGTH_LONG).show();
                } else {
                    String actionText = (mSavedFile.deleteOriginal ? "Transferred" : "Copied ");
                    String folderText = stats.foldersMoved == 0 ? ""
                            : stats.foldersMoved + (stats.foldersMoved == 1 ? "folder" : "folders");
                    String fileText = stats.filesMoved == 0 ? ""
                            : stats.filesMoved + (stats.filesMoved == 1 ? " file" : " files");
                    String failedText = stats.failed == 0 ? "" : "Failed: " + stats.failed;
                    if(!folderText.isEmpty() && !fileText.isEmpty()) {
                        fileText = ", " + fileText;
                    }

                    loadFiles(String.format(Locale.US,"%s: %s %s. %s",
                            actionText, folderText, fileText, failedText));
                }

                try {
                    FileUtils.deleteDirectory(getContext().getCacheDir());
                } catch (IOException ignored) {
                }

                mSavedFile = null;
                getActivity().invalidateOptionsMenu();
            }
        }).executeTask(mFolderId);
    }

    public void createNewFolder() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("New Folder Name");

        final EditText input = new EditText(getContext());
        alert.setView(input);

        alert.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();

                getService().createFolderTask(new CloudService.CreateFolderCallback() {
                    @Override
                    public void onComplete(CloudResource createdFolder) {
                        loadFiles(getString(R.string.create_folder_success));
                    }

                    @Override
                    public void onError(Exception e) {
                        if(e instanceof DropboxUniqueFolderNameException) {
                            Toast.makeText(getActivity(), R.string.duplicate_folder_name_error,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity(), R.string.create_folder_failed,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }).executeTask(mFolderId, value);
            }
        });

        alert.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
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
                    .setMessage(R.string.permissions_rationale)
                    .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissionsForAction(action);
                        }
                    })
                    .setNegativeButton(R.string.cancel_button, null)
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
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    getActivity(), permission)) {
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
                        loadFiles(getString(R.string.file_upload_success));
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getActivity(), R.string.file_upload_failed,
                                Toast.LENGTH_LONG).show();
                    }
                }).executeTask(mFolderId, localFile.getName());
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

        getService().downloadFileTask(dialog, false, new CloudService.DownloadFileCallback() {
            @Override
            public void onComplete(final File file) {
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(file));
                getContext().sendBroadcast(intent);
                Snackbar mySnackbar = Snackbar.make(mInflatedView, R.string.file_dowload_success, Snackbar.LENGTH_INDEFINITE);
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
                Toast.makeText(getActivity(), R.string.file_download_failed,
                        Toast.LENGTH_LONG).show();
            }
        }).executeTask(file.getId(), file.getName());
    }

    @Override
    public boolean getUserVisibleHint() {
        mFilesAdapter.onContextMenuClosed();
        return true;
    }
}
