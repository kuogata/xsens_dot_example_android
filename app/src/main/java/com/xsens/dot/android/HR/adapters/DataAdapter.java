//  Copyright (c) 2003-2020 Xsens Technologies B.V. or subsidiaries worldwide.
//  All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without modification,
//  are permitted provided that the following conditions are met:
//
//  1.      Redistributions of source code must retain the above copyright notice,
//           this list of conditions, and the following disclaimer.
//
//  2.      Redistributions in binary form must reproduce the above copyright notice,
//           this list of conditions, and the following disclaimer in the documentation
//           and/or other materials provided with the distribution.
//
//  3.      Neither the names of the copyright holders nor the names of their contributors
//           may be used to endorse or promote products derived from this software without
//           specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
//  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
//  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
//  THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
//  SPECIAL, EXEMPLARY OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
//  OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
//  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY OR
//  TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
//  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.THE LAWS OF THE NETHERLANDS
//  SHALL BE EXCLUSIVELY APPLICABLE AND ANY DISPUTES SHALL BE FINALLY SETTLED UNDER THE RULES
//  OF ARBITRATION OF THE INTERNATIONAL CHAMBER OF COMMERCE IN THE HAGUE BY ONE OR MORE
//  ARBITRATORS APPOINTED IN ACCORDANCE WITH SAID RULES.
//

package com.xsens.dot.android.HR.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xsens.dot.android.HR.R;
import com.xsens.dot.android.sdk.events.XsensDotData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * A view adapter for item view to present data.
 */
