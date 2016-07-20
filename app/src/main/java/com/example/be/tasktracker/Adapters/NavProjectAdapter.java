package com.example.be.tasktracker.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.be.tasktracker.R;
import com.example.be.tasktracker.StatisticsActivity;

import java.util.ArrayList;

/**
 * Created by BE on 7/19/2016.
 */
public class NavProjectAdapter extends BaseAdapter {

    private final Context mContext;
    private final ArrayList<String> mArrayList;
    private final LayoutInflater inflater;
    RadioButton selectedButton;
    StatisticsActivity.SelectedProject selected;

    public NavProjectAdapter(Context context, ArrayList<String> arrayList, StatisticsActivity.SelectedProject selectedProject) {
        mContext = context;
        mArrayList = arrayList;
        selectedButton = new RadioButton(mContext);
        selected=selectedProject;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HolderView view;
        if (convertView == null) {
            view = new HolderView();
            convertView = inflater.inflate(R.layout.item_with_radiobtn, parent, false);
            view.radioButton = (RadioButton) convertView.findViewById(R.id.projectRdBtn);
            view.textView = (TextView) convertView.findViewById(R.id.project_item_nv);


            convertView.setTag(view);
        } else
            view = (HolderView) convertView.getTag();

       view.radioButton.setOnClickListener(new Listener (convertView, position));
       convertView.setOnClickListener(new Listener (convertView,position));
        System.out.println(position);
        if(selected.getPosition()==position){
            view.radioButton.setChecked(true);
            selectedButton=view.radioButton;
        }
        else{
            view.radioButton.setChecked(false);
        }
        view.textView.setText(mArrayList.get(position));

        return convertView;
    }

    private static class HolderView {
        TextView textView;
        RadioButton radioButton;
    }


    class Listener implements View.OnClickListener {
        View itemView;
        int position;

        Listener (View view, int position) {
            itemView = view;
            this.position=position;
        }

        public void onClick(View v) {
            if (position == selected.getPosition())
                return;

            if (v instanceof RadioButton) {
                selected.setPosition(position);
                selectedButton.setChecked(false);
                selectedButton= ((HolderView) itemView.getTag()).radioButton;
                itemView.setSelected(true);
            } else {
                v.setSelected(true);
                selected.setPosition(position);
                selectedButton.setChecked(false);
                selectedButton= ((HolderView) itemView.getTag()).radioButton;
                selectedButton.setChecked(true);
            }

        }

    }

}