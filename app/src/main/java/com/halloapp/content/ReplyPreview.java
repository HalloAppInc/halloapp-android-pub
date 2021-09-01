package com.halloapp.content;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReplyPreview {

    public final long rowId;
    public final String text;
    public final @Media.MediaType int mediaType;
    public final File file;
    public final List<Mention> mentions = new ArrayList<>();

    ReplyPreview(long rowId, String text, int mediaType, File file) {
        this.rowId = rowId;
        this.text = text;
        this.mediaType = mediaType;
        this.file = file;
    }
}
