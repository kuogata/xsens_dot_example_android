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

    // The view binder of DataFragment
    private FragmentGraphBinding mBinding;

    // RadarChart library
    private RadarChart mRadarChart;

    /**
     * Get the instance of DataFragment
     *
     * @return The instance of DataFragment
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
        //return inflater.inflate(R.layout.fragment_graph, container, false);
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

        // レーダーチャート 描画エリアの設定
        mRadarChart = view.findViewById(R.id.RadarChart);
        mRadarChart.setBackgroundColor(Color.rgb(245, 245, 245));
        //RadarChart.setBackgroundColor(Color.WHITE);       // グラフエリアの背景色
        mRadarChart.getDescription().setEnabled(false);      // 説明テキストの表示
        mRadarChart.setWebLineWidth(1f);                     // ウェブラインの軸の太さ
        mRadarChart.setWebColor(Color.LTGRAY);               // ウェブラインの軸の色
        mRadarChart.setWebLineWidthInner(1f);                // ウェブラインの幅の線
        mRadarChart.setWebColorInner(Color.LTGRAY);          // ウェブラインの幅の線の色
        mRadarChart.setWebAlpha(100);                        // ウェブラインの透明度 デフォルト=150, 0=100%透明
        mRadarChart.setTouchEnabled(false);

        // 表示データの取得とスタイル設定
        setData();

        // 表示のアニメーション
        mRadarChart.animateXY(1400, 1400, Easing.EaseInOutQuad);

        // X軸設定
        XAxis xAxis = mRadarChart.getXAxis();
        xAxis.setTextSize(10f);     // Xラベルのテキストサイズ
        xAxis.setYOffset(0f);       // ?
        xAxis.setXOffset(0f);       // ?
        xAxis.setValueFormatter(new ValueFormatter() {
            private final String[] paramLabel = new String[]{"HR", "歩行速度", "再現性"}; // 各軸のラベル名

            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return paramLabel[(int) value % paramLabel.length];
            }
        });
        xAxis.setTextColor(Color.BLACK);    // ラベルの文字色

        // Y軸設定
        YAxis yAxis = mRadarChart.getYAxis();
        yAxis.setLabelCount(5, false);      // ラベルの数(幅の線の数)
        yAxis.setTextSize(9f);                          // Yラベルのテキストサイズ
        yAxis.setAxisMinimum(0f);                       // Y軸の最小値
        yAxis.setAxisMaximum(80f);                      // Y軸の最大値
        yAxis.setDrawLabels(true);                      // Y軸のラベル表示

        // 凡例設定
        Legend l = mRadarChart.getLegend();                                     // 凡例
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);             // 表示位置（縦）
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);      // 表示位置（横）
        l.setOrientation(Legend.LegendOrientation.VERTICAL);                    // 複数個表示する場合の表示位置
        l.setDrawInside(false);                                                 // 内側に表示するか
        l.setXEntrySpace(7f);                                                   // 横の間隔
        l.setYEntrySpace(5f);                                                   // 縦の間隔？
        l.setTextColor(Color.BLUE);                                             // テキストの色
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void setData() {

        long cnt = 0;
        String entry1 = "今回";
        String entry2 = "前回";

        ArrayList<RadarEntry> entries1 = new ArrayList<>();     // 今回
        ArrayList<RadarEntry> entries2 = new ArrayList<>();     // 前回

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
                float steps = Float.parseFloat(values[1]);                 // 1 : 歩数
                float hr = Float.parseFloat(values[2]);                    // 2 : HR
                float walkVelocity = Float.parseFloat(values[3]);          // 3 : 歩行速度
                float sd = Float.parseFloat(values[4]);                    // 4 : 再現性(標準偏差)
                float hrScr = Math.round(Float.parseFloat(values[5]));     // 5 : HR(100点)
                float walkScr = Math.round(Float.parseFloat(values[6]));   // 6 : 歩行速度(100点)
                float sdScr = Math.round(Float.parseFloat(values[7]));     // 7 : 再現性(100点)

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

        RadarDataSet set1 = new RadarDataSet(entries1, entry1);  // データ１(DataEntry1)
        set1.setColor(Color.CYAN);
        set1.setFillColor(Color.CYAN);
        //set1.setColor(Color.parseColor("#fffacd"));            // 線の色 (lemonchiffon)
        //set1.setFillColor(Color.parseColor("#fffacd"));        // 塗り潰したフィールドの色 (lemonchiffon)
        set1.setDrawFilled(true);                                // 線の下を塗り潰すか
        set1.setFillAlpha(180);                                  // 塗り潰しの透明度
        set1.setLineWidth(2f);                                   // 線の太さ 1f〜
        set1.setDrawHighlightCircleEnabled(true);                // ?
        set1.setDrawHighlightIndicators(false);                  // ?

        RadarDataSet set2 = new RadarDataSet(entries2, entry2);
        //set2.setColor(Color.rgb(121, 162, 175));
        //set2.setFillColor(Color.rgb(121, 162, 175));
        set2.setColor(Color.parseColor("#dda0dd"));            // 線の色 (plum)
        set2.setFillColor(Color.parseColor("#dda0dd"));        // 塗り潰したフィールドの色 (plum)
        set2.setDrawFilled(true);                                       // 線の下を塗り潰すか
        set2.setFillAlpha(100);                                         // 塗り潰しの透明度
        set2.setLineWidth(2f);                                          // 線の太さ 1f〜
        set2.setDrawHighlightCircleEnabled(true);                       // ?
        set2.setDrawHighlightIndicators(false);                         // ?

        ArrayList<IRadarDataSet> sets = new ArrayList<>();
        sets.add(set1);
        sets.add(set2);

        RadarData data = new RadarData(sets);
        data.setValueTextSize(8f);              // 値の文字サイズ
        data.setDrawValues(false);               // 値の表示
        data.setValueTextColor(Color.DKGRAY);   // 値の表示色

        mRadarChart.setData(data);   // sets data & also calls notifyDataSetChanged()
        mRadarChart.invalidate();    // refreshes chart
    }

}