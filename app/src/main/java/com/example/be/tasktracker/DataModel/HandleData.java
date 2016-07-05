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
    public final static String JSONARRAYKEY = "values";

    public static boolean saveNewProject(Project project, File file, boolean saved) {

        boolean successed = false;
        JSONArray jsonArray = null;
        Gson gson = new Gson();
        JSONObject jsonObject = null;

        try {
            if (file.exists() && getJsonStr(file) != null) {
                jsonObject = new JSONObject(getJsonStr(file));
                jsonArray = jsonObject.getJSONArray(JSONARRAYKEY);
            } else {
                file.createNewFile();
                jsonArray = new JSONArray();
            }
            if (!saved) {
                jsonArray.put(jsonArray.length(), gson.toJson(project, Project.class));
            } else {
                jsonArray.put(jsonArray.length() - 1, gson.toJson(project, Project.class));
            }
            successed = writeJsonToFile(new Gson().toJson(jsonArray, JSONArray.class), file);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return successed;
    }

    public static boolean writeJsonToFile(String str, File file) throws IOException {
        PrintWriter outputStream = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        outputStream.write(str);
        if (outputStream != null)
            outputStream.close();
        return true;
    }

    public static String getJsonStr(File file) {
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
        if (jsonStr == null || jsonStr.toString().length() == 0)
            return null;
        else
            return jsonStr.toString();
    }

    public static ArrayList<String> readProjectsNames(File file) {

        String jsonStr = HandleData.getJsonStr(file);
        ArrayList<String> projectsNames = new ArrayList<>();
        if (jsonStr != null) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject = new JSONObject(jsonStr);
                JSONArray tempJsArray = jsonObject.getJSONArray("values");
                Gson gson = new Gson();
                for (int i = 0; i < tempJsArray.length(); i++) {
                    System.out.println(tempJsArray.getString(i));
                    Project project = gson.fromJson(tempJsArray.getString(i), Project.class);
                    projectsNames.add(project.getProjectName());
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return projectsNames;
    }

    public static ArrayList<Project> readProjects(Context context, File file) {

        ArrayList<Project> projects = new ArrayList<>();
        JSONObject jsonObject = null;
        String str = getJsonStr(file);
        try {
            if (str != null)
                jsonObject = new JSONObject(str);
        } catch (JSONException je) {
            je.printStackTrace();
        }
        if (file.exists() && jsonObject != null) {
            try {

                JSONArray tempJsArray = jsonObject.getJSONArray(context.getString(R.string.JSON_ARRAY_KEY));
                Gson gson = new Gson();
                for (int i = 0; i < tempJsArray.length(); i++) {
                    projects.add(gson.fromJson(tempJsArray.getString(i), Project.class));
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return projects;
    }

    public static boolean saveSession(Task task, File file) {
        JSONArray jsonArray = null;
        Gson gson = new Gson();
        try {
            if (!file.exists()||getJsonStr(file)==null) {
                file.createNewFile();
                jsonArray = new JSONArray();

            } else
                jsonArray = (new JSONObject(getJsonStr(file))).getJSONArray("values");
            jsonArray.put(jsonArray.length(), gson.toJson(task, Task.class));
            System.out.println(gson.toJson(jsonArray, JSONArray.class));
            writeJsonToFile(gson.toJson(jsonArray, JSONArray.class), file);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return true;
    }
}
