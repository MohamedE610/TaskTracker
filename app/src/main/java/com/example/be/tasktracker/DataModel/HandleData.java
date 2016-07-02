package com.example.be.tasktracker.DataModel;

import android.content.Context;

import com.example.be.tasktracker.R;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by BE on 7/1/2016.
 */
public class HandleData {
    public static boolean saveNewProject(Context context, Project project, File file, JSONObject jsonObject, boolean saved) {
        boolean successed = false;
        JSONArray jsonArray;
        PrintWriter outputStream = null;
        Gson gson=new Gson();
        try {
            if (!file.exists()) {
                file.createNewFile();
                jsonArray = new JSONArray();
            } else {
                jsonArray = jsonObject.getJSONArray(context.getString(R.string.JSON_ARRAY_KEY));
            }
            if (!saved) {
                jsonArray.put(jsonArray.length(), gson.toJson(project,Project.class));
            } else {
                jsonArray.put(jsonArray.length() - 1, gson.toJson(project,Project.class));
            }
            outputStream = new PrintWriter(new BufferedWriter(new FileWriter(file)));

            System.out.println("Fuck u all "+gson.toJson(jsonArray));
            outputStream.write(gson.toJson(jsonArray));
            successed = true;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null)
                outputStream.close();
        }
        return successed;
    }

    public static String getProjectsJsonStr(Context context, File file) {
        StringBuilder jsonStr = null;
        try {

            if (file.exists()) {
                jsonStr = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

                String str = bufferedReader.readLine();
                while (str != null) {
                    jsonStr.append(str);
                    str = bufferedReader.readLine();
                }
                bufferedReader.close();

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
//        System.out.println(jsonStr.toString());
        if (jsonStr == null)
            return null;
        else
            return jsonStr.toString();
    }

    public static ArrayList<String> readProjectsNames(Context context, File file, JSONObject jsonObject) {
        ArrayList<String> projectsNames = new ArrayList<>();
        try {
            JSONArray tempJsArray = jsonObject.getJSONArray(context.getString(R.string.JSON_ARRAY_KEY));
            Gson gson=new Gson();
            for (int i = 0; i < tempJsArray.length(); i++) {
                System.out.println(tempJsArray.getString(i));
                Project project= gson.fromJson(tempJsArray.getString(i),Project.class);
                projectsNames.add(project.getProjectName());
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        return projectsNames;
    }

    public static ArrayList<Project> readProjects(Context context, File file) {

        ArrayList<Project> projects = new ArrayList<>();
        JSONObject jsonObject =null;
        String str = getProjectsJsonStr(context, file);
        try {
            if (str != null)
                jsonObject = new JSONObject(str);
        } catch (JSONException je) {
            je.printStackTrace();
        }
        if (file.exists()&&jsonObject!=null) {
            try {

                JSONArray tempJsArray = jsonObject.getJSONArray(context.getString(R.string.JSON_ARRAY_KEY));
                Gson gson=new Gson();
                for (int i = 0; i < tempJsArray.length(); i++) {
                    projects.add(gson.fromJson(tempJsArray.getString(i),Project.class));
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return projects;
    }
}
