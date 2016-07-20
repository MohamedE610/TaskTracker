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
import com.example.be.tasktracker.DataModel.Session;

import com.example.be.tasktracker.R;
import com.example.be.tasktracker.StopwatchFragment;

import java.util.ArrayList;
import java.util.Arrays;


public class XYLineFragment extends Fragment {
    ArrayList<Session> sessions;
    ArrayList<XYSeries> xySeries;
    ArrayList<LineAndPointFormatter> lineAndPointFormatters;
    String[] tasksNames;
    float strokeWidth;
    boolean compare = true;
    private XYPlot plot;
    int[] colors = {
            Color.rgb(255, 0, 0), Color.rgb(0, 255, 0), Color.rgb(0, 0, 255), Color.rgb(255, 0, 255),
            Color.rgb(255, 255, 0), Color.rgb(0, 255, 255), Color.rgb(255, 102, 0), Color.rgb(255, 204, 102),
            Color.rgb(204, 255, 153), Color.rgb(204, 153, 0), Color.rgb(153, 102, 255), Color.rgb(102, 255, 255),
            Color.rgb(0, 0, 153), Color.rgb(0, 153, 0), Color.rgb(153, 0, 0), Color.rgb(153, 0, 153),
            Color.rgb(0, 153, 153), Color.rgb(153, 153, 0), Color.rgb(50, 50, 0), Color.rgb(50, 0, 50),
    };

    public XYLineFragment() {
        super();
        // Required empty public constructor
    }

    public void setSessions(ArrayList<Session> sessions) {
        this.sessions = sessions;
    }

    public ArrayList<Session> getSessions() {
        return sessions;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // xySeries=new ArrayList<>(sessions.size());

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_line, container, false);
        tasksNames = sessions.get(0).getTasksNames();
        xySeries=new ArrayList<>();
        lineAndPointFormatters=new ArrayList<>();
        strokeWidth = sessions.size() > 5 ? PixelUtils.dpToPix(2) : PixelUtils.dpToPix(5);
        plot = (XYPlot) view.findViewById(R.id.plot);
        if (compare) {
            for (int i = 0; i < sessions.size(); i++) {
                LineAndPointFormatter lineAndPointFormatter = new LineAndPointFormatter();
                configureLineAndPointFormateer(lineAndPointFormatter, colors[i]);
                Long[] vals = new Long[tasksNames.length];
                for (int j = 0; j < vals.length; j++)
                    vals[j] = sessions.get(i).getTaskTime(tasksNames[j]);

                xySeries.add(new SimpleXYSeries(Arrays.asList(vals),
                        SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                        StopwatchFragment.getDateString(sessions.get(i).getDateInMs())));
                lineAndPointFormatters.add(lineAndPointFormatter);
                plot.addSeries(xySeries.get(i), lineAndPointFormatters.get(i));
            }
        }
        else{
            Float[] vals = new Float[tasksNames.length];
            LineAndPointFormatter lineAndPointFormatter = new LineAndPointFormatter();
            configureLineAndPointFormateer(lineAndPointFormatter, colors[0]);
            for (int i = 0; i <tasksNames.length;i++){
                for(int j=0;j<sessions.size();j++){
                    vals[i]+=sessions.get(j).getTaskTime(tasksNames[i]);
                }
                vals[i]/=sessions.size();
            }
            plot.addSeries( new SimpleXYSeries(Arrays.asList(vals),SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,"Merged"),
                    lineAndPointFormatter);
        }

        plot.setTicksPerRangeLabel(1);
        plot.getGraphWidget().setDomainLabelOrientation(-45);
        return view;
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

}
