package com.example.android.bookstoreapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstoreapp.data.BookContract.BookEntry;

public class EditInfoActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    //editTexts
    private EditText bookNameText;
    private EditText bookAuthorText;
    private EditText bookPriceText;
    private TextView bookQuantityText;
    private EditText supplierNameText;
    private EditText supplierNumberText;

    //Buttons
    private ImageButton minusQuantityBtn;
    private ImageButton plusQuantityBtn;
    private ImageButton orderBtn;

    private static final int BOOK_LOADER = 1;

    private Uri currentUri = null;

    private boolean bookHasChanged = false;

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            bookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_info_activity);

        Intent intent = getIntent();
        currentUri = intent.getData();

        if (currentUri == null) {
            setTitle("Add Book");
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            //invalidateOptionsMenu();
        } else {
            setTitle("Edit Book");
            getLoaderManager().initLoader(BOOK_LOADER, null, this);
        }

        //connect with the view that i need to procces
        bookNameText = (EditText) findViewById(R.id.book_name_edit_text);
        bookAuthorText = (EditText) findViewById(R.id.book_author_edit_text);
        bookPriceText = (EditText) findViewById(R.id.price_edit_text);
        bookQuantityText = (TextView) findViewById(R.id.quantity_text_view);
        bookQuantityText.setHint(R.string.quantity);

        minusQuantityBtn = (ImageButton) findViewById(R.id.minus_btn_view);
        plusQuantityBtn = (ImageButton) findViewById(R.id.plus_btn_view);

        supplierNameText = (EditText) findViewById(R.id.supplier_edit_text);
        supplierNumberText = (EditText) findViewById(R.id.supplier_number_edit_text);
        orderBtn = (ImageButton) findViewById(R.id.order_btn);

        bookNameText.setOnTouchListener(touchListener);
        bookAuthorText.setOnTouchListener(touchListener);
        bookPriceText.setOnTouchListener(touchListener);
        bookQuantityText.setOnTouchListener(touchListener);
        supplierNameText.setOnTouchListener(touchListener);
        supplierNumberText.setOnTouchListener(touchListener);

        //set the action of button
        minusQuantityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = Integer.parseInt(bookQuantityText.getText().toString());
                if (quantity > 0) {
                    bookQuantityText.setText(Integer.toString(quantity - 1));
                    bookHasChanged = true;
                }
            }
        });

        //set the action of the button
        plusQuantityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int q;
                String quantity = bookQuantityText.getText().toString();
                if (TextUtils.isEmpty(quantity)) {
                    q = 0;
                } else {
                    q = Integer.parseInt(quantity);
                }
                bookQuantityText.setText("" + (q + 1));
            }
        });

        //set the action of orderBtn
        orderBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(EditInfoActivity.this, Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(EditInfoActivity.this, "Permission not granted", Toast.LENGTH_SHORT).show();
                    requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 1);
                } else {
                    Intent intentCall = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + supplierNumberText.getText().toString().trim()));
                    startActivity(intentCall);
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Intent intentCall = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + supplierNumberText.getText().toString().trim()));
                    startActivity(intentCall);
                }
                break;
            default:
                break;
        }
    }

    private void addBook() {
        int quantity = 0;
        double price = 0;

        String bookName = bookNameText.getText().toString().trim();
        String bookAuthor = bookAuthorText.getText().toString().trim();
        String bookPrice = bookPriceText.getText().toString().trim();
        String quantityBook = bookQuantityText.getText().toString().trim();
        String supplierName = supplierNameText.getText().toString().trim();
        String supplierNumber = supplierNumberText.getText().toString().trim();

        if (currentUri == null && TextUtils.isEmpty(bookName)) {
            Toast.makeText(this, "Please insert Book Title", Toast.LENGTH_LONG).show();
            return;
        } else if (currentUri == null && TextUtils.isEmpty(bookAuthor)) {
            Toast.makeText(this, "Please insert Book Author", Toast.LENGTH_LONG).show();
            return;
        } else if (currentUri == null && TextUtils.isEmpty(bookPrice)) {
            Toast.makeText(this, "Please insert Book Price", Toast.LENGTH_LONG).show();
            return;
        } else if (currentUri == null && TextUtils.isEmpty(supplierName)) {
            Toast.makeText(this, "Please insert Book Supplier Name", Toast.LENGTH_LONG).show();
            return;
        } else if (currentUri == null && TextUtils.isEmpty(supplierNumber)) {
            Toast.makeText(this, "Please insert Book Supplier Phone Number", Toast.LENGTH_LONG).show();
            return;
        }

        if (!TextUtils.isEmpty(quantityBook)) {
            quantity = Integer.parseInt(quantityBook);
        }
        if (!TextUtils.isEmpty(bookPrice)) {
            price = Double.parseDouble(bookPrice);
        }

        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_BOOK_NAME, bookName);
        values.put(BookEntry.COLUMN_BOOK_AUTHOR, bookAuthor);
        values.put(BookEntry.COLUMN_BOOK_PRICE, price);
        values.put(BookEntry.COLUMN_QUANTITY, quantity);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierName);
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierNumber);

        if (currentUri == null) {
            //new book insertion
            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, "Book addition failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Book addition complete", Toast.LENGTH_SHORT).show();
            }
        } else {
            //update book info
            int rowsAffected = getContentResolver().update(currentUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, "Error updating book", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Book update complete", Toast.LENGTH_SHORT).show();
            }
        }
    finish();
    }

    //remove the delete option from menu if i add a new book
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (currentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //I handle the menu choice of the user
        switch (item.getItemId()) {
            case R.id.action_save:
                addBook();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!bookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditInfoActivity.this);
                    return true;
                }

                //if there are changes then warn the user with a dialog
                DialogInterface.OnClickListener diascardButtonListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditInfoActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(diascardButtonListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //if there are no changes
        if (!bookHasChanged) {
            super.onBackPressed();
            return;
        }

        //if there are changes show the showUnsavedChangesDialog
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };

        showUnsavedChangesDialog(discardButtonClickListener);

    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.there_are_changes);
        builder.setPositiveButton(R.string.diascard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_message);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancelation, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteBook() {
        if (currentUri != null) {
            //do the delete
            int rowsDeleted = getContentResolver().delete(currentUri, null, null);

            //show state message
            if (rowsDeleted == 0) {
                //delete fialed
                Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Delete complete", Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Define the projection
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_AUTHOR,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER
        };
        return new CursorLoader(this, currentUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            //Find the collmns
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int authorColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_AUTHOR);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            //get the values
            String bookName = cursor.getString(nameColumnIndex);
            String bookAuthor = cursor.getString(authorColumnIndex);
            double bookPrice = cursor.getDouble(priceColumnIndex);
            int bookQuantity = cursor.getInt(quantityColumnIndex);
            String bookSupplier = cursor.getString(supplierColumnIndex);
            String bookSupplierPhone = cursor.getString(phoneColumnIndex);

            //Updare the view values
            bookNameText.setText(bookName);
            bookAuthorText.setText(bookAuthor);
            bookPriceText.setText("" + bookPrice);
            bookQuantityText.setText("" + bookQuantity);
            supplierNameText.setText(bookSupplier);
            supplierNumberText.setText(bookSupplierPhone);

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
