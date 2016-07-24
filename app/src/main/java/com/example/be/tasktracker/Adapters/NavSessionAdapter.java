package com.example.be.tasktracker.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.be.tasktracker.DataModel.Session;
import com.example.be.tasktracker.R;
import com.example.be.tasktracker.StopwatchFragment;

import java.util.ArrayList;

/**
 * Created by BE on 7/19/2016.
 */
public class NavSessionAdapter extends BaseAdapter {
    ArrayList<Session> sessions;
    Context mContext;
    LayoutInflater inflater;


    boolean checkedItems[];

    public NavSessionAdapter(Context context, ArrayList<Session> sessions,boolean[]checkedItems) {
        this.mContext = context;
        this.sessions = sessions;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.checkedItems = checkedItems;
    }
    public void setSessions(ArrayList<Session> sessions){
        this.sessions = sessions;
        checkedItems=new boolean[sessions.size()];

    }

    public ArrayList<Session> getSessions() {
        return sessions;
    }

    public void setAll(){
     for(int i=0;i<checkedItems.length;i++)
         checkedItems[i]=true;
    }
    public void setNone(){

        for(int i=0;i<checkedItems.length;i++)
            checkedItems[i]=false;

    }
    @Override
    public int getCount() {
        return sessions.size();
    }

    @Override
    public Object getItem(int position) {
        return sessions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_with_checkbox, parent, false);
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.nav_session_checkBox);
            viewHolder.titleTV = (TextView) convertView.findViewById(R.id.nav_session_title);
            viewHolder.dateTV = (TextView) convertView.findViewById(R.id.nav_session_date);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.titleTV.setText(
                sessions.get(position).getTitle()!=null&& sessions.get(position).getTitle().length() > 0 ?
                        sessions.get(position).getTitle() : "UnTitled Session");
        viewHolder.dateTV.setText(StopwatchFragment.getDateString(sessions.get(position).getDateInMs()));
        if (checkedItems[position]) {
            convertView.setSelected(true);
            viewHolder.checkBox.setChecked(true);
        }else {
            convertView.setSelected(false);
            viewHolder.checkBox.setChecked(false);
        }
        viewHolder.checkBox.setOnClickListener(new Listener(convertView,position));
        convertView.setOnClickListener(new Listener(convertView,position));
        return convertView;
    }

    public boolean[] getCheckedItems() {
        return checkedItems;
    }

    public void setCheckedItems(boolean[] checkedItems) {
        this.checkedItems = checkedItems;
    }
    private static class ViewHolder {
        CheckBox checkBox;
        TextView titleTV;
        TextView dateTV;

    }

    class Listener implements View.OnClickListener {
        View itemView;
        int position;

        Listener(View view, int position) {
            itemView = view;
            this.position = position;
        }

        public void onClick(View v) {
            if (checkedItems[position]) {
                itemView.setSelected(false);
                checkedItems[position] = false;
            } else {
                itemView.setSelected(true);
                checkedItems[position] = true;
            }

            if (!(v instanceof CheckBox))
                ((ViewHolder) v.getTag()).checkBox.setChecked(checkedItems[position]);

        }


    }

}
