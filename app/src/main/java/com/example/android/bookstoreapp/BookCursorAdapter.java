package com.example.android.bookstoreapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstoreapp.data.BookContract;
import com.example.android.bookstoreapp.data.BookContract.BookEntry;

import org.w3c.dom.Text;

public class BookCursorAdapter extends CursorAdapter {

    TextView bookQuantityTextView;

    public BookCursorAdapter(Context context, Cursor cursor){
        super(context, cursor);
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        //find the text views from the list_item
        TextView bookNameTextView = (TextView) view.findViewById(R.id.product_name);
        TextView bookAuthorTextView = (TextView) view.findViewById(R.id.product_owner);

        TextView bookPriceTextView = (TextView) view.findViewById(R.id.product_price);
        bookQuantityTextView = (TextView) view.findViewById(R.id.product_quantity);

        //collect the data from the cursor
        String bookName = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME));
        String bookAuthor = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_AUTHOR));

        double bookPrice = cursor.getDouble(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE));
        final int bookQuantity = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY));

        //Set the values to the textviews
        bookNameTextView.setText(bookName);
        bookAuthorTextView.setText(bookAuthor);
        bookPriceTextView.setText(Double.toString(bookPrice));
        bookQuantityTextView.setText(Integer.toString(bookQuantity));

        ImageButton buyBtnView = (ImageButton) view.findViewById(R.id.shop_button);
        buyBtnView.setTag(cursor.getPosition());
        buyBtnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateQuantity((int)view.getTag(), view, cursor, context);
                // here you pass right position to your activity
            }
        });
    }

    public void updateQuantity(int position, View view, Cursor cursor, Context context) {

        ContentValues values = new ContentValues();

        //Move the cursor to the position of the current item under operation
        cursor.moveToPosition(position);

        int quantity = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY));

        if(quantity>0){
            quantity--;
            values.put(BookEntry.COLUMN_QUANTITY, quantity);
            String selection = BookEntry._ID + "=?";
            int itemId = cursor.getInt(cursor.getColumnIndex(BookEntry._ID));

            //Selection args claus
            String[] selectionArgs = {Integer.toString(itemId)};
            //Update the value
            int rowsAffected = context.getContentResolver().update(BookEntry.CONTENT_URI, values, selection, selectionArgs);

            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(context, "The update was unsuccesful", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "I sold one book", Toast.LENGTH_SHORT).show();
            }
            //New quantity
            String newQu = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY));

            bookQuantityTextView.setText(newQu);
        } else {
            Toast.makeText(context, "The Quantity is zero", Toast.LENGTH_SHORT).show();
        }

    }
}