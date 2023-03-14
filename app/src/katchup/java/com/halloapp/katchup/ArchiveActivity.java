package com.halloapp.katchup;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.halloapp.R;
import com.halloapp.content.ContentDb;
import com.halloapp.content.KatchupPost;
import com.halloapp.content.Post;
import com.halloapp.media.MediaThumbnailLoader;
import com.halloapp.ui.HalloActivity;
import com.halloapp.util.ComputableLiveData;
import com.halloapp.util.Preconditions;
import com.halloapp.util.ViewUtils;
import com.halloapp.widget.AspectRatioFrameLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

public class ArchiveActivity extends HalloActivity {
    private static final float VERTICAL_PADDING_PERCENTAGE = 0.15f;
    private static final float ASPECT_RATIO_DAY_CELL = 1.33f;
    private static final int[] WEEKDAYS = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY};

    private MediaThumbnailLoader mediaThumbnailLoader;
    private ArchiveViewModel viewModel;
    private MonthAdapter calendarAdapter;

    private int[] orderedWeekdays;
    private String[] orderedWeekdayNames;

    private static class MonthDayData {
        public static final int PADDING_DAY = -1;
        final private int dayNumber;
        final private Post post;

        public MonthDayData(int dayNumber, @Nullable Post post) {
            this.dayNumber = dayNumber;
            this.post = post;
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
        final private int numberOfDays;
        final private int firstWeekday;
        final private Map<Integer, Post> dayPostMap;

        public MonthData(@NonNull String name, int numberOfDays, int firstWeekday, @NonNull Map<Integer, Post> dayPostMap) {
            this.name = name;
            this.numberOfDays = numberOfDays;
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
                    dayData.add(new MonthDayData(MonthDayData.PADDING_DAY, null));
                }
            }
            for (int i = 0; i < numberOfDays; ++i) {
                dayData.add(new MonthDayData(i + 1, dayPostMap.get(i + 1)));
            }
            return dayData;
        }
    }

    private static class ArchiveViewModel extends ViewModel {
        private final ContentDb contentDb;
        private final ComputableLiveData<List<Post>> posts;

        public ArchiveViewModel() {
            contentDb = ContentDb.getInstance();
            posts = new ComputableLiveData<List<Post>>() {
                @Override
                protected List<Post> compute() {
                    // TODO(vasil): Add paging data support with PagedList and PagedListAdapter to handle large number of posts.
                    return contentDb.getMyArchivePosts();
                }
            };
        }

        public LiveData<List<Post>> getPosts() {
            return posts.getLiveData();
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

        mediaThumbnailLoader = new MediaThumbnailLoader(this, 2 * getResources().getDimensionPixelSize(R.dimen.katchup_profile_archive_dim));

        generateWeekdayInfo();
        viewModel = new ArchiveViewModel();
        viewModel.getPosts().observe(this, posts -> {
            if (posts != null) {
                calendarAdapter.setData(generateCalendarDataList(posts));
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

    private List<MonthData> generateCalendarDataList(@NonNull List<Post> posts) {
        final Locale locale = Locale.getDefault();
        final Calendar calendar = Calendar.getInstance(locale);
        final List<MonthData> monthDataList = new ArrayList<>();

        final long now = System.currentTimeMillis();
        final long initialTimestamp = posts.size() > 0 ? ((KatchupPost) posts.get(0)).notificationTimestamp : now;
        final long endTimestamp = posts.size() > 0 ? ((KatchupPost) posts.get(posts.size() - 1)).notificationTimestamp : now;
        calendar.setTimeInMillis(endTimestamp);

        final int endYear = calendar.get(Calendar.YEAR);
        final int endMonth = calendar.get(Calendar.MONTH);

        calendar.setTimeInMillis(initialTimestamp);

        final ListIterator<Post> postIterator = posts.listIterator();
        while (true) {
            final long currentTime = calendar.getTimeInMillis();
            final int currentYear = calendar.get(Calendar.YEAR);
            final int currentMonth = calendar.get(Calendar.MONTH);

            if (currentYear > endYear || (currentYear == endYear && currentMonth > endMonth)) {
                break;
            }

            final Map<Integer, Post> dayPostMap = new HashMap<>();

            while (postIterator.hasNext()) {
                final KatchupPost post = (KatchupPost) postIterator.next();
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
            monthDataList.add(generateMonthData(locale, calendar, dayPostMap));
            calendar.add(Calendar.MONTH, 1);
        }

        return monthDataList;
    }

    private MonthData generateMonthData(@NonNull Locale locale, @NonNull Calendar calendar, @NonNull Map<Integer, Post> dayPostMap) {
        final String monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, locale).toLowerCase(locale);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        final int firstWeekday = calendar.get(Calendar.DAY_OF_WEEK);
        final int numberOfDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        return new MonthData(Preconditions.checkNotNull(monthName), numberOfDays, firstWeekday, dayPostMap);
    }

    class MonthViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameView;
        private final AspectRatioFrameLayout frameView;
        private final ArrayAdapter<String> headerAdapter;
        private final MonthDayAdapter dayGridAdapter;

        public MonthViewHolder(@NonNull View itemView) {
            super(itemView);

            nameView = itemView.findViewById(R.id.calendar_month_name);
            frameView = itemView.findViewById(R.id.calendar_month_frame);

            headerAdapter = new ArrayAdapter<>(itemView.getContext(), R.layout.calendar_header_item, R.id.calendar_header_text);
            final GridView gridLayout = itemView.findViewById(R.id.calendar_month_day_header);
            gridLayout.setAdapter(headerAdapter);

            dayGridAdapter = new MonthDayAdapter(itemView.getContext());
            final GridView monthGridView = itemView.findViewById(R.id.calendar_month_day_grid);
            monthGridView.setAdapter(dayGridAdapter);

        }

        public void bindTo(@NonNull MonthData monthData) {
            nameView.setText(monthData.getName());
            headerAdapter.clear();
            headerAdapter.addAll(Preconditions.checkNotNull(orderedWeekdayNames));
            dayGridAdapter.clear();
            dayGridAdapter.addAll(monthData.generateDayData(Preconditions.checkNotNull(orderedWeekdays)));
            final int numberOfRows = (dayGridAdapter.getCount() - 1) / WEEKDAYS.length + 1;
            final float frameRatio = ASPECT_RATIO_DAY_CELL * numberOfRows / WEEKDAYS.length;
            frameView.setAspectRatio(frameRatio);
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
            holder.bindTo(monthDataList.get(position));
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
            final MonthDayData dayData = getItem(position);
            final Post post = dayData.getPost();
            if (post != null) {
                if (post.media.size() > 1) {
                    mediaThumbnailLoader.load(dayImageView, post.media.get(1));
                }
                dayView.setOnClickListener(view -> startActivity(ViewKatchupCommentsActivity.viewPost(view.getContext(), post)));
            }
            if (dayData.getDayNumber() != MonthDayData.PADDING_DAY) {
                dayView.setBackground(AppCompatResources.getDrawable(getContext(), R.color.white_10));
                dayNumberView.setText(String.valueOf(getItem(position).getDayNumber()));
            } else {
                dayView.setBackground(null);
                dayNumberView.setText(null);
            }
            return dayView;
        }
    }
}
