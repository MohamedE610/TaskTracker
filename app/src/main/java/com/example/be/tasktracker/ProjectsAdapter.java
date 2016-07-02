package com.example.be.tasktracker;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.be.tasktracker.DataModel.Project;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by BE on 7/1/2016.
 */
public class ProjectsAdapter extends BaseAdapter{
    private final LayoutInflater inflater;
    Context mcontext;
    static ImageView imageView;
    TextView holderTextView;
    ArrayList<Project>list;

    public ProjectsAdapter(Context context, ArrayList<Project>list) {
        super();
        mcontext=context;
        this.list=list;
        inflater = (LayoutInflater)context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView==null){
            convertView= inflater.inflate(R.layout.start_list_item,parent,false);
            holderTextView= (TextView) convertView.findViewById(R.id.project_item);
            holderTextView.setText(list.get(position).getProjectName());
            convertView.setTag(holderTextView);

        }
        else{
            holderTextView= (TextView) convertView.getTag();
        }
        if(imageView==null){
            imageView= (ImageView) convertView.findViewById(R.id.go_image);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageView.setImageDrawable(mcontext.getDrawable(R.drawable.arrow));
            }
            else{
                imageView.setImageDrawable(mcontext.getResources().getDrawable(R.drawable.arrow));
            }
        }
        return convertView;
    }
}
