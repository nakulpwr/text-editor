package com.nakul.texteditor;

/**
 * Created by nakul on 8/31/2017.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;


public class RichTextActions extends LinearLayout {
    private Context mContext;
    private LayoutInflater mInflater;

    public RichTextActions(Context context) {
        super(context);
        setupView(context);
    }

    public RichTextActions(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupView(context);
    }

    public RichTextActions(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RichTextActions(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setupView(context);
    }

    private void setupView(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.rich_text_actions, this, true);
    }
}

