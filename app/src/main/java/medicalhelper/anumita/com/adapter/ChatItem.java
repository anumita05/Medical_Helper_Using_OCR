package medicalhelper.anumita.com.adapter;

import android.os.Parcel;
import android.os.Parcelable;

public class ChatItem implements Parcelable {
    //SenderId,RecId,Message,cdate,ctime
    String SenderId;
    String RecId;
    String message;
    String cdate;
    String ctime;
    String ASender;

    //cid,uid,did,message,date,time
    public ChatItem(String senderId, String recId, String message, String cdate, String ctime, String ASender) {
        setSenderId(senderId);
        setRecId(recId);
        setMessage(message);
        setCdate(cdate);
        setCtime(ctime);
        setASender(ASender);
    }

    public String getSenderId() {
        return SenderId;
    }

    public void setSenderId(String senderId) {
        SenderId = senderId;
    }


    public void setRecId(String recId) {
        RecId = recId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCdate() {
        return cdate;
    }

    public void setCdate(String cdate) {
        this.cdate = cdate;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public void setASender(String ASender) {
        this.ASender = ASender;
    }

    public String getASender(){
        return ASender;
    }

    public ChatItem(Parcel in ) {
        readFromParcel( in );
    }

    private void readFromParcel(Parcel in) {
        SenderId = in.readString();
        RecId = in.readString();
        message = in.readString();
        cdate = in.readString();
        ctime = in.readString();
    }

    public static final Creator CREATOR = new Creator() {
        public ChatItem createFromParcel(Parcel in ) {
            return new ChatItem( in );
        }

        public ChatItem[] newArray(int size) {
            return new ChatItem[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(SenderId);
        dest.writeString(RecId);
        dest.writeString(message);
        dest.writeString(cdate);
        dest.writeString(ctime);
    }
}
