package com.example.be.tasktracker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener {
    Context mcontext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mcontext = this;



    }
//Listener methods implementation
    @Override
    public void onHomeItemSelected(int itemNum) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (itemNum) {
            case CREATE_PATTERN:
                Intent intent=new Intent();
                intent.setClass(mcontext,CreatePatternActivity.class);
                startActivity(intent);
                //fragmentTransaction.replace(R.id.container, new CreatePattern(), "CreatePatternFragment");
                //fragmentTransaction.addToBackStack(null);
                //fragmentTransaction.commit();
        }

    }

}
