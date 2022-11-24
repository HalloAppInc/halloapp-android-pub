package com.halloapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.halloapp.contacts.ContactsSync;
import com.halloapp.registration.Registration;
import com.halloapp.registration.SmsVerificationManager;
import com.halloapp.ui.AppExpirationActivity;
import com.halloapp.ui.DebouncedClickListener;
import com.halloapp.ui.HalloActivity;
import com.halloapp.MainActivity;
import com.halloapp.ui.SystemUiVisibility;
import com.halloapp.ui.avatar.AvatarLoader;
import com.halloapp.util.BgWorkers;
import com.halloapp.util.KeyboardUtils;
import com.halloapp.util.logs.Log;
import com.halloapp.util.logs.LogProvider;
import com.halloapp.widget.DoodleBackgroundView;
import com.halloapp.widget.NetworkIndicatorView;
import com.halloapp.widget.SnackbarHelper;
import com.hbb20.CountryCodePicker;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

public class RegistrationRequestActivity extends HalloActivity {

    public static final String EXTRA_RE_VERIFY = "reverify";

    public static Intent register(Context context, long lastSync) {
        Intent i = new Intent(context, RegistrationRequestActivity.class);
        if (lastSync > 0) {
            i.putExtra(EXTRA_RE_VERIFY, true);
        }
        return i;
    }

    private static final int REQUEST_CODE_VERIFICATION = 1;
    private static final long INSTALL_REFERRER_TIMEOUT_MS = 2000;
    private static final long HASHCASH_MAX_WAIT_MS = 60_000;

    private static final int WELCOME_ANIMATION_DELAY = 1800;
    private static final int WELCOME_ANIMATION_DURATION = 450;
    private static final int DOODLE_FADE_OUT_DURATION = 500;

    private static boolean welcomeAnimationShown = false;

    private final BgWorkers bgWorkers = BgWorkers.getInstance();
    private final SmsVerificationManager smsVerificationManager = SmsVerificationManager.getInstance();

    private FirebaseAnalytics firebaseAnalytics;

    private RegistrationRequestViewModel registrationRequestViewModel;

    private CountryCodePicker countryCodePicker;
    private EditText phoneNumberEditText;
    private View nextButton;
    private View loadingProgressBar;
    private View sendLogsButton;
    private Preferences preferences;
    private ContactsSync contactsSync;
    private AvatarLoader avatarLoader;
    private View welcomeText;
    private View logoText;

    private View logoContainer;
    private ConstraintLayout welcomeContainer;
    private DoodleBackgroundView doodleView;

    private boolean isReverification = false;

    private final View.OnClickListener startRegistrationRequestListener = new DebouncedClickListener() {
        @Override
        public void onOneClick(@NonNull View view) {
            startRegistrationRequest();
        }
    };

