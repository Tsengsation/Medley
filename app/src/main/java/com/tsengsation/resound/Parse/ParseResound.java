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
import com.tsengsation.resound.Events.OnDownloadCompletedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jonathantseng on 1/21/15.
 */
public class ParseResound extends Application {

    private static final String ARTICLE_TABLE_KEY = "Article";
    private static final String AUTHOR_TABLE_KEY = "Author";

    private static final String ARTICLE_DATE_KEY = "articleDate";
    private static final String ARTICLE_IMAGE_KEY = "articleImage";
    private static final String ARTICLE_TEXT_KEY = "articleText";
    private static final String ARTICLE_TITLE_KEY = "articleTitle";
    private static final String ARTICLE_AUTHOR_ID_KEY = "authorID";
    private static final String ARTICLE_TYPE_KEY = "category";
    private static final String AUTHOR_IMAGE_KEY = "image";
    private static final String AUTHOR_NAME_KEY = "name";

    private static ParseResound mInstance = null;
    private OnDownloadCompletedListener mDownloadCompletedListener;
    private List<Article> mArticles;
    private List<Article> mFilteredArticles;
    private Map<String, Author> mAuthorsMap;
    private int mArticleIndex;
    private ParseQuery mCurrentQuery;

    /**
     * DO NOT EVER CALL THIS:
     * Public in order for android to compile and run
     */
    public ParseResound() {
        mArticles = new ArrayList<>();
        mFilteredArticles = new ArrayList<>();
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

    public Article getCurrentArticle() {
        return (mFilteredArticles.size() > mArticleIndex) ? mFilteredArticles.get(mArticleIndex) : mFilteredArticles.get(mFilteredArticles.size() - 1);
    }

    public Article getNextArticle() {
        mArticleIndex = (mArticleIndex < mFilteredArticles.size() - 1) ? mArticleIndex + 1 : mFilteredArticles.size() - 1;
        return getCurrentArticle();
    }

    public Article getPreviousArticle() {
        mArticleIndex = (mArticleIndex > 0) ? mArticleIndex - 1 : 0;
        return getCurrentArticle();
    }

    public void filterArticles(ArticleType type) {
        // todo filter mFilteredArticles
        mArticleIndex = 0;
    }

    private void pullAllAuthors() {
        mAuthorsMap = new HashMap<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(AUTHOR_TABLE_KEY);
        mCurrentQuery = query;
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> authors, ParseException e) {
                if (e == null) {
                    Log.d("Authors", "Retrieved " + authors.size() + " authors");
                    for (ParseObject authorObj : authors) {
                        Log.d("work pls", "aaa " + authorObj.getString("image"));
                        Author author = new Author(
                                authorObj.getParseFile(AUTHOR_IMAGE_KEY).getUrl(),
                                authorObj.getString(AUTHOR_NAME_KEY));
                        mAuthorsMap.put(authorObj.getObjectId(), author);
                    }
                    pullAllArticles();
                } else {
                    Log.d("Authors", "Error: " + e.getMessage());
                    mDownloadCompletedListener.onFail();
                }
            }
        });
    }

    private void pullAllArticles() {
        mArticles = new ArrayList<>();
        mFilteredArticles = new ArrayList<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ARTICLE_TABLE_KEY);
        mCurrentQuery = query;
        query.orderByAscending(ARTICLE_DATE_KEY);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> articles, ParseException e) {
                if (e == null) {
                    Log.d("Articles", "Retrieved " + articles.size() + " articles");
                    for (ParseObject articleObj : articles) {
                        Article article = new Article(
                                ArticleType.createType(articleObj.getString(ARTICLE_TYPE_KEY)),
                                articleObj.getParseFile(ARTICLE_IMAGE_KEY).getUrl(),
                                articleObj.getString(ARTICLE_DATE_KEY),
                                articleObj.getString(ARTICLE_TEXT_KEY),
                                articleObj.getString(ARTICLE_TITLE_KEY),
                                mAuthorsMap.get(articleObj.getString(ARTICLE_AUTHOR_ID_KEY)));
                        mArticles.add(article);
                    }
                    mFilteredArticles = mArticles;
                    mArticleIndex = 0;
                    mCurrentQuery = null;
                    mDownloadCompletedListener.onSuccess();
                } else {
                    Log.d("Articles", "Error: " + e.getMessage());
                    mDownloadCompletedListener.onFail();
                }
            }
        });
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
        Parse.initialize(this, "gqrp4017xBh0MVxkZ7RdbTZJkOhxjGF2QKVxMqCm", "uKREO45z7PlpPp72MUp3XUKvHPt8K3MQuwD2VyU8");

        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(false);
        ParseACL.setDefaultACL(defaultACL, true);
    }

}