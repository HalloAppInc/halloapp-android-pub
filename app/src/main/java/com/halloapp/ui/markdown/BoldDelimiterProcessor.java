package com.halloapp.ui.markdown;

import org.commonmark.node.Node;
import org.commonmark.node.StrongEmphasis;

public class BoldDelimiterProcessor extends SingleEmphasisDelimiterProcessor {

    protected BoldDelimiterProcessor() {
        super('*');
    }

    @Override
    protected Node parse(String delimiter) {
        return new StrongEmphasis(delimiter);
    }
}
