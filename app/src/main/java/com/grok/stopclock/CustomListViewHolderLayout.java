package com.grok.stopclock;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

/**
 * Created by hoche on 6/8/2018.
 */
public class CustomListViewHolderLayout extends FrameLayout {

    public int currentWidth = 0;
    public int currentHeight = 0;

    public CustomListViewHolderLayout(Context context) {
        super(context);
    }

    public CustomListViewHolderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomListViewHolderLayout(Context context, AttributeSet attrs,
                                      int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        //Debug Prints
        /*
        String wMode = "";
        switch(MeasureSpec.getMode(widthMeasureSpec)){
            case MeasureSpec.AT_MOST:
                wMode = "AT_MOST"; break;
            case MeasureSpec.EXACTLY:
                wMode = "EXACTLY"; break;
            case MeasureSpec.UNSPECIFIED:
                wMode = "UNSPECIFIED"; break;
            default:
                wMode = "";
        }
        String hMode = "";
        switch(MeasureSpec.getMode(heightMeasureSpec)){
            case MeasureSpec.AT_MOST:
                hMode = "AT_MOST"; break;
            case MeasureSpec.EXACTLY:
                hMode = "EXACTLY"; break;
            case MeasureSpec.UNSPECIFIED:
                hMode = "UNSPECIFIED"; break;
            default:
                hMode = "";
        }
        Log.i("onMeasure", "wMode = " + wMode + ", size = " + MeasureSpec.getSize(widthMeasureSpec));
        Log.i("onMeasure", "hMode = " + hMode + ", size = " + MeasureSpec.getSize(heightMeasureSpec));
        */

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int wScreen = displayMetrics.widthPixels;
        int hScreen = displayMetrics.heightPixels;
        //Log.i("onMeasure", "wScreen = " + wScreen);
        //Log.i("onMeasure", "hScreen = " + hScreen);

        //only remeasure child listview when the size changes (e.g. orientation change)
        if (getChildCount() == 1){
            if(((MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) ||
                    (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST))
                    &&((MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) ||
                    (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST))){
                //check if height dimensions are different than before
                int newWidth =  MeasureSpec.getSize(widthMeasureSpec);
                int newHeight = MeasureSpec.getSize(heightMeasureSpec);
                if((newWidth != currentWidth) || (newHeight != currentHeight)){
                    if (newWidth == currentWidth && newHeight == hScreen) {
                        // screen blanked? just keep our last height
                        //Log.i("onMeasure", "keeping old height");
                        this.setMeasuredDimension(currentWidth, currentHeight);
                    } else {
                        //remeasure if different
                        //Log.i("onMeasure", "measuring listView");
                        View childView = getChildAt(0);
                        childView.measure(widthMeasureSpec, heightMeasureSpec);
                        currentWidth = newWidth;
                        currentHeight = newHeight;
                        this.setMeasuredDimension(newWidth, newHeight);
                    }
                } else {
                    //still set this view's measured dimension
                    this.setMeasuredDimension(newWidth, newHeight);
                }
            } else {
                //Specify match parent if one of the dimensions is unspecified
                this.setMeasuredDimension(ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.FILL_PARENT);
            }
        } else {
            //view does not have the listview child, measure normally
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            currentWidth = 0;
            currentHeight = 0;
        }
    }
}
