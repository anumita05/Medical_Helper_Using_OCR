package medicalhelper.anumita.com;


import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import medicalhelper.anumita.com.helper.JSONParse;
import medicalhelper.anumita.com.helper.RestAPI;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private static final String ARG_UID = "uid";

    private String uUID="";

    //View for this Fragment
    private EditText name, email, contact, address;
    private TextView btnUpdate, btnCancel, btnEdit;
    private TableRow updateRow;

    private String userName, userEmail, userContact, userAddress;
    private Dialog mDialog;

    public ProfileFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param uid Parameter 1.
     * @param type Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */

    public static ProfileFragment newInstance(String uid, String type) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_UID, uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uUID = getArguments().getString(ARG_UID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        name = view.findViewById(R.id.user_name_profile);
        email = view.findViewById(R.id.user_profile_email);
        contact = view.findViewById(R.id.user_contact_profile);
        address = view.findViewById(R.id.user_addr_profile);

        updateRow = view.findViewById(R.id.update_row);
        btnUpdate = view.findViewById(R.id.btnUpdateProfile);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnEdit = view.findViewById(R.id.btnEditProfile);

        hideUpdateAndDisable(false);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mDialog = new Dialog(getActivity());
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.loading);
        mDialog.setCancelable(false);

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateAndEnable();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(contact.getText().length() == 0){
                    Snackbar.make(btnUpdate, "Please, Enter Contact Number", Snackbar.LENGTH_SHORT).show();
                    contact.requestFocus();
                }else if(address.getText().length() == 0){
                    Snackbar.make(btnUpdate, "Please, Enter Address", Snackbar.LENGTH_SHORT).show();
                    address.requestFocus();
                }else {
                    new UpdateProfile().execute(uUID, contact.getText().toString(), address.getText().toString());
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideUpdateAndDisable(true);
            }
        });

    }

    private void showUpdateAndEnable() {
        contact.setEnabled(true);
        address.setEnabled(true);

        contact.requestFocus();

        if(updateRow.getVisibility() == View.GONE)
            updateRow.setVisibility(View.VISIBLE);

        if(btnEdit.getVisibility() == View.VISIBLE)
            btnEdit.setVisibility(View.GONE);

    }

    private void hideUpdateAndDisable(boolean defaultv) {
        contact.setEnabled(false);
        address.setEnabled(false);
        name.setEnabled(false);
        email.setEnabled(false);
        if(updateRow.getVisibility() == View.VISIBLE)
            updateRow.setVisibility(View.GONE);

        if(btnEdit.getVisibility() == View.GONE)
            btnEdit.setVisibility(View.VISIBLE);

        if(defaultv){
            contact.setText(userContact);
            address.setText(userAddress);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        new GetProfile().execute(uUID);
    }

    private class GetProfile extends AsyncTask<String, JSONObject, String> {
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
                JSONObject jsonObject = restAPI.getProfile(strings[0]);
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
            Log.d("GetProfile", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
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
                    if (StatusValue.compareTo("ok") == 0) {

                        JSONArray jsonArray = json.getJSONArray("Data");
                        json = jsonArray.getJSONObject(0);
                        //Uid,Name,Contact,Email,Address
                        userName = json.getString("data1");
                        userContact = json.getString("data2");
                        userEmail = json.getString("data3");
                        userAddress = json.getString("data4");

                        setValues();

                    } else if (StatusValue.compareTo("no") == 0) {
                        Toast.makeText(getActivity(), "No Profile Exists", Toast.LENGTH_SHORT).show();
                    } else {
                        String error = json.getString("Data");
                        Log.d("GetProfile", error);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("GetProfile", e.getMessage());
                }
            }
        }
    }

    private class UpdateProfile extends AsyncTask<String, JSONObject, String> {
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
                JSONObject jsonObject = restAPI.UpdateProfile(strings[0], strings[1], strings[2]);
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
            Log.d("UpdateProfile", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
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

                        hideUpdateAndDisable(false);

                        new GetProfile().execute(uUID);

                    } else {
                        String error = json.getString("Data");
                        Log.d("UpdateProfile", error);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("UpdateProfile", e.getMessage());
                }
            }
        }
    }

    private void setValues() {
        name.setText(userName);
        contact.setText(userContact);
        email.setText(userEmail);
        address.setText(userAddress);
    }

}
