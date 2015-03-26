package com.tsengsation.resound.ViewHelpers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tsengsation.resound.R;

/**
 * Created by jonathantseng on 3/26/15.
 */
public class ResoundNavBar extends LinearLayout {

    private TextView mTextView;

    public ResoundNavBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_resound_navbar, this, true);
        mTextView = (TextView) findViewById(R.id.navbar_text);
    }

    public void setText(String text) {
        mTextView.setText(text.toUpperCase());
    }

}
