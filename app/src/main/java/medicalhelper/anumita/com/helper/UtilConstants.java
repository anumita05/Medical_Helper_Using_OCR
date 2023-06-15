package medicalhelper.anumita.com.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import medicalhelper.anumita.com.R;

public class UtilConstants {

    public static String SharedPreference = "MedHelperOCR";
    public static String UID = "uid";
    public static String UserName = "Name";
    public static String UserEmail = "Email";
    public static String UserType = "UserType";

    /**
     * Medicine Constants
     */
    //tid,uid,name,type,color,size,detail,pic
    public static String Med_Tid = "medicineId";
    public static String Med_Name = "medicineName";
    public static String Med_Type = "medicineType";
    public static String Med_Size = "medicineSize";
    public static String Med_Details = "medicineDetails";
    public static String Med_EXPDATE = "medicineExpiryDate";
    public static String Med_Pic = "medicinePic";
    public static String Med_Color = "medicineColor";
    public static String Med_Quantity = "medicineQty";
    public static String Med_Time = "medicineTime";
    public static String Med_Rid = "reminderId";

    /**
     * Patient Constants
     */
    public static String Pat_ID = "pateintID";
    public static String Pat_Name = "pateintName";
    public static String Pat_Contact = "pateintContact";
    public static String Pat_Email = "pateintEmail";
    public static String Pat_Addr = "pateintAddr";

    /**
     * Chat Constants
     */
    public static String Chat_ID = "chatDestID";
    public static String Chat_Type = "chatTypeSrc";
    public static String Chat_Name = "chatName";
    public static String Chat_NID = "NID";

    public static String ChatBroadcastID = "chatBroadCastId";
    public static String BROADCAST_CHAT = "RECEIVE_ID";

    public static String BROADCAST_REMINDER = "RECEIVE_REMINDER";

    //Notification Variable
    public static final String CHANNEL_ID = "10020";
    public static final String CHANNEL_ID_REM = "10021";

    //Reminder Variable
    public static final String REMINDER_BOOLEAN = "BooleanReminder";

    /**
     * Activity Helper Variables
     */
    public static byte[] imageBytes = new byte[]{};
    public static String MedPicImage = "";

    public static Bitmap getBitmap(String img) {
        byte[] imageBytes = Base64.decode(img, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }

    public static byte[] getImageBytes() {
        return imageBytes;
    }

    public static void setImageBytes(byte[] imageBytes) {
        UtilConstants.imageBytes = imageBytes;
    }

    public static String getMedPicImage() {
        return MedPicImage;
    }

    public static void setMedPicImage(String medPicImage) {
        MedPicImage = medPicImage;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth)    {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        //rotate Image
        matrix.postRotate(90);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public static void  createNotificationChannel(@NonNull Context context, @NonNull String CHANNEL_ID) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }else {
                Log.d("NotificationLog", "NotificationManagerNull");
            }
        }
    }
}
