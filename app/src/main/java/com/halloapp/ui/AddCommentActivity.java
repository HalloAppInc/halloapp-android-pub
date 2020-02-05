package com.halloapp.ui;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Preconditions;

import com.halloapp.R;
import com.halloapp.contacts.UserId;
import com.halloapp.posts.Comment;
import com.halloapp.posts.PostsDb;
import com.halloapp.util.Log;
import com.halloapp.util.RandomId;
import com.halloapp.widget.PostEditText;

public class AddCommentActivity extends AppCompatActivity {

    public static final String EXTRA_POST_SENDER_USER_ID = "post_sender_user_id";
    public static final String EXTRA_POST_ID = "post_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);

        Preconditions.checkNotNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final PostEditText editText = findViewById(R.id.entry);

        final View sendButton = findViewById(R.id.send);
        sendButton.setOnClickListener(v -> {
            final String postText = Preconditions.checkNotNull(editText.getText()).toString();
            if (postText.trim().isEmpty()) {
                Log.w("AddCommentActivity: cannot send empty comment");
                return;
            }
            final UserId userId = new UserId(Preconditions.checkNotNull(getIntent().getStringExtra(EXTRA_POST_SENDER_USER_ID)));
            final Comment comment = new Comment(
                    0,
                    userId,
                    getIntent().getStringExtra(EXTRA_POST_ID),
                    UserId.ME,
                    RandomId.create(),
                    null,
                    System.currentTimeMillis(),
                    false,
                    postText);
            PostsDb.getInstance(Preconditions.checkNotNull(getBaseContext())).addComment(comment);
            finish();
        });

        editText.requestFocus();
        editText.setPreImeListener((keyCode, event) -> {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
