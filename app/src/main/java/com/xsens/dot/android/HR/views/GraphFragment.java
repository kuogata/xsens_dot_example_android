package com.xsens.dot.android.HR.views;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.xsens.dot.android.HR.R;
import com.xsens.dot.android.HR.databinding.FragmentGraphBinding;
import com.xsens.dot.android.HR.viewmodels.GraphViewModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static com.xsens.dot.android.HR.views.MainActivity.FRAGMENT_TAG_GRAPH;

public class GraphFragment extends Fragment {

    private static final String TAG = GraphFragment.class.getSimpleName();

    // The view binder of GraphFragment
    private FragmentGraphBinding mBinding;

    // RadarChart library
    private RadarChart mRadarChart;

    /**
     * Get the instance of GraphFragment
     *
     * @return The instance of GraphFragment
     */
    public static GraphFragment newInstance() {

        return new GraphFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mBinding = FragmentGraphBinding.inflate(LayoutInflater.from(getContext()));
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // The devices view model instance
        GraphViewModel mGraphViewModel = new ViewModelProvider(this).get(GraphViewModel.class);
    }

    @Override
    public void onResume() {

        super.onResume();

        // Notify main activity to refresh menu.
        MainActivity.sCurrentFragment = FRAGMENT_TAG_GRAPH;
        if (getActivity() != null) getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //setTitle("RadarChart random_data");

        // ???????????????????????? ????????????????????????
        mRadarChart = view.findViewById(R.id.RadarChart);
        mRadarChart.setBackgroundColor(Color.rgb(245, 245, 245));
        //mRadarChart.setBackgroundColor(Color.WHITE);       // ??????????????????????????????
        mRadarChart.getDescription().setEnabled(false);      // ???????????????????????????
        mRadarChart.setWebLineWidth(1f);                     // ?????????????????????????????????
        mRadarChart.setWebColor(Color.LTGRAY);               // ??????????????????????????????
        mRadarChart.setWebLineWidthInner(1f);                // ??????????????????????????????
        mRadarChart.setWebColorInner(Color.LTGRAY);          // ????????????????????????????????????
        mRadarChart.setWebAlpha(100);                        // ?????????????????????????????? ???????????????=150, 0=100%??????
        mRadarChart.setTouchEnabled(false);

        // ?????????????????????????????????????????????
        setData();

        // ??????????????????????????????
        mRadarChart.animateXY(1400, 1400, Easing.EaseInOutQuad);

        // X?????????
        XAxis xAxis = mRadarChart.getXAxis();
        xAxis.setTextSize(10f);     // X?????????????????????????????????
        xAxis.setYOffset(0f);       // ?
        xAxis.setXOffset(0f);       // ?
        xAxis.setValueFormatter(new ValueFormatter() {
            private final String[] paramLabel = new String[]{"HR", "????????????", "?????????"}; // ?????????????????????

            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return paramLabel[(int) value % paramLabel.length];
            }
        });
        xAxis.setTextColor(Color.BLACK);    // ?????????????????????

        // Y?????????
        YAxis yAxis = mRadarChart.getYAxis();
        yAxis.setLabelCount(5, false);      // ???????????????(???????????????)
        yAxis.setTextSize(9f);                          // Y?????????????????????????????????
        yAxis.setAxisMinimum(0f);                       // Y???????????????
        yAxis.setAxisMaximum(80f);                      // Y???????????????
        yAxis.setDrawLabels(true);                      // Y?????????????????????

