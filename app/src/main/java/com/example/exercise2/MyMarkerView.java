package com.example.exercise2;
import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

import java.text.SimpleDateFormat;

public class MyMarkerView extends MarkerView  {
    private TextView tvContent;
    private SimpleDateFormat mformat;
    public MyMarkerView(Context context, int layoutResource,int chartflag) {
        super(context, layoutResource);
        tvContent = (TextView)findViewById(R.id.tvContent);
        SimpleDateFormat format;
        if(chartflag ==0)
        {
            format = new SimpleDateFormat("yyyy년");
            this.mformat = format;

        }
        else if(chartflag == 1) //월
        {
            format = new SimpleDateFormat("yy년MM월");
            this.mformat = format;
        }
        else if(chartflag == 2) //일
        {
            format = new SimpleDateFormat("yy/MM/dd");
            this.mformat = format;
        }
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        if (e instanceof CandleEntry) {
            CandleEntry ce = (CandleEntry) e;
            tvContent.setText("" + Utils.formatNumber(ce.getHigh(), 0, true));
        } else {


            Long lvalue = (long)e.getX();

            tvContent.setText("날짜 : " +mformat.format(datesToUtc(lvalue)) +"\n횟수 : "+Utils.formatNumber(e.getY(), 0, true)+"번");
        }
        super.refreshContent(e, highlight);
    }
    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
    private long datesToUtc(long dates)
    {
        return dates*(1000 * 60 * 60 *24);
    }
}
