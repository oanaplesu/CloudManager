package utils.tasks;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import utils.cloud.CloudResource;
import utils.services.CloudManager;
import utils.services.CloudService;


public class MoveFilesTask implements CloudRequestTask {
    private CloudService mSourceService;
    private CloudService mDestService;
    private CloudService.MoveFilesCallback mCallback;
    private boolean mDeleteOriginal;
    private CloudResource mSourceFile;
    private Context mContext;
    private ProgressDialog mDialog;
    private ArrayList<UUID> mTasksList;
    private Statistics mStats;

    public class Statistics {
        public int foldersMoved;
        public int filesMoved;
        public int failed;
    }

    public MoveFilesTask(CloudService destService, CloudResource sourceFile,
                         boolean deleteOriginal, Context context, Activity activity,
                         CloudService.MoveFilesCallback callback) {
        this.mSourceService = CloudManager.getService(context,
                sourceFile.getAccountType().ordinal(),
                sourceFile.getAccountEmail(), activity);
        this.mDestService = destService;
        this.mSourceFile = sourceFile;
        this.mDeleteOriginal = deleteOriginal;
        this.mCallback = callback;
        this.mContext = context;
        this.mTasksList = new ArrayList<>();
        this.mStats = new Statistics();
    }

    @Override
    public void executeTask(String... args) {
        String destFolderId = args[0];

        mDialog = new ProgressDialog(mContext);
        if(mDeleteOriginal) {
            mDialog.setMessage("Transferring files");
        } else {
            mDialog.setMessage("Copying files");
        }
        mDialog.show();

        if(mSourceFile.getType() == CloudResource.Type.FILE) {
            moveOneFile(mSourceFile, destFolderId);
        } else {
            moveFolder(mSourceFile, destFolderId);
        }
    }

    private void moveFolder(final CloudResource folder, String destFolderId) {
        final UUID tasksId = addNewTask();

        mDestService.createFolderTask(new CloudService.CreateFolderCallback() {
            @Override
            public void onComplete(final CloudResource createdFolder) {
                mSourceService.getFilesTask(null, new CloudService.GetFilesCallback() {
                    @Override
                    public void onComplete(List<CloudResource> files) {
                        files.remove(0);

                        for(CloudResource file: files) {
                            if(file.getType() == CloudResource.Type.FILE) {
                                moveOneFile(file, createdFolder.getId());
                            } else {
                                moveFolder(file, createdFolder.getId());
                            }
                        }

                        onTaskFinished(tasksId, true);
                    }

                    @Override
                    public void onError(Exception e) {
                        onTaskError(tasksId);
                    }
                }).executeTask(folder.getId());
            }

            @Override
            public void onError(Exception e) {
                onTaskError(tasksId);
            }
        }).executeTask(destFolderId, folder.getName());
    }

    private void moveOneFile(final CloudResource file, final String destFolderId) {
        final UUID tasksId = addNewTask();

        mSourceService.downloadFileTask(null, true,  new CloudService.DownloadFileCallback() {
            @Override
            public void onComplete(final File tmpFile) {
                mDestService.uploadFileTask(tmpFile, null, new CloudService.GenericCallback() {
                    @Override
                    public void onComplete() {
                        onTaskFinished(tasksId, false);
                    }

                    @Override
                    public void onError(Exception e) {
                        onTaskError(tasksId);
                    }
                }).executeTask(destFolderId, file.getName());
            }

            @Override
            public void onError(Exception e) {
               onTaskError(tasksId);
            }
        }).executeTask(file.getId(), file.getName(), mContext.getCacheDir().toString());
    }

    private void onTaskError(UUID taskId) {
        mStats.failed++;
        mTasksList.remove(taskId);
        checkAllTasksCompleted();
    }

    private void onTaskFinished(UUID taskId, boolean isFolder) {
        if(isFolder) {
            mStats.foldersMoved++;
        } else {
            mStats.filesMoved++;
        }

        mTasksList.remove(taskId);

        checkAllTasksCompleted();
    }

    private UUID addNewTask() {
        UUID tasksId = UUID.randomUUID();
        mTasksList.add(tasksId);

        return tasksId;
    }

    private void checkAllTasksCompleted() {
        if(mTasksList.isEmpty()) {
            if(mDeleteOriginal) {
                mSourceService.deleteFileTask(new CloudService.GenericCallback() {
                    @Override
                    public void onComplete() {
                        onAllTasksCompleted();
                    }

                    @Override
                    public void onError(Exception e) {
                        onAllTasksCompleted();
                    }
                }).executeTask(mSourceFile.getId());
            } else {
                onAllTasksCompleted();
            }
        }
    }

    private void onAllTasksCompleted() {
        mCallback.onComplete(mStats);

        if(mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
}

