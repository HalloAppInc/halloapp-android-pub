package com.halloapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Preconditions;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.halloapp.Connection;
import com.halloapp.R;
import com.halloapp.posts.Post;
import com.halloapp.posts.PostsDb;

import java.util.UUID;

public class PostComposerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_composer);

        EditText editText = findViewById(R.id.entry);
        editText.requestFocus();
        findViewById(R.id.send).setOnClickListener(v -> {
            String sendText = editText.getText().toString();
            final Post post = new Post(
                    0,
                    Connection.FEED_JID.toString(),
                    "",
                    UUID.randomUUID().toString().replaceAll("-", ""),
                    "",
                    0,
                    System.currentTimeMillis(),
                    Post.POST_STATE_OUTGOING_SENDING,
                    Post.POST_TYPE_TEXT,
                    sendText,
                    null,
                    null);
            PostsDb.getInstance(Preconditions.checkNotNull(getBaseContext())).addPost(post);
            finish();
        });
    }
}
