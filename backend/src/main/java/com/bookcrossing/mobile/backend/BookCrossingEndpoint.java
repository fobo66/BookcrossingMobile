/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.bookcrossing.mobile.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.QueryResultIterator;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.googlecode.objectify.cmd.Query;

import java.util.Collections;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "bookcrossingMobileApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "mobile.bookcrossing.com",
                ownerName = "Andrey Mukamolow",
                packagePath = ""
        ),
        clientIds = {Constants.ANDROID_CLIENT_ID},
        audiences = {Constants.ANDROID_AUDIENCE}
)
public class BookCrossingEndpoint {

    @ApiMethod(name = "getBooks")
    public BookList getBooks(@Named("cursor") String cursor) {
        Query<Book> query = ofy().load().type(Book.class);

        if (cursor != null) {
            query = query.startAt(Cursor.fromWebSafeString(cursor)).limit(Constants.PAGE_SIZE);
        } else {
            query = query.limit(Constants.PAGE_SIZE);

        }

        List<Book> result = Collections.emptyList();
        boolean toContinue = false;

        QueryResultIterator<Book> iterator = query.iterator();
        while (iterator.hasNext()) {
            Book book = iterator.next();
            result.add(book);
            toContinue = true;
        }

        if (toContinue) {
            String realCursor = iterator.getCursor().toWebSafeString();
            return new BookList(result, realCursor);
        }

        return new BookList(result, null);
    }

    @ApiMethod(
            name = "addBook",
            httpMethod = ApiMethod.HttpMethod.POST
    )
    public void addBook(Book book) {
        ofy().save().entity(book);
    }

    @ApiMethod(name = "getBook")
    public Book getBook(@Named("id") String id) {
        return ofy().load().type(Book.class).id(id).safe();
    }
}
