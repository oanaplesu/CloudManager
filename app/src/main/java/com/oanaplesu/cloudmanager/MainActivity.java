package com.oanaplesu.cloudmanager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import utils.cloud.AccountType;
import utils.db.AppDatabase;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Menu mNavigationMenu;
    private Toolbar mToolbar;
    public static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationMenu = navigationView.getMenu();
        navigationView.setNavigationItemSelectedListener(this);
      //  getApplicationContext().deleteDatabase("userdatabase");

        UpdateNavigationMenu();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.contentFrame, new AccountsFragment());
        ft.commit();
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

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
         checkSelectedMenuItem(item.getTitle(), item.getGroupId());

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
                bundle.putInt(getString(R.string.account_type), AccountType.GOOGLE_DRIVE.ordinal());
                bundle.putString(getString(R.string.account_email), item.toString());
                bundle.putString(getString(R.string.folder_id), "");
                fragment.setArguments(bundle);
            }
        } else if (groupId == R.id.dropbox_accounts) {
            bundle.putInt(getString(R.string.account_type), AccountType.DROPBOX.ordinal());
            bundle.putString(getString(R.string.account_email), item.toString());
            bundle.putString(getString(R.string.folder_id), "");
            fragment = new FilesFragment();
            fragment.setArguments(bundle);
        } else if(groupId == R.id.other_options){
            switch(id) {
                case R.id.add_new_account:
                    fragment = new AddNewAccountFragment();
                    break;
                case R.id.settings:
                    fragment = new AccountsFragment();
                    break;
                case R.id.about:
                    fragment = new AboutFragment();
                    break;
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

    public void checkSelectedMenuItem(CharSequence tabName, int groupId) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu nav_menu = navigationView.getMenu();

        for (int i = 0; i < nav_menu.size(); i++) {
            MenuItem menuItem = nav_menu.getItem(i);

            if(menuItem.getTitle().equals(tabName)
                    && menuItem.getGroupId() == groupId) {

                int mainColor = getResources().getColor(R.color.colorPrimary);
                switch(groupId) {
                    case R.id.google_drive_accounts:
                        mainColor = getResources().getColor(R.color.colorGoogleDrive);
                        break;
                    case R.id.dropbox_accounts:
                        mainColor = getResources().getColor(R.color.colorDropbox);
                        break;
                    default:
                        break;
                }
                mToolbar.setBackgroundColor(mainColor);
                mToolbar.setTitle(tabName);

                menuItem.setChecked(true);
            } else {
                menuItem.setChecked(false);
            }
        }
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

            mNavigationMenu.add(R.id.other_options, R.id.settings,
                    Menu.NONE, "Settings").setIcon(R.drawable.settings_icon);
            mNavigationMenu.add(R.id.other_options, R.id.about,
                    Menu.NONE, "About").setIcon(R.drawable.about_icon);
        }
    }

    public void UpdateNavigationMenu() {
        new UpdateNavigationMenuTask().execute();
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        getSupportFragmentManager().findFragmentById(R.id.contentFrame)
                .getUserVisibleHint();
    }
}

