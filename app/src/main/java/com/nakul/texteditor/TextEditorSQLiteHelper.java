package com.nakul.texteditor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class TextEditorSQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_TEXT_EDITOR = "textEditor";
    public static final String ID_COLUMN = "id";
    public static final String TITLE_COLUMN = "title";
    public static final String BODY_COLUMN = "body";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "textEditor.db";
    public static final String TOTAL_WORDS_COUNT = "wordsCount";

    public static final String DATABASE_CREATE = "create table "
            + TABLE_TEXT_EDITOR + "( " + ID_COLUMN
            + " integer primary key autoincrement, " + TITLE_COLUMN
            + " text not null, " + BODY_COLUMN
            + " integer not null, " + TOTAL_WORDS_COUNT
            + ");";

    public TextEditorSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TextEditorSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEXT_EDITOR);
        onCreate(db);
    }

}

