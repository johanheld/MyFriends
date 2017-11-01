package com.example.johan.myfriends;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.johan.myfriends.Modules.TextMessage;
import java.util.List;

/**
 * Created by johan on 2017-11-01.
 */

public class ListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List <TextMessage> list;


    public ListAdapter(Context context, List<TextMessage> list) {
        this.context = context;
        this.list = list;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public TextMessage getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextMessage message = getItem(position);
        TextView tvUser;
        TextView tvGroup;
        TextView tvMessage;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_text, parent, false);
        }

        tvUser = (TextView) convertView.findViewById(R.id.tvUser);
        tvGroup = (TextView) convertView.findViewById(R.id.tvGroup);
        tvMessage = (TextView) convertView.findViewById(R.id.tvMessage);

        tvUser.setText(message.getMember());
        tvGroup.setText(message.getGroup());
        tvMessage.setText(message.getMessage());

        return convertView;
    }
}
