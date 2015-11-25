package com.tsengsation.medley.Views;

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
import com.tsengsation.medley.Parse.Article;
import com.tsengsation.medley.Parse.ParseMedley;
import com.tsengsation.medley.PicassoHelper.PicassoImageSwitcher;
import com.tsengsation.medley.R;
import com.tsengsation.medley.ViewHelpers.FontManager;
import com.tsengsation.medley.ViewHelpers.ObservableScrollView;
import com.tsengsation.medley.ViewHelpers.ObservableScrollView.OnFlingListener;
import com.tsengsation.medley.ViewHelpers.ObservableScrollView.OnScrolledListener;
import com.tsengsation.medley.ViewHelpers.NavBar;
import com.tsengsation.medley.ViewHelpers.NavBar.NavButtonClickListener;
import com.tsengsation.medley.ViewHelpers.ViewCalculator;


public class ContentFragment extends Fragment implements ViewFactory, OnPageChangeListener,
        DrawerListener, NavButtonClickListener, OnScrolledListener, OnFlingListener{

    private final static float MAX_ALPHA = 0.6f;
    private final static int MAX_ALPHA_OFFSET_DP = 80;

    private PicassoImageSwitcher mArticleImageSwitcherCurr;
    private PicassoImageSwitcher mArticleImageSwitcherPrev;
    private PicassoImageSwitcher mArticleImageSwitcherNext;
    private ViewPager mArticlePager;
    private ArticlePagerAdapter mArticlePagerAdapter;
    private View mFadeView;
    private RelativeLayout mForegroundLayout;
    private NavBar mNavbar;
    private MainActivity mActivity;

    private ParseMedley mParseMedley;
    private int mCurrPosition;
    private float mCurrFadeY;
    private String mArticleType;
    private int mArticleTypeCode;
    private boolean mIsOverlay;
    private Article mOverlayArticle;
    private CountDownTimer mFlungTimer;

    public static ContentFragment newOverlayInstance(Article article, MainActivity mainActivity) {
        ContentFragment fragment = new ContentFragment();
        fragment.mIsOverlay = true;
        fragment.mOverlayArticle = article;
        fragment.mActivity = mainActivity;
        return fragment;
    }

    public static ContentFragment newInstance(String articleType, int type,
                                              MainActivity mainActivity) {
        ContentFragment fragment = new ContentFragment();
        fragment.mArticleType = articleType;
        fragment.mArticleTypeCode = type;
        fragment.mActivity = mainActivity;
        fragment.mIsOverlay = false;
        return fragment;
    }

    public void updateArticles(String articleType, int type) {
        mArticleType = articleType;
        mArticleTypeCode = type;
        mNavbar.setFont(FontManager.PETITA_MEDIUM);
        mNavbar.setText(mArticleType);
        mParseMedley.filterArticles(mArticleTypeCode);
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
        mParseMedley = ParseMedley.getInstance();
        initViewReferences(view);
        setUpSwipes();
        setUpImageSwitching();
        initImages();
        if (mArticleType != null) {
            updateArticles(mArticleType, mArticleTypeCode);
        }
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
        mArticlePagerAdapter = mIsOverlay
                ? new OverlayArticlePagerAdapter(this, getChildFragmentManager(),
                mOverlayArticle)
                : new ArticlePagerAdapter(this, getChildFragmentManager());
        mArticlePager.setAdapter(mArticlePagerAdapter);
        mArticlePager.setOffscreenPageLimit(1);
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
        DrawerLayout drawer = mActivity.getDrawerLayout();
        if (!mIsOverlay && drawer != null) {
            drawer.openDrawer(Gravity.LEFT);
        } else if (mIsOverlay) {
            mActivity.closeOverlay();
        }
    }

    private void initImages() {
        Article currArticle = ((ArticleFragment) mArticlePagerAdapter.getItem(0)).getArticle();
        Article nextArticle = mParseMedley.getNumArticles() > 1
                ? mParseMedley.getArticle(1) : currArticle;
        Article prevArticle = currArticle;
        mArticleImageSwitcherCurr.getImageSwitcher().setAlpha(1f);
        mArticleImageSwitcherPrev.getImageSwitcher().setAlpha(0f);
        mArticleImageSwitcherNext.getImageSwitcher().setAlpha(0f);
        setImage(mArticleImageSwitcherCurr, currArticle);
        setImage(mArticleImageSwitcherPrev, prevArticle);
        setImage(mArticleImageSwitcherNext, nextArticle);
    }

    private void updateImages(int position) {
        Article currArticle = mParseMedley.getArticle(position);
                ((ArticleFragment) mArticlePagerAdapter.getItem(position)).getArticle();
        Article prevArticle = position > 0 ? mParseMedley.getArticle(position - 1) : currArticle;
        Article nextArticle = position < mParseMedley.getNumArticles() - 1
                ? mParseMedley.getArticle(position + 1) : currArticle;
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
        mNavbar = (NavBar) view.findViewById(R.id.navbar);
        if (mIsOverlay) {
            mNavbar.setButtonDrawable(R.drawable.xbutton);
        }
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

    @Override
    public void onScrolled(ObservableScrollView view, int oldY, int newY) {
        updateFadeY(newY);
    }

    @Override
    public void onFlung(final ObservableScrollView view, int velocityY) {
        if (mFlungTimer != null) {
            mFlungTimer.cancel();
        }
        mFlungTimer = new CountDownTimer(1000, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateFadeY(view.getScrollY());
            }

            @Override
            public void onFinish() {
                updateFadeY(view.getScrollY());
            }
        }.start();
    }

    public class OverlayArticlePagerAdapter extends ArticlePagerAdapter {

        private Article mArticle;

        public OverlayArticlePagerAdapter(ContentFragment parent, FragmentManager manager,
                                          Article article) {
            super(parent, manager);
            mArticle = article;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Fragment getItem(int position) {
            return getItem(mArticle);
        }
    }

    public class ArticlePagerAdapter extends FragmentStatePagerAdapter {

        private ContentFragment mParent;

        public ArticlePagerAdapter(ContentFragment parent, FragmentManager manager) {
            super(manager);
            mParent = parent;
        }

        @Override
        public int getItemPosition(Object object){
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public int getCount() {
            return ParseMedley.getInstance().getNumArticles();
        }

        @Override
        public Fragment getItem(int position) {
            Article article = ParseMedley.getInstance().getArticle(position);
            return getItem(article);
        }

        protected Fragment getItem(Article article) {
            ArticleFragment fragment = ArticleFragment.newInstance(article);
            fragment.setOnScrolled(mParent);
            fragment.setOnFlung(mParent);
            return fragment;
        }

    }

}
