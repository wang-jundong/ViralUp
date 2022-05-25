package com.qboxus.musictok.SimpleClasses;

import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.ColorInt;

public class ClickableForegroundColorSpanCustom extends ClickableSpan {

    private OnFriendsTagClickListener mOnHashTagClickListener;

    public interface OnFriendsTagClickListener {
        void onFriendsTagClicked(String hashTag);
    }

    private final int mColor;

    public ClickableForegroundColorSpanCustom(@ColorInt int color, OnFriendsTagClickListener listener) {
        mColor = color;
        mOnHashTagClickListener = listener;

        if (mOnHashTagClickListener == null) {
            throw new RuntimeException("constructor, click listener not specified. Are you sure you need to use this class?");
        }
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(mColor);
    }

    @Override
    public void onClick(View widget) {
        CharSequence text = ((TextView) widget).getText();

        Spanned s = (Spanned) text;
        int start = s.getSpanStart(this);
        int end = s.getSpanEnd(this);

        mOnHashTagClickListener.onFriendsTagClicked(text.subSequence(start/*skip "#" sign*/, end).toString());
    }
}