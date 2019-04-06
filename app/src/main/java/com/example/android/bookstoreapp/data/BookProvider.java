package com.example.android.bookstoreapp.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.example.android.bookstoreapp.data.BookContract.BookEntry;

public class BookProvider extends ContentProvider {

    private static final int BOOKS = 100;

    private static final int BOOK_ID = 101;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS, BOOKS);
        uriMatcher.addURI(BookContract.CONTENT_AUTHORITY, BookContract.PATH_BOOKS + "/#", BOOK_ID);
    }

    //Log message tag
    public static final String LOG_TAG = BookProvider.class.getSimpleName();

    private BookDBHelper bookDBHelper;

    @Override
    public boolean onCreate() {
        //I connect to the database
        bookDBHelper = new BookDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String s1) {
        //i can read from the database
        SQLiteDatabase booksDb = bookDBHelper.getReadableDatabase();

        Cursor cursor = null;

        //based on the read type i want i execute the query to the database
        int match = uriMatcher.match(uri);
        if (match == BOOKS){
            cursor = booksDb.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, s1);
        } else if (match == BOOK_ID){
            selection = BookEntry._ID + "=?";
            selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
            cursor = booksDb.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, s1);
        } else {
            throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int matcher = uriMatcher.match(uri);
        if (matcher == BOOKS){
            return BookEntry.CONTENT_LIST_TYPE;
        } else if (matcher == BOOK_ID){
            return BookEntry.CONTENT_ITEM_TYPE;
        } else {
            throw new IllegalStateException("Unknown URI " + uri + " with match " + matcher);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = uriMatcher.match(uri);
        if (match == BOOKS){
            return insertBook(uri, contentValues);
        } else {
            throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    //in this funstion i insert into the database a book with the given values
    private Uri insertBook(Uri uri, ContentValues values){
        SQLiteDatabase booksDb = bookDBHelper.getWritableDatabase();

        //Values validation
        String bookName = values.getAsString(BookEntry.COLUMN_BOOK_NAME);
        if (bookName == null){
            throw new IllegalArgumentException("Please insert book name");
        }
        String bookAuthor = values.getAsString(BookEntry.COLUMN_BOOK_AUTHOR);
        if (bookAuthor == null){
            throw new IllegalArgumentException("Please insert book author");
        }
        double bookPrice = values.getAsDouble(BookEntry.COLUMN_BOOK_PRICE);
        if (bookPrice < 0){
            throw new IllegalArgumentException("The price should be a positive value");
        }
        int bookQuantity = values.getAsInteger(BookEntry.COLUMN_QUANTITY);
        if (bookQuantity < 0){
            throw new IllegalArgumentException("The quantity can not be negative");
        }

        // Book insertion
        long id = booksDb.insert(BookEntry.TABLE_NAME, null, values);
        //checking if the insertion is complete
        if (id == -1){
            Toast.makeText(getContext(), "The book insertion failed", Toast.LENGTH_LONG).show();
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase booksDb = bookDBHelper.getWritableDatabase();

        int rowsDeleted = 0;
        final int match = uriMatcher.match(uri);
            if (match == BOOKS){
                rowsDeleted = booksDb.delete(BookEntry.TABLE_NAME, selection, selectionArgs);

                if (rowsDeleted != 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            } else if (match == BOOK_ID){
                // Delete a single row given by the ID in the URI
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};

                rowsDeleted = booksDb.delete(BookEntry.TABLE_NAME, selection, selectionArgs);

                if (rowsDeleted != 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            } else {
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
            }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        if (match == BOOKS) {
            getContext().getContentResolver().notifyChange(uri, null);
            return updateBook(uri, contentValues, selection, selectionArgs);
        }else if (match == BOOK_ID){
            selection = BookEntry._ID + "=?";
            selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
            getContext().getContentResolver().notifyChange(uri, null);
            return updateBook(uri, contentValues, selection, selectionArgs);
        } else {
            throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        SQLiteDatabase booksDb = bookDBHelper.getWritableDatabase();

        if (values.containsKey(BookEntry.COLUMN_BOOK_NAME)) {
            String name = values.getAsString(BookEntry.COLUMN_BOOK_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Book requires a name");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_BOOK_AUTHOR)) {
            String author = values.getAsString(BookEntry.COLUMN_BOOK_AUTHOR);
            if (author == null) {
                throw new IllegalArgumentException("Book requires an author");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_BOOK_PRICE)) {
            double price = values.getAsDouble(BookEntry.COLUMN_BOOK_PRICE);
            if (price < 0) {
                throw new IllegalArgumentException("Book requires a positive price");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_QUANTITY)) {
            int quantity = values.getAsInteger(BookEntry.COLUMN_QUANTITY);
            if (quantity <0) {
                throw new IllegalArgumentException("The quantity have to be positive");
            }
        }

        //check if we have values to update
        if (values.size() == 0){
            return 0;
        }

        int rowsUpdated = booksDb.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);

        if(rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}