package com.nakul.texteditor;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;

/**
 * Created by nakul on 9/1/2017.
 */
public class RichEditText extends RelativeLayout {
    private Context mContext;
    private LayoutInflater mInflater;
    private Handler mHandler;
    private String mPreviewText;

    private RichWebView mEditor;
    private RichTextActions mActions;

    private ChangeListener mListener;

    private ImageButton mBoldButton;
    private ImageButton mItalicButton;
    private ImageButton mUnderlineButton;
    private ImageButton mTextAlignmentButton;
    private ImageButton mTextBulletButton;

    private boolean mBoldEnabled;
    private boolean mBoldAllowed;
    private boolean mItalicEnabled;
    private boolean mItalicAllowed;
    private boolean mUnderlineEnabled;
    private boolean mUnderlineAllowed;
    private boolean mTextColorAllowed;
    private boolean mBackgroundColorAllowed;
    private boolean isBulletEnable;

    private static final String[] ALIGNMENT_OPTIONS_ARRAY = {"Left", "Center", "Right"};
    private static final String[] BULLET_OPTIONS_ARRAY = {"Normal", "Numbers"};
    private int mBulletOption = 0, mAlignmentOption = 0;
    private static final String DIALOG_BTN_TEXT = "OK";
    private String wordCounts = "0";

    public RichEditText(Context context) {
        super(context);
        setupView(context);
    }

