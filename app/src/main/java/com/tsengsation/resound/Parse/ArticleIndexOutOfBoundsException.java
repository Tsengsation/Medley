package com.tsengsation.resound.Parse;

/**
 * Created by jonathantseng on 1/29/15.
 */
public class ArticleIndexOutOfBoundsException extends Exception {

    public ArticleIndexOutOfBoundsException(int index, int size) {
        super(String.format(
                "Article index (%d) is out of bounds of article list (size: %d).",
                index, size));
    }
}
