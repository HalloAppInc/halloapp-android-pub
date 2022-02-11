package com.halloapp;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;
import com.halloapp.content.Media;
import com.halloapp.proto.clients.EncryptedResource;
import com.halloapp.proto.clients.Image;
import com.halloapp.proto.clients.Link;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.Objects;

public class UrlPreview {

    public static UrlPreview parseFromDocument(String url, @NonNull Document document) {
        UrlPreview preview = new UrlPreview();
        preview.url = url;
        Uri uri = Uri.parse(url);
        preview.tld = uri.getHost();

        preview.title = parseContent(document.selectFirst("meta[property=og:title]"));
        if (preview.title == null) {
            preview.title = parseContent(document.selectFirst("meta[name=twitter:title]"));
        }
        if (preview.title == null) {
            preview.title = parseContent(document.selectFirst("meta[itemprop=name]"));
        }
        if (preview.title == null) {
            preview.title = document.title();
        }
        preview.description = parseContent(document.selectFirst("meta[property=og:description]"));
        if (preview.description == null) {
            preview.description = parseContent(document.selectFirst("meta[name=twitter:description]"));
        }
        if (preview.description == null) {
            preview.description = parseContent(document.selectFirst("meta[itemprop=description]"));
        }
        preview.previewImage = parseUrl(document.selectFirst("meta[property=og:image]"), "content");
        if (preview.previewImage == null) {
            preview.previewImage = parseUrl(document.selectFirst("link[rel=image_src]"), "href");
        }
        if (preview.previewImage == null) {
            preview.previewImage = parseUrl(document.selectFirst("link[rel=apple-touch-icon]"), "href");
        }
        if (preview.previewImage == null) {
            preview.previewImage = parseUrl(document.selectFirst("link[rel=icon]"), "href");
        }
        return preview;
    }

    @Nullable
    public static UrlPreview create(long rowId, @Nullable String title, @Nullable String url) {
        UrlPreview preview = new UrlPreview();
        Uri uri = Uri.parse(url);
        preview.tld = uri.getHost();
        preview.url = url;
        preview.title = title;
        preview.rowId = rowId;
        return preview;
    }

    private static String parseUrl(@Nullable Element element, @NonNull String key) {
        if (element == null) {
            return null;
        }
        return element.absUrl(key);
    }

    private static String parseContent(@Nullable Element element) {
        if (element == null) {
            return null;
        }
        return element.attr("content");
    }

    public UrlPreview() {
    }

    public UrlPreview(UrlPreview copy) {
        this.rowId = copy.rowId;
        this.url = copy.url;
        this.tld = copy.tld;
        this.title = copy.title;
        this.description = copy.description;
        this.previewImage = copy.previewImage;
        this.imageMedia = new Media(copy.imageMedia);
    }

    public long rowId;
    public String url;
    public String tld;
    public String title;
    public String description;
    public String previewImage;
    public Media imageMedia;

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getPreviewImageUrl() {
        return previewImage;
    }

    public Link toProto() {
        Link.Builder builder = Link.newBuilder();
        if (title != null) {
            builder.setTitle(title);
        }
        if (url != null) {
            builder.setUrl(url);
        }
        if (imageMedia != null) {
            EncryptedResource encryptedResource = EncryptedResource.newBuilder()
                    .setEncryptionKey(ByteString.copyFrom(imageMedia.encKey))
                    .setCiphertextHash(ByteString.copyFrom(imageMedia.encSha256hash))
                    .setDownloadUrl(imageMedia.url).build();

            builder.addPreview(Image.newBuilder()
                    .setWidth(imageMedia.width)
                    .setHeight(imageMedia.height)
                    .setImg(encryptedResource).build());
        }
        return builder.build();
    }

    public static UrlPreview fromProto(@NonNull Link link) {
        UrlPreview preview = new UrlPreview();
        preview.url = link.getUrl();
        preview.title = link.getTitle();
        List<Image> previews = link.getPreviewList();
        if (!previews.isEmpty()) {
            preview.imageMedia = Media.parseFromProto(previews.get(0));
        }
        return preview;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UrlPreview preview = (UrlPreview) o;
        return Objects.equals(title, preview.title) &&
                Objects.equals(url, preview.url) &&
                Objects.equals(imageMedia, preview.imageMedia);
    }
}