    public static void reVerify(final Context context) {
        context.startActivity(new Intent(context, RegistrationRequestActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(RegistrationRequestActivity.EXTRA_RE_VERIFY, true));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getFullScreenSystemUiVisibility(this));
        setContentView(R.layout.activity_registration_request);

        transitionSplashScreen();

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        preferences = Preferences.getInstance();
        contactsSync = ContactsSync.getInstance();
        avatarLoader = AvatarLoader.getInstance();

        phoneNumberEditText = findViewById(R.id.phone_number);
        countryCodePicker = findViewById(R.id.ccp);
        countryCodePicker.registerCarrierNumberEditText(phoneNumberEditText);
        countryCodePicker.useFlagEmoji(Build.VERSION.SDK_INT >= 28);
        loadingProgressBar = findViewById(R.id.loading);
        nextButton = findViewById(R.id.next);
        sendLogsButton = findViewById(R.id.send_logs);

        welcomeText = findViewById(R.id.welcome_text);

        logoContainer = findViewById(R.id.logo_container);
        welcomeContainer = findViewById(R.id.welcome_constraint);

        logoText = findViewById(R.id.logo_text);
        doodleView = findViewById(R.id.doodle_view);

        final TextView titleView = findViewById(R.id.title);
        isReverification = getIntent().getBooleanExtra(EXTRA_RE_VERIFY, false);
        if (isReverification) {
            titleView.setText(R.string.reverify_registration_title);
        } else {
            titleView.setText(R.string.phone_entry_title);
        }
        Notifications.getInstance(this).clearLoginFailedNotification();

        phoneNumberEditText.setTextColor(phoneNumberEditText.getCurrentTextColor()); // so phoneNumberEditText.setEnabled(false) doesn't change color

        registrationRequestViewModel = new ViewModelProvider(this).get(RegistrationRequestViewModel.class);
        registrationRequestViewModel.getRegistrationRequestResult().observe(this, result -> {
            if (result == null) {
                return;
            }

            if (result.result == Registration.RegistrationRequestResult.RESULT_OK) {
                final Intent intent = new Intent(this, RegistrationVerificationActivity.class);
                intent.putExtra(RegistrationVerificationActivity.EXTRA_PHONE_NUMBER, result.phone);
                intent.putExtra(RegistrationVerificationActivity.EXTRA_RETRY_WAIT_TIME, result.retryWaitTimeSeconds);
                intent.putExtra(RegistrationVerificationActivity.EXTRA_GROUP_INVITE_TOKEN, registrationRequestViewModel.groupInviteToken);
                intent.putExtra(RegistrationVerificationActivity.EXTRA_CAMPAIGN_ID, registrationRequestViewModel.campaignId);
                startActivityForResult(intent, REQUEST_CODE_VERIFICATION);
            } else {
                if (result.result == Registration.RegistrationRequestResult.RESULT_FAILED_SERVER_NO_FRIENDS
                        || result.result == Registration.RegistrationRequestResult.RESULT_FAILED_SERVER_NOT_INVITED) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationRequestActivity.this)
                        .setMessage(getBaseContext().getString(R.string.registration_failed_no_friends))
                        .setTitle(getBaseContext().getString(R.string.registration_failed_no_friends_title))
                        .setPositiveButton(R.string.ok, null)
                        .setCancelable(true);
                    builder.show();
                } else if (result.result == Registration.RegistrationRequestResult.RESULT_FAILED_CLIENT_EXPIRED) {
                    AppExpirationActivity.open(this, 0);
                    finish();
                } else if (result.result == Registration.RegistrationRequestResult.RESULT_FAILED_RETRIED_TOO_SOON && result.phone != null) {
                    final Intent intent = new Intent(this, RegistrationVerificationActivity.class);
                    intent.putExtra(RegistrationVerificationActivity.EXTRA_PHONE_NUMBER, result.phone);
                    intent.putExtra(RegistrationVerificationActivity.EXTRA_RETRY_WAIT_TIME, result.retryWaitTimeSeconds);
                    intent.putExtra(RegistrationVerificationActivity.EXTRA_GROUP_INVITE_TOKEN, registrationRequestViewModel.groupInviteToken);
                    intent.putExtra(RegistrationVerificationActivity.EXTRA_CAMPAIGN_ID, registrationRequestViewModel.campaignId);
                    startActivityForResult(intent, REQUEST_CODE_VERIFICATION);
                } else if (result.result == Registration.RegistrationRequestResult.RESULT_FAILED_INVALID_PHONE_NUMBER) {
                    SnackbarHelper.showWarning(this, R.string.invalid_phone_number);
                } else {
                    SnackbarHelper.showInfo(this, R.string.registration_failed);
                }
                nextButton.setVisibility(View.VISIBLE);
                countryCodePicker.setCcpClickable(true);
            }
            loadingProgressBar.setVisibility(View.INVISIBLE );
        });

        phoneNumberEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                startRegistrationRequestListener.onClick(v);
            }
            return false;
        });
        phoneNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("RegistrationRequestActivity phone updated to " + countryCodePicker.getFormattedFullNumber());
                updateNextButton();
            }
        });
        nextButton.setOnClickListener(startRegistrationRequestListener);

        final NetworkIndicatorView indicatorView = findViewById(R.id.network_indicator);
        indicatorView.bind(this);

        registrationRequestViewModel.showSendLogs.observe(this, show -> {
            sendLogsButton.setVisibility(Boolean.TRUE.equals(show) ? View.VISIBLE : View.INVISIBLE);
        });
        sendLogsButton.setOnClickListener(v -> {
            ProgressDialog progressDialog = ProgressDialog.show(this, null, getString(R.string.preparing_logs));
            LogProvider.openLogIntent(this).observe(this, intent -> {
                startActivity(intent);
                progressDialog.dismiss();
            });
        });

        updateNextButton();
        if (!isReverification && !welcomeAnimationShown) {
            startWelcomeAnimation();
        } else {
            doodleView.setVisibility(View.GONE);
        }
    }

    private void transitionSplashScreen() {
        if (Build.VERSION.SDK_INT >= 31) {
            getSplashScreen().setOnExitAnimationListener(splashScreenView -> {
                final ObjectAnimator fadeIn = ObjectAnimator.ofFloat(
                        splashScreenView,
                        View.ALPHA,
                        1f,
                        0
                );
                fadeIn.setInterpolator(new AnticipateInterpolator());
                fadeIn.setDuration(500L);

                fadeIn.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        splashScreenView.remove();
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        splashScreenView.remove();
                    }
                });
                fadeIn.start();
            });
        }
    }

    private void startWelcomeAnimation() {
        welcomeContainer.postDelayed(() -> {
            ChangeBounds autoTransition = new ChangeBounds();
            autoTransition.setInterpolator(new FastOutSlowInInterpolator());
            autoTransition.setDuration(WELCOME_ANIMATION_DURATION);
            autoTransition.excludeTarget(R.id.welcome_text, true);
            TransitionManager.beginDelayedTransition(welcomeContainer, autoTransition);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(welcomeContainer);
            constraintSet.connect(R.id.logo_container,ConstraintSet.TOP, ConstraintSet.PARENT_ID,ConstraintSet.TOP,getResources().getDimensionPixelSize(R.dimen.welcome_logo_margin_top));
            constraintSet.connect(R.id.logo_container,ConstraintSet.BOTTOM,R.id.welcome_text,ConstraintSet.TOP, 0);
            constraintSet.applyTo(welcomeContainer);

            welcomeText.setVisibility(View.VISIBLE);
            ValueAnimator f = ValueAnimator.ofFloat(0, 1f);
            f.addUpdateListener(val -> {
                doodleView.setPos(val.getAnimatedFraction());
                welcomeText.setAlpha(val.getAnimatedFraction());
                logoText.setAlpha(1f - val.getAnimatedFraction());
            });
            Animation anim = new ScaleAnimation(1f, 0.74f, 1f, 0.74f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            anim.setFillAfter(true);
            anim.setDuration(WELCOME_ANIMATION_DURATION);
            logoContainer.startAnimation(anim);

            f.setDuration(WELCOME_ANIMATION_DURATION);
            f.start();
            f.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animateDoodleFadeOut();
                    welcomeAnimationShown = true;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    welcomeAnimationShown = true;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }, WELCOME_ANIMATION_DELAY);
    }

    private void animateDoodleFadeOut() {
        doodleView.postDelayed(() -> {
            doodleView.animate()
                    .alpha(0)
                    .setDuration(DOODLE_FADE_OUT_DURATION)
                    .setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    doodleView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }}).start();

            getWindow().getDecorView().setSystemUiVisibility(SystemUiVisibility.getDefaultSystemUiVisibility(RegistrationRequestActivity.this));
            KeyboardUtils.showSoftKeyboard(phoneNumberEditText);
        }, 2500);
    }

    private void updateNextButton() {
        boolean phoneOkayLength = isPhoneOkayLength();

        nextButton.setEnabled(phoneOkayLength);
    }

    private boolean isPhoneOkayLength() {
        boolean phoneOkayLength = false;
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.createInstance(this);
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(countryCodePicker.getFormattedFullNumber(), countryCodePicker.getSelectedCountryNameCode());
            phoneOkayLength = phoneNumberUtil.isPossibleNumberWithReason(phoneNumber) == PhoneNumberUtil.ValidationResult.IS_POSSIBLE;
        } catch (NumberParseException e) {
            Log.i("Failed to parse number: " + e);
        }
        return phoneOkayLength;
    }

    @Override
    public void onActivityResult(final int request, final int result, final Intent data) {
        super.onActivityResult(request, result, data);
        //noinspection SwitchStatementWithTooFewBranches
        switch (request) {
            case REQUEST_CODE_VERIFICATION: {
                if (result == RESULT_OK) {
                    if (BuildConfig.FLAVOR.equals("halloapp")) {
                        startActivity(new Intent(this, MainActivity.class));
                    } else {
                        startActivity(new Intent(this, MainActivity.class));
                    }
                    if (preferences.getLastFullContactSyncTime() > 0) {
                        onRereg();
                    }
                    finish();
                } else {
                    nextButton.setVisibility(View.VISIBLE);
                    phoneNumberEditText.setEnabled(true);
                    countryCodePicker.setCcpClickable(true);
                }
                break;
            }
        }
    }

    private void onRereg() {
        Log.i("RegistrationRequestActivity: rereg success");
        contactsSync.forceFullContactsSync();
        bgWorkers.execute(() -> {
            preferences.setCompletedFirstPostOnboarding(true);
            preferences.setLastGroupSyncTime(0);
            preferences.setLastPushToken("");
            preferences.setLastPushTokenSyncTime(0);
            preferences.setLastHuaweiPushToken("");
            preferences.setLastHuaweiPushTokenSyncTime(0);
            avatarLoader.removeMyAvatar();
        });
    }

    private void startRegistrationRequest() {
        firebaseAnalytics.logEvent("reg_requested", null);
        if (!isPhoneOkayLength()) {
            SnackbarHelper.showInfo(this, R.string.invalid_phone_number);
            phoneNumberEditText.requestFocus();
            return;
        }
        if (!isReverification) {
            preferences.applyCompletedFirstPostOnboarding(false);
        }
        loadingProgressBar.setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.INVISIBLE);
        countryCodePicker.setCcpClickable(false);
        Log.i("RegistrationRequestActivity.startRegistrationRequest for " + countryCodePicker.getFullNumber());

        smsVerificationManager.start(getApplicationContext());
        registrationRequestViewModel.requestRegistration(countryCodePicker.getFullNumber());
    }

    public static class RegistrationRequestViewModel extends AndroidViewModel {

        public final MutableLiveData<Boolean> showSendLogs = new MutableLiveData<>(false);

        private final BgWorkers bgWorkers = BgWorkers.getInstance();
        private final Registration registration = Registration.getInstance();

        private final MutableLiveData<Registration.RegistrationRequestResult> registrationRequestResult = new MutableLiveData<>();
        private CountDownLatch hashcashLatch = new CountDownLatch(1);

        private String groupInviteToken;
        private String campaignId;
        private Registration.HashcashResult hashcashResult;

        public RegistrationRequestViewModel(@NonNull Application application) {
            super(application);

            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    showSendLogs.postValue(true);
                }
            };
            timer.schedule(timerTask, Constants.SEND_LOGS_BUTTON_DELAY_MS);

            runHashcash();
        }

        private void runHashcash() {
            bgWorkers.execute(() -> {
                hashcashResult = registration.getHashcashSolution();
                if (hashcashResult.result != Registration.HashcashResult.RESULT_OK) {
                    Log.e("Got hashcash failure " + hashcashResult.result);
                    Log.sendErrorReport("Hashcash failed");
                }
                hashcashLatch.countDown();
            });
        }

        LiveData<Registration.RegistrationRequestResult> getRegistrationRequestResult() {
            return registrationRequestResult;
        }

        void requestRegistration(@NonNull String phone) {
            bgWorkers.execute(() -> {
                try {
                    hashcashLatch.await(HASHCASH_MAX_WAIT_MS, TimeUnit.MILLISECONDS);
                    Log.i("RegistrationRequestActivity/requestRegistration done waiting for hashcashLatch");
                } catch (InterruptedException e) {
                    Log.e("Interrupted while waiting for hashcash", e);
                }

                InstallReferrerClient referrerClient = InstallReferrerClient.newBuilder(AppContext.getInstance().get().getApplicationContext()).build();

                AtomicBoolean registerCalled = new AtomicBoolean(false);
                Timer timer = new Timer();
                Preferences.getInstance().setProfileSetup(false);
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (registerCalled.compareAndSet(false, true)) {
                            Log.i("RegistrationRequestViewModel InstallReferrer took too long; registering");
                            Registration.RegistrationRequestResult result = registration.registerPhoneNumber(phone, phone, groupInviteToken, campaignId, hashcashResult);
                            hashcashLatch = new CountDownLatch(1);
                            hashcashResult = null;
                            runHashcash();
                            registrationRequestResult.postValue(result);
                            referrerClient.endConnection();
                        }
                    }
                };
                timer.schedule(timerTask, INSTALL_REFERRER_TIMEOUT_MS);

                referrerClient.startConnection(new InstallReferrerStateListener() {
                    private static final String INVITE_TAG = "ginvite-";
                    private static final String CAMPAIGN_TAG = "utm_campaign=";

                    @Override
                    public void onInstallReferrerSetupFinished(int responseCode) {
                        String inviteCode = null;
                        String utmCampaign = null;
                        switch (responseCode) {
                            case InstallReferrerClient.InstallReferrerResponse.OK:
                                try {
                                    ReferrerDetails details = referrerClient.getInstallReferrer();
                                    String referrerUrl = details.getInstallReferrer();
                                    Log.i("RegistrationRequestActivity/requestRegistration got referrerUrl " + referrerUrl);
                                    if (!TextUtils.isEmpty(referrerUrl) && referrerUrl.contains(INVITE_TAG)) {
                                        int start = referrerUrl.indexOf(INVITE_TAG) + INVITE_TAG.length();
                                        int end = referrerUrl.indexOf("&", start);
                                        inviteCode = referrerUrl.substring(start, Math.max(referrerUrl.length(), end));
                                    } else {
                                        Log.i("RegistrationRequestActivity/requestRegistration no referrer invite");
                                    }
                                    if (!TextUtils.isEmpty(referrerUrl) && referrerUrl.contains(CAMPAIGN_TAG)) {
                                        int start = referrerUrl.indexOf(CAMPAIGN_TAG) + CAMPAIGN_TAG.length();
                                        int end = referrerUrl.indexOf("&", start);
                                        utmCampaign = referrerUrl.substring(start, Math.max(referrerUrl.length(), end));
                                    } else {
                                        Log.i("RegistrationRequestActivity/requestRegistration no referrer campaign id");
                                    }
                                } catch (RemoteException e) {
                                    Log.e("RegistrationRequestActivity/requestRegistration failed to get install referrer", e);
                                }
                                break;
                            case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                                Log.w("RegistrationRequestActivity/requestRegistration referrer not available");
                                break;
                            case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                                Log.w("RegistrationRequestActivity/requestRegistration no connection");
                                break;
                        }
                        groupInviteToken = inviteCode;
                        campaignId = utmCampaign;
                        bgWorkers.execute(() -> {
                            if (registerCalled.compareAndSet(false, true)) {
                                Log.i("RegistrationRequestViewModel registering from install referrer callback");
                                Registration.RegistrationRequestResult result = registration.registerPhoneNumber(phone, phone, groupInviteToken, campaignId, hashcashResult);
                                hashcashLatch = new CountDownLatch(1);
                                hashcashResult = null;
                                runHashcash();
                                registrationRequestResult.postValue(result);
                                referrerClient.endConnection();
                            }
                        });
                    }

                    @Override
                    public void onInstallReferrerServiceDisconnected() {
                    }
                });
            });
        }
    }

}