package com.grok.stopclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


/**
 * Created by hoche on 6/7/2018.
 */
public class TimeListAdapter extends BaseAdapter {

    private final String LOGTAG = "TimeListAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private TimeStore mTimeStore;

    private SharedPreferences mSharedPreferences;

    public TimeListAdapter(Context context, TimeStore timeStore) {
        mContext = context;
        mTimeStore = timeStore;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mSharedPreferences = mContext.getSharedPreferences("GrokStopClock", Context.MODE_PRIVATE);
    }

    @Override
    public int getCount() {
        int entryCount = mTimeStore.getEntryCount();
        LogUtil.INSTANCE.d(LOGTAG, "getCount() returned " + entryCount);
        return entryCount;
    }

    @Override
    public Object getItem(int position) {
        return mTimeStore.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LogUtil.INSTANCE.d(LOGTAG, "getView(" + position + ")");

        // Get view for row item
        View rowView = mInflater.inflate(R.layout.time_table_row, parent, false);

        TextView idTv = (TextView) rowView.findViewById(R.id.entry_id);
        TextView timeTv = (TextView) rowView.findViewById(R.id.entry_time);

        TimeEntry te = mTimeStore.get(position);

        idTv.setText(te.getId());
        timeTv.setText(te.getTime(TimeEntry.getTimeFormat(mSharedPreferences.getInt("TimeFormatId", 0))));

        return rowView;
    }
}
