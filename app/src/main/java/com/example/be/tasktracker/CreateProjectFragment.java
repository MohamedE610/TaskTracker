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

import java.util.ArrayList;


public class CreateProjectFragment extends Fragment {
    // TODO: Rename parameter arguments, choose  names that match
    private Button addBtn,saveBtn;
    private EditText titleTxt,subtaskTxt;
    private EditText editTextActionBar;
    private  ListView listView;
    private ArrayAdapter<String>arrayAdapter;
    private ArrayList<String> listItems;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {




        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);





            //menu.getItem(3);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            System.out.println("Fuck Item Clicked");
            editTextActionBar.setText("this is the new");
            switch (item.getItemId()){
                case R.id.action_done:



            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    };

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
        editTextActionBar=(EditText)v.findViewById(R.id.edit_text_actionbar);
        onButtonClicked();

        return rootView;


    }
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Context Menu");
        menu.add(0, v.getId(), 0, "Action 1");
        menu.add(0, v.getId(), 0, "Action 2");
        menu.add(0, v.getId(), 0, "Action 3");
    }
    private void onButtonClicked() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getActivity().startActionMode( mActionModeCallback);


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
