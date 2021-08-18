package com.xsens.dot.android.HR.views;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.RadarChart;
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
import com.xsens.dot.android.HR.databinding.FragmentDataBinding;
import com.xsens.dot.android.HR.viewmodels.GraphViewModel;

import java.util.ArrayList;

import static com.xsens.dot.android.HR.views.MainActivity.FRAGMENT_TAG_GRAPH;

public class GraphFragment extends Fragment {

    private static final String TAG = DataFragment.class.getSimpleName();

    // The view binder of DataFragment
    private FragmentDataBinding mBinding;

    // The devices view model instance
    private GraphViewModel mGraphViewModel;

    // RadarChart library
    private com.github.mikephil.charting.charts.RadarChart RadarChart;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_graph, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGraphViewModel = new ViewModelProvider(this).get(GraphViewModel.class);
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
        RadarChart = view.findViewById(R.id.RadarChart);
        RadarChart.setBackgroundColor(Color.rgb(245, 245, 245));
        //RadarChart.setBackgroundColor(Color.WHITE); // グラフエリアの背景色
        RadarChart.getDescription().setEnabled(false);  // 説明テキストの表示
        RadarChart.setWebLineWidth(1f);             // ウェブラインの軸の太さ
        RadarChart.setWebColor(Color.LTGRAY);       // ウェブラインの軸の色
        RadarChart.setWebLineWidthInner(1f);        // ウェブラインの幅の線
        RadarChart.setWebColorInner(Color.LTGRAY);  // ウェブラインの幅の線の色
        RadarChart.setWebAlpha(100);                // ウェブラインの透明度 デフォルト=150, 0=100%透明

        // 表示データの取得とスタイル設定
        setData();

        // 表示のアニメーション
        RadarChart.animateXY(1400, 1400, Easing.EaseInOutQuad);

        // X軸設定
        XAxis xAxis = RadarChart.getXAxis();
        xAxis.setTextSize(12f);  // Xラベルのテキストサイズ
        xAxis.setYOffset(0f);   // ?
        xAxis.setXOffset(0f);   // ?
        xAxis.setValueFormatter(new ValueFormatter() {
            private final String[] paramLabel = new String[]{"Label1", "Label2", "Label3", "Label4", "Label5"}; // 各軸のラベル名

            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return paramLabel[(int) value % paramLabel.length];
            }
        });
        xAxis.setTextColor(Color.BLACK);    // ラベルの文字色

        // Y軸設定
        YAxis yAxis = RadarChart.getYAxis();
        yAxis.setLabelCount(5, false);  // ラベルの数(幅の線の数)
        yAxis.setTextSize(9f);                      // Yラベルのテキストサイズ
        yAxis.setAxisMinimum(0f);                   // Y軸の最小値
        yAxis.setAxisMaximum(80f);                  // Y軸の最大値
        yAxis.setDrawLabels(false);                 // Y軸のラベル表示

        // 凡例設定
        Legend l = RadarChart.getLegend();                                  // 凡例
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);      // 表示位置（縦）
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);  // 表示位置（横）
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);              // 複数個表示する場合の表示位置
        l.setDrawInside(false);                                             // 内側に表示するか
        l.setXEntrySpace(15f);                                              // 横の間隔
        l.setYEntrySpace(5f);                                               // 縦の間隔？
        l.setTextColor(Color.BLUE);                                         // テキストの色
    }

    private void setData() {

        float mul = 80;
        float min = 20;
        int cnt = 5;

        ArrayList<RadarEntry> entries1 = new ArrayList<>();
        ArrayList<RadarEntry> entries2 = new ArrayList<>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (int i = 0; i < cnt; i++) {
            float val1 = (float) (Math.random() * mul) + min;
            entries1.add(new RadarEntry(val1));

            float val2 = (float) (Math.random() * mul) + min;
            entries2.add(new RadarEntry(val2));
        }

        RadarDataSet set1 = new RadarDataSet(entries1, "DataEntry1");  // データ１(DataEntry1)
        //set1.setColor(Color.CYAN);
        //set1.setFillColor(Color.CYAN);
        set1.setColor(Color.parseColor("#fffacd"));            // 線の色 (lemonchiffon)
        set1.setFillColor(Color.parseColor("#fffacd"));        // 塗り潰したフィールドの色 (lemonchiffon)
        set1.setDrawFilled(true);                                       // 線の下を塗り潰すか
        set1.setFillAlpha(180);                                         // 塗り潰しの透明度
        set1.setLineWidth(2f);                                          // 線の太さ 1f〜
        set1.setDrawHighlightCircleEnabled(true);                       // ?
        set1.setDrawHighlightIndicators(false);                         // ?

        RadarDataSet set2 = new RadarDataSet(entries2, "DataEntry2");
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
        data.setDrawValues(true);               // 値の表示
        data.setValueTextColor(Color.DKGRAY);   // 値の表示色

        RadarChart.setData(data);   // sets data & also calls notifyDataSetChanged()
        RadarChart.invalidate();    // refreshes chart
    }

}