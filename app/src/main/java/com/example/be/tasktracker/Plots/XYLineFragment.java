package com.example.be.tasktracker.Plots;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.PointLabeler;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.be.tasktracker.DataModel.Project;
import com.example.be.tasktracker.DataModel.Session;

import com.example.be.tasktracker.R;
import com.example.be.tasktracker.StatisticsActivity;
import com.example.be.tasktracker.StopwatchFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;


public class XYLineFragment extends Fragment {
    private static final String JSONSESSION = "JSESSIONS";
    private static final String COMPARE = "COMPARE";
    private static final String JSONPROJECT ="JSONPROJECT" ;
    ArrayList<Session> sessions;
    ArrayList<XYSeries> xySeries;
    ArrayList<LineAndPointFormatter> lineAndPointFormatters;
    ArrayList<String> tasksNames;
    float strokeWidth;
    String jsonSessions;
    Project project;
    boolean compare = true;

    private XYPlot plot;
    final int[] COLORS = {
            Color.rgb(255, 0, 0), Color.rgb(0, 255, 0), Color.rgb(0, 0, 255), Color.rgb(255, 0, 255),
            Color.rgb(255, 255, 0), Color.rgb(70, 50, 155), Color.rgb(255, 102, 0), Color.rgb(255, 204, 102),
            Color.rgb(204, 255, 153), Color.rgb(204, 153, 0), Color.rgb(153, 102, 255), Color.rgb(102, 255, 255),
            Color.rgb(0, 0, 153), Color.rgb(0, 153, 0), Color.rgb(153, 0, 0), Color.rgb(153, 0, 153),
            Color.rgb(0, 153, 153), Color.rgb(153, 153, 0), Color.rgb(50, 50, 0), Color.rgb(50, 0, 50),
    };
    private boolean changed;

    public XYLineFragment() {
        super();
        // Required empty public constructor
    }

    public void setSessions(ArrayList<Session> sessions) {

        if (this.sessions == null)
            changed = true;
        else if (this.sessions.size() != sessions.size())
            changed = true;
        else {
            for (int i = 0; i < sessions.size(); i++) {
                if (!sessions.get(i).equals(this.sessions.get(i))) {
                    changed = true;
                    break;
                }
            }
        }
        if (changed) {
            this.sessions = sessions;
            plot.clear();
            buildGraph();
            plot.redraw();
        }
    }

    public ArrayList<Session> getSessions() {
        return sessions;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String jsonProject;
        Type type = new TypeToken<ArrayList<Session>>() {
        }.getType();
        if (savedInstanceState != null) {
            jsonSessions = savedInstanceState.getString(JSONSESSION);
            compare = savedInstanceState.getBoolean(COMPARE);
            jsonProject=savedInstanceState.getString(JSONPROJECT);
        } else{
            jsonSessions =  getArguments().getString(StatisticsActivity.SESSIONS_ARGS);
            jsonProject=getArguments().getString(StatisticsActivity.PROJECT_ARG);
            System.out.println("Fuck JSONsESSION "+jsonSessions);
        }

        project=new Gson().fromJson(jsonProject,Project.class);
        sessions = new Gson().fromJson(jsonSessions, type);
        // setRetainInstance(true);
        // System.out.println();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(JSONSESSION, new Gson().toJson(sessions));
        outState.putBoolean(COMPARE, compare);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_line, container, false);
        plot = (XYPlot) view.findViewById(R.id.plot);
        buildGraph();
        return view;
    }

    @Override
    public void onResume() {
        if (changed) {
            plot.clear();
            buildGraph();
            plot.redraw();
        }
        super.onResume();
    }

    void buildGraph() {
        if(sessions.size()==0)
            return;
        tasksNames = project.getTasks();
        xySeries = new ArrayList<>();
        lineAndPointFormatters = new ArrayList<>();
        strokeWidth = sessions.size() > 5 ? PixelUtils.dpToPix(2) : PixelUtils.dpToPix(5);
        if (compare) {
            for (int i = 0; i < sessions.size(); i++) {
                LineAndPointFormatter lineAndPointFormatter = new LineAndPointFormatter();
                configureLineAndPointFormateer(lineAndPointFormatter, COLORS[i]);
                Long[] vals = new Long[tasksNames.size()];
                for (int j = 0; j < vals.length; j++)
                    vals[j] = sessions.get(i).getTaskTime(tasksNames.get(j));

                xySeries.add(new SimpleXYSeries(Arrays.asList(vals),
                        SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                        StopwatchFragment.getDateString(sessions.get(i).getDateInMs())));
                lineAndPointFormatters.add(lineAndPointFormatter);
                plot.addSeries(xySeries.get(i), lineAndPointFormatters.get(i));
            }
        } else {
            Float[] vals = new Float[tasksNames.size()];
            LineAndPointFormatter lineAndPointFormatter = new LineAndPointFormatter();
            configureLineAndPointFormateer(lineAndPointFormatter, COLORS[0]);
            for (int i = 0; i < tasksNames.size(); i++) {
                for (int j = 0; j < sessions.size(); j++) {
                    vals[i] += sessions.get(j).getTaskTime(tasksNames.get(i));
                }
                vals[i] /= sessions.size();
            }
            plot.addSeries(new SimpleXYSeries(Arrays.asList(vals), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Merged"),
                    lineAndPointFormatter);
        }

        plot.setTicksPerRangeLabel(1);
        plot.getGraphWidget().setDomainLabelOrientation(-45);
        changed = false;
    }

    private void configureLineAndPointFormateer(LineAndPointFormatter lineAndPointFormatter, int color) {
        lineAndPointFormatter.setPointLabelFormatter(new PointLabelFormatter());
        lineAndPointFormatter.getLinePaint().setColor(color);
        lineAndPointFormatter.getLinePaint().setStrokeWidth(strokeWidth);
        lineAndPointFormatter.setFillPaint(null);
        lineAndPointFormatter.getVertexPaint().setColor(Color.WHITE);
        lineAndPointFormatter.getVertexPaint().setStrokeWidth(strokeWidth);
        lineAndPointFormatter.setInterpolationParams(
                new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));
    }

    public XYPlot getPlot() {
        return plot;
    }

    public void setPlot(XYPlot plot) {
        this.plot = plot;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
