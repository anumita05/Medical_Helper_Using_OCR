package medicalhelper.anumita.com.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import medicalhelper.anumita.com.patient.NotificationService;
import medicalhelper.anumita.com.patient.ReminderService;

public class MyReceiver extends BroadcastReceiver {
    public MyReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try
        {
            SharedPreferences sharedPreferences = context.getSharedPreferences(UtilConstants.SharedPreference, Context.MODE_PRIVATE);
            if(sharedPreferences.getString(UtilConstants.UID, "").compareTo("") != 0){
                if(sharedPreferences.getString(UtilConstants.UserType, "").compareTo("Doctor") !=0){
                    Intent i = new Intent(context, NotificationService.class);
                    context.startService(i);
                }else {
                    Intent i = new Intent(context, NotificationService.class);
                    context.startService(i);

                    Intent i1 = new Intent(context,ReminderService.class);
                    context.startService(i1);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
