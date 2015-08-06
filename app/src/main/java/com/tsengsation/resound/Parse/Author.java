package com.tsengsation.resound.Parse;

import java.io.Serializable;

/**
 * Struct for author info.
 */
public class Author implements Serializable {

    public final String imageUrl;
    public final String name;

    public Author(String imageUrl, String name) {
        this.imageUrl = imageUrl;
        this.name = name;
    }
}
