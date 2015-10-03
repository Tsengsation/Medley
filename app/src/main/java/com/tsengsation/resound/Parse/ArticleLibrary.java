package com.tsengsation.resound.Parse;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Library of article objects.
 */
public class ArticleLibrary {

    private List<Article> mArticles;
    private List<Article> mFilteredArticles;
    private Map<String, ParseObject> mParseArticleMap;
    private Map<String, Integer> mArticlePositions;
    private int mArticleType;

    public ArticleLibrary(Map<String, ParseObject> parseArticleMap, List<Article> articles) {
        mParseArticleMap = parseArticleMap;
        mArticles = articles;
        Collections.sort(articles, new Comparator<Article>() {
            @Override
            public int compare(Article art1, Article art2) {
                return art2.date.compareTo(art1.date);
            }
        });
        mFilteredArticles = new ArrayList<>();
        mArticlePositions = new HashMap<>();
        for (int i = 0; i < mArticles.size(); i++) {
        }
        // Default to all articles.
        filterByType(Article.TYPE_FEATURED);
    }

    public int getCount() {
        return mFilteredArticles.size();
    }

    public Article getArticle(int position) throws ArticleIndexOutOfBoundsException {
        if (position < 0 || position >= mFilteredArticles.size()) {
            throw new ArticleIndexOutOfBoundsException(position, mFilteredArticles.size());
        }
        return mFilteredArticles.get(position);
    }

    public Article findArticleById(String id) {
        for (Article article : mArticles) {
            if (article.id.equals(id)) {
                return article;
            }
        }
        return null;
    }

    public ParseObject getParseArticleObject(String id) {
        return mParseArticleMap.containsKey(id) ? mParseArticleMap.get(id) : null;
    }

    public void updateArticle(Article updatedArticle) {
        if (mArticlePositions.containsKey(updatedArticle.id)) {
            int pos = mArticlePositions.get(updatedArticle.id);
            mArticles.set(pos, updatedArticle);
            filterByType(mArticleType);
        }
    }

    public void filterByType(int articleType) {
        mArticleType = articleType;
        mFilteredArticles.clear();
        if (mArticleType == Article.TYPE_FEATURED) {
            mFilteredArticles.addAll(mArticles);
        } else {
            for (Article article : mArticles) {
                if (article.type == articleType) {
                    mFilteredArticles.add(article);
                }
            }
        }
    }
}
