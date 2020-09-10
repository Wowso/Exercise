package com.example.exercise2.ui.exercise;

import androidx.lifecycle.ViewModelProviders;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.exercise2.DbContract;
import com.example.exercise2.DbHelper;
import com.example.exercise2.MainActivity;
import com.example.exercise2.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.IOError;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import androidx.navigation.Navigation;

public class ExerciseFragment extends Fragment implements Button.OnClickListener {

    private ExerciseViewModel mViewModel;
    private View root;
    private LineChart lineChart;
    private LineDataSet dataSet;
    private LineDataSet dataSet2;
    private LineData data;

    Date mtime = new Date();
    Calendar cal = Calendar.getInstance();

    long start =0l;
    long end = mtime.getTime();

    private long mDbId = -1;
    private int count=0;

    private Button mBu_End;
    private Button mBu_Random;
    private TextView vGoals_Text;
    private TextView vCount_Text;
    private TextView vRemain_Text;

    private float[] target_X = {0f, 10f, -10f};
    private float[] target_Y = {0f, 10f, -10f};
    private double gyroX =0f;
    private double gyroY =0f;
    private double gyroX_init = 0f;
    private double gyroY_init = 0f;

    private String[] arr= {"01","02","00","10","20","00"}; //좌표 이동 순서
    private int A=0;
    private int B=1;
    private int cValue=0;
    public int max_Count=10; // defalut 10 보통은 난이도에 따라 전 액티비티에서 갯수 불러오기

    private boolean change_Check = false;
    private boolean start_Check = false;
    private static Handler mHandler;
    private boolean Stopflag = true;

