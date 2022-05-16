package com.halloapp.widget;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.halloapp.R;
import com.halloapp.contacts.Contact;
import com.halloapp.content.Mention;
import com.halloapp.ui.markdown.MarkdownUtils;
import com.halloapp.ui.mentions.MentionPickerView;
import com.halloapp.ui.mentions.MentionsFormatter;
import com.halloapp.util.Preconditions;
import com.halloapp.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.editor.MarkwonEditorTextWatcher;

public class MentionableEntry extends PostEditText implements MentionPickerView.OnMentionListener {

    public MentionableEntry(Context context) {
        super(context);
        init();
    }

    public MentionableEntry(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MentionableEntry(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setAutoLinkMask(Linkify.WEB_URLS);
        setLinksClickable(false);
        setLinkTextColor(ContextCompat.getColor(getContext(), R.color.color_link));
        final MarkwonEditor editor = MarkdownUtils.createMarkwonEditor(getContext());

        addTextChangedListener(MarkwonEditorTextWatcher.withProcess(editor));
        addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Editable text = getText();
                if (text != null) {
                    URLSpan[] spans = text.getSpans(0, text.length(), URLSpan.class);

                    for (URLSpan span : spans) {
                        text.removeSpan(span);
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s == null) {
                    return;
                }
                MentionSpan[] spans = s.getSpans(0, s.length(), MentionSpan.class);
                for (MentionSpan span : spans) {
                    int start = s.getSpanStart(span);
                    int end = s.getSpanEnd(span);
                    String displayText = span.getDisplayText();
                    String newSpanRange = s.subSequence(start, end).toString();
                    if (!displayText.equals(s.subSequence(start, end).toString())) {
                        s.removeSpan(span);
                        // span changed
                        if (newSpanRange.contains(displayText)) {
                            int newStart = start + newSpanRange.indexOf(displayText);
                            s.setSpan(span, newStart, newStart + displayText.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                        } else {
                            s.delete(start, end);
                            setSelection(start);
                        }
                    }
                }
                Linkify.addLinks(MentionableEntry.this, getAutoLinkMask());
            }
        });
    }

    private @Nullable MentionPickerView pickerView;

    public void setMentionPickerView(@Nullable MentionPickerView mentionPickerView) {
        if (pickerView != mentionPickerView) {
            if (pickerView != null) {
                pickerView.setOnMentionListener(null);
            }
        }
        this.pickerView = mentionPickerView;
        if (pickerView != null) {
            pickerView.setOnMentionListener(this);
        }
    }
    private CharSequence mentionFilter;

    private void updateMentionFilter(@Nullable CharSequence mentionFilter) {
        if (pickerView == null) {
            return;
        }
        pickerView.setMentionFilter(mentionFilter);
        this.mentionFilter = mentionFilter;
        if (mentionFilter == null) {
            pickerView.hide();
        } else {
            pickerView.show();
        }
    }

