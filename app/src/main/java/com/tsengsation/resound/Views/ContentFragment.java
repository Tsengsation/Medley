package com.tsengsation.resound.Views;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ViewSwitcher.ViewFactory;

import com.squareup.picasso.Picasso;
import com.tsengsation.resound.Parse.Article;
import com.tsengsation.resound.Parse.ArticleIndexOutOfBoundsException;
import com.tsengsation.resound.Parse.ParseResound;
import com.tsengsation.resound.PicassoHelper.PicassoImageSwitcher;
import com.tsengsation.resound.R;
import com.tsengsation.resound.ViewHelpers.ObservableScrollView.OnFlingListener;
import com.tsengsation.resound.ViewHelpers.ObservableScrollView.OnScrolledListener;
import com.tsengsation.resound.ViewHelpers.ResoundNavBar;
import com.tsengsation.resound.ViewHelpers.ResoundNavBar.NavButtonClickListener;
import com.tsengsation.resound.ViewHelpers.ViewCalculator;


public class ContentFragment extends Fragment implements ViewFactory, OnPageChangeListener,
        DrawerListener, NavButtonClickListener {

    private final static float MAX_ALPHA = 0.6f;
    private final static int MAX_ALPHA_OFFSET_DP = 80;

    private PicassoImageSwitcher mArticleImageSwitcherCurr;
    private PicassoImageSwitcher mArticleImageSwitcherPrev;
    private PicassoImageSwitcher mArticleImageSwitcherNext;
    private ViewPager mArticlePager;
    private ArticlePagerAdapter mArticlePagerAdapter;
    private View mFadeView;
    private RelativeLayout mForegroundLayout;
    private ResoundNavBar mNavbar;

    private ParseResound mParseResound;
    private int mCurrPosition;
    private float mCurrFadeY;
    private String mArticleType;
    private int mArticleTypeCode;
    private DrawerLayout mDrawer;

    public static ContentFragment newInstance(String articleType, int type, DrawerLayout drawer) {
        ContentFragment fragment = new ContentFragment();
        fragment.mArticleType = articleType;
        fragment.mArticleTypeCode = type;
        fragment.mDrawer = drawer;
        return fragment;
    }

    public void updateArticles(String articleType, int type) {
        mArticleType = articleType;
        mArticleTypeCode = type;
        mNavbar.setText(mArticleType);
        mParseResound.filterArticles(mArticleTypeCode);
        mArticlePagerAdapter.notifyDataSetChanged();
        mArticlePager.setCurrentItem(0);
        if (mCurrPosition == 0) {
            initImages();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_content, container, false);
        mParseResound = ParseResound.getInstance();
        initViewReferences(view);
        setUpSwipes();
        setUpImageSwitching();
        initImages();
        updateArticles(mArticleType, mArticleTypeCode);
        return view;
    }

    private void setUpImageSwitching() {
        mArticleImageSwitcherCurr.getImageSwitcher().setFactory(this);
        mArticleImageSwitcherPrev.getImageSwitcher().setFactory(this);
        mArticleImageSwitcherNext.getImageSwitcher().setFactory(this);
    }

    @Override
    public View makeView() {
        ImageView imageView = new ImageView(getActivity());
        imageView.setBackgroundColor(0xFFFFFFFF);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new ImageSwitcher.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        return imageView;
    }

    private void setUpSwipes() {
        mArticlePagerAdapter = new ArticlePagerAdapter(getActivity().getSupportFragmentManager());
        mArticlePager.setAdapter(mArticlePagerAdapter);
        mArticlePager.setOffscreenPageLimit(0);
        mArticlePager.setOnPageChangeListener(this);
    }

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
        // do nothing
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        float moveFactor = (drawerView.getWidth() * slideOffset);
        mForegroundLayout.setTranslationX(moveFactor);
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        // do nothing
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        // do nothing
    }

    @Override
    public void onDrawerStateChanged(int newState) {
        // do nothing
    }

    @Override
    public void onButtonClick() {
        mDrawer.openDrawer(Gravity.LEFT);
    }

    private void initImages() {
        Article currArticle = ((ArticleFragment) mArticlePagerAdapter.getItem(0)).getArticle();
        Article nextArticle = mParseResound.getNumArticles() > 1
                ? mParseResound.getArticle(1) : currArticle;
        Article prevArticle = currArticle;
        mArticleImageSwitcherCurr.getImageSwitcher().setAlpha(1f);
        mArticleImageSwitcherPrev.getImageSwitcher().setAlpha(0f);
        mArticleImageSwitcherNext.getImageSwitcher().setAlpha(0f);
        setImage(mArticleImageSwitcherCurr, currArticle);
        setImage(mArticleImageSwitcherPrev, prevArticle);
        setImage(mArticleImageSwitcherNext, nextArticle);
    }

    private void updateImages(int position) {
        Article currArticle = mParseResound.getArticle(position);
                ((ArticleFragment) mArticlePagerAdapter.getItem(position)).getArticle();
        Article prevArticle = position > 0 ? mParseResound.getArticle(position - 1) : currArticle;
        Article nextArticle = position < mParseResound.getNumArticles() - 1
                ? mParseResound.getArticle(position + 1) : currArticle;
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
        Picasso.with(getActivity().getApplicationContext())
                .load(article.imageUrl)
                .into(imageSwitcher);
    }

    private void initViewReferences(View view) {
        ImageSwitcher imageSwitcher1 = (ImageSwitcher) view.findViewById(R.id.article_image_switcher_1);
        ImageSwitcher imageSwitcher2 = (ImageSwitcher) view.findViewById(R.id.article_image_switcher_2);
        ImageSwitcher imageSwitcher3 = (ImageSwitcher) view.findViewById(R.id.article_image_switcher_3);
        mArticleImageSwitcherCurr =
                new PicassoImageSwitcher(getActivity().getApplicationContext(), imageSwitcher1);
        mArticleImageSwitcherPrev =
                new PicassoImageSwitcher(getActivity().getApplicationContext(), imageSwitcher2);
        mArticleImageSwitcherNext =
                new PicassoImageSwitcher(getActivity().getApplicationContext(), imageSwitcher3);
        mArticlePager = (ViewPager) view.findViewById(R.id.article_pager);
        mFadeView = view.findViewById(R.id.fade_view);
        mForegroundLayout = (RelativeLayout) view.findViewById(R.id.foreground_content_layout);
        mNavbar = (ResoundNavBar) view.findViewById(R.id.navbar);
        mNavbar.setOnButtonClick(this);
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
        float maxOffset = (float) ViewCalculator.dpToPX(getActivity(), MAX_ALPHA_OFFSET_DP);
        if (yScroll > maxOffset) {
            alpha = MAX_ALPHA;
        } else {
            alpha = MAX_ALPHA * yScroll / maxOffset;
        }
        mFadeView.setAlpha(alpha);
        mCurrFadeY = alpha;
    }

    public class ArticlePagerAdapter extends FragmentStatePagerAdapter implements
            OnScrolledListener, OnFlingListener {

        public ArticlePagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public int getItemPosition(Object object){
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public int getCount() {
            return ParseResound.getInstance().getNumArticles();
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            try {
                fragment = ArticleFragment.newInstance(
                        ParseResound.getInstance().getArticle(position));
                final ArticleFragment articleFragment = (ArticleFragment) fragment;
                articleFragment.setOnScrolled(this);
                articleFragment.setOnFlung(this);
            } catch (ArticleIndexOutOfBoundsException e) {
                Log.e("Article Swipe", e.getMessage());
            } finally {
                return fragment;
            }
        }

        @Override
        public void onScrolled(int oldY, int newY) {
            updateFadeY(newY);
        }

        @Override
        public void onFlung(int velocityY) {
            if (velocityY < 0) {
                new CountDownTimer(100, 5) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        float maxOffset = (float) ViewCalculator.dpToPX(
                                getActivity().getApplicationContext(), MAX_ALPHA_OFFSET_DP);
                        updateFadeY((int) (maxOffset * (millisUntilFinished / 350f)));
                    }

                    @Override
                    public void onFinish() {
                        updateFadeY(0);
                    }
                }.start();
            }
        }
    }

}