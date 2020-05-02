package com.halloapp.content;

import java.io.File;

public class ReplyPreview {

    public final String text;
    public final @Media.MediaType int mediaType;
    public final File file;

    ReplyPreview(String text, int mediaType, File file) {
        this.text = text;
        this.mediaType = mediaType;
        this.file = file;
    }
}
