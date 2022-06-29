package com.halloapp.lint_checks;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class LintRegistry extends IssueRegistry {

    @NotNull
    @Override
    public List<Issue> getIssues() {
        return Arrays.asList(
                RtlStringIssueDetector.ISSUE_PERCENT_AT_END,
                RtlStringIssueDetector.ISSUE_BACKWARDS_FORMAT,
                PrintStackTraceDetector.ISSUE_PRINT_STACK_TRACE_CALL
                );
    }
}
