package com.halloapp.xmpp;

import android.util.Base64;

import androidx.annotation.NonNull;

import com.halloapp.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class PublishedAvatarMetadata {

    private static final String ELEMENT_METADATA = "metadata";
    private static final String ELEMENT_INFO = "info";

    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_WIDTH = "width";
    private static final String ATTRIBUTE_HEIGHT = "height";
    private static final String ATTRIBUTE_BYTES = "bytes";
    private static final String ATTRIBUTE_ID = "id";

    private static final String NAMESPACE = "urn:xmpp:avatar:metadata";

    final String id;
    final String type = "image/png";
    final long numBytes;
    final int height;
    final int width;

    // Assume PNG and not URL for now
    PublishedAvatarMetadata(String id, long numBytes, int height, int width) {
        this.id = id;
        this.numBytes = numBytes;
        this.height = height;
        this.width = width;
    }

    @NonNull String toXml() {
        final XmlSerializer serializer = android.util.Xml.newSerializer();
        final StringWriter writer = new StringWriter();

        try {
            serializer.setOutput(writer);
            serializer.setPrefix("", NAMESPACE);
            serializer.startTag(NAMESPACE, ELEMENT_METADATA);
            serializer.startTag(null, ELEMENT_INFO);
            serializer.attribute(null, ATTRIBUTE_BYTES, Long.toString(numBytes));
            serializer.attribute(null, ATTRIBUTE_ID, id);
            serializer.attribute(null, ATTRIBUTE_TYPE, type);
            serializer.attribute(null, ATTRIBUTE_WIDTH, Integer.toString(width));
            serializer.attribute(null, ATTRIBUTE_HEIGHT, Integer.toString(height));
            serializer.endTag(null, ELEMENT_INFO);
            serializer.endTag(NAMESPACE, ELEMENT_METADATA);
            serializer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

    public static PublishedAvatarMetadata getPublishedItem(@NonNull PubsubItem item) {
        final String xml = item.getPayload().toXML(null);
        final XmlPullParser parser = android.util.Xml.newPullParser();
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(xml));
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, null, ELEMENT_METADATA);
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, null, ELEMENT_INFO);

            String id = parser.getAttributeValue(null, ATTRIBUTE_ID);
            String type = parser.getAttributeValue(null, ATTRIBUTE_TYPE); // TODO(jack): specify type
            long numBytes = Long.parseLong(parser.getAttributeValue(null, ATTRIBUTE_BYTES));
            int height = Integer.parseInt(parser.getAttributeValue(null, ATTRIBUTE_HEIGHT));
            int width = Integer.parseInt(parser.getAttributeValue(null, ATTRIBUTE_WIDTH));

            return new PublishedAvatarMetadata(id, numBytes, height, width);
        } catch (XmlPullParserException | IOException e) {
            Log.e("PublishedAvatarMetadata.getPublishedItem", e);
        }
        return null;
    }

    @Override
    public @NonNull String toString() {
        return "PublishedAvatarMetadata[id=" + id + " type=" + type + " bytes=" + numBytes + " width=" + width + " height=" + height + "]";
    }

    public String getId() {
        return id;
    }
}
