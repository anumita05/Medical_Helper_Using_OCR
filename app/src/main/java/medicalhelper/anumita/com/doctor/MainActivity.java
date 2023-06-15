package medicalhelper.anumita.com.doctor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import medicalhelper.anumita.com.chat.ChatFragment;
import medicalhelper.anumita.com.LoginActivity;
import medicalhelper.anumita.com.ProfileFragment;
import medicalhelper.anumita.com.R;
import medicalhelper.anumita.com.UserFragment;
import medicalhelper.anumita.com.helper.UtilConstants;
import medicalhelper.anumita.com.patient.NotificationService;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences sharedPreference;
    private String username = "", useremail ="",uid = "";
    private TextView userName, userEmail;

    private Fragment fragment;

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreference = getSharedPreferences(UtilConstants.SharedPreference, Context.MODE_PRIVATE);

        uid = sharedPreference.getString(UtilConstants.UID, "");
        username = sharedPreference.getString(UtilConstants.UserName, "");
        useremail = sharedPreference.getString(UtilConstants.UserEmail, "");


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();
        menu.removeItem(R.id.nav_query);
        menu.removeItem(R.id.nav_reminders);

        navigationView.invalidate();

        View headerview = navigationView.getHeaderView(0);
        userName = headerview.findViewById(R.id.username);
        userEmail = headerview.findViewById(R.id.useremail);

        userName.setText(username);
        userEmail.setText(useremail);

        loadFragments(R.id.nav_profile);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent service = new Intent(MainActivity.this, NotificationService.class);
        stopService(service);
        startService(service);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        loadFragments(item.getItemId());
        return true;
    }

    private void loadFragments(int id){
        navigationView.setCheckedItem(id);
        switch (id){
            case R.id.nav_profile:
                getSupportActionBar().setTitle("Profile");
                fragment = ProfileFragment.newInstance(uid,"");
                break;
            case R.id.nav_patient:
                getSupportActionBar().setTitle("Patient");
                fragment = new UserFragment().newInstance("Patient", "All", "name");
                break;
            case R.id.nav_chat:
                getSupportActionBar().setTitle("Chat");
                fragment = new ChatFragment().newInstance("Doctor");
                break;
            case R.id.nav_logout:
                clearSharedPreference();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        if (fragment != null) {
//            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.framelayout, fragment).commit();
        }
    }

    private void clearSharedPreference() {
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putString(UtilConstants.UID, "");
        editor.putString(UtilConstants.UserName, "");
        editor.putString(UtilConstants.UserEmail, "");
        editor.putString(UtilConstants.UserType, "");
        editor.apply();
        editor.commit();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);

        Intent service = new Intent(MainActivity.this, NotificationService.class);
        stopService(service);

        finish();
    }

}
