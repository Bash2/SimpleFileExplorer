package lk.bash.simplefileexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class Core {
    private static ItemAdapter itemAdapter;

    public static void setItemAdapter(ItemAdapter itemAdapter) {
        Core.itemAdapter = itemAdapter;
    }

    public static class Load extends AsyncTask<String, Integer, ArrayList<Item>> {
        @Override
        protected ArrayList<Item> doInBackground(String... params) {
            ArrayList<Item> itemData = new ArrayList<Item>();
            File dir = new File(params[0]);
            File[] dirs = dir.listFiles();
            Arrays.sort(dirs);

            for(File file: dirs){
                if(!file.isHidden()){
                    Item item = new Item();
                    item.name = file.getName();
                    item.uri = file.getAbsolutePath();
                    item.isFolder = file.isDirectory();
                    itemData.add(item);
                }
            }
            return itemData;
        }

        @Override
        protected void onPostExecute(ArrayList<Item> itemData) {
            itemAdapter.setItemList(itemData);
            itemAdapter.notifyDataSetChanged();
        }
    }

    public static class Delete extends AsyncTask<Item, Integer, Item> {
        private Context context;
        ProgressDialog progress;

        public Delete(Context context) {
            this.context = context;
            this.progress = new ProgressDialog(context);
            progress.setTitle("Deleting");
            progress.setMessage("Please wait");
        }

        @Override
        protected Item doInBackground(Item... params) {
            deleteRecursive(new File(params[0].uri));
            return params[0];
        }

        @Override
        protected void onPreExecute() {
            progress.show();
        }

        public void deleteRecursive(File file){
            if(file.isDirectory()){
                File[] content = file.listFiles();
                for(File temp: content){
                    if(temp.isDirectory()){
                        deleteRecursive(temp);
                    } else {
                        temp.delete();
                    }
                }
            }
            file.delete();
        }

        @Override
        protected void onPostExecute(Item item) {
            itemAdapter.getItemList().remove(item);
            itemAdapter.notifyDataSetChanged();
            progress.dismiss();
            Toast.makeText(context, "Deleted", Toast.LENGTH_LONG).show();
        }
    }
}
