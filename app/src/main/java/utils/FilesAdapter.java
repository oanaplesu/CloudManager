package utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import com.oanaplesu.cloudmanager.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.MetadataViewHolder> {
    private List<CloudResource> mFiles;
    private final Callback mCallback;

    public void setFiles(List<CloudResource> files) {
        mFiles = Collections.unmodifiableList(new ArrayList<>(files));
        notifyDataSetChanged();
    }

    public interface Callback {
        void onFolderClicked(CloudResource folder);
        void onFileClicked(CloudResource file);
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

    private int i = 0;

    @Override
    public void onBindViewHolder(MetadataViewHolder metadataViewHolder, int i) {
        metadataViewHolder.bind(mFiles.get(i));
    }

    @Override
    public long getItemId(int position) {
        return i++;
    }

    @Override
    public int getItemCount() {
        return mFiles == null ? 0 : mFiles.size();
    }

    public class MetadataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTextView;
        private final ImageView mImageView;
        private CloudResource mItem;

        public MetadataViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView)itemView.findViewById(R.id.image);
            mTextView = (TextView)itemView.findViewById(R.id.text);
            itemView.setOnClickListener(this);
            itemView.setLongClickable(true);
        }

        @Override
        public void onClick(View v) {
            if (mItem.getType() == CloudResource.Type.FOLDER) {
                mCallback.onFolderClicked(mItem);
            }  else if (mItem.getType() == CloudResource.Type.FILE) {
                mCallback.onFileClicked(mItem);
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
    }
}
