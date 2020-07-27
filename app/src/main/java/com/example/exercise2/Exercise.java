package com.example.exercise2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.AndroidCharacter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.w3c.dom.Text;

import java.io.IOError;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

public class Exercise extends AppCompatActivity {

    private LineChart lineChart;
    public static Context fContext;
    private LineDataSet dataSet;
    private LineDataSet dataSet2;
    private LineData data;

    Date mtime = new Date();
    Calendar cal = Calendar.getInstance();

    long start =0l;
    long end = mtime.getTime();

    private long mDbId = -1;
    private int count=0;

    private SensorManager mSensorManager = null;
    private SensorEventListener mGyroLis;
    private Sensor mGyroSensor = null;

    private Button mBu_End;
    private Button mBu_Random;
    private TextView vGoals_Text;
    private TextView vCount_Text;
    private TextView vRemain_Text;

    private float[] target_X = {0f, 10f, -10f};
    private float[] target_Y = {0f, 10f, -10f};
    double gyroX =0f;
    double gyroY =0f;

    private String[] arr= {"01","02","00","10","20","00"}; //좌표 이동 순서
    private int A=0;
    private int B=1;
    private int cValue=0;
    public int max_Count=10; // defalut 10 보통은 난이도에 따라 전 액티비티에서 갯수 불러오기

    private boolean change_Check = false;
    private boolean start_Check = false;
    private static Handler mHandler;
    private boolean Stopflag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        setup(); // 변수 객체 초기화
        Chartinit(); //차트 초기화

        //액티비티 새로고침 스레드
        mHandler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(start_Check == false)
                {
                    sendD("a");
                    sendD("a");
                    sendD("a");
                    Stopflag =false;
                    //sendD("n");
                }
                while (!Stopflag){
                    Log.d("ttt","Thread play");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Activity_Refresh();
                        }
                    });
                    try {
                        Thread.sleep(50);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        //운동종료 버튼
        mBu_End.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Stopflag = true;
                sendD("q");
                db_Input(0); //db 데이터 삽입
                Intent intent;
                intent = new Intent(Exercise.this,MainActivity.class);
                startActivity(intent);


            }
        });

        mBu_Random.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db_Input(1);
            }
        });
    }

    public void setup(){
        Intent intent = getIntent();
        lineChart = (LineChart)findViewById(R.id.lineChart);
        mBu_End = (Button)findViewById(R.id.bu_End);
        mBu_Random = (Button)findViewById(R.id.bu_Random);
        vGoals_Text = (TextView)findViewById(R.id.goals_value);
        vCount_Text = (TextView)findViewById(R.id.count_value);
        vRemain_Text = (TextView)findViewById(R.id.remain_value);

        max_Count = intent.getExtras().getInt("max_count");
        count =0;
        cal.setTime(mtime);
        cal.add(Calendar.YEAR, -3);
        start =(cal.getTime()).getTime();
        fContext =this;
        start_Check = false;


        //핸드폰 자이로 센서(추후 교체)
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mGyroSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGyroLis = new GyroscopeListener();
        mSensorManager.registerListener(mGyroLis, mGyroSensor, SensorManager.SENSOR_DELAY_UI); //센서 실행
        //mSensorManager.unregisterListener(mGyroLis); //센서 종료
    }

    public void db_Input(int flag){
        SimpleDateFormat format1 = new SimpleDateFormat ( "yyyy-MM-dd:HH:mm:ss");
        Date time = new Date();
        String time1 = format1.format(time);

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

        SQLiteDatabase db = DbHelper.getInstance(this).getWritableDatabase();
        if(mDbId == -1) // 처음 저장 할때 id defalut값은 -1
        {
            long newRowId = db.insert(DbContract.DbEntry.TABLE_NAME, null, contentValues);
            Log.d("ttt","RowId : "+newRowId);
            if(newRowId == -1){
                Toast.makeText(this, "저장에 문제가 발생했습니다", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "저장되었습니다", Toast.LENGTH_SHORT).show();
                Log.d("ttt","ID : " + DbContract.DbEntry._ID +" 시간 : " + time1 + " 횟수 : " + count + " X : " + target_X[A] + " Y : " + target_Y[B] + " cValue :" + cValue);
                //setResult(RESULT_OK);
            }
        }
    }

    public void MessagePr(String s) {
        s = s.replaceAll("(\r\n|\r|\n|\n\r)", "");
        Message_Read(s);
    }

    public void sendD(String s)
    {
        ((MainActivity)MainActivity.mContext).sendData(s);
    }

    public void Message_Read(String str)
    {
        Log.d("tttt",str);
        if (str.indexOf(0) == 'A') {
            gyroX = Double.parseDouble(str.substring(1));
        }
        else if(str.indexOf(0) == 'M')
        {
            gyroY = Double.parseDouble(str.substring(1));
        }

        if(str.equals("o"))
        {
            start_Check = true;
            Stopflag = false;
        }
        if (str.equals("e"))
        {
            //Stopflag = true;
        }
    }

    private class GyroscopeListener implements SensorEventListener{
        @Override
        public void onSensorChanged(SensorEvent event) {
            double gyroX = event.values[0];
            double gyroY = event.values[1];
            double gyroZ = event.values[2];

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }


    private void Chartinit(){
        //차트 데이터 추가
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(3,3));

        ArrayList<Entry> entries2 = new ArrayList<>();
        entries2.add(new Entry(3,3.7f));

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

    private void datasetCreate()
    {
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
    void Activity_Change() //Activity 바꾸기
    {
        Intent intent = new Intent(Exercise.this, MainActivity.class);
        startActivity(intent);
    }

    void Activity_Refresh() //TextView 처리
    {
        if(start_Check)
            ChartRefresh(gyroY,gyroX);
        vCount_Text.setText(String.valueOf(count));
        vGoals_Text.setText(Math.round((float)count/(float)max_Count*100)+"%");
        vRemain_Text.setText(String.valueOf(max_Count-count));
        if(max_Count==count&&!Stopflag) // 모든 횟수를 끝마쳐서 저장 후 메인 이동
        {
            Stopflag = true;
            db_Input(0);
            Activity_Change();
        }
    }

    void ChartRefresh(double X, double Y) //Chart 처리
    {
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
    }

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


    void check_Count(float X1, float Y1,float X2, float Y2,float area)
    {

        if(((X2-area)<=X1&&(X2+area)>=X1)&&((Y2-area)<=Y1&&(Y2+area)>=Y1)&&change_Check == false) // X2,Y2 +-1f 범위에 X1.Y1이 들어왔을 경우 카운트+ 그리고 change_Check 카운트가 false일때
        {
            change_Check = true;
            lineChart.animateY(1000, Easing.EaseInCubic);
            //lineChart.animateX(1000,Easing.EaseInCubic);
            //lineChart.animateXY(1000, 1000,Easing.EaseInBounce);

            change_Point();
        }
    }
}
