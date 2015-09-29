package com.tsengsation.resound.Parse;

import android.app.Application;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton class that represents Application and mediator for all parse calls.
 */
public class ParseResound extends Application {

    private static final String ARTICLE_TABLE_KEY = "Article";
    private static final String AUTHOR_TABLE_KEY = "Author";
    private static final String DEVICES_TABLE_KEY = "AndroidDevices";

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

    private static final String DEVICES_UID_KEY = "deviceUid";
    private static final String DEVICES_ARTICLES_KEY = "likedArticles";

    private static ParseResound mInstance = null;
    private OnDownloadCompletedListener mDownloadCompletedListener;
    private Map<String, Author> mAuthorsMap;
    private ParseObject mParseDevice;
    private ArticleLibrary mArticleLibrary;
    private ParseQuery mCurrentQuery;

    private final FindCallback<ParseObject> GET_AUTHORS_CALLBACK = new FindCallback<ParseObject>() {
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
                    mDownloadCompletedListener.onDownloadFail();
                }
            }
        }
    };

    private final FindCallback<ParseObject> GET_ARTICLES_CALLBACK = new FindCallback<ParseObject>() {
        public void done(List<ParseObject> parseArticles, ParseException e) {
            List<Article> foundArticles = new ArrayList<>();
            Map<String, ParseObject> parseArticleMap = new HashMap<>();
            if (e == null) {
                Log.d("Articles", "Retrieved " + parseArticles.size() + " articles");
                for (ParseObject articleObj : parseArticles) {
                    Article article = new Article(
                            articleObj.getObjectId(),
                            articleObj.getString(ARTICLE_TYPE_KEY),
                            articleObj.getParseFile(ARTICLE_IMAGE_KEY).getUrl(),
                            articleObj.getDate(ARTICLE_DATE_KEY),
                            articleObj.getString(ARTICLE_TEXT_KEY),
                            articleObj.getString(ARTICLE_TITLE_KEY),
                            articleObj.getString(ARTICLE_IMAGE_SOURCENAME_KEY),
                            articleObj.getString(ARTICLE_IMAGE_SOURCEURL_KEY),
                            articleObj.getNumber(ARTICLE_LIKES_KEY).longValue(),
                            articleObj.getString(ARTICLE_URL_KEY),
                            mAuthorsMap.get(articleObj.getString(ARTICLE_AUTHOR_ID_KEY)),
                            false /* previously liked */);
                    foundArticles.add(article);
                    parseArticleMap.put(articleObj.getObjectId(), articleObj);
                }
                mArticleLibrary = new ArticleLibrary(parseArticleMap, foundArticles);
                pullLikedArticles();
            } else {
                Log.d("Articles", "Error: " + e.getMessage());
                if (mDownloadCompletedListener != null) {
                    mDownloadCompletedListener.onDownloadFail();
                }
            }
        }
    };

    private final GetCallback<ParseObject> GET_LIKED_ARTICLES_CALLBACK = new GetCallback<ParseObject>() {
        @Override
        public void done(ParseObject parseObject, ParseException e) {
            if (e == null || e.getMessage().contains("no results found for query")) {
                if (parseObject == null) {
                    Log.d("Devices", "No history of liked articles found for this device.");
                    mParseDevice = new ParseObject(DEVICES_TABLE_KEY);
                    mParseDevice.put(DEVICES_UID_KEY, Installation.id(ParseResound.this));
                } else {
                    Log.d("Devices", "Found history of liked articles.");
                    mParseDevice = parseObject;
                }
                mCurrentQuery = null;
                if (mDownloadCompletedListener != null) {
                    mDownloadCompletedListener.onDownloadSuccess();
                }
            } else {
                Log.d("Liked articles", "Error: " + e.getMessage());
                if (mDownloadCompletedListener != null) {
                    mDownloadCompletedListener.onDownloadFail();
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
        // Calls pullAllArticles, in turn calls pullLikedArticles, in turn calls listener.
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

    public void likeArticle(Article article, OnUpdateCompletedListener<Article> updateListener) {
        Article updatedArticle = new Article(article.id, article.type, article.imageUrl,
                article.date, article.text, article.title, article.sourceName,
                article.sourceUrl, article.numLikes + 1, article.url, article.author,
                !article.prevLiked);
        mParseDevice.addUnique(DEVICES_ARTICLES_KEY, article.id);
        updateArticle(article, updatedArticle, updateListener);
    }

    public void unlikeArticle(Article article, OnUpdateCompletedListener<Article> updateListener) {
        Article updatedArticle = new Article(article.id, article.type, article.imageUrl,
                article.date, article.text, article.title, article.sourceName,
                article.sourceUrl, article.numLikes - 1, article.url, article.author,
                !article.prevLiked);
        mParseDevice.removeAll(DEVICES_ARTICLES_KEY, Arrays.asList(article.id));
        updateArticle(article, updatedArticle, updateListener);
    }

    private void updateArticle(Article article, final Article updatedArticle,
                              final OnUpdateCompletedListener<Article> updateListener) {
        ParseObject parseArticle = mArticleLibrary.getParseArticleObject(article.id);
        if (article.numLikes != updatedArticle.numLikes) {
            parseArticle.put(ARTICLE_LIKES_KEY, updatedArticle.numLikes);
            parseArticle.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        mArticleLibrary.updateArticle(updatedArticle);
                        updateLikedArticles(updatedArticle, updateListener);
                    } else {
                        updateListener.onUpdateFail(e);
                    }
                }
            });
        }
    }

    public void updateLikedArticles(final Article updatedArticle,
                                    final OnUpdateCompletedListener updateListener) {
        mParseDevice.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    updateListener.onUpdateSuccess(updatedArticle);
                } else {
                    updateListener.onUpdateFail(e);
                }
            }
        });
    }

    public Article getArticle(int position) throws ArticleIndexOutOfBoundsException {
        Article article = mArticleLibrary.getArticle(position);
        if (mParseDevice.get(DEVICES_ARTICLES_KEY) != null
                && mParseDevice.getList(DEVICES_ARTICLES_KEY).contains(article.id)) {
            Article updatedArticle = new Article(article.id, article.type, article.imageUrl,
                    article.date, article.text, article.title, article.sourceName,
                    article.sourceUrl, article.numLikes, article.url, article.author, true);
            mArticleLibrary.updateArticle(updatedArticle);
        }
        return mArticleLibrary.getArticle(position);
    }

    public int getNumArticles() {
        return mArticleLibrary.getCount();
    }

    public void filterArticles(int type) {
        mArticleLibrary.filterByType(type);
    }

    private void pullLikedArticles() {
        String deviceUid = Installation.id(this);
        ParseQuery<ParseObject> query = ParseQuery.getQuery(DEVICES_TABLE_KEY);
        mCurrentQuery = query;
        query.whereEqualTo(DEVICES_UID_KEY, deviceUid);

        query.getFirstInBackground(GET_LIKED_ARTICLES_CALLBACK);
    }

    private void pullAllAuthors() {
        mAuthorsMap = new HashMap<>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(AUTHOR_TABLE_KEY);
        mCurrentQuery = query;
        query.findInBackground(GET_AUTHORS_CALLBACK);
    }

    private void pullAllArticles() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(ARTICLE_TABLE_KEY);
        mCurrentQuery = query;
        query.orderByAscending(ARTICLE_DATE_KEY);
        query.findInBackground(GET_ARTICLES_CALLBACK);
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
        ParseUser.getCurrentUser().saveInBackground();
        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }

    /**
     * Listener for parse data query download completions.
     */
    public interface OnDownloadCompletedListener {

        public void onDownloadSuccess();

        public void onDownloadFail();
    }

    /**
     * Listener for parse data query update completions.
     */
    public interface OnUpdateCompletedListener<T> {

        public void onUpdateSuccess(T updatedObject);

        public void onUpdateFail(Exception e);
    }
}