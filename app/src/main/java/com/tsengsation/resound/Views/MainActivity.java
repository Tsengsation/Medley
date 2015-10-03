package com.tsengsation.resound.Views;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.LayoutParams;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.tsengsation.resound.Parse.Article;
import com.tsengsation.resound.R;

public class MainActivity extends FragmentActivity {

    private String[] mArticleTypes;
    private LinearLayout mDrawer;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ContentFragment mContentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mArticleTypes = getResources().getStringArray(R.array.article_types);
        int[] articleTypeDrawable = new int[] {
                R.drawable.star,
                R.drawable.news,
                R.drawable.disc,
                R.drawable.microphone,
                R.drawable.playlist
        };
        mDrawer = (LinearLayout) findViewById(R.id.drawer_frame);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navigation_list);

        // Make the navigation drawer span 85 % of the screen width.
        int width = (int) (getResources().getDisplayMetrics().widthPixels * .85);
        LayoutParams params = (LayoutParams) mDrawer.getLayoutParams();
        params.width = width;
        mDrawer.setLayoutParams(params);

        // Need to pad everything in the drawer to achieve "centering" effect.
        int offset = (int) (getResources().getDisplayMetrics().widthPixels * .15);
        mDrawer.setPadding(offset, 0, 0, 0);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new NavItemAdapter(this, mArticleTypes, articleTypeDrawable));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        selectItem(0);
    }

    private void selectItem(int position) {
        String articleType = mArticleTypes[position];
        int type = Article.TYPE_FEATURED;
        if (articleType.equals(getString(R.string.news))) {
            type = Article.TYPE_NEWS;
        } else if (articleType.equals(getString(R.string.albums))) {
            type = Article.TYPE_ALBUMS;
        } else if (articleType.equals(getString(R.string.concerts))) {
            type = Article.TYPE_CONCERTS;
        } else if (articleType.equals(getString(R.string.playlists))) {
            type = Article.TYPE_PLAYLISTS;
        }

        if (mContentFragment == null) {
            mContentFragment = ContentFragment.newInstance(articleType, type, mDrawerLayout);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, mContentFragment).commit();
            mDrawerLayout.setDrawerListener(mContentFragment);
        } else {
            mContentFragment.updateArticles(articleType, type);
        }
        // update selected item and title, then close the drawer

        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawer);
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
}
