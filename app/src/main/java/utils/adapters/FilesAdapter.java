package utils.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oanaplesu.cloudmanager.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import utils.cloud.CloudResource;


public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.MetadataViewHolder> {
    private List<CloudResource> mFiles;
    private final Callback mCallback;
    private int position;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setFiles(List<CloudResource> files) {
        mFiles = Collections.unmodifiableList(new ArrayList<>(files));
        notifyDataSetChanged();
    }

    public CloudResource getFile(int position) {
        return mFiles.get(position);
    }

    public interface Callback {
        void onFolderClicked(CloudResource folder);
    }

    public FilesAdapter(Callback callback) {
        mCallback = callback;
    }

    @Override
    public MetadataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.files_item, viewGroup, false);
        return new MetadataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MetadataViewHolder metadataViewHolder, int i) {
        metadataViewHolder.bind(mFiles.get(i));
        metadataViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setPosition(metadataViewHolder.getAdapterPosition());
                return false;
            }
        });
    }

    @Override
    public void onViewRecycled(MetadataViewHolder metadataViewHolder) {
        metadataViewHolder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(metadataViewHolder);
    }

    @Override
    public long getItemId(int position) {
        return mFiles.get(position).getId().hashCode();
    }

    @Override
    public int getItemCount() {
        return mFiles == null ? 0 : mFiles.size();
    }

    public class MetadataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener
    {
        private final TextView mTextView;
        private final ImageView mImageView;
        private CloudResource mItem;

        public MetadataViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView)itemView.findViewById(R.id.image);
            mTextView = (TextView)itemView.findViewById(R.id.text);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItem.getType() == CloudResource.Type.FOLDER) {
                mCallback.onFolderClicked(mItem);
            } else {
                v.setSelected(true);
            }
        }

        public void bind(CloudResource item) {
            mItem = item;
            mTextView.setText(mItem.getName());

            if (mItem.getType() == CloudResource.Type.FILE) {
                mImageView.setImageResource(R.drawable.file_icon);
            } else if (mItem.getType() == CloudResource.Type.FOLDER) {
                mImageView.setImageResource(R.drawable.folder_icon);
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            if(getAdapterPosition() == 0) {
                return;
            }

            contextMenu.add(Menu.NONE, R.id.download_file,
                    1, R.string.download_file);
            contextMenu.add(Menu.NONE, R.id.cut_file,
                    2, R.string.cut_file);
            contextMenu.add(Menu.NONE, R.id.copy_file,
                    3, R.string.copy_file);
            contextMenu.add(Menu.NONE, R.id.delete_file,
                4, R.string.delete_file);

        }
    }
}
