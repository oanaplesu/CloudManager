package utils.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.oanaplesu.cloudmanager.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import utils.cloud.CloudAccount;


public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.MetadataViewHolder> {
    private List<CloudAccount> mAccounts;
    private final Callback mCallback;

    public void setAccounts(List<CloudAccount> accounts) {
        mAccounts = Collections.unmodifiableList(new ArrayList<>(accounts));
        notifyDataSetChanged();
    }

    public interface Callback {
        void onAccountClicked(CloudAccount account);
        void onDeleteAccountClicked(CloudAccount account);
    }

    public AccountAdapter(Callback callback) {
        mCallback = callback;
    }

    @Override
    public MetadataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.account_item, viewGroup, false);
        return new MetadataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MetadataViewHolder metadataViewHolder, int i) {
        metadataViewHolder.bind(mAccounts.get(i));
    }

    @Override
    public void onViewRecycled(MetadataViewHolder metadataViewHolder) {
        metadataViewHolder.itemView.setOnLongClickListener(null);
        super.onViewRecycled(metadataViewHolder);
    }

    @Override
    public long getItemId(int position) {
        return mAccounts.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return mAccounts == null ? 0 : mAccounts.size();
    }

    public class MetadataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private final TextView mAccountEmailView;
        private final ImageView mAccountProviderView;
        private final Button mDeleteAccountButton;
        private CloudAccount mItem;

        public MetadataViewHolder(View itemView) {
            super(itemView);
            mAccountProviderView = (ImageView)itemView.findViewById(R.id.account_provider);
            mAccountEmailView = (TextView)itemView.findViewById(R.id.account_name);
            mDeleteAccountButton = (Button)itemView.findViewById(R.id.remove_account);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mCallback.onAccountClicked(mItem);
        }

        public void bind(final CloudAccount item) {
            mItem = item;
            mAccountEmailView.setText(mItem.getEmail());

            if (mItem.getProvider() == CloudAccount.Provider.GOOGLE_DRIVE) {
                mAccountProviderView.setImageResource(R.drawable.google_drive_icon_large);
            } else if (mItem.getProvider() == CloudAccount.Provider.DROPBOX) {
                mAccountProviderView.setImageResource(R.drawable.dropbox_icon_large);
            }

            mDeleteAccountButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallback.onDeleteAccountClicked(item);
                }
            });
        }
    }
}
