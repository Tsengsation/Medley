package com.tsengsation.resound.Parse;

/**
 * Exception thrown when article index requested is out of bounds of the article list.
 */
public class ArticleIndexOutOfBoundsException extends Exception {

    public ArticleIndexOutOfBoundsException(int index, int size) {
        super(String.format(
                "Article index (%d) is out of bounds of article list (size: %d).",
                index, size));
    }
}
