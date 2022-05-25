package com.qboxus.musictok.SimpleClasses;

import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FriendsTagHelper implements ClickableForegroundColorSpanCustom.OnFriendsTagClickListener {

    /**
     * If this is not null then  all of the symbols in the List will be considered as valid symbols of hashtag
     * For example :
     * mAdditionalHashTagChars = {'$','_','-'}
     * it means that hashtag: "@this_is_hashtag-with$dollar-sign" will be highlighted.
     *
     * Note: if mAdditionalHashTagChars would be "null" only "@this" would be highlighted
     *
     */
    private final List<Character> mAdditionalHashTagChars;
    private TextView mTextView;
    private int mHashTagWordColor;
    private int mFriendsTagWordColor;

    private OnFriendsTagClickListener mOnFriendsTagClickListener;

    public static final class Creator{

        private Creator(){}

        public static FriendsTagHelper create(int hashColor,int friendsColor, OnFriendsTagClickListener listener){
            return new FriendsTagHelper(hashColor,friendsColor, listener, null);
        }

        public static FriendsTagHelper create(int hashColor,int friendsColor, OnFriendsTagClickListener listener, char... additionalHashTagChars){
            return new FriendsTagHelper(hashColor,friendsColor, listener, additionalHashTagChars);
        }

    }

    public interface OnFriendsTagClickListener{
        void onFriendsTagClicked(String friendsTag);
    }

    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {
            if (text.length() > 0) {
                eraseAndColorizeAllText(text);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private FriendsTagHelper(int hashColor,int friendsColor, OnFriendsTagClickListener listener, char... additionalHashTagCharacters) {
        mHashTagWordColor = hashColor;
        mFriendsTagWordColor = friendsColor;
        mOnFriendsTagClickListener = listener;
        mAdditionalHashTagChars = new ArrayList<>();

        if(additionalHashTagCharacters != null){
            for(char additionalChar : additionalHashTagCharacters){
                mAdditionalHashTagChars.add(additionalChar);
            }
        }
    }

    public void handle(TextView textView){
        if(mTextView == null){
            mTextView = textView;
            mTextView.addTextChangedListener(mTextWatcher);

            // in order to use spannable we have to set buffer type
            mTextView.setText(mTextView.getText(), TextView.BufferType.SPANNABLE);

            if(mOnFriendsTagClickListener != null){
                // we need to set this in order to get onClick event
                mTextView.setMovementMethod(LinkMovementMethod.getInstance());

                // after onClick clicked text become highlighted
                mTextView.setHighlightColor(Color.TRANSPARENT);
            } else {
                // hash tags are not clickable, no need to change these parameters
            }

            setColorsToAllHashTags(mTextView.getText());
        } else {
            throw new RuntimeException("TextView is not null. You need to create a unique HashTagHelper for every TextView");
        }

    }

    private void eraseAndColorizeAllText(CharSequence text) {

        Spannable spannable = ((Spannable) mTextView.getText());

        CharacterStyle[] spans = spannable.getSpans(0, text.length(), CharacterStyle.class);
        for (CharacterStyle span : spans) {
            spannable.removeSpan(span);
        }

        setColorsToAllHashTags(text);
    }

    private void setColorsToAllHashTags(CharSequence text) {

        int startIndexOfNextHashSign;

        int index = 0;
        while (index < text.length()-  1){
            char sign = text.charAt(index);
            int nextNotLetterDigitCharIndex = index + 1; // we assume it is next. if if was not changed by findNextValidHashTagChar then index will be incremented by 1
            if(sign == '#'){
                startIndexOfNextHashSign = index;

                nextNotLetterDigitCharIndex = findNextValidHashTagChar(text, startIndexOfNextHashSign);

                setColorForHashTagToTheEnd(startIndexOfNextHashSign, nextNotLetterDigitCharIndex);
            }
            if(sign == '@'){
                startIndexOfNextHashSign = index;

                nextNotLetterDigitCharIndex = findNextValidHashTagChar(text, startIndexOfNextHashSign);

                setColorForFriendsTagToTheEnd(startIndexOfNextHashSign, nextNotLetterDigitCharIndex);
            }

            index = nextNotLetterDigitCharIndex;
        }
    }

    private int findNextValidHashTagChar(CharSequence text, int start) {

        int nonLetterDigitCharIndex = -1; // skip first sign '@"
        for (int index = start + 1; index < text.length(); index++) {

            char sign = text.charAt(index);

            boolean isValidSign = Character.isLetterOrDigit(sign) || mAdditionalHashTagChars.contains(sign);
            if (!isValidSign) {
                nonLetterDigitCharIndex = index;
                break;
            }
        }
        if (nonLetterDigitCharIndex == -1) {
            // we didn't find non-letter. We are at the end of text
            nonLetterDigitCharIndex = text.length();
        }

        return nonLetterDigitCharIndex;
    }

    private void setColorForHashTagToTheEnd(int startIndex, int nextNotLetterDigitCharIndex) {
        Spannable s = (Spannable) mTextView.getText();

        CharacterStyle span;

        if(mOnFriendsTagClickListener != null){
            span = new ClickableForegroundColorSpanCustom(mHashTagWordColor, this);
        } else {
            // no need for clickable span because it is messing with selection when click
            span = new ForegroundColorSpan(mHashTagWordColor);
        }

        s.setSpan(span, startIndex, nextNotLetterDigitCharIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }


    private void setColorForFriendsTagToTheEnd(int startIndex, int nextNotLetterDigitCharIndex) {
        Spannable s = (Spannable) mTextView.getText();

        CharacterStyle span;

        if(mOnFriendsTagClickListener != null){
            span = new ClickableForegroundColorSpanCustom(mFriendsTagWordColor, this);
        } else {
            // no need for clickable span because it is messing with selection when click
            span = new ForegroundColorSpan(mFriendsTagWordColor);
        }

        s.setSpan(span, startIndex, nextNotLetterDigitCharIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public List<String> getAllHashTags() {

        String text = mTextView.getText().toString();
        Spannable spannable = (Spannable) mTextView.getText() ;

        // use set to exclude duplicates
        Set<String> hashTags = new LinkedHashSet<>();

        for(CharacterStyle span : spannable.getSpans(0, text.length(), CharacterStyle.class)){
            hashTags.add(
                    text.substring(
                            spannable.getSpanStart(span) + 1/*skip "@" sign*/,
                            spannable.getSpanEnd(span)));
        }

        return new ArrayList<>(hashTags);
    }

    @Override
    public void onFriendsTagClicked(String friendsTag) {
        mOnFriendsTagClickListener.onFriendsTagClicked(friendsTag);
    }
}
