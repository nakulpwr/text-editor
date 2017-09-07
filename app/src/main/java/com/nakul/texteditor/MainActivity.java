package com.nakul.texteditor;

import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    RichEditText mRichEditText;
    TextEditorDataSource mDataSource;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        mRichEditText = (RichEditText) findViewById(R.id.rich_edit_text);
        RichTextActions richTextActions = (RichTextActions) findViewById(R.id.rich_text_actions);
        mRichEditText.setRichTextActionsView(richTextActions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getApplication().getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }
        final TextView textView = (TextView)findViewById(R.id.wordsCountTxt);
        mDataSource = new TextEditorDataSource(this);
        List<TextEditorDataModel> dataModel = mDataSource.getAllTextData();
        if (!dataModel.isEmpty()) {
            setHtmlText(dataModel.get(0).getHtmlBody());
            textView.setText(String.valueOf(dataModel.get(0).getWordCount()));
        }
        else
            mRichEditText.setHint(getString(R.string.hint));
        mRichEditText.setonChangeInterface(new RichEditText.ChangeListener() {
            @Override
            public void onChange() {
               // handle callback
                textView.setText(mRichEditText.getWordCounts());
            }
        });

    }

    private void setHtmlText(String html){
        mRichEditText.setHtml(html);
    }

    private void saveData() {
        if (TextUtils.isEmpty(mRichEditText.getTitle()))
            return;
        TextEditorDataModel mDataModel = new TextEditorDataModel();
        mDataModel.setHtmlBody("<html>" + mRichEditText.getHtml() + "</html>");
        mDataModel.setWordCount(Integer.parseInt(mRichEditText.getWordCounts()));
        mDataModel.setTitle(mRichEditText.getTitle());
        boolean isSaved = mDataSource.insertData(mDataModel);
        String msg = isSaved ? getString(R.string.saved_msg) : getString(R.string.not_saved_msg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }


    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        WebView webView = new WebView(this);
        webView.loadData("<html>" + mRichEditText.getHtml() + "</html>", "text/html; charset=utf-8", "UTF-8");
        builder.setView(webView);
        AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_save:
                saveData();
                break;

            case R.id.action_submit:
                showDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
