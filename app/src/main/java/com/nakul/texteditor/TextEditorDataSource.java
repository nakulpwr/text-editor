package com.nakul.texteditor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class TextEditorDataSource {
    // Database fields
    private static final String EXCEPTION_TAG = "Sql exception";
    private SQLiteDatabase database;
    private TextEditorSQLiteHelper dbHelper;
    private String[] allColumns = {TextEditorSQLiteHelper.ID_COLUMN,
            TextEditorSQLiteHelper.TITLE_COLUMN
            , TextEditorSQLiteHelper.BODY_COLUMN
            , TextEditorSQLiteHelper.TOTAL_WORDS_COUNT};

    public TextEditorDataSource(Context context) {
        dbHelper = new TextEditorSQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public boolean insertData(TextEditorDataModel textData) {
        boolean result = false;
        try {
            open();
            database.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put(TextEditorSQLiteHelper.TITLE_COLUMN, textData.getTitle());
                values.put(TextEditorSQLiteHelper.BODY_COLUMN, textData.getHtmlBody());
                values.put(TextEditorSQLiteHelper.TOTAL_WORDS_COUNT, textData.getWordCount());
                // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
                long id = database.insertOrThrow(TextEditorSQLiteHelper.TABLE_TEXT_EDITOR, null, values);
                textData.setId(id);
                database.setTransactionSuccessful();
                result = true;
            } catch (Exception e) {
                Log.e(EXCEPTION_TAG, e.getMessage());
            } finally {
                database.endTransaction();
            }
        } catch (Exception e) {
            Log.e(EXCEPTION_TAG, e.getMessage());
        } finally {
            close();
        }
        return result;
    }

    public boolean deleteLetter(TextEditorDataModel letter) {
        boolean result = false;
        try {
            open();
            database.beginTransaction();
            try {
                long id = letter.getId();
                database.delete(TextEditorSQLiteHelper.TABLE_TEXT_EDITOR, TextEditorSQLiteHelper.ID_COLUMN
                        + " = " + id, null);
                database.setTransactionSuccessful();
                Log.i("Sqlite", "Letter deleted with id: " + id);
                result = true;
            } catch (Exception e) {
                Log.e(EXCEPTION_TAG, e.getMessage());
            } finally {
                database.endTransaction();
            }
        } catch (Exception e) {
            Log.e(EXCEPTION_TAG, e.getMessage());
        } finally {
            close();
        }
        return result;
    }

    public boolean updateTextData(TextEditorDataModel letter) {
        boolean result = false;
        try {
            open();
            database.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put(TextEditorSQLiteHelper.ID_COLUMN, letter.getId());
                values.put(TextEditorSQLiteHelper.BODY_COLUMN, letter.getHtmlBody());

                // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
                database.update(TextEditorSQLiteHelper.TABLE_TEXT_EDITOR, values, TextEditorSQLiteHelper.ID_COLUMN + "= ?", new String[]{String.valueOf(letter.getId())});
                database.setTransactionSuccessful();
                letter.setHtmlBody(letter.getHtmlBody());
                result = true;
            } catch (Exception e) {
                Log.e(EXCEPTION_TAG, e.getMessage());
            } finally {
                database.endTransaction();
            }
        } catch (Exception e) {
            Log.e(EXCEPTION_TAG, e.getMessage());
        } finally {
            close();
        }
        return result;
    }


    public List<TextEditorDataModel> getAllTextData() {
        List<TextEditorDataModel> textList = new ArrayList<TextEditorDataModel>();
        try {
            open();
            Cursor cursor = database.query(TextEditorSQLiteHelper.TABLE_TEXT_EDITOR,
                    allColumns, null, null, null, null, TextEditorSQLiteHelper.ID_COLUMN + " DESC ");

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                TextEditorDataModel dataModel = cursorToLetter(cursor);
                textList.add(dataModel);
                cursor.moveToNext();
            }
            // make sure to close the cursor
            cursor.close();

        } catch (Exception e) {
            Log.e(EXCEPTION_TAG, e.getMessage());
        } finally {
            close();
        }
        return textList;
    }

    public TextEditorDataModel getTextById(long textId) {
        TextEditorDataModel textEditorDataModel = new TextEditorDataModel();
        try {
            open();
            String where = TextEditorSQLiteHelper.ID_COLUMN + " = " + textId;
            Cursor cursor = database.query(TextEditorSQLiteHelper.TABLE_TEXT_EDITOR,
                    allColumns, where, null, null, null, TextEditorSQLiteHelper.ID_COLUMN + " DESC ");
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                textEditorDataModel = cursorToLetter(cursor);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(EXCEPTION_TAG, e.getMessage());
        } finally {
            close();
        }
        return textEditorDataModel;
    }

    public void clearDatabase() {
        try {
            open();

            try {
                database.beginTransaction();
                database.execSQL("DROP TABLE IF EXISTS " + TextEditorSQLiteHelper.TABLE_TEXT_EDITOR);
                database.execSQL(TextEditorSQLiteHelper.DATABASE_CREATE);
                database.setTransactionSuccessful();
            } catch (Exception e) {
                Log.e(EXCEPTION_TAG, e.getMessage());
            } finally {
                database.endTransaction();
            }

        } catch (Exception e) {
            Log.e(EXCEPTION_TAG, e.getMessage());
        }
    }

    private TextEditorDataModel cursorToLetter(Cursor cursor) {
        TextEditorDataModel textEditorDataModel = new TextEditorDataModel();
        textEditorDataModel.setId(cursor.getInt(0));
        textEditorDataModel.setTitle(cursor.getString(1));
        textEditorDataModel.setHtmlBody(cursor.getString(2));
        textEditorDataModel.setWordCount(cursor.getInt(3));
        return textEditorDataModel;
    }
}
