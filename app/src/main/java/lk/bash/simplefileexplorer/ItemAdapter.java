package lk.bash.simplefileexplorer;

import java.io.File;
import java.util.List;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.webkit.MimeTypeMap;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private List<Item> itemList;

    public ItemAdapter(List<Item> itemList) {
        this.itemList = itemList;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_list_item, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
            holder.setItem(itemList.get(position));
            setFadeAnimation(holder.itemView);
    }

    private void setFadeAnimation(View view) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200);
        view.startAnimation(anim);
    }

    @Override
    public void onViewDetachedFromWindow(ItemViewHolder holder)
    {
        holder.itemView.clearAnimation();
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        protected Item item;
        protected TextView itemName;
        protected ImageView itemThumbnail;

        public ItemViewHolder(View itemView) {
            super(itemView);
            itemName = (TextView) itemView.findViewById(R.id.item_name);
            itemThumbnail = (ImageView) itemView.findViewById(R.id.item_thumbnail);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void setItem(Item item) {
            this.item = item;
            itemName.setText(item.name);
            if(item.isFolder){
                this.itemThumbnail.setImageResource(R.drawable.icon_folder);
            } else {
                this.itemThumbnail.setImageResource(R.drawable.icon_file);
            }
        }

        @Override
        public void onClick(View v) {
            if(item.isFolder){
                Core.Load loader= new Core.Load();
                loader.execute(item.uri);
                MainActivity.setCurrentPath(item.uri);
            } else {
                File current = new File(item.uri);
                Uri uri = Uri.fromFile(current);

                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());

                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                ContentResolver cr = v.getContext().getContentResolver();
                intent.setDataAndType(uri, mimeType);
                v.getContext().startActivity(intent);
            }
        }

        @Override
        public boolean onLongClick(final View v) {
            AlertDialog.Builder ActionMenu = new AlertDialog.Builder(v.getContext());
            final AlertDialog.Builder ConfirmDelete = new AlertDialog.Builder(v.getContext());
            ActionMenu.setTitle("Select action");
            String[] actions = {"Delete"};
            ActionMenu.setItems(actions, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if(which==0){
                        ConfirmDelete.setTitle("Delete");
                        ConfirmDelete.setMessage("Are you sure you want to delete selected item?");
                        ConfirmDelete.setNegativeButton("No",null);
                        ConfirmDelete.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Core.Delete deleter = new Core.Delete(v.getContext());
                                deleter.execute(item);
                            }
                        });
                        ConfirmDelete.create().show();
                    }
                }
            });
            ActionMenu.create().show();
            return true;
        }
    }
}