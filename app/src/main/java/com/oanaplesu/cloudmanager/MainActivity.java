package com.oanaplesu.cloudmanager;

import android.Manifest;
import android.arch.persistence.room.Update;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import db.AppDatabase;
import db.GoogleDriveUser;
import utils.GoogleDriveService;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Menu mNavigationMenu;
    private final static int GOOGLE_ACCOUNT = 100;
    private final static int DROPBOX_ACCOUNT = 200;
    private static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationMenu = navigationView.getMenu();
        navigationView.setNavigationItemSelectedListener(this);
      //  getApplicationContext().deleteDatabase("userdatabase");

        UpdateNavigationMenu();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
       checkSelectedMenuItem(item);

        int id = item.getItemId();
        int groupId = item.getGroupId();

        Fragment fragment = null;
        Bundle bundle = new Bundle();

        if (groupId == R.id.google_drive_accounts) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                item.setChecked(false);
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.GET_ACCOUNTS},
                        MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);
            } else {
                fragment = new FilesFragment();
                bundle.putInt("accountType", GOOGLE_ACCOUNT);
                bundle.putString("accountEmail", item.toString());
                bundle.putString("folderId", "");
                fragment.setArguments(bundle);
            }
        } else if (groupId == R.id.dropbox_accounts) {
            bundle.putInt("accountType", DROPBOX_ACCOUNT);
            bundle.putString("accountEmail", item.toString());
            bundle.putString("folderId", "");
            fragment = new FilesFragment();
            fragment.setArguments(bundle);
        } else {
            if (id == R.id.add_new_account) {
                fragment = new AddNewAccountFragment();
            }
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.contentFrame, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkSelectedMenuItem(MenuItem item) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu nav_menu = navigationView.getMenu();

        for (int i = 0; i < nav_menu.size(); i++) {
            nav_menu.getItem(i).setChecked(false);
        }

        item.setChecked(true);
    }

    class UpdateNavigationMenuTask extends AsyncTask<Void, Void, Void> {
        private AppDatabase database;
        List<String> googleAccounts;
        List<String> dropboxAccounts;

        @Override
        protected Void doInBackground(Void... args) {
            database = AppDatabase.getDatabase(getApplicationContext());
            googleAccounts = database.googleDriveUserDao().getAllAccounts();
            dropboxAccounts = database.dropboxUserDao().getAllAccounts();

            return null;
        }

        @Override
        protected void onPostExecute(Void arg) {
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            Menu navigationMenu = navigationView.getMenu();
            navigationMenu.clear();

            for(String account : googleAccounts) {
                mNavigationMenu.add(R.id.google_drive_accounts,
                        Menu.NONE, Menu.NONE, account).setIcon(R.drawable.google_drive_icon);
            }

            for(String account : dropboxAccounts) {
                mNavigationMenu.add(R.id.dropbox_accounts,
                        Menu.NONE, Menu.NONE, account).setIcon(R.drawable.dropbox_icon_black);
            }

            mNavigationMenu.add(R.id.other_options, R.id.add_new_account,
                    Menu.NONE, "Add new account").setIcon(R.drawable.add_icon);
        }
    }

    public void UpdateNavigationMenu() {
        new UpdateNavigationMenuTask().execute();
    }
}

