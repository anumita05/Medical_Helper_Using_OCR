package medicalhelper.anumita.com;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import medicalhelper.anumita.com.helper.JSONParse;
import medicalhelper.anumita.com.helper.RestAPI;

public class RegisterActivity extends AppCompatActivity {
    private Dialog mDialog;
    private String selected = "";

    private EditText name, contact, email, address, password;
    private Spinner userType;
    private Button register;

    private String[] message = new String[]{"Please, Enter Your Name", "Please, Enter Your Contact Number", "Please, Enter Your Email", "Please Enter Valid Your Email"
            , "Please, Enter Your Address", "Please, Enter Your Password"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = findViewById(R.id.user_name_register);
        contact = findViewById(R.id.user_contact_register);
        email = findViewById(R.id.user_register_email);
        address = findViewById(R.id.user_addr_register);
        password = findViewById(R.id.user_register_pass);
        userType = findViewById(R.id.user_register_spin);
        register = findViewById(R.id.btn_SignUp);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mDialog = new Dialog(RegisterActivity.this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.loading);
        mDialog.setCancelable(false);

        setUpSpinner();

        userType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected = userType.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
//                    string Name, string Email, string Contact,string address,string pass,string type
                    new Register().execute(name.getText().toString(), email.getText().toString(),contact.getText().toString(),
                                            address.getText().toString(), password.getText().toString(), selected);
                }
            }
        });

    }

    private boolean validate() {
        boolean check = true;
        EditText[] editText = new EditText[]{name, contact, email,email, address, password};
        for(int i = 0;i<editText.length;i++){
            if(i == 3){

                if(!Patterns.EMAIL_ADDRESS.matcher(editText[i].getText().toString()).matches()){
                    Snackbar.make(register, message[i], Snackbar.LENGTH_SHORT).show();
                    editText[i].requestFocus();
                    check = false;
                    break;
                }
            }else {
                if(editText[i].getText().length() == 0){
                    Snackbar.make(register, message[i], Snackbar.LENGTH_SHORT).show();
                    editText[i].requestFocus();
                    check = false;
                    break;
                }
            }
        }
        return check;
    }

    private void setUpSpinner() {
        ArrayList<String> type = new ArrayList<String>();
        type.add("Doctor");
        type.add("Patient");

        ArrayAdapter<String> adapter =  new ArrayAdapter<String>(RegisterActivity.this, R.layout.spinner_item, type);
        adapter.setDropDownViewResource(R.layout.dropdown_spinner_item);
        userType.setAdapter(adapter);

    }

    private class Register extends AsyncTask<String, JSONObject, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String response = "";
            RestAPI restAPI = new RestAPI();
            JSONParse jsonParse = new JSONParse();
            try {
                JSONObject jsonObject = restAPI.Register(strings[0], strings[1], strings[2], strings[3], strings[4], strings[5]);
                response = jsonParse.Parse(jsonObject);
            } catch (Exception e) {
                response = e.getMessage();
                e.printStackTrace();

            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mDialog.dismiss();
            Log.d("LoginActivity", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(RegisterActivity.this);
                ad.setTitle("Unable to Connect!");
                ad.setMessage("Check your Internet Connection,Unable to connect the Server");
                ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                ad.show();

            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String StatusValue = json.getString("status");
                    if (StatusValue.compareTo("true") == 0) {

                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();

                        AlertDialog.Builder ad = new AlertDialog.Builder(RegisterActivity.this);
                        ad.setTitle("Success");
                        ad.setMessage("Registration Successful, Press OK to Login");
                        ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        ad.show();

                    } else if (StatusValue.compareTo("already") == 0) {
                        AlertDialog.Builder ad = new AlertDialog.Builder(RegisterActivity.this);
                        ad.setTitle("Account Already Exists");
                        ad.setMessage("A user already exists with this Email");
                        ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        ad.show();
                    } else {
                        String error = json.getString("Data");
                        Log.d("LoginActivity", error);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("LoginActivity", e.getMessage());
                }
            }
        }
    }
}
