package com.tsengsation.medley.ViewHelpers;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tsengsation.medley.R;

/**
 * Navigation bar for Resound application.
 */
public class ResoundNavBar extends LinearLayout implements View.OnClickListener {

    private ImageView mButton;
    private TextView mTextView;
    private NavButtonClickListener mButtonClickListener;

    public ResoundNavBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_navbar, this, true);
        mTextView = (TextView) findViewById(R.id.navbar_text);
        mButton = (ImageView) findViewById(R.id.navbar_button);
        mButton.setOnClickListener(this);
    }

    public void setFont(int font) {
        FontManager.setFont(getContext(), mTextView, font);
    }

    public void setText(String text) {
        mTextView.setText(text.toUpperCase());
    }

    public void setButtonDrawable(int drawable) {
        mButton.setImageDrawable(getResources().getDrawable(drawable));
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mButton)) {
            if (mButtonClickListener != null) {
                mButtonClickListener.onButtonClick();
            }
        }
    }

    public void setOnButtonClick(NavButtonClickListener listener) {
        mButtonClickListener = listener;
    }

    public interface NavButtonClickListener {
        void onButtonClick();
    }
}
