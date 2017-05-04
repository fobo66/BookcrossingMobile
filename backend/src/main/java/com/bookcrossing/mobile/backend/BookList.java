package com.bookcrossing.mobile.backend;

import com.googlecode.objectify.annotation.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * (c) 2017 Andrey Mukamolow aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 04.01.2017.
 *
 * Entity for paginated responses
 */

@Entity
public class BookList {
    private String cursor;
    private List<Book> books;

    public String getCursor() {
        return cursor;
    }

    public List<Book> getBooks() {
        return books;
    }

    BookList(List<Book> books, String cursor) {
        this.books = books;
        this.cursor = cursor;

    }
}
