package com.tsengsation.medley.Views;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tsengsation.medley.Parse.Article;
import com.tsengsation.medley.Parse.ParseMedley;
import com.tsengsation.medley.Parse.ParseMedley.OnUpdateCompletedListener;
import com.tsengsation.medley.PicassoHelper.CircleTransformation;
import com.tsengsation.medley.R;
import com.tsengsation.medley.ViewHelpers.FontManager;
import com.tsengsation.medley.ViewHelpers.ImageUrlViewPair;
import com.tsengsation.medley.ViewHelpers.MultiImageLoader;
import com.tsengsation.medley.ViewHelpers.MultiImageLoader.OnImageLoadedListener;
import com.tsengsation.medley.ViewHelpers.ObservableScrollView;
import com.tsengsation.medley.ViewHelpers.ObservableScrollView.OnFlingListener;
import com.tsengsation.medley.ViewHelpers.ObservableScrollView.OnScrolledListener;
import com.tsengsation.medley.ViewHelpers.ViewCalculator;

/**
 * Fragment that hosts an article view.
 */
public class ArticleFragment extends Fragment implements OnClickListener, OnImageLoadedListener,
        OnUpdateCompletedListener<Article> {

    // Argument bundle keys.
    private final static String KEY_ARTICLE = "key_article";

    private final static String CSS_TAG = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />";
    private final static String COVER_IMAGE_TEXT = "Cover Image Source:";
    private final static String ASSET_URL = "file:///android_asset/";
    private final static String HTML = "text/html";
    private final static String ENCODING = "UTF-8";

    private final static int SCROLL_MS = 350;
    private final static int SCROLL_ITERS = 100;
    private final static int SCROLL_WAIT = SCROLL_MS / SCROLL_ITERS;

    private ParseMedley mParseMedley;

    private ImageView mAuthorImageView;
    private TextView mAuthorName;
    private TextView mArticleTitle;
    private WebView mArticleWebView;
    private TextView mArticleDate;
    private LinearLayout mArticleLayout;
    private LinearLayout mArticleTitleLayout;
    private LinearLayout mArticleAuthorLayout;
    private ObservableScrollView mArticleScrollView;
    private TextView mLikesText;
    private ImageButton mLikesButton;
    private ImageButton mShareButton;

    private Article mArticle;
    private int mOffset;
    private OnScrolledListener mOnScrolledListener;
    private OnFlingListener mOnFlingListener;

    public static ArticleFragment newInstance(Article article) {
        ArticleFragment fragment = new ArticleFragment();

        fragment.mArticle = article;
        Bundle args = new Bundle();
        args.putParcelable(KEY_ARTICLE, article);
        fragment.setArguments(args);

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

    @Override
    public void onClick(View v) {
        // Article title bar.
        if (v.equals(mArticleTitleLayout)) {
            final int beginY = mArticleScrollView.getScrollY();
            new CountDownTimer(SCROLL_MS, SCROLL_WAIT) {
                @Override
                public void onTick(long millisUntilFinished) {
                    mArticleScrollView.smoothScrollTo(0, beginY +
                            (int) ((SCROLL_MS - millisUntilFinished)
                                    / ((float) SCROLL_MS) * (mOffset - beginY)));
                }

                @Override
                public void onFinish() {
                    mArticleScrollView.smoothScrollTo(0, mOffset);
                }
            }.start();
        } else if (v.equals(mLikesButton)) {
            if (mArticle.prevLiked) {
                mParseMedley.unlikeArticle(mArticle, this);
            } else {
                mParseMedley.likeArticle(mArticle, this);
            }
        } else if (v.equals(mShareButton)) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Medley");
            String shareMsg = String.format("Check out this article at %s",
                    mParseMedley.getShareUrl(mArticle));
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMsg);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        }
    }

    @Override
    public void onUpdateSuccess(Article updatedObject) {
        mArticle = updatedObject;
        updateLikesViews();
    }

    @Override
    public void onUpdateFail(Exception e) {
        Toast.makeText(getActivity(), "Connection failed", Toast.LENGTH_SHORT).show();
    }

    public void setOnScrolled(ObservableScrollView.OnScrolledListener listener) {
        mOnScrolledListener = listener;
    }

    public void setOnFlung(ObservableScrollView.OnFlingListener listener) {
        mOnFlingListener = listener;
    }

    private void setHtml(TextView textView, String text) {
        textView.setText(Html.fromHtml(text));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private String convertArticleToHtml(Article article) {
//        String pish = "<html><head><style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/font/BMitra.ttf\")}body {font-family: MyFont;font-size: medium;text-align: justify;}</style></head><body>";
//        String pas = "</body></html>";
//        String myHtmlString = pish + YourTxext + pas;
//        wv.loadDataWithBaseURL(null, myHtmlString, "text/html", "UTF-8", null);


        return String.format("<html> %s <body> <div> %s <hr/> <pre>%s <a href=\"%s\">%s</a></pre> </div> </body> </html>",
                CSS_TAG, article.text, COVER_IMAGE_TEXT, article.sourceUrl, article.sourceName);
    }

    private void resetScroll() {
        mArticleScrollView.scrollTo(0, 0);
    }

    private void loadArticle() {
        resetScroll();
        setHtml(mAuthorName, String.format("%s %s", "by ", mArticle.author.name));
        mArticleTitle.setText(mArticle.title);
        mArticleWebView.loadDataWithBaseURL(ASSET_URL, convertArticleToHtml(mArticle), HTML,
                ENCODING, null);
        String dateString = DateFormat.format("M/d/yyyy, h:mm a", mArticle.date).toString();
        setHtml(mArticleDate, dateString);

        MultiImageLoader multiImageLoader = new MultiImageLoader(
                getActivity().getApplicationContext());
        multiImageLoader.setOnImageLoaded(this);
        multiImageLoader.attachImages(new ImageUrlViewPair(mArticle.author.imageUrl,
                mAuthorImageView, new CircleTransformation()));
    }

    @Override
    public void onImageLoaded() {
        // TODO probably quit out of loading screen or something
        // i.e., start with loading screen
        // Initialize article offset.
        final ViewTreeObserver titleObserver = mArticleTitleLayout.getViewTreeObserver();
        final int windowHeight = ViewCalculator.getWindowHeight(getActivity());
        final int offsetPadding = (int) ViewCalculator.dpToPX(getActivity(), 48);
        final int layoutPadding = (int) ViewCalculator.dpToPX(getActivity(), 5);
        titleObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ViewTreeObserver authorObserver = mArticleAuthorLayout.getViewTreeObserver();
                authorObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        mOffset = windowHeight - (mArticleTitleLayout.getMeasuredHeight()
                                + mArticleAuthorLayout.getMeasuredHeight()) - offsetPadding;
                        mArticleLayout.setPadding(0, mOffset, 0, layoutPadding);
                    }
                });
            }
        });
    }



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_article, null);
        if (savedInstanceState != null && savedInstanceState.getParcelable(KEY_ARTICLE) != null) {
            mArticle = savedInstanceState.getParcelable(KEY_ARTICLE);
        }
        mParseMedley = ParseMedley.getInstance();
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
        mLikesText = (TextView) view.findViewById(R.id.likes_text);
        mLikesButton = (ImageButton) view.findViewById(R.id.likes_button);
        mShareButton = (ImageButton) view.findViewById(R.id.share_button);
    }

    private void setUpView() {
        mArticleScrollView.setOnScrolled(mOnScrolledListener);
        mArticleScrollView.setOnFlung(mOnFlingListener);
        mArticleWebView.setBackgroundColor(Color.TRANSPARENT);
        mArticleTitleLayout.setOnClickListener(this);
        loadArticle();
        mArticleLayout.setPadding(0, ViewCalculator.getWindowHeight(getActivity()), 0,
                (int) ViewCalculator.dpToPX(getActivity(), 5));
        updateLikesViews();
        mLikesButton.setOnClickListener(this);
        mShareButton.setOnClickListener(this);
        FontManager.setFont(getActivity(), mLikesText, FontManager.ARTICLE_FONT_BOLD);
        FontManager.setFont(getActivity(), mArticleTitle, FontManager.ARTICLE_FONT_BOLD);
        FontManager.setFont(getActivity(), mAuthorName, FontManager.ARTICLE_FONT_BOLD);
        FontManager.setFont(getActivity(), mArticleDate, FontManager.ARTICLE_FONT);
        // TODO: need to change to textview?
//        FontManager.setFont(getActivity(), mArticleText, FontManager.ARTICLE_FONT);
    }

    private void updateLikesViews() {
        Drawable likeDrawable = mArticle.prevLiked
                ? getResources().getDrawable(R.drawable.ic_favorite_fill)
                : getResources().getDrawable(R.drawable.ic_favorite_border);
        mLikesButton.setImageDrawable(likeDrawable);
        if (mArticle.numLikes > 0) {
            mLikesText.setText(Long.toString(mArticle.numLikes));
        } else {
            mLikesText.setText("");
        }
    }
}
