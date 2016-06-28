package com.example.be.tasktracker;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.SQLOutput;
import java.util.ArrayList;


public class CreateProjectFragment extends Fragment {
    // TODO: Rename parameter arguments, choose  names that match
    private Button addBtn,saveBtn;
    private EditText titleTxt,subtaskTxt;
    private  ListView listView;
    private ArrayAdapter<String>arrayAdapter;
    private ArrayList<String> listItems;
    private final int EDIT_ITEM_ID=2;
    private final int DELETE_ITEM_ID=1;
    private int selectedItem;


        @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView= inflater.inflate(R.layout.fragment_create_project, container, false);
        addBtn=(Button)rootView.findViewById(R.id.Addbtn);
        saveBtn=(Button)rootView.findViewById(R.id.saveBtn);
        titleTxt=(EditText)rootView.findViewById(R.id.projectTitle);
        subtaskTxt=(EditText)rootView.findViewById(R.id.subtaskTitle) ;
        listView=(ListView)rootView.findViewById(R.id.listView);
        listItems=new ArrayList<String>();
        arrayAdapter=new ArrayAdapter<String>(getActivity(),R.layout.list_item,listItems);
        listView.setAdapter(arrayAdapter);
        registerForContextMenu(listView);
        LayoutInflater inflator = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.edit_text_actionbar, null);
        handleActions();

        return rootView;


    }
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 1, 1, "Delete");
        menu.add(0, 2, 2, "Edit");

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        System.out.println("Fuck method in ");
        if (item.getItemId()==DELETE_ITEM_ID){
            arrayAdapter.remove(arrayAdapter.getItem(selectedItem));
        }
        else if(item.getItemId()==EDIT_ITEM_ID){
            System.out.println("Edit clicked");
        }
        return super.onContextItemSelected(item);
    }

    private void handleActions() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItem=position;
                return false;
            }
        });
        if(titleTxt.getText().toString().length()==0)
            saveBtn.setEnabled(false);


        titleTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                if(titleTxt.getText().toString().length()==0)
                    saveBtn.setEnabled(false);
                else
                    saveBtn.setEnabled(true);

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(titleTxt.getText().toString().length()==0)
                    saveBtn.setEnabled(false);
                else
                    saveBtn.setEnabled(true);

            }

            @Override
            public void afterTextChanged(Editable s) {

                if(titleTxt.getText().toString().length()==0)
                    saveBtn.setEnabled(false);
                else
                    saveBtn.setEnabled(true);
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(subtaskTxt.getText().toString().length()==0){
                    Toast.makeText(getActivity(),"Enter Subtask Title",Toast.LENGTH_SHORT).show();

                }
                else{
                    arrayAdapter.add(subtaskTxt.getText().toString());
                    subtaskTxt.setText("");
                }

            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }





    
}
