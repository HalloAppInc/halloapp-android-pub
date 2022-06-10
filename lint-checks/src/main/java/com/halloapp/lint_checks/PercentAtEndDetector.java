package com.halloapp.lint_checks;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.ResourceXmlDetector;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.TextFormat;
import com.android.tools.lint.detector.api.XmlContext;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.Collection;

public class PercentAtEndDetector extends ResourceXmlDetector {
    static final Issue ISSUE_PERCENT_AT_END = Issue.create(
            "PercentAtEnd",
            "Strings ending in '%' can pass Android lint but crash at runtime",
            "Strings ending in '%' can pass Android lint but crash at runtime. "
                    + "This has been happening most often when an RTL string looks like it starts "
                    + "with '%s' followed by the translated RTL text but is in actuality RTL text "
                    + "followed by 's%', which looks identical. It is unclear why this is not caught "
                    + "by the provided Android lint rules.",
            Category.I18N,
            8,
            Severity.ERROR,
            new Implementation(PercentAtEndDetector.class, Scope.ALL_RESOURCES_SCOPE));

    @Nullable
    @Override
    public Collection<String> getApplicableElements() {
        return Arrays.asList("string", "item");
    }

    @Override
    public void visitElement(@NotNull XmlContext context, @NotNull Element element) {
        String text = element.getTextContent();
        if (text.length() > 2) {
            int lastCharPos = text.length() - (text.charAt(text.length() - 1) == '"' ? 2 : 1);
            char lastChar = text.charAt(lastCharPos);
            if (lastChar == '%') {
                context.report(ISSUE_PERCENT_AT_END, context.getLocation(element), ISSUE_PERCENT_AT_END.getExplanation(TextFormat.TEXT));
            }
        }
    }
}