public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataViewHolder> {

    private static final String TAG = DataAdapter.class.getSimpleName();

    // The keys of HashMap
    public static final String KEY_ADDRESS = "address", KEY_TAG = "tag", KEY_DATA = "data";

    // The application context
    private Context mContext;

    // Put all data from sensors into one list
    private ArrayList<HashMap<String, Object>> mDataList;

    //
    private static float[] accX_series = new float[5];
    private int stepNum = 0;
    private int chkTime = 1000;
    private float stepLength = 0f;
    private int dataSize = 0;
    private float debug_max[] = {0f};
    private float debug_min[] = {0f};
    private static float[] disX_series = new float[5];

    float intYacc = 0.0f;
    String nowFoot = null;
    String supportFoot = null;
    boolean walkEvent = false;
    int walkCycle_length = 0;

    ArrayList<threeAxis> accData = new ArrayList<threeAxis>();
    ArrayList<ArrayList<threeAxis>> accDataAll = new ArrayList<ArrayList<threeAxis>>();

    ArrayList<threeAxis> walkAccData = new ArrayList<threeAxis>();
    ArrayList<ArrayList<threeAxis>> walkAccDataAll = new ArrayList<ArrayList<threeAxis>>();

    //Vector<Vector<threeAxis>> accDataAll = new Vector<Vector<threeAxis>>();
    //Vector<threeAxis> accData = new Vector<threeAxis>();

    class threeAxis{
        float x;
        float y;
        float z;
    }

    /**
     * Default constructor.
     *
     * @param context  The application context
     * @param dataList A list contains tag and data
     */
    public DataAdapter(Context context, ArrayList<HashMap<String, Object>> dataList) {

        mContext = context;
        mDataList = dataList;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data, parent, false);
        return new DataViewHolder(itemView);
    }

    @Override
    @SuppressLint("DefaultLocale")
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {

        threeAxis accAxis = new threeAxis();
        String tag = (String) mDataList.get(position).get(KEY_TAG);
        XsensDotData xsData = (XsensDotData) mDataList.get(position).get(KEY_DATA);

        holder.sensorName.setText(tag);

        float[] quaternions = xsData.getQuat();
        double[] eulerAngles = xsData.getEuler();
        float[] freeAcc = xsData.getFreeAcc();
        float[][] rotM = calcQuaternionRotationMatrix(quaternions);
        float[] accV = calcSensCoordinateAcc(rotM, freeAcc);



        accAxis.x = accV[0];
        accAxis.y = accV[1];
        accAxis.z = accV[2];
        accData.add(accAxis);

        if(walkEvent){
            walkAccData.add(accAxis);
        }

        accX_series[0] = accX_series[1];
        accX_series[1] = accX_series[2];
        accX_series[2] = accX_series[3];
        accX_series[3] = accX_series[4];
        accX_series[4] = accV[2];

        if(chkTime > 15){ //about 0.25 sec
            /*if(detectMaxim(accX_series,5f,15f)) {
                stepNum++;
                chkTime = 0;
                dataSize = accData.size();
                accZs = accData.firstElement().z;
                if(dataSize > 4){
                    accZ[0] = Math.abs(accData.elementAt(dataSize-4).z - accData.elementAt(0).z)*bodyLength/9.81f;
                    accZ[1] = accData.elementAt(dataSize-3).z;
                    accZ[2] = accData.elementAt(3).z;
                }
                accDataAll.add(accData);
                accData.clear();
            }

            if(chkTime>200){
                accData.clear();
            }*/

            if(detectMaximSagittal(accX_series, 1.5f)){ //walk: 1.5f, step: 1.0f
                stepNum++;
                chkTime = 0;

                int accL = accData.size();
                float[] accV_buf = new float[accL];
                for(int i=0;i<accL;i++){
                    accV_buf[i] = accData.get(i).z;
                }
                stepLength = calcCogDis(accV_buf, 1780f, debug_max, debug_min);

                disX_series[0] = disX_series[1];
                disX_series[1] = disX_series[2];
                disX_series[2] = disX_series[3];
                disX_series[3] = disX_series[4];
                disX_series[4] = stepLength;

                stepLength = (disX_series[0]+disX_series[1]+disX_series[2]+disX_series[3]+disX_series[4])*0.2f;

                accDataAll.add(accData);

                //Judge left or right
                intYacc = 0.0f;
                for(int k=0; k<accData.size(); ++k){
                    intYacc += accData.get(k).y * 0.01667;
                }

                if(intYacc > 0.0f){
                    //if(stepNum > 3){
                    //    if(nowFoot == "left"){
                            nowFoot = "right";
                            supportFoot = "left";
                    /*    }
                        else{
                            nowFoot = "left";
                            supportFoot = "right";
                        }
                    }
                    /else{
                        nowFoot = "right";
                        supportFoot = "left";
                    }*/
                }
                else if(intYacc < 0.0f){

                    //if(stepNum > 3){
                    //    if(nowFoot == "right"){
                            nowFoot = "left";
                            supportFoot = "right";
                    /*    }
                        else{
                            nowFoot = "right";
                            supportFoot = "left";
                        }
                    }
                    else{
                        nowFoot = "left";
                        supportFoot = "right";
                    }*/

                    if(walkEvent == false){
                        walkEvent = true;
                    }
                    else{
                        walkCycle_length = walkAccData.size();
                        walkAccDataAll.add(walkAccData);
                        walkAccData.clear();
                    }

                }

                accData.clear();
            }
        }

        if( stepNum > 0 ){
        }
        chkTime++;



        String eulerAnglesStr =
                        String.format("%d", stepNum) + ", " +
                        String.format("%d", walkCycle_length) + ", " +
                        String.format("%s", nowFoot) ;
        holder.orientationData.setText(eulerAnglesStr);

        String freeAccStr =
                        String.format("%.6f", freeAcc[0]) + ", " +
                        String.format("%.6f", freeAcc[1]) + ", " +
                        String.format("%.6f", freeAcc[2]);
        holder.freeAccData.setText(freeAccStr);

    }

    @Override
    public int getItemCount() {

        return mDataList == null ? 0 : mDataList.size();
    }



    public float[][] calcQuaternionRotationMatrix(float[] q){
        float[][] rotM = new float[3][3];
        float w = q[0];
        float x = q[1];
        float y = q[2];
        float z = q[3];

        rotM[0][0] = 1.0f - 2*y*y - 2*z*z;
        rotM[0][1] = 2*x*y + 2*w*z;
        rotM[0][2] = 2*x*z - 2*w*y;

        rotM[1][0] = 2*x*y - 2*w*z;
        rotM[1][1] = 1.0f - 2*x*x -2*z*z;
        rotM[1][2] = 2*y*z + 2*w*x;

        rotM[2][0] = 2*x*z + 2*w*y;
        rotM[2][1] = 2*y*z - 2*w*x;
        rotM[2][2] = 1.0f - 2*x*x - 2*y*y;

        return rotM;
    }

    public float[] calcSensCoordinateAcc(float[][] rotM, float[] accV){
        float[] actV_cord = new float[3];

        actV_cord[0] = rotM[0][0] * accV[0] + rotM[0][1] * accV[1] + rotM[0][2] * accV[2];
        actV_cord[1] = rotM[1][0] * accV[0] + rotM[1][1] * accV[1] + rotM[1][2] * accV[2];
        actV_cord[2] = rotM[2][0] * accV[0] + rotM[2][1] * accV[1] + rotM[2][2] * accV[2];

        return actV_cord;
    }

    /*public boolean detectMaxim(float[] accV, float thereL, float thereU){

        if(accV[2] > thereL && accV[2] < thereU){
            if((accV[2]-accV[0] > 0) && (accV[4]-accV[2] < 0)){
                return(true);
            }
            else{
                return(false);
            }
        }
        else{
            return(false);
        }
    }*/
    
    public boolean detectMaximSagittal(float[] accV, float threshould){

        float ddacc = Math.abs(accV[2] -2*accV[1]+accV[0]);

        if(ddacc > threshould){
            return(true);
        }
        else{
            return(false);
        }
    }

    /*public float calcStepLength(Vector<threeAxis> accData){
        float stepLength = 1.0f;
        int L = accData.size();
        float bodyLength = 0.5441f * 1780f - 92.95f;

        stepLength = (accData.get(0).z - accData.get(L).z)*bodyLength/9.81f;

        return stepLength;
    }*/

    public float calcCogDis( float[] accV, float Length, float[] dmax, float[] dmin){
        float l = 0.5441f * Length - 92.95f;
        int N = accV.length;
        int[] minN = {0};
        int[] maxN = {0};
        //float minVal = calcDataMin(accV, minN);
        //データサイズを更新
        float[] accVs = new float[N];
        for(int i=2; i<(N-2); i++){
            accVs[i] = accV[i];
        }

        float minVal = calcDataMin(accVs, minN);
        float maxVal = calcDataMax(accVs, maxN);

        dmax[0] = maxVal;
        dmin[0] = minVal;

        System.out.println(dmax[0]);
        System.out.println(dmin[0]);
        return l * (maxVal - minVal) / 9.81f;
    }

    public float calcDataMax(float[] data, int[] pN){

        int L = data.length;
        float maxVal = data[0];
        pN[0] = 0;

        for(int i=0; i<L; i++){
            if(data[i] > maxVal){
                maxVal = data[i];
                pN[0] = i;
            }
        }

        System.out.println(pN[0]);
        return maxVal;
    }

    public float calcDataMin(float[] data, int[] pN){
        int L = data.length;
        float minVal = data[0];
        pN[0] = 0;

        for(int i=0; i<L; i++){
            if(data[i] < minVal){
                minVal = data[i];
                pN[0] = i;
            }
        }

        System.out.println(pN[0]);
        return minVal;
    }

    /**
     * A Customized class for ViewHolder of RecyclerView.
     */
    static class DataViewHolder extends RecyclerView.ViewHolder {

        View rootView;
        TextView sensorName;
        TextView orientationData;
        TextView freeAccData;

        DataViewHolder(View v) {

            super(v);

            rootView = v;
            sensorName = v.findViewById(R.id.sensor_name);
            orientationData = v.findViewById(R.id.orientation_data);
            freeAccData = v.findViewById(R.id.free_acc_data);
        }
    }
}