    private boolean onCursorChanged(int index) {
        Editable text = getText();
        if (text == null) {
            return false;
        }
        MentionSpan[] currentSpan = text.getSpans(index, index, MentionSpan.class);
        if (currentSpan.length > 0) {
            int spanStart = text.getSpanStart(currentSpan[0]);
            int spanEnd = text.getSpanEnd(currentSpan[0]);
            if (index > spanStart && index < spanEnd) {
                if (spanEnd - index < index - spanStart) {
                    super.setSelection(spanEnd);
                } else {
                    super.setSelection(spanStart);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        if (id == android.R.id.paste) {
            if (Build.VERSION.SDK_INT >= 23) {
                id = android.R.id.pasteAsPlainText;
            } else {
                onInterceptClipDataToPlainText();
            }
        }
        return super.onTextContextMenuItem(id);
    }


    private void onInterceptClipDataToPlainText() {
        ClipboardManager clipboard = (ClipboardManager) getContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = clipboard.getPrimaryClip();
        if (clip != null) {
            for (int i = 0; i < clip.getItemCount(); i++) {
                final CharSequence text = clip.getItemAt(i).coerceToText(getContext());
                final CharSequence paste = (text instanceof Spanned) ? text.toString() : text;
                if (paste != null) {
                    ClipData clipData = ClipData.newPlainText("plaintext", text);
                    ClipboardManager manager = (ClipboardManager) getContext()
                            .getSystemService(Context.CLIPBOARD_SERVICE);
                    manager.setPrimaryClip(clipData);
                }
            }
        }
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        // Single cursor case
        if (selStart == selEnd) {
            onCursorChanged(selStart);
        }
        super.onSelectionChanged(selStart, selEnd);
        Editable text = getText();
        if (TextUtils.isEmpty(text)) {
            updateMentionFilter(null);
            return;
        }
        int atIndex = -1;
        for (int i = selStart - 1; i >= 0; i--) {
            char character = Preconditions.checkNotNull(text).charAt(i);
            if (Character.isWhitespace(character)) {
                break;
            }
            if (character == '@') {
                if (text.getSpans(i, i, MentionSpan.class).length == 0) {
                    atIndex = i;
                }
            }
        }
        if (atIndex >= 0) {
            updateMentionFilter(text.subSequence(atIndex + 1, selStart));
        } else {
            updateMentionFilter(null);
        }
    }

    @Override
    public void onMention(@NonNull Contact contact) {
        if (mentionFilter == null) {
            return;
        }
        int selStart = getSelectionStart();
        Editable text = getText();
        if (TextUtils.isEmpty(text)) {
            updateMentionFilter(null);
            return;
        }
        Preconditions.checkNotNull(text);
        int atIndex = -1;
        for (int i = selStart - 1; i >= 0; i--) {
            char character = text.charAt(i);
            if (Character.isWhitespace(character)) {
                break;
            }
            if (character == '@') {
                atIndex = i;
            }
        }
        CharSequence mentionReplacement = createMentionPlaceholder(contact);
        text.replace(atIndex, selStart, mentionReplacement);
        text.insert(atIndex + mentionReplacement.length(), " ");
        setText(text);
        setSelection(atIndex + mentionReplacement.length() + 1);
        updateMentionFilter(null);
    }

    @NonNull
    public Pair<String, List<Mention>> getTextWithMentions() {
        if (getText() == null) {
            return new Pair<>(null, Collections.emptyList());
        }
        Editable editable = Editable.Factory.getInstance().newEditable(getText());
        replaceMentionsWithPlaceholders(editable);
        String text = Preconditions.checkNotNull(editable).toString();
        String textToPost = StringUtils.preparePostText(text);
        List<Mention> mentions = extractMentionsFromPlaceholders(editable);
        List<Mention> adjustedMentions = MentionsFormatter.recomputeMentionIndices(mentions, text, textToPost);
        return new Pair<>(textToPost, adjustedMentions);
    }

    private List<Mention> extractMentionsFromPlaceholders(@NonNull Editable editable) {
        PlaceholderSpan[] placeholderSpans = editable.getSpans(0, editable.length(), PlaceholderSpan.class);
        List<Mention> mentions = new ArrayList<>();
        for (PlaceholderSpan span : placeholderSpans) {
            int index = editable.getSpanStart(span);
            mentions.add(new Mention(index, span.contact.userId, span.contact.halloName));
        }
        return mentions;
    }

    private void replaceMentionsWithPlaceholders(@NonNull Editable editable) {
        MentionSpan[] spans = editable.getSpans(0, editable.length(), MentionSpan.class);
        for (MentionSpan span : spans) {
            int start = editable.getSpanStart(span);
            int end = editable.getSpanEnd(span);
            editable.removeSpan(span);
            SpannableString placeHolder = new SpannableString("@");
            placeHolder.setSpan(new PlaceholderSpan(span.contact), 0, placeHolder.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            editable.replace(start, end, placeHolder);
        }
    }

    private CharSequence createMentionPlaceholder(@NonNull Contact contact) {
        String mentionText = "@" + contact.getDisplayName();
        SpannableString mentionString = new SpannableString(mentionText);
        mentionString.setSpan(new MentionSpan(mentionText, contact), 0, mentionString.length(),  Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        return new SpannedString(mentionString);
    }

    public void appendMention(@NonNull Contact contact) {
        Editable text = getText();
        if (text == null) {
            return;
        }
        text.append(createMentionPlaceholder(contact));
        text.append(" ");
    }

    private static class PlaceholderSpan {

        public final Contact contact;

        PlaceholderSpan(@NonNull Contact contact) {
            this.contact = contact;
        }
    }

    private static class MentionSpan extends ClickableSpan {

        private final Contact contact;

        private final String mentionText;

        MentionSpan(@NonNull String mentionText, @NonNull Contact contact) {
            this.contact = contact;
            this.mentionText = mentionText;
        }

        public String getDisplayText() {
            return mentionText;
        }

        @Override
        public void onClick(@NonNull View widget) {

        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            ds.setUnderlineText(false);
            ds.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        }
    }
}


