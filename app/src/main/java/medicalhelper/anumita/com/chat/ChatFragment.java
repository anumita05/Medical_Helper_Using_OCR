package medicalhelper.anumita.com.chat;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import medicalhelper.anumita.com.R;
import medicalhelper.anumita.com.helper.JSONParse;
import medicalhelper.anumita.com.helper.RestAPI;
import medicalhelper.anumita.com.helper.UtilConstants;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    public static String CHAT_SRC ="Chat_Source";

    private View cView;
    private SharedPreferences sharedPreferences;
    private String UID = "", chat_source = "";
    private ArrayList<String> uid, uname;
    //uid,name
    private ListView listView;
    private TextView error;
    private Dialog mDialog;


    public ChatFragment() {
    }

    public ChatFragment newInstance(@NonNull String c_Source){
        ChatFragment chatFragment = new ChatFragment();
        Bundle bundle = new Bundle();
        bundle.putString(CHAT_SRC, c_Source);
        chatFragment.setArguments(bundle);
        return chatFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle != null){
            chat_source = bundle.getString(CHAT_SRC);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        cView =  inflater.inflate(R.layout.fragment_chat, container, false);
        sharedPreferences = getActivity().getSharedPreferences(UtilConstants.SharedPreference, Context.MODE_PRIVATE);
        UID = sharedPreferences.getString(UtilConstants.UID, "");

        mDialog = new Dialog(getActivity());
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.loading);
        mDialog.setCancelable(false);


        listView = (ListView) cView.findViewById(R.id.chatList);
        error = (TextView) cView.findViewById(R.id.chatMsg);

        error.setText("No chats yet....");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Intent chat = new Intent(getActivity(), ChatActivity.class);
                chat.putExtra(UtilConstants.Chat_ID, uid.get(position));
                chat.putExtra(UtilConstants.Chat_Type, chat_source);
                chat.putExtra(UtilConstants.Chat_Name, uname.get(position));
                chat.putExtra(UtilConstants.Chat_NID, 0);
                startActivity(chat);
            }
        });

        return cView;
    }

    @Override
    public void onResume() {
        super.onResume();
        new GetChatByName().execute(chat_source,UID);
    }

    private class GetChatByName extends AsyncTask<String, JSONObject, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String a = "back";
            RestAPI api = new RestAPI();
            try {
                JSONObject json = api.getNamesforChat(params[0],params[1]);
                JSONParse jp = new JSONParse();
                a = jp.Parse(json);
            } catch (Exception e) {
                a = e.getMessage();
            }
            return a;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mDialog.dismiss();
            Log.d("GetChat", s);
            if (s.contains("Unable to resolve host")) {
                AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                ad.setTitle("Unable to Connect!");
                ad.setMessage("Check your Internet Connection,Unable to connect the Server");
                ad.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                ad.show();
            } else {
                try {
                    JSONObject json = new JSONObject(s);
                    String ans = json.getString("status");
//                    Log.d("ANS", s);
                    if (ans.compareTo("ok") == 0) {
                        uid = new ArrayList<String>();
                        uname = new ArrayList<String>();

                        JSONArray jarray = json.getJSONArray("Data");
                        for (int i = 0; i < jarray.length(); i++) {
                            JSONObject jdata = jarray.getJSONObject(i);
                            uid.add(jdata.getString("data0"));
                            uname.add(jdata.getString("data1"));
                        }

                        if(error.getVisibility() == View.VISIBLE)
                            error.setVisibility(View.GONE);

                        if(listView.getVisibility()==View.GONE)
                            listView.setVisibility(View.VISIBLE);

                        Adapter adapter = new Adapter(getActivity(), uname);
                        listView.setAdapter(adapter);

                    } else if (ans.compareTo("no") == 0) {

                        listView.setAdapter(null);

                        if(error.getVisibility() == View.GONE)
                            error.setVisibility(View.VISIBLE);

                        if(listView.getVisibility()==View.VISIBLE)
                            listView.setVisibility(View.GONE);

                    } else if (ans.compareTo("error") == 0) {
                        String error = json.getString("Data");
                        Log.d("GetChat", error);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("GetChat", e.getMessage());
                }
            }
        }
    }

    private class Adapter extends ArrayAdapter<String> {
        Context con;

        public Adapter(@NonNull Context context, ArrayList<String> a) {
            super(context, R.layout.chat_name_item, a);
            con = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = LayoutInflater.from(con).inflate(R.layout.chat_name_item, null, true);
            TextView fname = (TextView) v.findViewById(R.id.chat_names);

            fname.setText(uname.get(position));

            return v;
        }
    }

}
