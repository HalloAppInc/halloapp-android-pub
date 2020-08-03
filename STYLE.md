# HalloApp Android Style Guide

This style guide is intended to help all contributors to the
Android code base to write code in a consistent manner. There may
be exceptions to these rules, but there should be a compelling
reason for deviating.

## General guidelines

Code should fit in with the code around it. Sometimes code that we
have modified from elsewhere will follow a different set of guidelines
than the ones we use for code we author ourselves. In such a case it
is important to keep the style inside the file consistent.

The ultimate goal is to have eminently readable code. These rules are
intended to make the code more readable either directly or through consistency.

## Naming

- File names must match the case-sensitive name of the top-level class inside
- Classes are named using PascalCase
- Variables are named using dromedaryCase
- Final static variables (constants) use MACRO_CASE
- XML identifiers use kebab_case
- Names should describe what the variable represents

## Formatting

- Import statements are kept in alphabetical order by name
- There is one blank line between imports with different top-level packages (`com`, `org`)
- At most a single blank line may separate logical groupings of code
- Indentation uses 4 spaces
- Blocks must be used even when optional (of `if`, `while`, etc.)
- A conditional (of `if`, `while`, etc) should have a single space on either side
- Empty blocks may be written as `{}`
- Otherwise `{` is followed by a newline and `}` is preceded by a newline
- For `if/else` statements, it is preferred to write the shorter block first
- Statements are followed by a line break
- Avoid linewraps; with high-resolution monitors 80 characters should not be a hard limit
- If linewraps are required, they should be spread evenly and systematically, for example
  - Each parameter to a function on a separate line
  - Each statement in a SQL query on a separate line
- All text files end in a newline

## Comments

- Should be avoided unless absolutely necessary; comments easily detach from
the code they describe
  - Try making variable names more descriptive
  - Try factoring out a function and make its name explicit
- A comment may be necessary to:
  - Document a known bug in platform or library code
  - Explain a workaround for such a bug
  - Caution about non-intuitive library behavior (our code should be intuitive)
  - Reference a source from which code was drawn (i.e. StackOverflow)
- TODOs represent a commitment to future work and should contain the author's name

## Annotations

- `@Override` should be used if and only if the function overrides one from a superclass
- `@SuppressWarnings` and ilk should be used if and only if the warning cannot be resolved
- Other annotations typically should only be used if not clear from context
  - Use `@NonNull` and `@Nullable` on return types and parameters
  - Use `@WorkerThread` for anything that must not happen on the UI thread (i.e. disk read)
  - Use `@MainThread` for anything that must happen on the UI thread (i.e. touching views)

## Singletons

- When using singletons, only use `getInstance` in:
  - ViewModels
  - Activities
  - Fragments
  - Views
  - Services
  - Broadcast Receivers
  - `getInstance` calls for other singletons
- If you need to access a singleton from elsewhere, use dependency injection and pass the dependency down.
- Don't intersperse `getInstance` calls whenever you need them, instead add a field. (see sample below)
- Group singletons together and order them by length. Tie break with alphabetical order

## Other

- All checked exceptions should be explicitly mentioned in a catch block
- Log statements must not contain side effects
- Wildcard imports are not allowed
- Generated code and written code should not be updated in the same commit

## Samples

```java
package com.halloapp.test;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.halloapp.ui.HalloActivity;
import com.halloapp.util.Log;

public abstract class TestActivity extends HalloActivity {
    private static final int REQUEST_CODE_TEST = 1;

    private final String input;

    private final SingletonFoo singletonFoo = SingletonFoo.getInstance();
    private final SingletonFooTwo singletonFooTwo = SingletonFooTwo.getInstance();

    public TestActivity(String input) {
        this.input = input;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static final Boolean wayTooManyArgs(
        int thefirstOne,
        String theSecondOne,
        Boolean theThirdOne,
        [...]
        String theLastOne
    ) {
        return theThirdOne;
    }

    @WorkerThread
    public void riskyFunction(@NonNull String s) throws IOException;

    @WorkerThread
    private void helperFunction(@Nullable String s) {
        if (s != null) {
            try {
                riskyFunction(s)
            } catch (IOException e) {
                Log.e("TestActivity: riskyFunction failed", e);
            }
        }
    }
}
```

Note that `onCreate` is not annotated with `@MainThread` here because it is easy
to infer due to this being UI code since it's an Activity.
