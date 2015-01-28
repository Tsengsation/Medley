package com.tsengsation.resound.Views;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseAnalytics;
import com.tsengsation.resound.Events.OnDownloadCompletedListener;
import com.tsengsation.resound.Events.OnImageLoadedListener;
import com.tsengsation.resound.Parse.Article;
import com.tsengsation.resound.Parse.ParseResound;
import com.tsengsation.resound.PicassoHelper.CircleTransformation;
import com.tsengsation.resound.R;


public class MainActivity extends Activity {

    private ParseResound mParseResound;

    private ImageView mAuthorImageView;
    private ImageView mArticleImageView;
    private TextView mAuthorName;
    private TextView mArticleTitle;
    private TextView mArticleText;
    private TextView mArticleDate;
    private LinearLayout mArticleLayout;
    private LinearLayout mArticleTitleLayout;
    private LinearLayout mArticleAuthorLayout;

    // TODO remove
    private Button mNextButton;
    private Button mPreviousButton;

    private double pxToDP(double px) {
        return getApplicationContext().getResources().getDisplayMetrics().density * px;
    }

    private double dpToPX(double dp) {
        return dp / (getApplicationContext().getResources().getDisplayMetrics().density);
    }

    private void setUpView() {
        // TODO: need to wait for ParseResound to be ready before enabling buttons
        // probably load screen
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadArticle(mParseResound.getNextArticle());
            }
        });
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadArticle(mParseResound.getPreviousArticle());
            }
        });
    }

    private void loadArticle(Article article) {
        mAuthorName.setText("by " + article.getAuthor().getName());
        mArticleTitle.setText(article.getTitle());
        mArticleText.setText(article.getText());
        mArticleDate.setText(article.getDate());

        MultiImageLoader multiImageLoader = new MultiImageLoader(getApplicationContext());
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
                                Display display = getWindowManager().getDefaultDisplay();
                                Point size = new Point();
                                display.getSize(size);
                                int height = size.y;
                                int offset = mArticleTitleLayout.getMeasuredHeight() + mArticleAuthorLayout.getMeasuredHeight();
                                mArticleLayout.setPadding(0, height - offset, 0, (int) dpToPX(5));
                            }
                        });
                    }
                });
            }
        });
        multiImageLoader.attachImages(
                new ImageUrlViewPair(article.getImageUrl(), mArticleImageView),
                new ImageUrlViewPair(article.getAuthor().getImageUrl(), mAuthorImageView, new CircleTransformation()));
    }

    private void initializeViewReferences() {
        mAuthorImageView = (ImageView) findViewById(R.id.author_image);
        mArticleImageView = (ImageView) findViewById(R.id.article_image);
        mAuthorName = (TextView) findViewById(R.id.author_name);
        mArticleTitle = (TextView) findViewById(R.id.article_title);
        mArticleText = (TextView) findViewById(R.id.article_text);
        mArticleDate = (TextView) findViewById(R.id.article_date);
        mArticleLayout = (LinearLayout) findViewById(R.id.article_card);
        mArticleTitleLayout = (LinearLayout) findViewById(R.id.article_header);
        mArticleAuthorLayout = (LinearLayout) findViewById(R.id.article_info);

        // TODO remove
        mNextButton = (Button) findViewById(R.id.next);
        mPreviousButton = (Button) findViewById(R.id.prev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
        mParseResound = ParseResound.getInstance();

        mParseResound.setOnDownloadCompleted(new OnDownloadCompletedListener() {
            @Override
            public void onSuccess() {
                loadArticle(mParseResound.getCurrentArticle());
            }

            @Override
            public void onFail() {
                Toast.makeText(getApplicationContext(), "download failed", Toast.LENGTH_LONG).show();
            }
        });

        mParseResound.downloadData();
        initializeViewReferences();
        setUpView();
    }

}
