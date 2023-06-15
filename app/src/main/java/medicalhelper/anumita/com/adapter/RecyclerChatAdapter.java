package medicalhelper.anumita.com.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.ArrayList;

import medicalhelper.anumita.com.R;

/**
 * Created by nevon on 7/24/2018.
 */

public class RecyclerChatAdapter extends RecyclerView.Adapter<RecyclerChatAdapter.MyViewHolder>{

    private Context context;
    private ArrayList<ChatItem> chatItems;
    private String docID;
    //ItemClick Listener
    public OnItemClickListener onItemClickListener;

    private int PATIENT_VIEW = 0;
    private int DOCTOR_VIEW = 1;


    public RecyclerChatAdapter(Context context, ArrayList<ChatItem> chatItems, String pid) {
        this.context = context;
        this.chatItems = chatItems;
        this.docID = pid;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View rView;
        MyViewHolder vh;
        if(viewType == DOCTOR_VIEW){
            rView = LayoutInflater.from(parent.getContext()).inflate(R.layout.doctor_chat_view, parent, false);
            vh = new MyViewHolder(rView,onItemClickListener);
        } else {
            rView = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_chat_view, parent, false);
            vh = new MyViewHolder(rView,onItemClickListener);
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
//        Toast.makeText(context, position+"", Toast.LENGTH_SHORT).show();
        final ChatItem item = chatItems.get(position);
        holder.message.setText(item.getMessage());
        holder.time.setText(item.getCdate()+" "+item.getCtime());
    }

    @Override
    public int getItemCount() {
        return chatItems.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{

        public TextView message, time;

        public MyViewHolder(View itemView,OnItemClickListener onItemClick) {
            super(itemView);
            message = itemView.findViewById(R.id.docChatMessage);
            time = itemView.findViewById(R.id.chatTime);
            onItemClickListener = onItemClick;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(onItemClickListener != null){
                onItemClickListener.onItemClickListener(v,getPosition());
            }
        }

    }

    public void setOnItemClickListener(OnItemClickListener onItemClick){
        this.onItemClickListener = onItemClick;
    }

    @Override
    public int getItemViewType (int position) {
        ChatItem chatItem = (ChatItem) chatItems.get(position);
        if (chatItem.getASender().equals(docID)) {
            return DOCTOR_VIEW;
        } else {
            return PATIENT_VIEW;
        }
    }

}
