package com.halloapp.ui.markdown;

import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

public abstract class SingleEmphasisDelimiterProcessor implements DelimiterProcessor {

    private final char delimiterChar;

    protected SingleEmphasisDelimiterProcessor(char delimiterChar) {
        this.delimiterChar = delimiterChar;
    }

    @Override
    public char getOpeningCharacter() {
        return delimiterChar;
    }

    @Override
    public char getClosingCharacter() {
        return delimiterChar;
    }

    @Override
    public int getMinLength() {
        return 1;
    }

    @Override
    public int getDelimiterUse(DelimiterRun opener, DelimiterRun closer) {
        // "multiple of 3" rule for internal delimiter runs
        if ((opener.canClose() || closer.canOpen()) &&
                closer.originalLength() % 3 != 0 &&
                (opener.originalLength() + closer.originalLength()) % 3 == 0) {
            return 0;
        }
        return 1;
    }

    protected abstract Node parse(String delimiter);

    @Override
    public void process(Text opener, Text closer, int delimiterUse) {
        String singleDelimiter = String.valueOf(getOpeningCharacter());
        Node emphasis = parse(singleDelimiter);

        Node tmp = opener.getNext();
        while (tmp != null && tmp != closer) {
            Node next = tmp.getNext();
            emphasis.appendChild(tmp);
            tmp = next;
        }

        opener.insertAfter(emphasis);
    }
}
