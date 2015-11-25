package com.tsengsation.resound.Views;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tsengsation.resound.R;
import com.tsengsation.resound.ViewHelpers.FontManager;

public class NavItemAdapter extends ArrayAdapter<String> {

    Activity mContext;
    String[] mItemTexts;
    int[] mItemDrawables;

    public NavItemAdapter(Activity context, String[] itemTexts, int[] itemDrawables) {
        super(context, R.layout.drawer_list_item, itemTexts);
        mContext = context;
        mItemTexts = itemTexts;
        mItemDrawables = itemDrawables;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = mContext.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.drawer_list_item, null, true);

        TextView textView = (TextView) rowView.findViewById(R.id.drawer_item_text);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.drawer_item_image);

        textView.setText(mItemTexts[position]);
        FontManager.setFont(getContext(), textView, FontManager.PETITA_MEDIUM);
        imageView.setImageResource(mItemDrawables[position]);
        return rowView;
    }

}
