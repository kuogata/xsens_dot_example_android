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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xsens.dot.android.HR.R;
import com.xsens.dot.android.sdk.events.XsensDotData;

import org.jtransforms.fft.DoubleFFT_1D;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static com.xsens.dot.android.HR.views.DataFragment.hrfilename;

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
    private int preStepNum = 0;
    public static int stepNum = 0;
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
    public static  ArrayList<ArrayList<threeAxis>> walkAccDataAll = new ArrayList<ArrayList<threeAxis>>();

    ArrayList<threeAxis> accData_wg = new ArrayList<threeAxis>();

    public class threeAxis{
        float x;
        float y;
        float z;
    }

    private long startTime = System.currentTimeMillis();
    private long endTime = 0;
    private double dt = 0;
    private boolean timeFlg = true;
    public static ArrayList<Double> timeData = new ArrayList<>();
    public static String grphdata = "";

    int FFT_SIZE = 20; //16;
    int ANALYSIS_DATA_SIZE = 101;
    int PCA_DATA_SIZE = 17;

    private float[][] PCA_MATRIX;
    private float[] LM_COEFFICIENTS;

    float[] HR_result = new float[3];
    float walkVelocity = 0f;

    /**
     * Default constructor.
     *
     * @param context  The application context
     * @param dataList A list contains tag and data
     */
    public DataAdapter(Context context, ArrayList<HashMap<String, Object>> dataList) {

        mContext = context;
        mDataList = dataList;

        PCA_MATRIX = new float[ANALYSIS_DATA_SIZE*3][PCA_DATA_SIZE];
        LM_COEFFICIENTS = new float[PCA_DATA_SIZE+1];

        try {
            LM_COEFFICIENTS[0] = 1.34912883911447f;
            LM_COEFFICIENTS[1] = -0.0487452870771206f;
            LM_COEFFICIENTS[2] = 0.127465027068761f;
            LM_COEFFICIENTS[3] = 0.050801659397906f;
            LM_COEFFICIENTS[4] = -0.00337089188096662f;
            LM_COEFFICIENTS[5] = 0.00895374117831713f;
            LM_COEFFICIENTS[6] = 0.0141840734605453f;
            LM_COEFFICIENTS[7] = -0.00805168813337192f;
            LM_COEFFICIENTS[8] = 0.0174030442458897f;
            LM_COEFFICIENTS[9] = -0.0182085079393274f;
            LM_COEFFICIENTS[10] = 0.0117300501853723f;
            LM_COEFFICIENTS[11] = -0.0148210165528988f;
            LM_COEFFICIENTS[12] = -0.0105962483101206f;
            LM_COEFFICIENTS[13] = 0.0117463885717435f;
            LM_COEFFICIENTS[14] = -0.00359027104477415f;
            LM_COEFFICIENTS[15] = 0.0113971954047659f;
            LM_COEFFICIENTS[16] = -0.0103477801155749f;
            LM_COEFFICIENTS[17] = 0.00575515971645595f;


            File dir = context.getExternalFilesDir(null);
            String parafilename = dir.getAbsolutePath() + File.separator + "Export_Rotation_non.csv";
            File pfile = new File(parafilename);
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pfile),"UTF-8"));
            String line = br.readLine();

            int i = 0;
            while (line != null){
                String[] data = line.split(",");
                for(int j=0; j < PCA_DATA_SIZE; j++){
                    PCA_MATRIX[i][j] = Float.parseFloat(data[j]);
                }
                line = br.readLine();
                i++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        HR_result[0] = 0;
        HR_result[1] = 0;
        HR_result[2] = 0;

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
        accAxis.y = -accV[1];
        accAxis.z = -accV[2];
        accData.add(accAxis);

        accX_series[0] = accX_series[1];
        accX_series[1] = accX_series[2];
        accX_series[2] = accX_series[3];
        accX_series[3] = accX_series[4];
        accX_series[4] = accV[2];

        float[] freeAcc_wg = new float[3];
        threeAxis accAxis_wg = new threeAxis();

        if(walkEvent){
            walkAccData.add(accAxis);

            freeAcc_wg[0] = freeAcc[0];
            freeAcc_wg[1] = freeAcc[1];
            freeAcc_wg[2] = freeAcc[2] - 9.81f;
            float[] accV_wg = calcSensCoordinateAcc(rotM, freeAcc_wg);
            accAxis_wg.x = accV_wg[0];
            accAxis_wg.y = -accV_wg[1];
            accAxis_wg.z = -accV_wg[2];

            accData_wg.add(accAxis_wg);
        }

        if (timeFlg) {
            startTime = System.currentTimeMillis();
            timeFlg = false;
        }

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

                if (preStepNum != stepNum) {
                    endTime = System.currentTimeMillis();
                    //dt = ((endTime - startTime) * 0.001);  // 秒
                    dt = endTime - startTime;     // ミリ秒
                    timeData.add(dt);
                }

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

                        threeAxis bufData    = new threeAxis();
                        threeAxis bufData_wg = new threeAxis();
                        double[][] input    = new double[3][walkCycle_length];
                        double[][] input_wg = new double[3][walkCycle_length];

                        for(int i=0; i < walkCycle_length; i++){
                            bufData = walkAccData.get(i);
                            input[0][i] = bufData.x;
                            input[1][i] = bufData.y;
                            input[2][i] = bufData.z;

                            bufData_wg = accData_wg.get(i);
                            input_wg[0][i] = bufData_wg.x;
                            input_wg[1][i] = bufData_wg.y;
                            input_wg[2][i] = bufData_wg.z;

                        }

                        accData_wg.clear();

                        HR_result[0] = calcHR(input[0]);
                        HR_result[1] = calcHR(input[1]);
                        HR_result[2] = calcHR(input[2]);

                        float[][] expAccData = new float[3][ANALYSIS_DATA_SIZE];
                        expAccData[0] = setDataLength(walkCycle_length,  input_wg[0]);
                        expAccData[1] = setDataLength(walkCycle_length,  input_wg[1]);
                        expAccData[2] = setDataLength(walkCycle_length,  input_wg[2]);

                        walkVelocity = calcWalkVelocity(expAccData[1], expAccData[2], expAccData[0]);
                        walkAccData.clear();
                    }

                }

                accData.clear();

                startTime = endTime;
            }
        }

        if( stepNum > 0 ){
        }
        chkTime++;

        // display on UI
        String eulerAnglesStr =
                        //String.format("%.6f", PCA_MATRIX[0][0]) + ", " +
                        String.format("%d", stepNum) + ", " +
                        String.format("HR: %f", HR_result[1]) + ", " +
                        String.format("vel: %f", walkVelocity) + ", " +
                        String.format("%s", nowFoot) ;
        holder.orientationData.setText(eulerAnglesStr);

        String freeAccStr =
                        String.format("%.6f", accAxis_wg.y) + ", " +
                        String.format("%.6f", accAxis_wg.z) + ", " +
                        String.format("%.6f", accAxis_wg.x);
        holder.freeAccData.setText(freeAccStr);

        // save to file
        if (preStepNum != stepNum) {
            String datetime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS", Locale.getDefault()).format(new Date());

            String str = String.format("%s", datetime) + ", " +
                    String.format("%d", stepNum) + ", " +
                    String.format("%.6f", stepLength) + ", " +
                    String.format("%d", walkCycle_length) + ", " +
                    String.format("%s", nowFoot) + ", " +
                    String.format("%.6f", freeAcc[0]) + ", " +
                    String.format("%.6f", freeAcc[1]) + ", " +
                    String.format("%.6f", freeAcc[2]);

            Log.i(TAG, "steps - str = " + str);

            new SavingThread(str).start();

            grphdata = String.format("%s", datetime) + ", " +
                    "3.05, " +      // HR
                    String.format("%d", stepNum) + ", " +
                    String.format("%.6f", stepLength);

            preStepNum = stepNum;
        }
    }

    private static class SavingThread extends Thread {
        String str;
        public SavingThread(String str) {
            this.str = str;
        }

        @Override
        public void run() {
            super.run();

            File outputFile = new File(hrfilename);

            try {
                FileWriter outputWriter = new FileWriter(outputFile, true);
                outputWriter.append(str).append("\n");
                outputWriter.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {

        return mDataList == null ? 0 : mDataList.size();
    }

    public float calcHR(double[] input){

        double hr = 0f;
        double even_harmonics = 0, odd_harmonics = 0;
        double[] data = new double[FFT_SIZE];
        double[] fft_data = new double[FFT_SIZE/2];
        data = Arrays.copyOf(input, FFT_SIZE);

        DoubleFFT_1D fft = new DoubleFFT_1D(FFT_SIZE);
        fft.realForward(data);

        for(int i=0, j=0; i < FFT_SIZE / 2 ; i+=2, j++){
            fft_data[i/2] = Math.pow(data[i], 2) + Math.pow(data[i+1], 2);

            //if(j != 0){
                if(j % 2 == 0){
                    even_harmonics += fft_data[j];
                }
                else{
                    odd_harmonics += fft_data[j];
                }
            //}
        }

        hr = even_harmonics / (even_harmonics + odd_harmonics) * 100;
        return (float)hr;
    }

    public int calcGCD(int a, int b){

        int tmp = 0;

        if(a > b){
            tmp = a;
            a = b;
            b = tmp;
        }
        if(a == 0){
            return b;
        }

        return  calcGCD(b%a, a);
    }

    public float[] setDataLength(int dataL, double[] data){
        int gcdNum = calcGCD(dataL-1, (ANALYSIS_DATA_SIZE-1));
        //check gcdNum == 0 ?
        int lcmNum = (dataL-1) * (ANALYSIS_DATA_SIZE-1) / gcdNum;
        int num_sep = lcmNum / (dataL-1);
        int num_get = lcmNum / (ANALYSIS_DATA_SIZE-1);

        float[] expandedData = new float[lcmNum];
        float[] adjustedData = new float[ANALYSIS_DATA_SIZE];
        int count = 0;


        for(int i=0; i < (dataL-1); i++){
            for(int k=0; k<num_sep; k++){
                expandedData[count] = (float)data[i] + ((float)data[i+1] - (float)data[i]) * (float)k / (float)num_sep;
                count++;
            }
        }

        adjustedData[0] = expandedData[0];
        for(int i=1; i < ANALYSIS_DATA_SIZE; i++){
            adjustedData[i] = expandedData[i*num_get-1];
        }



        return adjustedData;
    }

    public float calcWalkVelocity(float[] dataX, float[] dataY, float[] dataZ){
        float[] all_data = new float[ANALYSIS_DATA_SIZE*3];

        for(int i=0; i<ANALYSIS_DATA_SIZE; i++){
            all_data[i] = dataX[i];
            all_data[i + ANALYSIS_DATA_SIZE] = dataY[i];
            all_data[i + 2*ANALYSIS_DATA_SIZE] = dataZ[i];
        }

        float[] pcaAccData = new float[PCA_DATA_SIZE];
        float all_data_sum = 0f;
        float data_ave = 0f;
        float data_sd = 0f;
        float data_sqrt_sum = 0f;
        float[] pcaAccData_norm = new float[PCA_DATA_SIZE];

        for(int i=0; i<PCA_DATA_SIZE; i++){
            pcaAccData[i] = 0f;
            for(int j=0; j<ANALYSIS_DATA_SIZE; j++){
                pcaAccData[i] +=  PCA_MATRIX[j][i] * all_data[j];
            }

            all_data_sum += pcaAccData[i];
        }

        data_ave = all_data_sum / (float)(PCA_DATA_SIZE);

        for(int i=0; i<PCA_DATA_SIZE; i++){
            data_sqrt_sum += (pcaAccData[i]-data_ave)*(pcaAccData[i]-data_ave);
        }

        data_sd = (float)Math.sqrt(data_sqrt_sum / (double)(PCA_DATA_SIZE));

        for(int i=0; i<PCA_DATA_SIZE; i++){
            pcaAccData_norm[i] = (pcaAccData[i] - data_ave) / data_sd;
        }

        float velocity = 0f;

        for(int i=0; i<PCA_DATA_SIZE; i++){
            velocity += LM_COEFFICIENTS[i] * pcaAccData_norm[i];
        }

        return velocity;
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
