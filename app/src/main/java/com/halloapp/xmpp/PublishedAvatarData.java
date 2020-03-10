package com.halloapp.xmpp;

import androidx.annotation.NonNull;

import com.halloapp.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class PublishedAvatarData {

    private static final String ELEMENT_DATA = "data";

    private static final String NAMESPACE = "urn:xmpp:avatar:data";

    private final String base64Data;

    // Assume PNG and not URL for now
    PublishedAvatarData(String base64Data) {
        this.base64Data = base64Data;
    }

    @NonNull String toXml() {
        final XmlSerializer serializer = android.util.Xml.newSerializer();
        final StringWriter writer = new StringWriter();

        try {
            serializer.setOutput(writer);
            serializer.setPrefix("", NAMESPACE);
            serializer.startTag(NAMESPACE, ELEMENT_DATA);
            serializer.text(base64Data);
            serializer.endTag(NAMESPACE, ELEMENT_DATA);
            serializer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

    public static
    PublishedAvatarData getPublishedItem(@NonNull PubsubItem item) {
        final String xml = item.getPayload().toXML(null);
        final XmlPullParser parser = android.util.Xml.newPullParser();
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(xml));
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, null, ELEMENT_DATA);

            String d = parser.nextText();
            return new PublishedAvatarData(d);
        } catch (XmlPullParserException | IOException e) {
            Log.e("PublishedAvatarData.getPublishedItem", e);
        }
        return null;
    }

    @Override
    public @NonNull String toString() {
        return "PublishedAvatarData[data=" + base64Data + "]";
    }

    public String getBase64Data() {
        return base64Data;
    }
}
