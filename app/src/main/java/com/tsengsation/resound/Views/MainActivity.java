package com.tsengsation.resound.Views;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import com.tsengsation.resound.Events.OnFlingListener;
import com.tsengsation.resound.Events.OnScrolledListener;
import com.tsengsation.resound.Parse.Article;
import com.tsengsation.resound.Parse.ArticleIndexOutOfBoundsException;
import com.tsengsation.resound.Parse.ParseResound;
import com.tsengsation.resound.PicassoHelper.PicassoImageSwitcher;
import com.tsengsation.resound.R;
import com.tsengsation.resound.ViewHelpers.ResoundNavBar;
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
    private ResoundNavBar mNavbar;

    private ParseResound mParseResound;
    private int mCurrPosition;
    private float mCurrFadeY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mParseResound = ParseResound.getInstance();
        // instantiate calculator so it has context for rest of application
        ViewCalculator.getInstance(getApplicationContext());
        initViewReferences();
        setUpSwipes();
        setUpImageSwitching();
        initImages();
    }

    private void setUpImageSwitching() {
        mArticleImageSwitcherCurr.getImageSwitcher().setFactory(this);
        mArticleImageSwitcherPrev.getImageSwitcher().setFactory(this);
        mArticleImageSwitcherNext.getImageSwitcher().setFactory(this);
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
                PicassoImageSwitcher nextImage;
                if (position == mCurrPosition) { // scrolling to right
                    offset = positionOffset;
                    nextImage = mArticleImageSwitcherNext;
                } else { // scrolling to left
                    offset = 1f - positionOffset;
                    nextImage = mArticleImageSwitcherPrev;
                }
                updateFadeX(offset);
                updateImageFades(nextImage, offset);
            }

            @Override
            public void onPageSelected(int position) {
                updateImages(position);
                updateFadeY(0);
                mCurrPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initImages() {
        Article currArticle = ((ArticleFragment) mArticlePagerAdapter.getItem(0)).getArticle();
        Article prevArticle = currArticle;
        Article nextArticle = currArticle;
        try {
            nextArticle = mParseResound.getArticle(1);
        } catch (ArticleIndexOutOfBoundsException e) {
            // do nothing
        }
        mArticleImageSwitcherCurr.getImageSwitcher().setAlpha(1f);
        mArticleImageSwitcherPrev.getImageSwitcher().setAlpha(0f);
        mArticleImageSwitcherNext.getImageSwitcher().setAlpha(0f);
        setImage(mArticleImageSwitcherCurr, currArticle);
        setImage(mArticleImageSwitcherPrev, prevArticle);
        setImage(mArticleImageSwitcherNext, nextArticle);
    }

    private void updateImages(int position) {
        Article currArticle = ((ArticleFragment) mArticlePagerAdapter.getItem(position)).getArticle();
        Article prevArticle = currArticle;
        Article nextArticle = currArticle;
        try {
            prevArticle = mParseResound.getArticle(position - 1);
        } catch (ArticleIndexOutOfBoundsException e) {
            // do nothing
        }
        try {
            nextArticle = mParseResound.getArticle(position + 1);
        } catch (ArticleIndexOutOfBoundsException e) {
            // do nothing
        }
        if (position > mCurrPosition) { // moving right to next article
            PicassoImageSwitcher temp = mArticleImageSwitcherPrev;
            mArticleImageSwitcherPrev = mArticleImageSwitcherCurr;
            mArticleImageSwitcherCurr = mArticleImageSwitcherNext;
            mArticleImageSwitcherNext = temp;
            setImage(mArticleImageSwitcherNext, nextArticle);
        } else { // moving left to previous article
            PicassoImageSwitcher temp = mArticleImageSwitcherNext;
            mArticleImageSwitcherNext = mArticleImageSwitcherCurr;
            mArticleImageSwitcherCurr = mArticleImageSwitcherPrev;
            mArticleImageSwitcherPrev = temp;
            setImage(mArticleImageSwitcherPrev, prevArticle);
        }
        mArticleImageSwitcherCurr.getImageSwitcher().setAlpha(1f);
        mArticleImageSwitcherPrev.getImageSwitcher().setAlpha(0f);
        mArticleImageSwitcherNext.getImageSwitcher().setAlpha(0f);
    }

    private void setImage(PicassoImageSwitcher imageSwitcher, Article article) {
        imageSwitcher.getImageSwitcher().setImageDrawable(null);
        Picasso.with(getApplicationContext()).load(article.getImageUrl()).into(imageSwitcher);
    }

    private void initViewReferences() {
        ImageSwitcher imageSwitcher1 = (ImageSwitcher) findViewById(R.id.article_image_switcher_1);
        ImageSwitcher imageSwitcher2 = (ImageSwitcher) findViewById(R.id.article_image_switcher_2);
        ImageSwitcher imageSwitcher3 = (ImageSwitcher) findViewById(R.id.article_image_switcher_3);
        mArticleImageSwitcherCurr = new PicassoImageSwitcher(getApplicationContext(), imageSwitcher1);
        mArticleImageSwitcherPrev = new PicassoImageSwitcher(getApplicationContext(), imageSwitcher2);
        mArticleImageSwitcherNext = new PicassoImageSwitcher(getApplicationContext(), imageSwitcher3);
        mArticlePager = (ViewPager) findViewById(R.id.article_pager);
        mFadeView = findViewById(R.id.fade_view);
        mNavbar = (ResoundNavBar) findViewById(R.id.navbar);
        mNavbar.setText("temp text");
    }

    private void updateImageFades(PicassoImageSwitcher nextImageSwitcher, float offset) {
        nextImageSwitcher.getImageSwitcher().setAlpha(offset);
        mArticleImageSwitcherCurr.getImageSwitcher().setAlpha(1 - offset);
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
                articleFragment.setOnFlung(new OnFlingListener() {
                    @Override
                    public void onFlung(int velocityY) {
                        if (velocityY < 0) {
                            new CountDownTimer(100, 5) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    float maxOffset = (float) ViewCalculator.dpToPX(MAX_ALPHA_OFFSET_DP);
                                    updateFadeY((int) (maxOffset * (millisUntilFinished / 350f)));
                                }

                                @Override
                                public void onFinish() {
                                    updateFadeY(0);
                                }
                            }.start();
                        }
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
