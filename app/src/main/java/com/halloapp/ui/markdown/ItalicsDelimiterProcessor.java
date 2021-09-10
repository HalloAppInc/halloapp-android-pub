package com.halloapp.ui.markdown;

import org.commonmark.node.Emphasis;
import org.commonmark.node.Node;

public class ItalicsDelimiterProcessor extends SingleEmphasisDelimiterProcessor {

    protected ItalicsDelimiterProcessor() {
        super('_');
    }

    @Override
    protected Node parse(String delimiter) {
        return new Emphasis(delimiter);
    }
}
