package com.example.android.bookfly4;

/**
 * Created by Michael on 9/27/16.
 */
import java.util.ArrayList;

/**
 * Created by Michael on 9/26/16.
 */
public class Book extends ArrayList<Book> {

    private String mTitle;
    private String mAuthor;

    public Book (String title, String author) {
        mTitle = title;
        mAuthor = author;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }
}
