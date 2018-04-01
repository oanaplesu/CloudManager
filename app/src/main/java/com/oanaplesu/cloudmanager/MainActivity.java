package com.oanaplesu.cloudmanager;

import android.arch.persistence.room.Update;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import db.AppDatabase;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Menu mNavigationMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationMenu = navigationView.getMenu();
        navigationView.setNavigationItemSelectedListener(this);
        getApplicationContext().deleteDatabase("userdatabase");

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
           // fragment = new FirstFragment();
           // fragment.setArguments(bundle);
        } else if (groupId == R.id.dropbox_accounts) {
           // fragment = new FirstFragment();
           // fragment.setArguments(bundle);
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

    class UpdateNavigationMenuTask extends AsyncTask<Void, Boolean, List<String>> {
        private AppDatabase database;

        @Override
        protected List<String> doInBackground(Void... args) {
            database = AppDatabase.getDatabase(getApplicationContext());

            return database.googleDriveUserDao().getAllAccounts();
        }

        @Override
        protected void onPostExecute(List<String> googleAccounts) {
            super.onPostExecute(googleAccounts);

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            Menu navigationMenu = navigationView.getMenu();
            navigationMenu.clear();

            for(String account : googleAccounts) {
                mNavigationMenu.add(R.id.google_drive_accounts,
                        Menu.NONE, Menu.NONE, account).setIcon(R.drawable.google_drive_icon);
            }

            mNavigationMenu.add(R.id.other_options, R.id.add_new_account,
                    Menu.NONE, "Add new account").setIcon(R.drawable.add_icon);
        }
    }

    public void UpdateNavigationMenu() {
        new UpdateNavigationMenuTask().execute();
    }
}

