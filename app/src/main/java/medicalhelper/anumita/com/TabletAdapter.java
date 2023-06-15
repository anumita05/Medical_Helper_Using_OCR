package medicalhelper.anumita.com;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import medicalhelper.anumita.com.R;
import medicalhelper.anumita.com.helper.UtilConstants;

public class TabletAdapter extends ArrayAdapter<String> {

    private Context appContext;
    private ArrayList<String> Name, Type, Image;

    public TabletAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> name,
                         @NonNull ArrayList<String> type,@NonNull ArrayList<String> img) {
        super(context, resource, name);
        appContext = context;
        this.Name = name;
        this.Type = type;
        this.Image = img;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(appContext).inflate(R.layout.tablet_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tab_image = convertView.findViewById(R.id.tablet_item_image);
            viewHolder.tab_name = convertView.findViewById(R.id.tablet_item_name);
            viewHolder.tab_type = convertView.findViewById(R.id.tablet_item_type);
            convertView.setTag(viewHolder);
        }else {
            viewHolder =(ViewHolder) convertView.getTag();
        }

        viewHolder.tab_image.setImageBitmap(UtilConstants.getBitmap(Image.get(position)));
        viewHolder.tab_name.setText("Name : "+Name.get(position));
        viewHolder.tab_type.setText("Type : "+Type.get(position));
        return convertView;
    }

    private class ViewHolder{
        ImageView tab_image;
        TextView tab_name, tab_type;
    }
}
