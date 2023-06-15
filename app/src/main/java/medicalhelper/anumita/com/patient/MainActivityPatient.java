package medicalhelper.anumita.com.patient;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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

public class MainActivityPatient extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences sharedPreference;
    private String username = "", useremail ="",uid = "";
    private TextView userName, userEmail;

    private Fragment fragment;
    private int rem_nid = 0;

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreference = getSharedPreferences(UtilConstants.SharedPreference, Context.MODE_PRIVATE);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

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
        MenuItem menuItem = menu.getItem(1);
        menuItem.setTitle("Doctor");

        View headerview = navigationView.getHeaderView(0);
        userName = headerview.findViewById(R.id.username);
        userEmail = headerview.findViewById(R.id.useremail);

        userName.setText(username);
        userEmail.setText(useremail);
        loadFragments(R.id.nav_profile);

        try{
            Intent intent = getIntent();
            rem_nid = intent.getIntExtra(UtilConstants.Chat_NID, 0);
            boolean reminder = intent.getBooleanExtra(UtilConstants.REMINDER_BOOLEAN, false);
            if(rem_nid != 0 && reminder){
                nm.cancel("Medical_OCR_REM", rem_nid);
                loadFragments(R.id.nav_reminders);
            }else {
                nm.cancel("Medical_OCR_REM", rem_nid);
                loadFragments(R.id.nav_query);
            }
        }catch (NullPointerException nullPointer){
            Log.d("MainActivityPatient", "Reminder Id Null");
            nullPointer.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent service = new Intent(MainActivityPatient.this, NotificationService.class);
        stopService(service);
        startService(service);

        Intent reminder = new Intent(MainActivityPatient.this, ReminderService.class);
        stopService(reminder);
        startService(reminder);
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
                getSupportActionBar().setTitle("Doctor");
                fragment = new UserFragment().newInstance("Doctor", "All", "name");
                break;
            case R.id.nav_chat:
                getSupportActionBar().setTitle("Chat");
                fragment = new ChatFragment().newInstance("Patient");
                break;

            case R.id.nav_query:
                getSupportActionBar().setTitle("Medicine");
                fragment = new TabletFragment();
                break;

            case R.id.nav_reminders:
                getSupportActionBar().setTitle("Reminder");
                fragment = new RemindersFragment();
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

        Intent intent = new Intent(MainActivityPatient.this, LoginActivity.class);
        startActivity(intent);
        finish();

        Intent intent1 = new Intent(MainActivityPatient.this, NotificationService.class);
        stopService(intent1);

        Intent intent2 = new Intent(MainActivityPatient.this, ReminderService.class);
        stopService(intent2);
    }
}
