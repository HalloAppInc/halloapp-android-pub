package com.halloapp.lint_checks;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.SourceCodeScanner;
import com.android.tools.lint.detector.api.TextFormat;
import com.intellij.psi.PsiMethod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;

import java.util.Collections;
import java.util.List;

public class PrintStackTraceDetector extends Detector implements SourceCodeScanner {
    static final Issue ISSUE_PRINT_STACK_TRACE_CALL = Issue.create(
            "PrintStackTraceCall", // OTHERS TODO
            "Calls to printStackTrace are not captured by our logging",
            "Calls to printStackTrace are not captured by our logging. "
                    + "All logging must go through our Log helper class.",
            Category.CORRECTNESS,
            5,
            Severity.ERROR,
            new Implementation(PrintStackTraceDetector.class, Scope.JAVA_FILE_SCOPE));

    @Nullable
    @Override
    public List<String> getApplicableMethodNames() {
        return Collections.singletonList("printStackTrace");
    }

    @Override
    public void visitMethodCall(@NotNull JavaContext context, @NotNull UCallExpression node, @NotNull PsiMethod method) {
        super.visitMethodCall(context, node, method);
        if (context.getEvaluator().isMemberInClass(method, "java.lang.Throwable")) {
            context.report(ISSUE_PRINT_STACK_TRACE_CALL, context.getLocation(method), ISSUE_PRINT_STACK_TRACE_CALL.getExplanation(TextFormat.TEXT));
        }
    }
}
