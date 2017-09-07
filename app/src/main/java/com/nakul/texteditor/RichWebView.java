package com.nakul.texteditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nakul on 9/1/2017.
 */

public class RichWebView extends WebView {

    public enum Type {
        BOLD,
        ITALIC,
        UNDERLINE,
        STRIKETHROUGH,
        FORECOLOR,
        HILITECOLOR
    }

    public enum StateType {
        ENABLE,
        ALLOW
    }

    public interface OnStateChangeListener {
        void onStateChanged(String text, List<Type> types, StateType stateType);
    }

    public interface AfterInitialLoadListener {
        void onAfterInitialLoad(boolean isReady);
    }

    private static final String SETUP_HTML = "file:///android_asset/editor.html";
    private static final String CALLBACK_SEPARATOR = "~!~!~!";
    private static final String JAVA_SCRIPT_INTERFACE_NAME = "JSInterface";
    private boolean isReady;
    private String mContents, mMainText;
    private OnStateChangeListener mStateChangeListener;
    private AfterInitialLoadListener mLoadListener;

    public RichWebView(Context context) {
        this(context, null);
    }

    public RichWebView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.webViewStyle);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public RichWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        getSettings().setJavaScriptEnabled(true);
        addJavascriptInterface(new EditorJavaScriptInterface(), JAVA_SCRIPT_INTERFACE_NAME);
        setWebChromeClient(new WebChromeClient());
        setWebViewClient(createWebviewClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (context.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }
        loadUrl(SETUP_HTML);

        applyAttributes(context, attrs);
    }

    protected EditorWebViewClient createWebviewClient() {
        return new EditorWebViewClient();
    }

    public void setStateChangeListener(OnStateChangeListener listener) {
        mStateChangeListener = listener;
    }

    public void setOnInitialLoadListener(AfterInitialLoadListener listener) {
        mLoadListener = listener;
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(Color.WHITE);
    }

    private void handleCallback(String text) {
        String[] stringParts = text.split(CALLBACK_SEPARATOR);
        String allowedString = "";
        String enabledString = "";
        if (stringParts.length > 0) {
            allowedString = stringParts[0];
            if (stringParts.length > 1) {
                enabledString = stringParts[1];
                if (stringParts.length > 4) {
                    mContents = stringParts[4];
                    mMainText = stringParts[5];
                }
            }
        }

        List<Type> types = new ArrayList<>();
        for (Type type : Type.values()) {
            if (TextUtils.indexOf(allowedString, type.name()) != -1) {
                types.add(type);
            }
        }
        String countString = stringParts[stringParts.length - 1].replaceAll("\\s+", " ").trim();
        if (!countString.equals("0"))
            countString = countString.split(" ").length + "";
        if (mStateChangeListener != null) {
            mStateChangeListener.onStateChanged(countString, types, StateType.ALLOW);
        }

        types = new ArrayList<>();
        for (Type type : Type.values()) {
            if (TextUtils.indexOf(enabledString, type.name()) != -1) {
                types.add(type);
            }
        }

        if (mStateChangeListener != null) {
            mStateChangeListener.onStateChanged(countString, types, StateType.ENABLE);
        }

    }

    private void applyAttributes(Context context, AttributeSet attrs) {
        final int[] attrsArray = new int[]{android.R.attr.gravity};
        TypedArray ta = context.obtainStyledAttributes(attrs, attrsArray);

        int gravity = ta.getInt(0, NO_ID);
        switch (gravity) {
            case Gravity.LEFT:
                exec("javascript:RE.setTextAlign(\"left\")");
                break;
            case Gravity.RIGHT:
                exec("javascript:RE.setTextAlign(\"right\")");
                break;
            case Gravity.TOP:
                exec("javascript:RE.setVerticalAlign(\"top\")");
                break;
            case Gravity.BOTTOM:
                exec("javascript:RE.setVerticalAlign(\"bottom\")");
                break;
            case Gravity.CENTER_VERTICAL:
                exec("javascript:RE.setVerticalAlign(\"middle\")");
                break;
            case Gravity.CENTER_HORIZONTAL:
                exec("javascript:RE.setTextAlign(\"center\")");
                break;
            case Gravity.CENTER:
                exec("javascript:RE.setVerticalAlign(\"middle\")");
                exec("javascript:RE.setTextAlign(\"center\")");
                break;
        }

        ta.recycle();
    }

    public void setHtml(String contents) {
        if (contents == null) {
            contents = "";
        }
        mContents = contents;
        execSetHtml();
    }

    private void execSetHtml() {
        try {
            if (isReady) {
                load("javascript:RE.setHtml('" + URLEncoder.encode(mContents, "UTF-8") + "');");
            } else {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        execSetHtml();
                    }
                }, 100);
            }
        } catch (UnsupportedEncodingException e) {
            // No handling
        }
    }

    public String getHtml() {
        if (mContents == null) {
            return "";
        }
        return mContents;
    }

    public String getTitle() {
        if (TextUtils.isEmpty(mMainText)) {
            Toast.makeText(getContext(), getResources().getString(R.string.not_saved_msg), Toast.LENGTH_LONG).show();
            return "";
        } else
            return mMainText.substring(0, Math.min(mMainText.length() - 1, 8));
    }

    public void setEditorHeight(int px) {
        exec("javascript:RE.setHeight('" + px + "px');");
    }

    public void setEditorFontColor(int color) {
        String hex = convertHexColorString(color);
        exec("javascript:RE.setBaseTextColor('" + hex + "');");
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        exec("javascript:RE.setPadding('" + left + "px', '" + top + "px', '" + right + "px', '" + bottom + "px');");
    }

    public void setPlaceholder(String placeholder) {
        exec("javascript:RE.setPlaceholder('" + placeholder + "');");
    }

    public void setBold() {
        exec("javascript:RE.setBold();");
    }

    public void setItalic() {
        exec("javascript:RE.setItalic();");
    }

    public void setUnderline() {
        exec("javascript:RE.setUnderline();");
    }

    public void setBullet() {
        exec("javascript:RE.setBullets();");
    }

    public void setNumbers() {
        exec("javascript:RE.setNumbers();");
    }

    public void doubleQuotes() {
        exec("javascript:RE.doubleQuotes();");
    }


    private String convertHexColorString(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    protected void exec(final String trigger) {
        if (isReady) {
            load(trigger);
        } else {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    exec(trigger);
                }
            }, 100);
        }
    }

    private void load(String trigger) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(trigger, null);
        } else {
            loadUrl(trigger);
        }
    }

    public class EditorWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            isReady = url.equalsIgnoreCase(SETUP_HTML);
            if (mLoadListener != null) {
                mLoadListener.onAfterInitialLoad(isReady);
            }
        }
    }

    public class EditorJavaScriptInterface {
        @JavascriptInterface
        public void callback(String callbackString) {
            if (TextUtils.isEmpty(callbackString)) {
                return;
            }

            try {
                String decodedString = URLDecoder.decode(callbackString, "UTF-8");
                handleCallback(decodedString);
            } catch (UnsupportedEncodingException e) {
                // No handling
            }
        }
    }

    public void changeAlignment(int selectedOption) {
        switch (selectedOption) {
            case 0:
                exec("javascript:RE.setJustifyLeft();");
                break;
            case 1:
                exec("javascript:RE.setJustifyCenter();");
                break;
            case 2:
                exec("javascript:RE.setJustifyRight();");
                break;
        }
    }
}
