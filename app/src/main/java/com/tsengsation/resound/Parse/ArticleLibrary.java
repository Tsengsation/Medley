package com.tsengsation.resound.Parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by jonathantseng on 3/25/15.
 */
public class ArticleLibrary {

    private List<Article> mArticles;
    private List<Article> mFilteredArticles;

    public ArticleLibrary(List<Article> mArticles) {
        this.mArticles = mArticles;
        Collections.sort(mArticles, new Comparator<Article>() {
            @Override
            public int compare(Article art1, Article art2) {
                return art1.getDate().compareTo(art2.getDate());
            }
        });
        mFilteredArticles = new ArrayList<>(mArticles);
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

    public void filterByType(ArticleType type) {
        mFilteredArticles = new ArrayList<>();
        for (Article article : mArticles) {
            if (article.getType().equals(type)) {
                mFilteredArticles.add(article);
            }
        }
    }

}
