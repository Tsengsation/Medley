package com.tsengsation.resound.Parse;

import android.app.Application;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton class that represents Application and mediator for all parse calls.
 */
public class ParseResound extends Application {

    private static final String ARTICLE_TABLE_KEY = "Article";
    private static final String AUTHOR_TABLE_KEY = "Author";

    private static final String ARTICLE_DATE_KEY = "articleDate";
    private static final String ARTICLE_IMAGE_KEY = "articleImage";
    private static final String ARTICLE_IMAGE_SOURCENAME_KEY = "articleImageSourceName";
    private static final String ARTICLE_IMAGE_SOURCEURL_KEY = "articleImageSourceURL";
    private static final String ARTICLE_TEXT_KEY = "articleText";
    private static final String ARTICLE_TITLE_KEY = "articleTitle";
    private static final String ARTICLE_URL_KEY = "articleURL";
    private static final String ARTICLE_AUTHOR_ID_KEY = "authorID";
    private static final String ARTICLE_TYPE_KEY = "category";
    private static final String ARTICLE_LIKES_KEY = "numLikes";

    private static final String AUTHOR_IMAGE_KEY = "image";
    private static final String AUTHOR_NAME_KEY = "name";

    private static ParseResound mInstance = null;
    private OnDownloadCompletedListener mDownloadCompletedListener;
    private Map<String, Author> mAuthorsMap;
    private ArticleLibrary mArticleLibrary;
    private ParseQuery mCurrentQuery;

    private final FindCallback<ParseObject> AUTHOR_CALLBACK = new FindCallback<ParseObject>() {
        public void done(List<ParseObject> authors, ParseException e) {
            if (e == null) {
                Log.d("Authors", "Retrieved " + authors.size() + " authors");
                for (ParseObject authorObj : authors) {
                    Author author = new Author(
                            authorObj.getParseFile(AUTHOR_IMAGE_KEY).getUrl(),
                            authorObj.getString(AUTHOR_NAME_KEY));
                    mAuthorsMap.put(authorObj.getObjectId(), author);
                }
                pullAllArticles();
            } else {
                Log.d("Authors", "Error: " + e.getMessage());
                if (mDownloadCompletedListener != null) {
                    mDownloadCompletedListener.onFail();
                }
            }
        }
    };

    private final FindCallback<ParseObject> ARTICLE_CALLBACK = new FindCallback<ParseObject>() {
        public void done(List<ParseObject> articles, ParseException e) {
            List<Article> foundArticles = new ArrayList<>();
            if (e == null) {
                Log.d("Articles", "Retrieved " + articles.size() + " articles");
                for (ParseObject articleObj : articles) {
                    Article article = new Article(
                            articleObj.getString(ARTICLE_TYPE_KEY),
                            articleObj.getParseFile(ARTICLE_IMAGE_KEY).getUrl(),
                            articleObj.getDate(ARTICLE_DATE_KEY),
                            articleObj.getString(ARTICLE_TEXT_KEY),
                            articleObj.getString(ARTICLE_TITLE_KEY),
                            articleObj.getString(ARTICLE_IMAGE_SOURCENAME_KEY),
                            articleObj.getString(ARTICLE_IMAGE_SOURCEURL_KEY),
                            articleObj.getNumber(ARTICLE_LIKES_KEY).longValue(),
                            articleObj.getString(ARTICLE_URL_KEY),
                            mAuthorsMap.get(articleObj.getString(ARTICLE_AUTHOR_ID_KEY)));
                    foundArticles.add(article);
                }
                mArticleLibrary = new ArticleLibrary(foundArticles);
                mCurrentQuery = null;
                if (mDownloadCompletedListener != null) {
                    mDownloadCompletedListener.onSuccess();
                }
            } else {
                Log.d("Articles", "Error: " + e.getMessage());
                if (mDownloadCompletedListener != null) {
                    mDownloadCompletedListener.onFail();
                }
            }
        }
    };

    /**
     * DO NOT EVER CALL THIS:
     * Public in order for android to compile and run
     */
    public ParseResound() {
        mInstance = this;
    }

    public void downloadData() {
        pullAllAuthors();
        //calls pullAllArticles, in turn calls listener
    }

    public void setOnDownloadCompleted(OnDownloadCompletedListener listener) {
        mDownloadCompletedListener = listener;
    }

    public synchronized static ParseResound getInstance() {
        if (mInstance == null) {
            mInstance = new ParseResound();
        }
        return mInstance;
    }

    public Article getArticle(int position) throws ArticleIndexOutOfBoundsException {
        return mArticleLibrary.getArticle(position);
    }

    public int getNumArticles() {
        return mArticleLibrary.getCount();
    }

    public void filterArticles(int type) {
        mArticleLibrary.filterByType(type);
    }

    private void pullAllAuthors() {
        mAuthorsMap = new HashMap<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(AUTHOR_TABLE_KEY);
        mCurrentQuery = query;
        query.findInBackground(AUTHOR_CALLBACK);
    }

    private void pullAllArticles() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ARTICLE_TABLE_KEY);
        mCurrentQuery = query;
        query.orderByAscending(ARTICLE_DATE_KEY);
        query.findInBackground(ARTICLE_CALLBACK);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        parseInit();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        mCurrentQuery.cancel();
    }

    private void parseInit() {
        // Initialize Crash Reporting.
        ParseCrashReporting.enable(this);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Add your initialization code here
        Parse.initialize(this, "gqrp4017xBh0MVxkZ7RdbTZJkOhxjGF2QKVxMqCm",
                "uKREO45z7PlpPp72MUp3XUKvHPt8K3MQuwD2VyU8");

        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(false);
        ParseACL.setDefaultACL(defaultACL, true);
    }

    /**
     * Listener for parse data query download completions.
     */
    public static interface OnDownloadCompletedListener {

        public void onSuccess();

        public void onFail();
    }
}