        // ????????????
        Legend l = mRadarChart.getLegend();                                     // ??????
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);             // ?????????????????????
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);      // ?????????????????????
        l.setOrientation(Legend.LegendOrientation.VERTICAL);                    // ??????????????????????????????????????????
        l.setDrawInside(false);                                                 // ????????????????????????
        l.setXEntrySpace(7f);                                                   // ????????????
        l.setYEntrySpace(5f);                                                   // ???????????????
        l.setTextColor(Color.BLUE);                                             // ??????????????????
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void setData() {

        long cnt = 0;
        String entry1 = "??????";
        String entry2 = "??????";

        ArrayList<RadarEntry> entries1 = new ArrayList<>();     // ??????
        ArrayList<RadarEntry> entries2 = new ArrayList<>();     // ??????

        File dir = requireContext().getExternalFilesDir(null);
        assert dir != null;
        String grfilename = dir.getAbsolutePath() + File.separator + "graphdata.csv";
        Path path = Paths.get(grfilename);
        File file = new File(grfilename);

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            long lineCount = Files.lines(path).count();

            String line;
            while ((line = br.readLine()) != null) {
                cnt ++;
                String[] values = line.split(",");
                String datetime = values[0];                               // 0 : datetime
                float steps = Float.parseFloat(values[1]);                 // 1 : ??????
                float hr = Float.parseFloat(values[2]);                    // 2 : HR
                float walkVelocity = Float.parseFloat(values[3]);          // 3 : ????????????
                float sd = Float.parseFloat(values[4]);                    // 4 : ?????????(????????????)
                float hrScr = Math.round(Float.parseFloat(values[5]));     // 5 : HR(100???)
                float walkScr = Math.round(Float.parseFloat(values[6]));   // 6 : ????????????(100???)
                float sdScr = Math.round(Float.parseFloat(values[7]));     // 7 : ?????????(100???)

                if (cnt == lineCount){
                    entry1 = datetime;
                    entries1.add(new RadarEntry(hrScr));
                    entries1.add(new RadarEntry(walkScr));
                    entries1.add(new RadarEntry(sdScr));

                    mBinding.curVal.setText(datetime);
                    mBinding.hrValue1.setText(String.format("%.2f", hr));
                    mBinding.repValue1.setText(String.format("%.2f", sd));
                    mBinding.speedValue1.setText(String.format("%.2f", walkVelocity));
                }

                if (lineCount == 1) {
                    mBinding.preVal.setText("-");
                    mBinding.hrValue2.setText("-");
                    mBinding.repValue2.setText("-");
                    mBinding.speedValue2.setText("-");
                }
                else {
                    if (cnt == lineCount - 1 ){
                        entry2 = datetime;
                        entries2.add(new RadarEntry(hrScr));
                        entries2.add(new RadarEntry(walkScr));
                        entries2.add(new RadarEntry(sdScr));

                        mBinding.preVal.setText(datetime);
                        mBinding.hrValue2.setText(String.format("%.2f", hr));
                        mBinding.repValue2.setText(String.format("%.2f", sd));
                        mBinding.speedValue2.setText(String.format("%.2f", walkVelocity));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        RadarDataSet set1 = new RadarDataSet(entries1, entry1);  // ????????????(DataEntry1)
        set1.setColor(Color.CYAN);                     // ????????? (cyan)
        set1.setFillColor(Color.CYAN);                 // ???????????????????????????????????? (cyan)
        set1.setDrawFilled(true);                      // ???????????????????????????
        set1.setFillAlpha(180);                        // ????????????????????????
        set1.setLineWidth(2f);                         // ???????????? 1f???
        set1.setDrawHighlightCircleEnabled(true);      // ?
        set1.setDrawHighlightIndicators(false);        // ?

        RadarDataSet set2 = new RadarDataSet(entries2, entry2);
        set2.setColor(Color.parseColor("#dda0dd"));            // ????????? (plum)
        set2.setFillColor(Color.parseColor("#dda0dd"));        // ???????????????????????????????????? (plum)
        set2.setDrawFilled(true);                                       // ???????????????????????????
        set2.setFillAlpha(100);                                         // ????????????????????????
        set2.setLineWidth(2f);                                          // ???????????? 1f???
        set2.setDrawHighlightCircleEnabled(true);                       // ?
        set2.setDrawHighlightIndicators(false);                         // ?

        ArrayList<IRadarDataSet> sets = new ArrayList<>();
        sets.add(set1);
        sets.add(set2);

        RadarData data = new RadarData(sets);
        data.setValueTextSize(8f);              // ?????????????????????
        data.setDrawValues(false);               // ????????????
        data.setValueTextColor(Color.DKGRAY);   // ???????????????

        mRadarChart.setData(data);   // sets data & also calls notifyDataSetChanged()
        mRadarChart.invalidate();    // refreshes chart
    }

}