package com.tsengsation.resound.Views;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tsengsation.resound.Events.OnFlingListener;
import com.tsengsation.resound.Events.OnImageLoadedListener;
import com.tsengsation.resound.Events.OnScrolledListener;
import com.tsengsation.resound.Parse.Article;
import com.tsengsation.resound.Parse.ParseResound;
import com.tsengsation.resound.PicassoHelper.CircleTransformation;
import com.tsengsation.resound.R;
import com.tsengsation.resound.ViewHelpers.ImageUrlViewPair;
import com.tsengsation.resound.ViewHelpers.MultiImageLoader;
import com.tsengsation.resound.ViewHelpers.ObservableScrollView;
import com.tsengsation.resound.ViewHelpers.ViewCalculator;

/**
 * Created by jonathantseng on 1/29/15.
 */
public class ArticleFragment extends Fragment {

    private final static String CSS_TAG = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />";
    private final static String COVER_IMAGE_TEXT = "Cover Image Source:";
    private final static String ASSET_URL = "file:///android_asset/";
    private final static String HTML = "text/html";
    private final static String ENCODING = "UTF-8";

    private final static int SCROLL_MS = 350;
    private final static int SCROLL_ITERS = 100;
    private final static int SCROLL_WAIT = SCROLL_MS / SCROLL_ITERS;

    private ParseResound mParseResound;

    private ImageView mAuthorImageView;
    private TextView mAuthorName;
    private TextView mArticleTitle;
    private WebView mArticleWebView;
    private TextView mArticleDate;
    private LinearLayout mArticleLayout;
    private LinearLayout mArticleTitleLayout;
    private LinearLayout mArticleAuthorLayout;
    private ObservableScrollView mArticleScrollView;

    private Context mContext;
    private Article mArticle;
    private int mOffset;
    private OnScrolledListener mOnScrolledListener;
    private OnFlingListener mOnFlingListener;

    public static ArticleFragment newInstance(Context context, Article article) {
        ArticleFragment fragment = new ArticleFragment();
        fragment.setContext(context);
        fragment.setArticle(article);
        return fragment;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser && mArticleScrollView != null) {
            mArticleScrollView.removeOnScrolled();
            resetScroll();
            mArticleScrollView.setOnScrolled(mOnScrolledListener);
        }
    }

    public Article getArticle() {
        return mArticle;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public void setArticle(Article article) {
        mArticle = article;
    }

    public void setOnScrolled(OnScrolledListener listener) {
        mOnScrolledListener = listener;
    }

    public void setOnFlung(OnFlingListener listener) {
        mOnFlingListener = listener;
    }

    private void setUpView() {
        mArticleScrollView.setOnScrolled(mOnScrolledListener);
        mArticleScrollView.setOnFlung(mOnFlingListener);
        mArticleWebView.setBackgroundColor(Color.TRANSPARENT);
        mArticleTitleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mArticleScrollView.getScrollY() < 5) {
                    new CountDownTimer(SCROLL_MS, SCROLL_WAIT) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            mArticleScrollView.smoothScrollTo(0, (int) ((SCROLL_MS - millisUntilFinished) / ((float) SCROLL_MS) * mOffset));
                        }

                        @Override
                        public void onFinish() {
                            mArticleScrollView.smoothScrollTo(0, mOffset);
                        }
                    }.start();
                }
            }
        });
        loadArticle();
        mArticleLayout.setPadding(0, ViewCalculator.getWindowHeight(), 0, (int) ViewCalculator.dpToPX(5));
    }

    public void setHtml(TextView textView, String text) {
        textView.setText(Html.fromHtml(text));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public String convertArticleToHtml(Article article) {
        return String.format("<html> %s <body> <div> %s <hr/> <pre>%s <a href=\"%s\">%s</a></pre> </div> </body> </html>",
                CSS_TAG, article.getText(), COVER_IMAGE_TEXT, article.getImageSourceUrl(), article.getImageSourceName());
    }

    private void resetScroll() {
        mArticleScrollView.scrollTo(0, 0);
    }

    public void loadArticle() {
        resetScroll();
        setHtml(mAuthorName, String.format("%s %s", "by ", mArticle.getAuthor().getName()));
        mArticleTitle.setText(mArticle.getTitle());
        mArticleWebView.loadDataWithBaseURL(ASSET_URL, convertArticleToHtml(mArticle), HTML, ENCODING, null);
        String dateString = DateFormat.format("M/d/yyyy, h:mm a", mArticle.getDate()).toString();
        setHtml(mArticleDate, dateString);

        MultiImageLoader multiImageLoader = new MultiImageLoader(mContext);
        multiImageLoader.setOnImageLoaded(new OnImageLoadedListener() {
            @Override
            public void onSuccess() {
                // TODO probably quit out of loading screen or something
                // i.e., start with loading screen
                final ViewTreeObserver titleObserver = mArticleTitleLayout.getViewTreeObserver();
                titleObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        final ViewTreeObserver authorObserver = mArticleAuthorLayout.getViewTreeObserver();
                        authorObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                mOffset = ViewCalculator.getWindowHeight() - (mArticleTitleLayout.getMeasuredHeight() + mArticleAuthorLayout.getMeasuredHeight())
                                        - (int) ViewCalculator.dpToPX(26);
                                mArticleLayout.setPadding(0, mOffset, 0, (int) ViewCalculator.dpToPX(5));
                            }
                        });
                    }
                });
            }
        });
        multiImageLoader.attachImages(
                new ImageUrlViewPair(mArticle.getAuthor().getImageUrl(), mAuthorImageView, new CircleTransformation()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article, null);
        mParseResound = ParseResound.getInstance();
        initViewReferences(view);
        setUpView();
        return view;
    }

    private void initViewReferences(View view) {
        mAuthorImageView = (ImageView) view.findViewById(R.id.author_image);
        mAuthorName = (TextView) view.findViewById(R.id.author_name);
        mArticleTitle = (TextView) view.findViewById(R.id.article_title);
        mArticleWebView = (WebView) view.findViewById(R.id.article_webview);
        mArticleDate = (TextView) view.findViewById(R.id.article_date);
        mArticleLayout = (LinearLayout) view.findViewById(R.id.article_card);
        mArticleTitleLayout = (LinearLayout) view.findViewById(R.id.article_header);
        mArticleAuthorLayout = (LinearLayout) view.findViewById(R.id.article_info);
        mArticleScrollView = (ObservableScrollView) view.findViewById(R.id.article_scroll);
    }
}