    private boolean gyroX_Check = false;
    private boolean gyroY_Check = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_exercise, container, false);

        setup(root); // 변수 객체 초기화
        Chartinit(); //차트 초기화

        //액티비티 새로고침 스레드
        Thread_play();
        return root;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bu_End:
                End_flag(v);
                //((MainActivity)getActivity()).replaceFragment(SlideshowFragment.newInstance());
                break;
            case R.id.bu_Random:
                //db_Input(1);
                break;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ExerciseViewModel.class);
        // TODO: Use the ViewModel
    }

    public void End_flag(View v) {
        Stopflag = true;
        //start_Check = true;
        sendD("q");
        db_Input(0); //db 데이터 삽입
        Navigation.findNavController(v).navigate(R.id.action_nav_exercise_to_nav_main);
    }

    public void setup(View view){
        lineChart = (LineChart)view.findViewById(R.id.lineChart);
        mBu_End = (Button)view.findViewById(R.id.bu_End);
        mBu_End.setOnClickListener(this);
        mBu_Random = (Button)view.findViewById(R.id.bu_Random);
        mBu_Random.setOnClickListener(this);
        vGoals_Text = (TextView)view.findViewById(R.id.goals_value);
        vCount_Text = (TextView)view.findViewById(R.id.count_value);
        vRemain_Text = (TextView)view.findViewById(R.id.remain_value);
        max_Count = getArguments().getInt("max_count");
        count =0;
        cal.setTime(mtime);
        cal.add(Calendar.YEAR, -3);
        start =(cal.getTime()).getTime();
        start_Check = false;
    }

    private void Thread_play() {
        mHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!start_Check)
                {
                     start_Check=true;
                     sendD("a");
                    //Stopflag = false;
                    //sendD("n");
                }
                while (!Stopflag){
                    Stopflag=true;
                    Log.d("ttt","Thread play");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Activity_Refresh();
                        }
                    });
                    try {
                        Thread.sleep(10);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void db_Input(int flag){
        SimpleDateFormat format1 = new SimpleDateFormat ( "yyyy-MM-dd:HH:mm:ss");
        Date time = new Date();
        String time1 = format1.format(time);
        if(count > 0 || flag == 1)
        {
            if(flag == 1)
            {
                long random = ThreadLocalRandom.current().nextLong(start,end);
                time = new Date(random);
                time1 = format1.format(time);
                count = (int)(Math.random()*10)%11;
                start =time.getTime();
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(DbContract.DbEntry.COLUMN_DATE, time1);
            contentValues.put(DbContract.DbEntry.COLUMN_COUNT, count);

            SQLiteDatabase db = DbHelper.getInstance(getActivity()).getWritableDatabase();
            if(mDbId == -1) // 처음 저장 할때 id defalut값은 -1
            {
                long newRowId = db.insert(DbContract.DbEntry.TABLE_NAME, null, contentValues);
                Log.d("ttt","RowId : "+newRowId);
                if(newRowId == -1){
                    Toast.makeText(getActivity(), "저장에 문제가 발생했습니다", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity(), "저장되었습니다", Toast.LENGTH_SHORT).show();
                    Log.d("ttt","ID : " + DbContract.DbEntry._ID +" 시간 : " + time1 + " 횟수 : " + count + " X : " + target_X[A] + " Y : " + target_Y[B] + " cValue :" + cValue);
                    //setResult(RESULT_OK);
                }
            }
        }
        else{
            Toast.makeText(getActivity(), "횟수가 없기 때문에 저장하지 않습니다.", Toast.LENGTH_SHORT).show();
        }

    }

    public void MessagePr(String s) {
        s = s.replaceAll("(\r\n|\r|\n|\n\r)", "");
        Message_Read(s);
    }

    public void sendD(String s) {
        ((MainActivity)getActivity()).sendData(s);
    }

    public void Message_Read(String str) {
        String Check_text = new String(str.substring(0,1));

        if (Check_text.equals("A")) {
            gyroY = Double.parseDouble(str.substring(1)) + gyroY_init;
            Log.d("ttt",String.valueOf(gyroY));
            if(!gyroY_Check)
            {
                gyroY_init = gyroY;
                gyroY_Check = true;
            }
        }
        else if(Check_text.equals("M"))
        {
            gyroX = Double.parseDouble(str.substring(1)) + gyroX_init;
            Log.d("ttt",String.valueOf(gyroX));
            if(!gyroX_Check)
            {
                gyroX_init = - gyroX;
                gyroX_Check = true;
            }
        }

        if(str.equals("o"))
        {

            Log.d("ttt","Machine Start");
            start_Check = true;
            Stopflag = false;
            sendD("n");
        }
        if (str.equals("e"))
        {
            Stopflag = true;
        }
    }

    private void Chartinit(){
        //차트 데이터 추가
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0,0));

        ArrayList<Entry> entries2 = new ArrayList<>();
        entries2.add(new Entry(0,0));

        dataSet = new LineDataSet(entries, "현재위치");
        dataSet2 = new LineDataSet(entries2, "운동방향");

        data = new LineData(dataSet,dataSet2);
        lineChart.setData(data);

        XAxis xAxis = lineChart.getXAxis(); //x 축 디자인
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setLabelCount(2);
        xAxis.setDrawLabels(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMaximum(20f); //x축 최대 범위
        xAxis.setAxisMinimum(-20f); //x축 최소 범위
        xAxis.enableGridDashedLine(8, 24, 0);

        YAxis yRAxis = lineChart.getAxisRight(); //y축 오른쪽 디자인
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);

        YAxis yLxis = lineChart.getAxisLeft(); //y축 왼쪽 디자인
        yLxis.setAxisMaximum(20f); //y축 왼쪽 최대 범위
        yLxis.setAxisMinimum(-20f); //y축 왼쪽 최소 범위

        Description description = new Description();
        description.setText("");

        //lineChart.getXAxis().setDrawLabels(false); //x축 삭제
        lineChart.getAxisLeft().setDrawLabels(false); //왼쪽 라벨 삭제
        lineChart.getAxisLeft().setLabelCount(2);
        //lineChart.getAxisRight().setDrawLabels(false); // 오른쪽 라벨 삭제
        //lineChart.getLegend().setEnabled(false);  //legend 삭제

        lineChart.getAxisRight().setDrawGridLines(false);
        lineChart.setTouchEnabled(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDescription(description);
        lineChart.animateY(1000, Easing.EaseInCubic);
    }

    private void datasetCreate() {
        dataSet.setLineWidth(2);
        dataSet.setCircleRadius(6);
        dataSet.setCircleColor(Color.parseColor("#FFA1B4DC"));
        dataSet.setCircleHoleColor(Color.BLUE);
        dataSet.setColor(Color.BLUE);
        dataSet.setDrawCircleHole(true);
        dataSet.setDrawCircles(true);
        dataSet.setDrawHorizontalHighlightIndicator(false);
        dataSet.setDrawHighlightIndicators(false);
        dataSet.setDrawValues(false);

        dataSet2.setLineWidth(2);
        dataSet2.setCircleRadius(6);
        dataSet2.setCircleColor(Color.parseColor("#FFFF0000"));
        dataSet2.setCircleHoleColor(Color.RED);
        dataSet2.setColor(Color.RED);
        dataSet2.setDrawCircleHole(true);
        dataSet2.setDrawCircles(true);
        dataSet2.setDrawHorizontalHighlightIndicator(false);
        dataSet2.setDrawHighlightIndicators(false);
        dataSet2.setDrawValues(false);
    }

    void Activity_Change(){
        Navigation.findNavController(root).navigate(R.id.action_nav_exercise_to_nav_main);
    } //Activity 바꾸기

    void Activity_Refresh() {
        if(start_Check)
            ChartRefresh(-gyroY,gyroX);
        vCount_Text.setText(String.valueOf(count));
        vGoals_Text.setText(Math.round((float)count/(float)max_Count*100)+"%");
        vRemain_Text.setText(String.valueOf(max_Count-count));
        if(max_Count==count&&!Stopflag) // 모든 횟수를 끝마쳐서 저장 후 메인 이동
        {
            Stopflag = true;
            sendD("q");
            db_Input(0);
            Activity_Change();
        }
    } //TextView 처리

    void ChartRefresh(double X, double Y) {
        try
        {
            ArrayList<Entry> entries = new ArrayList<>();
            entries.add(new Entry((float)X,(float)Y));

            ArrayList<Entry> entries2 = new ArrayList<>();
            entries2.add(new Entry(target_X[A],target_Y[B]));

            check_Count((float)X,(float)Y,target_X[A],target_Y[B],5f);
            dataSet = new LineDataSet(entries, "현재위치");
            dataSet2 = new LineDataSet(entries2, "운동방향");

            datasetCreate();
            data = new LineData(dataSet,dataSet2);
            lineChart.setData(data);
            lineChart.invalidate();
        }catch (IOError e)
        {

        }
    } //Chart 처리

    void change_Point(){
        String[] array_word;
        array_word = arr[cValue].split("");
        for(int i=0;i<array_word.length;i++){
            System.out.println(array_word[i]);
        }
        cValue++;
        if(cValue >= arr.length)
            cValue=0;

        A = Integer.parseInt(arr[cValue].substring(0,1));
        B = Integer.parseInt(arr[cValue].substring(1));
        if(A == 0&&B == 1)//북
        {
            sendD("n");
            sendD("n");
            sendD("n");
        }
        else if(A == 0&&B == 2)//남
        {
            sendD("s");
            sendD("s");
            sendD("s");
        }
        else if(A == 1&&B == 0)//동
        {
            sendD("e");
            sendD("e");
            sendD("e");
        }
        else if(A == 2&&B == 0)//서
        {
            sendD("w");
            sendD("w");
            sendD("w");
        }
        if(A == 0&&B == 0)
            count++;

        change_Check = false;
        //if() //이전 위치로 다시 배정되면 다시 랜덤으로 뽑기
        // 위치 X = 0f Y = 10f
        // 위치2 X = 0f Y = -10f
        // 위치3 X = 10f Y = 0f
        // 위치4 X = +10f Y= 0f
        // 배열로 만들경우 X[3] = {0f,10f,-10f}
        // Y[3] = {0f,10f,-10f}
        // X[0],Y[1] = 01 -> 0 위
        // X[0],Y[2] = 02 -> 1 아래
        // X[1],Y[0] = 10 -> 2 오
        // X[2],Y[0] = 20 -> 3 왼
        // 숫자 두자리를 분리해서 저장.
        // 랜덤으로 뽑은 숫자중(0~3) 나온 숫자로 위치 설정

    }

    void check_Count(float X1, float Y1,float X2, float Y2,float area) {
        if(((X2-area)<=X1&&(X2+area)>=X1)&&((Y2-area)<=Y1&&(Y2+area)>=Y1)&&!change_Check) // X2,Y2 +-1f 범위에 X1.Y1이 들어왔을 경우 카운트+ 그리고 change_Check 카운트가 false일때
        {
            change_Check = true;
            lineChart.animateY(1000, Easing.EaseInCubic);
            //lineChart.animateX(1000,Easing.EaseInCubic);
            //lineChart.animateXY(1000, 1000,Easing.EaseInBounce);
            change_Point();
        }
    }
}
