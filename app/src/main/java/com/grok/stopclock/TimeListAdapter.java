package com.grok.stopclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Formatter;


/**
 * Created by hoche on 6/7/2018.
 */
public class TimeListAdapter extends BaseAdapter {

    private final String LOGTAG = "TimeListAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private TimeStore mTimeStore;

    private SharedPreferences mSharedPreferences;

    class TimeListViewHolder {
        public TextView mTvId;
        public TextView mTvTimeEntry;
        public TimeListViewHolder(View base) {
            mTvId = (TextView) base.findViewById(R.id.entry_id);
            mTvTimeEntry = (TextView) base.findViewById(R.id.entry_time);
        }
    }

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
        LogUtil.INSTANCE.d(LOGTAG, "getItem()");
        // Note that we return the object from the array in reverse order
        // TODO: handle IndexOutOfBoundsException
        return mTimeStore.get(mTimeStore.getEntryCount() - position - 1);
    }

    @Override
    public long getItemId(int position) {
        LogUtil.INSTANCE.d(LOGTAG, "getItemId(" + position + ")");
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LogUtil.INSTANCE.d(LOGTAG, "getView()");
        TimeEntry te = (TimeEntry)getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        View resultView = convertView;
        TimeListViewHolder viewHolder;
        if (resultView == null) {
            resultView = mInflater.inflate(R.layout.time_table_row, parent, false);
            viewHolder = new TimeListViewHolder(resultView);
            resultView.setTag(viewHolder);
        } else {
            viewHolder = (TimeListViewHolder)convertView.getTag();
        }

        int hour = te.mHour;
        if (mSharedPreferences.getBoolean("Use12HourTime", false)) {
            hour = (hour % 12);
            if (hour == 0) hour = 12;
        }

        StringBuilder sbuf = new StringBuilder();
        Formatter fmt = new Formatter(sbuf);
        switch (mSharedPreferences.getInt("TimeFormatId", 0)) {
            case 1:
                fmt.format("%d:%02d:%02d.%d", hour, te.mMin, te.mSec, te.mTenth);
                break;

            case 2:
                fmt.format("%d:%02d.%02d", hour, te.mMin, ((te.mSec * 1000) + te.mTenth)/600);
                break;

            case 0:
            default:
                fmt.format("%d:%02d:%02d", hour, te.mMin, te.mSec + ((te.mTenth >= 5) ? 1 : 0) );
        }

        viewHolder.mTvId.setText(te.getId());
        viewHolder.mTvTimeEntry.setText(sbuf.toString());

        return resultView;
    }
}
