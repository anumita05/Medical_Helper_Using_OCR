package medicalhelper.anumita.com;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import medicalhelper.anumita.com.doctor.MedicineActivity;
import medicalhelper.anumita.com.doctor.RemindersActivity;
import medicalhelper.anumita.com.helper.UtilConstants;

public class UserDetails extends AppCompatActivity {
    //View for this Fragment
    private EditText name, email, contact, address;
    private TextView btnMedicine, btnReminders, btnGone;
    private TableRow updateRow;

    private String uUid = "", userName = "", userEmail="", userContact="", userAddress="", userType="";
    private SharedPreferences sharedPreferences;
    private String D_ID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        sharedPreferences = getSharedPreferences(UtilConstants.SharedPreference, Context.MODE_PRIVATE);
        D_ID = sharedPreferences.getString(UtilConstants.UID, "");

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name =  findViewById(R.id.user_name_profile);
        email =  findViewById(R.id.user_profile_email);
        contact =  findViewById(R.id.user_contact_profile);
        address =  findViewById(R.id.user_addr_profile);

        updateRow =  findViewById(R.id.update_row);
        btnMedicine =  findViewById(R.id.btnUpdateProfile);
        btnReminders =  findViewById(R.id.btnCancel);
        btnGone =  findViewById(R.id.btnEditProfile);

        btnMedicine.setText("View Medicine");
        btnReminders.setText("View Reminders");

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            getAndSetValues(bundle);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        btnReminders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDetails.this, RemindersActivity.class);
                intent.putExtra(UtilConstants.Pat_ID, uUid);
                startActivity(intent);
            }
        });

        btnMedicine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDetails.this, MedicineActivity.class);
                intent.putExtra(UtilConstants.Pat_ID, uUid);
                startActivity(intent);
            }
        });
    }

    private void getAndSetValues(Bundle bundle) {

        uUid = bundle.getString(UtilConstants.Pat_ID);
        userName = bundle.getString(UtilConstants.Pat_Name);
        userContact = bundle.getString(UtilConstants.Pat_Contact);
        userEmail = bundle.getString(UtilConstants.Pat_Email);
        userAddress = bundle.getString(UtilConstants.Pat_Addr);
        userType = bundle.getString(UtilConstants.UserType);

        getSupportActionBar().setTitle(userType+" Details");

        if(userType.compareTo("Doctor") == 0){
            btnGone.setVisibility(View.GONE);
            updateRow.setVisibility(View.GONE);
        }else {
            btnGone.setVisibility(View.GONE);
            updateRow.setVisibility(View.VISIBLE);
        }

        name.setText(userName);
        email.setText(userEmail);
        contact.setText(userContact);
        address.setText(userAddress);

        EditText[] editTexts = new EditText[]{name, email, contact, address};

        for (EditText editText : editTexts) {
            editText.setEnabled(false);
            editText.setTextColor(getResources().getColor(R.color.primaryColor));
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
