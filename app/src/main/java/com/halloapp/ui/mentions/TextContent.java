package com.halloapp.ui.mentions;

import com.halloapp.content.Mention;

import java.util.List;

public interface TextContent {
    List<Mention> getMentions();
    String getText();
}