    public RichEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupView(context);
    }

    public RichEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RichEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setupView(context);
    }

    public void setRichTextActionsView(RichTextActions actionsView) {
        mActions = actionsView;
        setupActions();
    }

    public void setPreviewText(String previewText) {
        mPreviewText = previewText;
    }

    public void setHtml(String html) {
        if (html == null) {
            return;
        }
        mEditor.setHtml(html);
    }

    public String getHtml() {
        return mEditor.getHtml();
    }

    public String getTitle() {
        return mEditor.getTitle();
    }

    public void setHint(String hint) {
        if (!TextUtils.isEmpty(hint) && mEditor != null) {
            mEditor.setPlaceholder(hint);
        }
    }


    private void setupView(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.rich_edit_text, this, true);
        mHandler = new Handler();

        setupEditor();
    }

    private void setupEditor() {
        mEditor = (RichWebView) findViewById(R.id.editor);
        mEditor.setEditorFontColor(Color.BLACK);
        mEditor.setPadding(16, 16, 16, 16);

        mEditor.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mActions.setVisibility(GONE);
                    blockAndDisableAllButtons();
                } else {
                    mActions.setVisibility(VISIBLE);
                }
            }
        });

        mEditor.setStateChangeListener(new RichWebView.OnStateChangeListener() {
            @Override
            public void onStateChanged(final String text, final List<RichWebView.Type> types,
                                       final RichWebView.StateType stateType) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleStateChange(text, types, stateType);
                    }
                });
            }
        });
    }

    private void handleStateChange(String text, List<RichWebView.Type> types, RichWebView.StateType stateType) {
        wordCounts = isInteger(text) ? text : wordCounts;
        notifyChangeListener();
        switch (stateType) {
            case ALLOW:
                if (types.contains(RichWebView.Type.BOLD)) {
                    allowBoldButton();
                } else {
                    blockBoldButton();
                }
                if (types.contains(RichWebView.Type.ITALIC)) {
                    allowItalicButton();
                } else {
                    blockItalicButton();
                }
                if (types.contains(RichWebView.Type.UNDERLINE)) {
                    allowUnderlineButton();
                } else {
                    blockUnderlineButton();
                }
                if (types.contains(RichWebView.Type.FORECOLOR)) {
                    allowTextColorButton();
                } else {
                    blockTextColorButton();
                }
                if (types.contains(RichWebView.Type.HILITECOLOR)) {
                    allowBackgroundColorButton();
                } else {
                    blockTextBackgroundColorButton();
                }
                break;
            case ENABLE:
                if (types.contains(RichWebView.Type.BOLD)) {
                    enableBoldButton();
                } else {
                    disableBoldButton();
                }
                if (types.contains(RichWebView.Type.ITALIC)) {
                    enableItalicButton();
                } else {
                    disableItalicButton();
                }
                if (types.contains(RichWebView.Type.UNDERLINE)) {
                    enableUnderlineButton();
                } else {
                    disableUnderlineButton();
                }
                break;
        }
    }

    public void setonChangeInterface(ChangeListener changeListener) {
        this.mListener = changeListener;
    }

    public String getWordCounts() {

        return wordCounts;
    }

    private boolean isInteger(String text) {
        try {
            int result = Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void setupActions() {
        mActions.setVisibility(GONE);

        setupBoldButton();
        setupItalicButton();
        setupUnderlineButton();
        setupTextColorButton();
        setupTextBackgroundColorButton();
    }

    private void notifyChangeListener() {
        if (mListener != null) {
            mListener.onChange();
        }
    }

    private void blockAndDisableAllButtons() {
        blockBoldButton();
        disableBoldButton();
        blockItalicButton();
        disableItalicButton();
        blockUnderlineButton();
        disableUnderlineButton();
        blockTextColorButton();
        blockTextBackgroundColorButton();
    }

    private void setupBoldButton() {
        mBoldButton = (ImageButton) mActions.findViewById(R.id.action_bold);
        blockBoldButton();
        disableBoldButton();
        mBoldButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBoldButton();
                mEditor.setBold();
            }
        });
    }

    private void setupItalicButton() {
        mItalicButton = (ImageButton) mActions.findViewById(R.id.action_italic);
        blockItalicButton();
        disableItalicButton();
        mItalicButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleItalicButton();
                mEditor.setItalic();
            }
        });
    }

    private void setupUnderlineButton() {
        mUnderlineButton = (ImageButton) mActions.findViewById(R.id.action_underline);
        blockUnderlineButton();
        disableUnderlineButton();
        mUnderlineButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleUnderlineButton();
                mEditor.setUnderline();
            }
        });
    }


    private void setupTextColorButton() {
        mTextAlignmentButton = (ImageButton) mActions.findViewById(R.id.action_txt_alignment);
        blockTextColorButton();
        mTextAlignmentButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mTextColorAllowed) {
//                    mEditor.doubleQuotes();
                    showTextBulletTypeChooser(true);


                }
            }
        });
    }

    private void setupTextBackgroundColorButton() {
        mTextBulletButton = (ImageButton) mActions.findViewById(R.id.action_txt_bullet);
        blockTextBackgroundColorButton();
        mTextBulletButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBackgroundColorAllowed) {
                    showTextBulletTypeChooser(false);

                }
            }
        });
    }

    private void allowBoldButton() {
        mBoldAllowed = true;
        mBoldButton.setImageDrawable(getResources().getDrawable(R.drawable.bold_48));
    }

    private void blockBoldButton() {
        mBoldAllowed = false;
        mBoldButton.setImageDrawable(getResources().getDrawable(R.drawable.bold_grey_48));
    }

    private void toggleBoldButton() {
        if (mBoldAllowed) {
            if (mBoldEnabled) {
                disableBoldButton();
            } else {
                enableBoldButton();
            }
        }
    }

    private void enableBoldButton() {
        mBoldEnabled = true;
        int backgroundColor;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            backgroundColor = mBoldAllowed ? getContext().getColor(R.color.colorAccent) :
                    getContext().getColor(R.color.colorEnabledAndNotAllowedButtonBackground);
        } else {
            backgroundColor = mBoldAllowed ? getResources().getColor(R.color.colorAccent) :
                    getResources().getColor(R.color.colorEnabledAndNotAllowedButtonBackground);
        }
        mBoldButton.setBackgroundColor(backgroundColor);

    }

    private void disableBoldButton() {
        mBoldEnabled = false;
        mBoldButton.setBackgroundColor(getResources().getColor(R.color.colorDisabledButtonBackground));
    }

    private void allowItalicButton() {
        mItalicAllowed = true;
        mItalicButton.setImageDrawable(getResources().getDrawable(R.drawable.italic_48));
    }

    private void blockItalicButton() {
        mItalicAllowed = false;
        mItalicButton.setImageDrawable(getResources().getDrawable(R.drawable.italic_grey_48));
    }

    private void toggleItalicButton() {
        if (mItalicAllowed) {
            if (mItalicEnabled) {
                disableItalicButton();
            } else {
                enableItalicButton();
            }
        }
    }

    private void enableItalicButton() {
        mItalicEnabled = true;
        int backgroundColor;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            backgroundColor = mItalicAllowed ? getContext().getColor(R.color.colorAccent) :
                    getContext().getColor(R.color.colorEnabledAndNotAllowedButtonBackground);
        } else {
            backgroundColor = mItalicAllowed ? getResources().getColor(R.color.colorAccent) :
                    getResources().getColor(R.color.colorEnabledAndNotAllowedButtonBackground);
        }
        mItalicButton.setBackgroundColor(backgroundColor);
    }

    private void disableItalicButton() {
        mItalicEnabled = false;
        mItalicButton.setBackgroundColor(getResources().getColor(R.color.colorDisabledButtonBackground));
    }

    private void allowUnderlineButton() {
        mUnderlineAllowed = true;
        mUnderlineButton.setImageDrawable(getResources().getDrawable(R.drawable.underline_48));
    }

    private void blockUnderlineButton() {
        mUnderlineAllowed = false;
        mUnderlineButton.setImageDrawable(getResources().getDrawable(R.drawable.underline_grey_48));
    }

    private void toggleUnderlineButton() {
        if (mUnderlineAllowed) {
            if (mUnderlineEnabled) {
                disableUnderlineButton();
            } else {
                enableUnderlineButton();
            }
        }
    }

    private void enableUnderlineButton() {
        mUnderlineEnabled = true;
        int backgroundColor;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            backgroundColor = mUnderlineAllowed ? getContext().getColor(R.color.colorAccent) :
                    getContext().getColor(R.color.colorEnabledAndNotAllowedButtonBackground);
        } else {
            backgroundColor = mUnderlineAllowed ? getResources().getColor(R.color.colorAccent) :
                    getResources().getColor(R.color.colorEnabledAndNotAllowedButtonBackground);
        }
        mUnderlineButton.setBackgroundColor(backgroundColor);
    }

    private void disableUnderlineButton() {
        mUnderlineEnabled = false;
        mUnderlineButton.setBackgroundColor(getResources().getColor(R.color.colorDisabledButtonBackground));
    }


    private void allowTextColorButton() {
        mTextColorAllowed = true;
        mTextAlignmentButton.setImageDrawable(getResources().getDrawable(R.drawable.text_alignment_48));
    }

    private void blockTextColorButton() {
        mTextColorAllowed = false;
        mTextAlignmentButton.setImageDrawable(getResources().getDrawable(R.drawable.text_alignment_grey_48));
    }

    private void allowBackgroundColorButton() {
        mBackgroundColorAllowed = true;
        mTextBulletButton.setImageDrawable(getResources().getDrawable(R.drawable.bullet_48));
    }

    private void blockTextBackgroundColorButton() {
        mBackgroundColorAllowed = false;
        mTextBulletButton.setImageDrawable(getResources().getDrawable(R.drawable.bullet_grey_48));
    }

    public interface ChangeListener {
        void onChange();
    }


    private void showTextBulletTypeChooser(final boolean isAlignment) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if (isAlignment) {
            builder.setSingleChoiceItems(ALIGNMENT_OPTIONS_ARRAY, mAlignmentOption, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mAlignmentOption = which;
                }
            });
        } else {
            builder.setSingleChoiceItems(BULLET_OPTIONS_ARRAY, mBulletOption, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mBulletOption = which;
                }
            });
        }
        builder.setPositiveButton(DIALOG_BTN_TEXT, null);
        final AlertDialog alert = builder.create();
        alert.show();
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAlignment) {
                    mEditor.changeAlignment(mAlignmentOption);
                } else {
                    if (mBulletOption == 0) {
                        mEditor.setBullet();
                    } else {
                        mEditor.setNumbers();
                    }
                }
                alert.dismiss();
            }
        });

    }


}