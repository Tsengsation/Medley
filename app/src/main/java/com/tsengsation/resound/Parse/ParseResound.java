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

    /**
     * DO NOT EVER CALL THIS:
     * Public in order for android to compile and run
     */
    public ParseResound() {
        mInstance = this;
        // download completed listener that does nothing
        mDownloadCompletedListener = new OnDownloadCompletedListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFail() {

            }
        };
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

    public void filterArticles(ArticleType type) {
        mArticleLibrary.filterByType(type);
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
        final List<Article> foundArticles = new ArrayList<>();
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