package com.example.exercise2.ui.record;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.exercise2.DbContract;
import com.example.exercise2.DbHelper;
import com.example.exercise2.MainActivity;
import com.example.exercise2.MyMarkerView;
import com.example.exercise2.R;
import com.example.exercise2.datePickerActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class RecordFragment extends Fragment implements Button.OnClickListener{

    private RecordViewModel galleryViewModel;
    static final int REQ_ADD_FDATE = 1;
    static final int REQ_ADD_EDATE = 2;

    private LineChart lineChart;

    private LineDataSet dataSet;

    private LineData data;
    private Button bu_Delete;
    private Button bu_Fdate;
    private Button bu_Edate;
    private Spinner spinner;

    private int chartFlag = 2; //default = -1

    private int yearCount = 0;
    private int monthCount = 0;
    private int dayCount = 0;

    private int mCount = 0;

    private int tyear = 0;
    private int tmonth = 0;
    private int tday = 0;

    private int mfYear =0;
    private int mfMonth =0;
    private int mfDay =0;
    private int meYear =0;
    private int meMonth =0;
    private int meDay =0;

    private boolean last_flag=false;

    private Long start_date=0l;
    private Long end_date=0l;

    private Long before_ldate;

    ArrayList<Entry> entries = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(RecordViewModel.class);
        View root = inflater.inflate(R.layout.fragment_record, container, false);
        setup(root);
        Db_setup();
        Chartinit();
        return root;
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bu_delete:
                SQLiteDatabase db = DbHelper.getInstance(getActivity()).getWritableDatabase();
                int count = db.delete(DbContract.DbEntry.TABLE_NAME, null, null);
                if (count == 0) {
                    Toast.makeText(getActivity(), "삭제에 문제가 발생하였습니다", Toast.LENGTH_SHORT).show();
                } else {
                    lineChart.invalidate();
                    lineChart.clear();
                    Toast.makeText(getActivity(), "성공적으로 삭제가 되었습니다", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bu_fdate:
                ((MainActivity)getActivity()).open_datePicker(mfYear,mfMonth,mfDay,1);
                break;
            case R.id.bu_edate:
                ((MainActivity)getActivity()).open_datePicker(meYear,meMonth,meDay,2);
                break;
        }
    }

    public void date_picker(int Year, int Month, int Day, int key) {
        if(key == 1)
        {
            mfYear = Year;
            mfMonth = Month;
            mfDay = Day;
            bu_Fdate.setText(mfYear+"-"+mfMonth+"-"+mfDay);
            start_date=stringToLong(mfYear+"-"+mfMonth+"-"+mfDay+":00:00:00");
        }
        else if(key == 2)
        {
            meYear = Year;
            meMonth = Month;
            meDay = Day;
            bu_Edate.setText(meYear+"-"+meMonth+"-"+meDay);
            end_date=stringToLong(meYear+"-"+meMonth+"-"+meDay+":00:00:00");
        }
        entries =new ArrayList<>();
        //setup(getView());
        Db_setup();
        Chartinit();
    }

    private void setup(View view) {
        lineChart = (LineChart)view.findViewById(R.id.lineChart);
        bu_Delete = (Button)view.findViewById(R.id.bu_delete);
        bu_Delete.setOnClickListener(this);
        bu_Fdate = (Button)view.findViewById(R.id.bu_fdate);
        bu_Fdate.setOnClickListener(this);
        bu_Edate = (Button)view.findViewById(R.id.bu_edate);
        bu_Edate.setOnClickListener(this);
        spinner = (Spinner)view.findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0)
                {
                    chartFlag=2;
                    entries =new ArrayList<>();
                    //setup(view);
                    Db_setup();
                    Chartinit();
                    Toast.makeText(getActivity(), "일별로 출력합니다", Toast.LENGTH_SHORT).show();
                }
                else if(position == 1)
                {
                    chartFlag = 1;
                    entries = new ArrayList<>();
                    //setup(view);
                    Db_setup();
                    Chartinit();
                    Toast.makeText(getActivity(), "월별로 출력합니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        yearCount = 0;
        monthCount = 0;
        dayCount = 0;
        tyear = 0;
        tmonth = 0;
        tday = 0;
        mCount = 0;
        before_ldate=0l;
        last_flag = false;
    }

    public void Db_setup() { //Db에서 데이터 로드

        mCount = 0;
        yearCount = 0;
        monthCount = 0;
        dayCount = 0;
        last_flag = false;
        //before_ldate=0l;
        SQLiteDatabase db = DbHelper.getInstance(getActivity()).getReadableDatabase();
        String[] projection = {
                BaseColumns._ID,
                DbContract.DbEntry.COLUMN_DATE,
                DbContract.DbEntry.COLUMN_COUNT
        };

        //String selection = DbContract.DbEntry.COLUMN_DATE + " = ?"; //WHERE 조건
        //String[] selectionArgs = {"My Title"};

        String sortOrder = DbContract.DbEntry.COLUMN_COUNT + " DESC";

        Cursor cursor = db.query(
                DbContract.DbEntry.TABLE_NAME, //쿼리의 대상이 되는 테이블
                projection, //반환되는 컬럼의 리스트 , null은 모든 컬럼
                null, //SQL WHERE에 해당, null은 모든 컬럼
                null, //selection의 ?를 순서대로 대체한다.
                null, //SQL GROUP BY에 해당
                null, //SQL HAVING에 해당
                null //SQL ORDER BY에 해당
                //"4" //limit 반환되는 행의 개수 제한
        );

        if(start_date != 0 && end_date !=0)
        {
            if (cursor.getCount() > 0) {
                Log.d("ttt", "데이터가 있습니다.");
                ArrayList<Entry> entrie_init = new ArrayList<Entry>();
                while (cursor.moveToNext()) {
                    String itemdate = cursor.getString(
                            cursor.getColumnIndexOrThrow(DbContract.DbEntry.COLUMN_DATE)); //날짜 형식 문자열로 받아오기
                    int itemcount = cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.DbEntry.COLUMN_COUNT)); //횟수 받아오기

                    entrie_init.add(new Entry(stringToLong(itemdate),itemcount));
                }
                entrie_sort(entrie_init);
                for(int i=0;i<entrie_init.size();i++)
                {
                    entrie_Input(entrie_init.get(i).getX(), (int)entrie_init.get(i).getY()); //entrie에 데이터 집어넣기
                }

                if(last_flag==true&&start_date <=before_ldate && end_date >=before_ldate)
                    entries.add(new Entry(before_ldate, mCount));

            } else {
                Log.d("ttt", "조회결과가 없습니다.");
            }
        }
    }

    private void entrie_sort(ArrayList<Entry> entrie) {
        Collections.sort(entrie, new Comparator<Entry>() {
            @Override
            public int compare(Entry o1, Entry o2) {
                return Float.compare(o1.getX(),o2.getX());
            }
        });
    }

    private void entrie_Input(Float date, int count) {
        //SimpleDateFormat mformat = new SimpleDateFormat("yyyy/MM/dd");
        Long mdate = FloatToLong(date);
        float fchange = 0f;
        if(start_date <=mdate && end_date >=mdate)
        {
            longToCalender(datesToUtc(mdate));
            date_Classification(chartFlag, mdate, count);
            last_flag=true;
        }

        /*
        Log.d("ttt","처음 값");
        Log.d("ttt",date);
        Log.d("ttt","long값");
        Log.d("ttt",String.valueOf(mdate));
        fchange = mdate;

        Log.d("ttt","long->float값");
        Log.d("ttt",String.valueOf(fchange));
        Log.d("ttt","float->long값");
        Log.d("ttt",String.valueOf((long)fchange));
        Log.d("ttt","string값");
        Log.d("ttt",mformat.format(mdate));
        Log.d("ttt","변환후 0string값");
        Log.d("ttt",mformat.format((long)fchange));
         */
        /*
        if (chartFlag == 0) //년
        {
            if (yearCount == 0) {
                yearCount = tyear;
            }
            if (yearCount == tyear) {
                mCount += count;
                before_ldate = mdate;
            } else if (yearCount != tyear) {
                entries.add(new Entry(before_ldate, mCount));
                yearCount = tyear;
                before_ldate = mdate;
                mCount = count;
            }
        } else if (chartFlag == 1) // 월
        {
            if (yearCount == 0 && monthCount == 0) {
                monthCount = tmonth;
                yearCount = tyear;
            }
            if (yearCount == tyear) {
                if (monthCount == tmonth) {
                    mCount += count;
                    before_ldate = mdate;
                } else if (monthCount != tmonth) {
                    entries.add(new Entry(before_ldate, mCount));
                    monthCount = tmonth;
                    yearCount = tyear;
                    before_ldate = mdate;
                    mCount = count;
                }
            } else if (yearCount != tyear) {
                entries.add(new Entry(before_ldate, mCount));
                monthCount = tmonth;
                yearCount = tyear;
                before_ldate = mdate;
                mCount = count;
            }
        } else if (chartFlag == 2) //일
        {

        }

 */
    }

    private void date_Classification(int flag, Long mdate, int count) {
        if(yearCount == 0&&monthCount == 0&&dayCount==0)
        {
            monthCount = tmonth;
            yearCount = tyear;
            dayCount = tday;
        }
        if(yearCount == tyear)
        {
            if(flag ==0)
            {
                mCount += count;
                before_ldate = mdate;
            }
            if(flag >=1)
            {
                if(monthCount ==tmonth) {
                    if(flag ==1)
                    {
                        mCount += count;
                        before_ldate = mdate;
                    }
                    if(flag >=2) {
                        if (dayCount == tday) {
                            mCount += count;
                            before_ldate = mdate;
                        } else if (dayCount != tday) {
                            entriesadd(mdate, count);
                        }
                    }
                }
                else if(monthCount != tmonth)
                {
                    entriesadd(mdate, count);
                }
            }
        }else if(yearCount != tyear) {
            entriesadd(mdate, count);
        }
    }

    private long utcToDates(long timeMillis) {
        return (timeMillis+ Calendar.getInstance().get(Calendar.ZONE_OFFSET)) / (1000*60*60*24);
    }

    private long datesToUtc(long dates) {
        return dates*(1000 * 60 * 60 *24);
    }

    private void entriesadd(Long mdate, int count) {
        entries.add(new Entry(before_ldate,mCount));
        monthCount = tmonth;
        yearCount = tyear;
        dayCount = tday;
        before_ldate = mdate;
        mCount = count;
    }

    private Long FloatToLong(Float fdata) {
        String s_num = String.valueOf(fdata);
        s_num = s_num.substring(0,s_num.length()-2);
        return Long.parseLong(s_num);
    }

    private Long stringToLong(String dtext) {
        Date mdate = new Date();
        Long ldate=0l;
        SimpleDateFormat format1 = new SimpleDateFormat ( "yyyy-MM-dd:HH:mm:ss", Locale.ENGLISH);
        try{
            mdate = format1.parse(dtext);
        }catch (ParseException e)
        {
            Log.d("ttt","뭔가문제가있다",e); //parse가 안되는 이유는 String 날짜 형식과 내가 변환하려는 날짜 형식이 달라서 오류가 났음. 날짜 형식을 맞혀주고 Locale.ENGLISH를 통해 맞춰주면 해결
            e.printStackTrace();
        }
        ldate = utcToDates(mdate.getTime());
        return ldate;
    }

    private void longToCalender(Long ldate) {
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy",Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM",Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd",Locale.getDefault());

        tyear = Integer.parseInt(yearFormat.format(ldate));
        tmonth = Integer.parseInt(monthFormat.format(ldate));
        tday = Integer.parseInt(dayFormat.format(ldate));

    }

    public class DateAxsFormatter implements IAxisValueFormatter {
        private SimpleDateFormat mformat;
        private String formatdate;

        public DateAxsFormatter(int flag) { // 생성자, 여기서는 그 변수의 특별한 성질을 파라미터로 받아와서 년, 월, 일 다르게 처리해야함
            SimpleDateFormat format;
            if(flag ==0)
            {
                format = new SimpleDateFormat("yyyy년");
                this.mformat = format;

            }
            else if(flag == 1) //월
            {
                format = new SimpleDateFormat("yy년MM월");
                this.mformat = format;
            }
            else if(flag == 2) //일
            {
                format = new SimpleDateFormat("yy/MM/dd");
                this.mformat = format;
            }
            else
            {

            }
        }
        @Override
        public String getFormattedValue(float value, AxisBase axis) { // 해당 축의 라벨에 표현되는 float 값을 재 가공해서 String 형태로 return 함. 해당되는 축의 min~max값과 인덱스가 일치해야함(아니면 오류)
            Long lvalue = (long)value;
            formatdate = mformat.format(datesToUtc(lvalue));
            return formatdate;
        }
    }

    void Chartinit() {
        datasetCreate(); //데이터셋 만들기 및 라인데이터 정의
        xyCreate(); //x, y축 속성 정의
        Chartdesign(); //차트 디자인 정의
        lineChart.invalidate(); //차트 그리기
    }

    void datasetCreate() {
        dataSet = new LineDataSet(entries, "운동횟수");
        dataSet.setLineWidth(2); //선 굴기
        dataSet.setCircleRadius(3); //곡률
        dataSet.setDrawCircleHole(true);
        dataSet.setDrawCircles(true);
        dataSet.setColor(Color.WHITE); //Line Color 설정
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setCircleColor(Color.WHITE); //CircleColor 설정 Color.parseColor("#FFA1B4DC")
        dataSet.setCircleHoleColor(Color.WHITE); //CircleHoleColor 설정

        /*
        dataSet.setDrawCircleHole(true);
        dataSet.setDrawCircles(true);
        dataSet.setDrawHorizontalHighlightIndicator(true);
        dataSet.setDrawHighlightIndicators(true);
        dataSet.setDrawFilled(true); //선아래로 색상표시
        dataSet.setDrawValues(true);
        */

        data = new LineData(dataSet);
        lineChart.setData(data);
    }

    void xyCreate(){
        XAxis xAxis = lineChart.getXAxis(); //x 축 디자인
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); //x축 표시에 대한 위치 설정
        xAxis.setTextColor(Color.WHITE); //x축 컬러 설정
        //xAxis.setLabelCount(2); //x축 데이터를 최대 몇개까지 보여줄지. force가 true이면 반드시 보여줌
        //xAxis.setDrawLabels(true);
        //xAxis.setDrawAxisLine(true);
        //xAxis.setDrawGridLines(true);
        if(chartFlag == 0)
        {
            xAxis.setGranularity(240.0f); // x-axis scale will be 0,1,2,3,4....
        }else if(chartFlag ==1)
        {
            //xAxis.setGranularity(31.0f); // x-axis scale will be 0,1,2,3,4....
        }else if(chartFlag ==2)
        {
            //xAxis.setGranularity(1.0f); // x-axis scale will be 0,1,2,3,4....
        }
        xAxis.setValueFormatter(new DateAxsFormatter(chartFlag));
        //xAxis.setGranularity(1.0f); // x-axis scale will be 0,1,2,3,4....
        /*
        if(chartFlag ==1) //월별
        {
            //xAxis.setAxisMinimum(0f); //x축 최소 범위
            //xAxis.setAxisMaximum(11f); //x축 최대 범위
        }
        else if(chartFlag ==2) //일별
        {
            xAxis.setAxisMinimum(1.5f); //x축 최소 범위
            xAxis.setAxisMaximum(2f); //x축 최대 범위
        }

         */
        xAxis.enableGridDashedLine(8, 24, 0);
        xAxis.setGranularityEnabled(true);

        //오른쪽 비활성화
        YAxis yRAxis = lineChart.getAxisRight(); //y축 오른쪽 디자인
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);

        YAxis yLxis = lineChart.getAxisLeft(); //y축 왼쪽 디자인
        //yLxis.setAxisMaximum(30f); //y축 왼쪽 최대 범위
        yLxis.setAxisMinimum(0f); //y축 왼쪽 최소 범위
        yLxis.setTextColor(Color.WHITE);
    }

    void Chartdesign() {
        Description description = new Description();
        description.setText("");

        //lineChart.getXAxis().setDrawLabels(false); //x축 삭제
        //lineChart.getAxisLeft().setDrawLabels(false); //왼쪽 라벨 삭제
        //lineChart.getAxisLeft().setLabelCount(2);
        //lineChart.getAxisRight().setDrawLabels(false); // 오른쪽 라벨 삭제
        //lineChart.getAxisRight().setDrawGridLines(false);
        MyMarkerView marker = new MyMarkerView(getActivity(),R.layout.activity_my_marker_view,chartFlag);
        marker.setChartView(lineChart);
        lineChart.setMarker(marker);

        Legend L = lineChart.getLegend();
        L.setEnabled(true);  //false : legend 삭제
        L.setPosition(Legend.LegendPosition.ABOVE_CHART_LEFT); //legend 위치 위로
        L.setTextColor(Color.WHITE);
        L.setYOffset(5f);

        lineChart.setTouchEnabled(true);
        lineChart.setDoubleTapToZoomEnabled(true);
        lineChart.setDrawGridBackground(false);
        lineChart.setDescription(description);
        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                //String value = lineChart.getLineData().get
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

}
