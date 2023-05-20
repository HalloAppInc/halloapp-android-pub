package com.halloapp.katchup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.protobuf.InvalidProtocolBufferException;
import com.halloapp.Me;
import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.KatchupPost;
import com.halloapp.content.Post;
import com.halloapp.id.UserId;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.proto.clients.Container;
import com.halloapp.proto.clients.KMomentContainer;
import com.halloapp.proto.server.MomentInfo;
import com.halloapp.ui.ExternalMediaThumbnailLoader;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ViewUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.widget.AspectRatioFrameLayout;
import com.halloapp.widget.SnackbarHelper;
import com.halloapp.xmpp.Connection;
import com.halloapp.xmpp.feed.FeedContentParser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ArchiveActivity extends HalloActivity {
    private static final String EXTRA_USER_ID = "user_id";

    private static final float VERTICAL_PADDING_PERCENTAGE = 0.15f;
    private static final float ASPECT_RATIO_DAY_CELL = 1.33f;
    private static final int[] WEEKDAYS = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY};

    public static Intent open(@NonNull Context context, @NonNull UserId userId) {
        Intent intent = new Intent(context, ArchiveActivity.class);
        intent.putExtra(EXTRA_USER_ID, userId);
        return intent;
    }

    private MediaThumbnailLoader mediaThumbnailLoader;
    private ExternalMediaThumbnailLoader externalMediaThumbnailLoader;
    private ArchiveViewModel viewModel;
    private MonthAdapter calendarAdapter;

    private int[] orderedWeekdays;
    private String[] orderedWeekdayNames;

    private static class MonthDayData {
        public static final int PADDING_DAY = -1;
        final private int dayNumber;
        final private Post post;
        final private boolean isFaded;
        final private boolean isPresent;

        public MonthDayData(int dayNumber, @Nullable Post post, boolean isFaded, boolean isPresent) {
            this.dayNumber = dayNumber;
            this.post = post;
            this.isFaded = isFaded;
            this.isPresent = isPresent;
        }

        public int getDayNumber() {
            return dayNumber;
        }

        public @Nullable Post getPost() {
            return post;
        }
    }

    private static class MonthData {
        final private String name;
        final private int numberOfDays;;
        final private int fadeCutoffDayIndex;
        final private int presentDayIndex;
        final private int firstWeekday;
        final private Map<Integer, Post> dayPostMap;

        public MonthData(@NonNull String name, int numberOfDays, int fadeCutoffDayIndex, int presentDayIndex, int firstWeekday, @NonNull Map<Integer, Post> dayPostMap) {
            this.name = name;
            this.numberOfDays = numberOfDays;
            this.fadeCutoffDayIndex = fadeCutoffDayIndex;
            this.presentDayIndex = presentDayIndex;
            this.firstWeekday = firstWeekday;
            this.dayPostMap = dayPostMap;
        }

        public String getName() {
            return name;
        }

        public ArrayList<MonthDayData> generateDayData(@NonNull int[] orderedWeekdays) {
            final ArrayList<MonthDayData> dayData = new ArrayList<>();
            for (int orderedWeekday : orderedWeekdays) {
                if (orderedWeekday == firstWeekday) {
                    break;
                } else {
                    dayData.add(new MonthDayData(MonthDayData.PADDING_DAY, null, false, false));
                }
            }
            for (int i = 0; i < numberOfDays; ++i) {
                dayData.add(new MonthDayData(i + 1, dayPostMap.get(i + 1), i < fadeCutoffDayIndex, i == presentDayIndex));
            }
            return dayData;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_calendar);

        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.BLACK);

        mediaThumbnailLoader = new MediaThumbnailLoader(this, 2 * getResources().getDimensionPixelSize(R.dimen.katchup_profile_archive_dim));
        externalMediaThumbnailLoader = new ExternalMediaThumbnailLoader(this, 2 * getResources().getDimensionPixelSize(R.dimen.katchup_profile_archive_dim));

        final UserId userId = getIntent().getParcelableExtra(EXTRA_USER_ID);

        generateWeekdayInfo();
        viewModel = new ViewModelProvider(this, new ArchiveViewModel.Factory(userId)).get(ArchiveViewModel.class);
        viewModel.posts.observe(this, posts -> {
            if (posts != null) {
                calendarAdapter.setData(generateCalendarDataList(posts, userId.isMe()));
            }
        });
        viewModel.error.observe(this, error -> {
            if (error) {
                SnackbarHelper.showWarning(this, R.string.failed_to_request_archive);
            }
        });

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        calendarAdapter = new MonthAdapter();
        RecyclerView calendarView = findViewById(R.id.calendar_view);
        calendarView.setLayoutManager(layoutManager);
        calendarView.setClipToPadding(false);
        calendarView.setClipChildren(false);
        calendarView.setAdapter(calendarAdapter);

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int verticalPageOffset = (int) (displayMetrics.heightPixels * VERTICAL_PADDING_PERCENTAGE);
        final int paddingLeft = calendarView.getPaddingLeft();
        final int paddingRight = calendarView.getPaddingRight();
        calendarView.setPadding(paddingLeft, verticalPageOffset, paddingRight, verticalPageOffset);

        final View back = findViewById(R.id.back);
        back.setOnClickListener(view -> finish());
    }

    @Override
    public void onResume() {
        super.onResume();
        Analytics.getInstance().openScreen("archive");
    }

    private void generateWeekdayInfo() {
        final Locale locale = Locale.getDefault();
        final Calendar calendar = Calendar.getInstance(locale);
        int firstDayInWeek = calendar.getFirstDayOfWeek();
        if (calendar.getFirstDayOfWeek() == WEEKDAYS[0]) {
            orderedWeekdays = WEEKDAYS.clone();
        } else {
            orderedWeekdays = new int[WEEKDAYS.length];
            int dayOffset = 0;
            for (int i = 0; i < WEEKDAYS.length; i++) {
                if (WEEKDAYS[i] == firstDayInWeek) {
                    dayOffset = i;
                    break;
                }
            }
            for (int i = 0; i < WEEKDAYS.length; i++) {
                orderedWeekdays[i] = WEEKDAYS[(i + dayOffset) % WEEKDAYS.length];
            }
        }
        final Map<String, Integer> displayNames = calendar.getDisplayNames(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale);
        final Map<Integer, String> dayNameMap = new HashMap<>(WEEKDAYS.length);
        if (displayNames != null) {
            for (Map.Entry<String, Integer> entry : displayNames.entrySet()) {
                dayNameMap.put(entry.getValue(), entry.getKey());
            }
        }
        orderedWeekdayNames = new String[WEEKDAYS.length];
        for (int i = 0; i < WEEKDAYS.length; i++) {
            orderedWeekdayNames[i] = dayNameMap.get(orderedWeekdays[i]);
            if (orderedWeekdayNames[i] != null) {
                orderedWeekdayNames[i] = orderedWeekdayNames[i].toLowerCase(locale);
            }
        }
    }

    private List<MonthData> generateCalendarDataList(@NonNull List<KatchupPost> posts, boolean isMyCalendar) {
        final Locale locale = Locale.getDefault();
        final Calendar calendar = Calendar.getInstance(locale);
        final List<MonthData> monthDataList = new ArrayList<>();

        final long now = System.currentTimeMillis();
        long initialTimestamp = posts.size() <= 0 ? now : posts.get(0).notificationTimestamp;
        long endTimestamp = posts.size() <= 0 ? now : posts.get(posts.size() - 1).notificationTimestamp;

        final long fadeCutoffTimestamp;
        if (isMyCalendar) {
            fadeCutoffTimestamp = 0;
        } else {
            calendar.setTimeInMillis(now);
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            fadeCutoffTimestamp = Math.min(calendar.getTimeInMillis(), initialTimestamp);
            initialTimestamp = fadeCutoffTimestamp;
        }

        calendar.setTimeInMillis(endTimestamp);
        final int endYear = calendar.get(Calendar.YEAR);
        final int endMonth = calendar.get(Calendar.MONTH);

        calendar.setTimeInMillis(initialTimestamp);
        final ListIterator<KatchupPost> postIterator = posts.listIterator();
        while (true) {
            final long currentTime = calendar.getTimeInMillis();
            final int currentYear = calendar.get(Calendar.YEAR);
            final int currentMonth = calendar.get(Calendar.MONTH);

            if (currentYear > endYear || (currentYear == endYear && currentMonth > endMonth)) {
                break;
            }

            final Map<Integer, Post> dayPostMap = new HashMap<>();

            while (postIterator.hasNext()) {
                final KatchupPost post = postIterator.next();
                calendar.setTimeInMillis(post.notificationTimestamp);

                final int postYear = calendar.get(Calendar.YEAR);
                final int postMonth = calendar.get(Calendar.MONTH);
                final int postDay = calendar.get(Calendar.DAY_OF_MONTH);

                if (postYear > currentYear || (postYear == currentYear && postMonth > currentMonth)) {
                    postIterator.previous();
                    break;
                } else {
                    dayPostMap.put(postDay, post);
                }
            }
            calendar.setTimeInMillis(currentTime);
            monthDataList.add(generateMonthData(locale, calendar, dayPostMap, fadeCutoffTimestamp, now));
            calendar.add(Calendar.MONTH, 1);
        }

        return monthDataList;
    }

    private MonthData generateMonthData(@NonNull Locale locale, @NonNull Calendar calendar, @NonNull Map<Integer, Post> dayPostMap, long fadeCutoffTimestamp, long now) {
        final String monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, locale).toLowerCase(locale);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        final int firstWeekday = calendar.get(Calendar.DAY_OF_WEEK);
        final int numberOfDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        final int fadeCutoffDayIndex = getDayInMonthIndex(fadeCutoffTimestamp - calendar.getTimeInMillis(), numberOfDays);
        final int presentDayIndex = getDayInMonthIndex(now - calendar.getTimeInMillis(), numberOfDays);
        return new MonthData(Preconditions.checkNotNull(monthName), numberOfDays, fadeCutoffDayIndex, presentDayIndex, firstWeekday, dayPostMap);
    }

    private int getDayInMonthIndex(long dayInMillisOffset, int numberOfDays) {
        return dayInMillisOffset < 0 ? -1 : (int) Math.min(dayInMillisOffset / DateUtils.DAY_IN_MILLIS, numberOfDays);
    }

    class MonthViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final AspectRatioFrameLayout frameView;
        private final ArrayAdapter<String> headerAdapter;
        private final MonthDayAdapter dayGridAdapter;
        private final View footerNote;

        public MonthViewHolder(@NonNull View itemView) {
            super(itemView);

            nameView = itemView.findViewById(R.id.calendar_month_name);
            frameView = itemView.findViewById(R.id.calendar_month_frame);
            footerNote = itemView.findViewById(R.id.calendar_footer_note);

            headerAdapter = new ArrayAdapter<>(itemView.getContext(), R.layout.calendar_header_item, R.id.calendar_header_text);
            final GridView gridLayout = itemView.findViewById(R.id.calendar_month_day_header);
            gridLayout.setAdapter(headerAdapter);

            dayGridAdapter = new MonthDayAdapter(itemView.getContext());
            final GridView monthGridView = itemView.findViewById(R.id.calendar_month_day_grid);
            monthGridView.setAdapter(dayGridAdapter);

        }

        public void bindTo(@NonNull MonthData monthData, boolean shouldShowPrivacyNote) {
            nameView.setText(monthData.getName());
            headerAdapter.clear();
            headerAdapter.addAll(Preconditions.checkNotNull(orderedWeekdayNames));
            dayGridAdapter.clear();
            dayGridAdapter.addAll(monthData.generateDayData(Preconditions.checkNotNull(orderedWeekdays)));
            final int numberOfRows = (dayGridAdapter.getCount() - 1) / WEEKDAYS.length + 1;
            final float frameRatio = ASPECT_RATIO_DAY_CELL * numberOfRows / WEEKDAYS.length;
            frameView.setAspectRatio(frameRatio);
            footerNote.setVisibility(shouldShowPrivacyNote ? View.VISIBLE : View.GONE);
        }
    }

    class MonthAdapter extends RecyclerView.Adapter<MonthViewHolder> {
        private List<MonthData> monthDataList = new ArrayList<>();

        public void setData(List<MonthData> monthDataList) {
            this.monthDataList = monthDataList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MonthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MonthViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_month_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MonthViewHolder holder, int position) {
            holder.bindTo(monthDataList.get(position), !viewModel.userId.isMe() && position == monthDataList.size() - 1);
        }

        @Override
        public int getItemCount() {
            return monthDataList.size();
        }
    }

    class MonthDayAdapter extends ArrayAdapter<MonthDayData> {
        public MonthDayAdapter(@NonNull Context context) {
            super(context, 0);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return !isEmpty() && isEnabled(0);
        }

        @Override
        public boolean isEnabled(int position) {
            return getItem(position).getDayNumber() != MonthDayData.PADDING_DAY;
        }

        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final View dayView;
            if (convertView == null) {
                AspectRatioFrameLayout layout = (AspectRatioFrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.calendar_day_item, parent, false);
                layout.setAspectRatio(ASPECT_RATIO_DAY_CELL);
                dayView = layout;
            } else {
                dayView = convertView;
            }
            ViewUtils.clipRoundedRect(dayView, R.dimen.calendar_day_corner_radius);
            final TextView dayNumberView = dayView.findViewById(R.id.calendar_day_text);
            final ImageView dayImageView = dayView.findViewById(R.id.calendar_day_image);
            final ImageView presentDayView = dayView.findViewById(R.id.calendar_present_day_indicator);
            final MonthDayData dayData = getItem(position);
            final Post post = dayData.getPost();
            if (post != null) {
                boolean isLocal = viewModel.isLocal(post);
                if (post.media.size() > 1) {
                    if (isLocal) {
                        mediaThumbnailLoader.load(dayImageView, post.media.get(1));
                    } else {
                        externalMediaThumbnailLoader.load(dayImageView, post.media.get(1));
                    }
                }

                dayView.setOnClickListener(view -> startActivity(ViewKatchupCommentsActivity.viewPost(view.getContext(), post.id, !isLocal, !isLocal, false, post.senderUserId.isMe(), false)));
            }
            if (dayData.getDayNumber() != MonthDayData.PADDING_DAY) {
                dayNumberView.setText(String.valueOf(getItem(position).getDayNumber()));
                if (!dayData.isFaded) {
                    dayView.setBackground(AppCompatResources.getDrawable(getContext(), R.color.white_10));
                    dayNumberView.setTextColor(getResources().getColor(dayData.isPresent ? R.color.black : R.color.white));
                    presentDayView.setVisibility(dayData.isPresent ? View.VISIBLE : View.GONE);
                } else {
                    dayView.setBackground(null);
                    dayNumberView.setTextColor(getResources().getColor(R.color.white_30));
                    presentDayView.setVisibility(View.GONE);
                }
            } else {
                dayView.setBackground(null);
                dayNumberView.setText(null);
                presentDayView.setVisibility(View.GONE);
            }
            return dayView;
        }
    }

    private static class ArchiveViewModel extends ViewModel {
        private final UserId userId;
        private final BgWorkers bgWorkers = BgWorkers.getInstance();
        private final ContentDb contentDb = ContentDb.getInstance();
        private final MutableLiveData<List<KatchupPost>> posts = new MutableLiveData<>();
        private final MutableLiveData<Boolean> error = new MutableLiveData<>();
        private final Set<String> local = new HashSet<>();

        public ArchiveViewModel(@NonNull UserId userId) {
            this.userId = userId;
            fetchUserArchive();
        }

        private void setArchivePosts(@NonNull List<KatchupPost> archivePosts) {
            Collections.sort(archivePosts, (o1, o2) -> Long.compare(o1.notificationTimestamp, o2.notificationTimestamp));
            posts.postValue(archivePosts);
        }

        public boolean isLocal(Post post) {
            return local.contains(post.id);
        }

        public void fetchUserArchive() {
            bgWorkers.execute(() -> {
                final List<KatchupPost> postList = new ArrayList<>();
                if (userId.isMe()) {
                    // TODO(vasil): Add paging data support with PagedList and PagedListAdapter to handle large number of posts.
                    final List<Post> myArchivePosts = contentDb.getMyArchivePosts();
                    for (Post post : myArchivePosts) {
                        if (post instanceof KatchupPost) {
                            postList.add((KatchupPost) post);
                            local.add(post.id);
                        }
                    }
                    setArchivePosts(postList);
                } else {
                    Connection.getInstance().requestArchive(userId).onResponse(response -> {
                        Preconditions.checkNotNull(response);
                        if (!response.success) {
                            error.postValue(true);
                        } else {
                            final FeedContentParser feedContentParser = new FeedContentParser(Me.getInstance());
                            final PublicContentCache cache = PublicContentCache.getInstance();
                            Log.d("ArchiveActivity.fetchUserArchive: startDate=" + response.startDate);

                            for (com.halloapp.proto.server.Post recentPost : response.posts) {
                                final Post post = contentDb.getPost(recentPost.getId());

                                if (post != null) {
                                    if (post instanceof KatchupPost) {
                                        postList.add((KatchupPost) post);
                                        local.add(post.id);
                                    }
                                } else {
                                    byte[] payload = recentPost.getPayload().toByteArray();

                                    Container container;
                                    try {
                                        container = Container.parseFrom(payload);
                                    } catch (InvalidProtocolBufferException e) {
                                        Log.e("ArchiveActivity.fetchUserArchive: invalid post payload", e);
                                        continue;
                                    }

                                    if (container.hasKMomentContainer()) {
                                        long timestamp = 1000L * recentPost.getTimestamp();

                                        KMomentContainer katchupContainer = container.getKMomentContainer();
                                        KatchupPost katchupPost = feedContentParser.parseKatchupPost(recentPost.getId(), userId, timestamp, katchupContainer, false);
                                        MomentInfo momentInfo = recentPost.getMomentInfo();
                                        katchupPost.timeTaken = momentInfo.getTimeTaken();
                                        katchupPost.numSelfieTakes = (int) momentInfo.getNumSelfieTakes();
                                        katchupPost.numTakes = (int) momentInfo.getNumTakes();
                                        katchupPost.notificationId = momentInfo.getNotificationId();
                                        katchupPost.notificationTimestamp = momentInfo.getNotificationTimestamp() * 1000L;
                                        katchupPost.contentType = momentInfo.getContentType();

                                        postList.add(katchupPost);
                                        cache.addPost(katchupPost);
                                    }
                                }
                            }
                            error.postValue(false);
                            setArchivePosts(postList);
                        }
                    }).onError(err -> {
                        Log.e("Failed to get archive posts", err);
                        error.postValue(true);
                    });
                }
            });
        }

        public static class Factory implements ViewModelProvider.Factory {
            private final UserId userId;

            public Factory(@Nullable UserId userId) {
                this.userId = userId;
            }

            @Override
            public @NonNull <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(ArchiveViewModel.class)) {
                    //noinspection unchecked
                    return (T) new ArchiveViewModel(userId);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        }
    }
}
