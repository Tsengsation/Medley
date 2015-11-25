package com.tsengsation.medley.ViewHelpers;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class FontManager {

    public static final int GENERICA = 1;
    public static final int PETITA_MEDIUM = 2;
    public static final int ARTICLE_FONT_BOLD = 3;
    public static final int ARTICLE_FONT = 4;

    private static final String generica = "fonts/Generica.otf";
    private static final String petitaMedium = "fonts/PetitaMedium.ttf";
    private static final String articleFontBold = "fonts/ArticleFontBold.ttf";
    private static final String articleFont = "fonts/ArticleFont.ttf";
    private static final Map<Integer, Typeface> typefaceMap = new HashMap<>();

    private static String getFont(int font) {
        switch (font) {
            case PETITA_MEDIUM:
                return petitaMedium;
            case GENERICA:
                return generica;
            case ARTICLE_FONT_BOLD:
                return articleFontBold;
            default:
                return articleFont;
        }
    }

    public static void setFont(Context context, TextView view, int font) {
        if (!typefaceMap.containsKey(font)) {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), getFont(font));
            typefaceMap.put(font, typeface);
        }
        view.setTypeface(typefaceMap.get(font));
    }
}
