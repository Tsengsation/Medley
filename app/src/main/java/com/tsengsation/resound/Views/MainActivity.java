package com.tsengsation.resound.Views;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import com.squareup.picasso.Picasso;
import com.tsengsation.resound.Events.OnScrolledListener;
import com.tsengsation.resound.Parse.ArticleIndexOutOfBoundsException;
import com.tsengsation.resound.Parse.ParseResound;
import com.tsengsation.resound.PicassoHelper.PicassoImageSwitcher;
import com.tsengsation.resound.R;
import com.tsengsation.resound.ViewHelpers.ViewCalculator;


public class MainActivity extends FragmentActivity implements ViewSwitcher.ViewFactory {

    private final static float MAX_ALPHA = 0.6f;
    private final static int MAX_ALPHA_OFFSET_DP = 80;

    private PicassoImageSwitcher mArticleImageSwitcherCurr;
    private PicassoImageSwitcher mArticleImageSwitcherPrev;
    private PicassoImageSwitcher mArticleImageSwitcherNext;
    private ViewPager mArticlePager;
    private ArticlePagerAdapter mArticlePagerAdapter;
    private View mFadeView;

    private int mCurrPosition;
    private float mCurrFadeY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        // instantiate calculator so it has context for rest of application
        ViewCalculator.getInstance(getApplicationContext());
        initViewReferences();
        setUpSwipes();
        setUpImageSwitching();
        setImage(0);
    }

    private void setUpImageSwitching() {
        mArticleImageSwitcherCurr.getImageSwitcher().setFactory(this);
//        mArticleImageSwitcherCurr.getImageSwitcher().setInAnimation(AnimationUtils.loadAnimation(this,
//                android.R.anim.fade_in));
//        mArticleImageSwitcherCurr.getImageSwitcher().setOutAnimation(AnimationUtils.loadAnimation(this,
//                android.R.anim.fade_out));
    }

    @Override
    public View makeView() {
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundColor(0xFFFFFFFF);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new ImageSwitcher.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        return imageView;
    }

    private void setUpSwipes() {
        mArticlePagerAdapter = new ArticlePagerAdapter(getApplicationContext(), getSupportFragmentManager());
        mArticlePager.setAdapter(mArticlePagerAdapter);
        mArticlePager.setOffscreenPageLimit(0);
        mArticlePager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                float offset;
                if (position == mCurrPosition) { // scrolling to right
                    offset = positionOffset;
                } else { // scrolling to left
                    offset = 1f - positionOffset;
                }
                updateFadeX(offset);
            }

            @Override
            public void onPageSelected(int position) {
                mCurrPosition = position;
                setImage(position);
                updateFadeY(0);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setImage(int position) {
        ArticleFragment articleFragment = (ArticleFragment) mArticlePagerAdapter.getItem(position);
        Picasso.with(getApplicationContext()).load(articleFragment.getArticle().getImageUrl()).into(mArticleImageSwitcherCurr);
    }

    private void initViewReferences() {
        ImageSwitcher imageSwitcher1 = (ImageSwitcher) findViewById(R.id.article_image_switcher_1);
        ImageSwitcher imageSwitcher2 = (ImageSwitcher) findViewById(R.id.article_image_switcher_2);
        ImageSwitcher imageSwitcher3 = (ImageSwitcher) findViewById(R.id.article_image_switcher_3);
        imageSwitcher2.setAlpha(0f);
        imageSwitcher3.setAlpha(0f);
        mArticleImageSwitcherCurr = new PicassoImageSwitcher(getApplicationContext(), imageSwitcher1);
        mArticleImageSwitcherPrev = new PicassoImageSwitcher(getApplicationContext(), imageSwitcher2);
        mArticleImageSwitcherNext = new PicassoImageSwitcher(getApplicationContext(), imageSwitcher3);
        mArticlePager = (ViewPager) findViewById(R.id.article_pager);
        mFadeView = findViewById(R.id.fade_view);
    }

    private void updateFadeX(float xScroll) {
        float alpha = mCurrFadeY * (1 - xScroll);
        mFadeView.setAlpha(alpha);
    }

    private void updateFadeY(int yScroll) {
        float alpha;
        float maxOffset = (float) ViewCalculator.dpToPX(MAX_ALPHA_OFFSET_DP);
        if (yScroll > maxOffset) {
            alpha = MAX_ALPHA;
        } else {
            alpha = MAX_ALPHA * yScroll / maxOffset;
        }
        mFadeView.setAlpha(alpha);
        mCurrFadeY = alpha;
    }

    public class ArticlePagerAdapter extends FragmentStatePagerAdapter {

        private Context mContext;

        public ArticlePagerAdapter(Context context, FragmentManager manager) {
            super(manager);
            mContext = context;
        }

        @Override
        public int getCount() {
            return ParseResound.getInstance().getNumArticles();
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            try {
                fragment = ArticleFragment.newInstance(mContext, ParseResound.getInstance().getArticle(position));
                final ArticleFragment articleFragment = (ArticleFragment) fragment;
                articleFragment.setOnScrolled(new OnScrolledListener() {
                    @Override
                    public void onScrolled(int oldY, int newY) {
                        updateFadeY(newY);
                    }
                });
            } catch (ArticleIndexOutOfBoundsException e) {
                Log.e("Article Swipe", e.getMessage());
            } finally {
                return fragment;
            }
        }
    }

}
