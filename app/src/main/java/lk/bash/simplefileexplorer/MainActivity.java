package lk.bash.simplefileexplorer;

import java.io.File;
import java.util.ArrayList;
import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private ItemAdapter itemAdapter = new ItemAdapter(new ArrayList<Item>());
    private static TextView emptyFolder, currentPathView;
    private static String currentPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {
            if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
        }
        startUp();
    }

    public void startUp() {
        emptyFolder = (TextView) findViewById(R.id.text_empty);
        currentPathView = (TextView) findViewById(R.id.text_path);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        setCurrentPath(pref.getString("pref_default_folder", Environment.getExternalStorageDirectory().getAbsolutePath()));

        Core.setItemAdapter(itemAdapter);
        Core.Load loader = new Core.Load();
        loader.execute(currentPath);

        RecyclerView itemList = (RecyclerView) findViewById(R.id.item_list);
        itemList.setHasFixedSize(true);
        RecyclerView.LayoutManager lm = null;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            lm = new LinearLayoutManager(this);
        } else {
            lm = new GridLayoutManager(this, 4);
        }
        itemList.setLayoutManager(lm);
        itemList.setAdapter(itemAdapter);
    }

    public static void setCurrentPath(String currentPath) {
        MainActivity.currentPath = currentPath;
        currentPathView.setText(currentPath);

        File file = new File(currentPath);
        if (file.isDirectory() && file.list().length == 0) {
            emptyFolder.setVisibility(View.VISIBLE);
        } else {
            emptyFolder.setVisibility(View.INVISIBLE);
        }
    }

    public void upLevel() {
        Core.Load loader = new Core.Load();
        String parent = new File(currentPath).getParent();
        if (!currentPath.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            setCurrentPath(parent);
            loader.execute(parent);
        } else {
            Toast.makeText(getApplicationContext(), "Highest directory reached", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        upLevel();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startUp();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_action, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_refresh:
                Core.Load loader = new Core.Load();
                loader.execute(currentPath);
                setCurrentPath(currentPath);
                return true;
            case R.id.action_up:
                upLevel();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
