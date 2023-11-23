/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.ziluone.wedisplay.future;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.ziluone.wedisplay.R;
import com.baidu.navisdk.module.futuretrip.BNRRNumberPickerView;
import com.baidu.navisdk.util.common.LogUtil;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.StringRes;

public abstract class FutureTripDateTimePickerView extends LinearLayout {
    public static final int UPDATE_MINUTE_NORMALLY = 0;
    public static final int UPDATE_MINUTE_SPECIALLY = 1;
    public static final int SCROLL_DATE = 0;
    public static final int SCROLL_HOUR = 1;
    public static final int SCROLL_MINUTE = 2;
    private static final String TAG = "DateTimePickerView";
    /**
     * 未来七天 ex [今天(星期一),星期日]
     */
    private static final int PICK_DAY_MAX_COUNT = 7;
    private static final int PICK_HOUR_MAX_COUNT = 24;
    /**
     * 00 & 15 & 30 & 45
     */
    private static final int PICK_MINUTE_MAX_COUNT = 4;
    /**
     * 分钟item的间隔时间
     */
    private static final int PICK_MINUTE_GAP = 15;
    private View mRootView;
    private BNRRNumberPickerView dateNumberPicker;
    private BNRRNumberPickerView hourNumberPicker;
    private BNRRNumberPickerView miniuteNumberPicker;
    private TextView mCancelBtn;
    private TextView mOkBtn;
    private String[] mDateDisplayValues;
    private String[] mHourDisplayValues;
    private String[] mMinuteDisplayValues;
    private ActionListener listener;
    private final Object lock = new Object();
    /**
     * 当前选中的日期序号[0,7]
     */
    private int mCurSelDateIndex;
    /**
     * 当前选中的小时序号[0,23]
     */
    private int mCurSelHourIndex = -1;
    /**
     * 当前选中的分钟[0,2]
     */
    private int mCurSelMinuteIndex = -1;
    private int dateIndex = 0;
    private int hourIndex = 0;
    private int minuteIndex = 0;
    private TextView mTitleTv;
    private int mEntryType;
    private Date mDefaultValidDate;
    private Date mTimingDate;
    private int mUpdateMinuteType;
    private final BNRRNumberPickerView.OnValueChangeListener mOnDateChangedListener =
            new BNRRNumberPickerView.OnValueChangeListener() {
                @Override
                public void onValueChange(BNRRNumberPickerView picker, int oldVal, int newVal) {
                    synchronized (lock) {
                        int scrollType = -1;
                        int vId = picker.getId();
                        if (vId == R.id.bus_np_date) {
                            scrollType = SCROLL_DATE;
                            mCurSelDateIndex = newVal;
                        } else if (vId == R.id.bus_np_hour) {
                            scrollType = SCROLL_HOUR;
                            mCurSelHourIndex = newVal;
                        } else if (vId == R.id.bus_np_minute) {
                            scrollType = SCROLL_MINUTE;
                            mCurSelMinuteIndex = newVal;
                        }

                        setIgnoreStartIndex();
                        ensureValidOnScroll(scrollType, true);
                    }
                }
            };

    public FutureTripDateTimePickerView(Context context) {
        super(context);
        initView();
    }

    public FutureTripDateTimePickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public FutureTripDateTimePickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public static Date getDate(String dateString) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        ParsePosition pos = new ParsePosition(0);
        Date date = formatter.parse(dateString, pos);
        return date;
    }

    public static Date change(Date dateIn) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(dateIn);
        int year = ca.get(Calendar.YEAR);
        int month = ca.get(Calendar.MONTH);
        int date = ca.get(Calendar.DATE);
        int hour = ca.get(Calendar.HOUR_OF_DAY);
        int minute = ca.get(Calendar.MINUTE);
        int second = ca.get(Calendar.SECOND);
        ca.set(Calendar.SECOND, 0);
        if (minute >= 0 && minute < 15) {
            minute = 15;
        } else if (minute >= 15 && minute < 30) {
            minute = 30;
        } else if (minute >= 30 && minute < 45) {
            minute = 45;
        } else {
            minute = 00;
            ca.add(Calendar.HOUR_OF_DAY, 1);
        }
        ca.set(Calendar.MINUTE, minute);
        Date dateOut = new Date(ca.getTimeInMillis());
        return dateOut;
    }

    private void printDate(String tag, Date date) {
        if (LogUtil.LOGGABLE) {
            if (date == null) {
                LogUtil.e(TAG, tag + ",printDate,date is null");
            } else {
                SimpleDateFormat minuteF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                LogUtil.e(TAG, tag + ",printDate,date:" + minuteF.format(date));
            }
        }
    }

    private Date getCurSelectTime() {
        if (mCurSelDateIndex < 0) {
            return new Date();
        } else if (mCurSelDateIndex >= 0 && mCurSelDateIndex < PICK_HOUR_MAX_COUNT) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, mCurSelDateIndex);
            cal.set(Calendar.HOUR_OF_DAY, mCurSelHourIndex < 0 ? 0 : mCurSelHourIndex);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            int minute;
            if (mCurSelMinuteIndex < 0) {
                minute = 0;
            } else if (mMinuteDisplayValues.length <= mCurSelMinuteIndex) {
                minute = 0;
            } else {
                minute = Integer.parseInt(mMinuteDisplayValues[mCurSelMinuteIndex]);
            }
            cal.set(Calendar.MINUTE, minute);
            return cal.getTime();
        } else {
            return null;
        }
    }

    private String getCurSelectTimeStr(Date date) {
        if (date != null) {
            String str =
                    (String) DateFormat.format("yyyy年MM月dd日 HH:mm", date.getTime());
            return str;
        }
        return "";
    }

    public abstract int getLayoutId();

    private void initView() {
        mRootView = LayoutInflater.from(getContext()).inflate(getLayoutId(), this);
        mRootView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mTitleTv = (TextView) mRootView.findViewById(R.id.title);
        dateNumberPicker = (BNRRNumberPickerView) mRootView.findViewById(R.id.bus_np_date);
        dateNumberPicker.setTag("-date-");
        hourNumberPicker = (BNRRNumberPickerView) mRootView.findViewById(R.id.bus_np_hour);
        hourNumberPicker.setTag("-hour-");
        miniuteNumberPicker = (BNRRNumberPickerView) mRootView.findViewById(R.id.bus_np_minute);
        miniuteNumberPicker.setTag("-minute-");
        mCancelBtn = (TextView) mRootView.findViewById(R.id.bus_tv_time_cancel_btn);
        mOkBtn = (TextView) mRootView.findViewById(R.id.bus_tv_time_ok_btn);
        mCancelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    Date date = getCurSelectTime();
                    String dateStr = getCurSelectTimeStr(date);
                    listener.onClickCancelBtn(dateStr, date,
                            mEntryType,
                            mCurSelDateIndex,
                            mCurSelHourIndex,
                            mCurSelMinuteIndex);
                }
            }
        });
        mOkBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    Date date = getCurSelectTime();
                    String dateStr = getCurSelectTimeStr(date);
                    listener.onClickConfirmBtn(dateStr, date,
                            mEntryType,
                            mCurSelDateIndex,
                            mCurSelHourIndex,
                            mCurSelMinuteIndex);
                }
            }
        });

        mDateDisplayValues = new String[PICK_DAY_MAX_COUNT];
        dateNumberPicker.setOnValueChangedListener(mOnDateChangedListener);
        dateNumberPicker.setOnValueChangeListenerInScrolling(
                new BNRRNumberPickerView.OnValueChangeListenerInScrolling() {
                    @Override
                    public void onValueChangeInScrolling(BNRRNumberPickerView picker, int oldVal,
                                                         int newVal) {
                        if (LogUtil.LOGGABLE) {
                            // nothing
                        }
                    }
                });
        mHourDisplayValues = new String[PICK_HOUR_MAX_COUNT];
        hourNumberPicker.setOnValueChangedListener(mOnDateChangedListener);
        hourNumberPicker.setOnValueChangeListenerInScrolling(
                new BNRRNumberPickerView.OnValueChangeListenerInScrolling() {
                    @Override
                    public void onValueChangeInScrolling(BNRRNumberPickerView picker, int oldVal,
                                                         int newVal) {
                        if (LogUtil.LOGGABLE) {
                            // nothing
                        }
                    }
                });

        mMinuteDisplayValues = new String[PICK_MINUTE_MAX_COUNT];
        miniuteNumberPicker.setOnValueChangedListener(mOnDateChangedListener);
        miniuteNumberPicker.setOnValueChangeListenerInScrolling(
                new BNRRNumberPickerView.OnValueChangeListenerInScrolling() {
                    @Override
                    public void onValueChangeInScrolling(BNRRNumberPickerView picker, int oldVal,
                                                         int newVal) {
                        if (LogUtil.LOGGABLE) {
                            // nothing
                        }
                    }
                });

        // setDisplayDate();
    }

    public void setEntryType(int entryType) {
        this.mEntryType = entryType;
    }

    public abstract void setTitle(View view, String title);

    public abstract void setTitle(View view, @StringRes int title);

    public void setDisplayDate() {
        updateDisplayDate();
        updateDisplayHour();
        updateDisplayMinute();
    }

    /**
     * 每日的显示形式
     * 只显示7天之内的日期
     */
    private void updateDisplayDate() {
        Calendar cal = Calendar.getInstance();
        String todayStr = (String) DateFormat.format("MM月dd日 E", cal.getTimeInMillis());
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        String tomorrowStr = (String) DateFormat.format("MM月dd日 E", calendar.getTimeInMillis());
        for (int i = 0; i < PICK_DAY_MAX_COUNT; i++) {
            String dayStr = (String) DateFormat.format("MM月dd日 E", cal);
            if (TextUtils.equals(dayStr, todayStr)) {
                String weekStr = (String) DateFormat.format("E", cal);
                mDateDisplayValues[i] = "今天" + " " + weekStr;
            } else if (TextUtils.equals(dayStr, tomorrowStr)) {
                String weekStr = (String) DateFormat.format("E", cal);
                mDateDisplayValues[i] = "明天" + " " + weekStr;
            } else {
                mDateDisplayValues[i] = dayStr;
            }

            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        dateNumberPicker.setDisplayedValues(mDateDisplayValues);
        dateNumberPicker.setMinValue(0);
        dateNumberPicker.setMaxValue(PICK_DAY_MAX_COUNT - 1);
        dateNumberPicker.postInvalidate();
    }

    private void updateDisplayHour() {
        for (int i = 0; i < PICK_HOUR_MAX_COUNT; i++) {
            mHourDisplayValues[i] = getFormatNum(i);
        }
        hourNumberPicker.setDisplayedValues(mHourDisplayValues);
        hourNumberPicker.setMinValue(0);
        hourNumberPicker.setMaxValue(PICK_HOUR_MAX_COUNT - 1);
        hourNumberPicker.postInvalidate();
    }

    private void updateDisplayMinute() {
        boolean isTimingDatePassed = DateServiceUtil.isPassedTime(mTimingDate);
        boolean isDefaultValidDateBigger = DateServiceUtil
                .compare(mDefaultValidDate, mTimingDate) > 0;
        boolean isMultipleBlock = DateServiceUtil.isMultipleBlock(mTimingDate);
        if (LogUtil.LOGGABLE) {
            LogUtil.e(TAG, "isTimingDatePassed:" + isTimingDatePassed
                    + ",isDefaultValidDateBigger:" + isDefaultValidDateBigger
                    + ",isMultipleBlock:" + isMultipleBlock);
        }
        boolean isNormalUpdate;
        if (mEntryType == FutureTripController.Config.ENTRY_TYPE_DEPART) {
            isNormalUpdate = (isTimingDatePassed || isMultipleBlock);
        } else {
            isNormalUpdate = (isTimingDatePassed || isMultipleBlock || isDefaultValidDateBigger);
        }
        if (isNormalUpdate) {
            mUpdateMinuteType = UPDATE_MINUTE_NORMALLY;
            updateDisplayMinuteNormally();
        } else {
            mUpdateMinuteType = UPDATE_MINUTE_SPECIALLY;
            updateDisplayMinuteSpecially();
        }

    }

    private void updateDisplayMinuteSpecially() {
        int timeMinutes = DateServiceUtil.getMinutes(mTimingDate);
        ArrayList<String> list = new ArrayList();
        for (int i = 0; i < PICK_MINUTE_MAX_COUNT; i++) {
            list.add(getFormatNum(i * PICK_MINUTE_GAP));
        }
        list.add(timeMinutes / PICK_MINUTE_GAP + 1, getFormatNum(timeMinutes));
        mMinuteDisplayValues = new String[PICK_MINUTE_MAX_COUNT + 1];
        for (int i = 0; i < list.size(); i++) {
            mMinuteDisplayValues[i] = list.get(i);
        }
        miniuteNumberPicker.resetDisplayValues();
        miniuteNumberPicker.setMinValue(0);
        miniuteNumberPicker.setDisplayedValues(mMinuteDisplayValues);
        miniuteNumberPicker.setMaxValue(PICK_MINUTE_MAX_COUNT);
        miniuteNumberPicker.postInvalidate();
    }

    private void updateDisplayMinuteNormally() {
        mMinuteDisplayValues = new String[PICK_MINUTE_MAX_COUNT];
        for (int i = 0; i < PICK_MINUTE_MAX_COUNT; i++) {
            mMinuteDisplayValues[i] = getFormatNum(i * PICK_MINUTE_GAP);
        }
        miniuteNumberPicker.resetDisplayValues();
        miniuteNumberPicker.setMinValue(0);
        miniuteNumberPicker.setDisplayedValues(mMinuteDisplayValues);
        miniuteNumberPicker.setMaxValue(PICK_MINUTE_MAX_COUNT - 1);
        miniuteNumberPicker.postInvalidate();
    }

    private void checkSelMinuteIndex() {
        if (mCurSelMinuteIndex < 0) {
            mCurSelMinuteIndex = miniuteNumberPicker.getPickedIndexRelativeToRaw();
        }
    }

    private void checkSelHourIndex() {
        if (mCurSelHourIndex < 0) {
            mCurSelHourIndex = hourNumberPicker.getPickedIndexRelativeToRaw();
        }
    }

    private String getFormatNum(int num) {
        if (num < 10) {
            return "0" + num;
        }
        return "" + num;
    }

    public void setFunctionBtnListener(ActionListener listener) {
        this.listener = listener;
    }

    /**
     * 当前系统时间+当前路线所有时段最小ETA(>0）
     */
    public void setDefaultValidDate(Date date) {
        this.mDefaultValidDate = date;

    }

    public void setCurShowingDate(Date date) {
        this.mTimingDate = date;
    }

    public void build() {
        updateDisplayDate();
        updateDisplayHour();
        updateDisplayMinute();
    }

    public void scrollToPosOnInit() {
        dateNumberPicker.invalidate();
        hourNumberPicker.invalidate();
        miniuteNumberPicker.invalidate();
        try {
            Date date;
            // 最早不能早于这个时间
            Date dateLeftBoundary = null;
            if (mUpdateMinuteType == UPDATE_MINUTE_NORMALLY) {
                boolean isTimingDatePassed = DateServiceUtil.isPassedTime(mTimingDate);
                boolean isDefaultValidDateBigger =
                        DateServiceUtil.compare(mDefaultValidDate, mTimingDate) > 0;
                if (isDefaultValidDateBigger || isTimingDatePassed) {
                    date = mDefaultValidDate;
                } else {
                    date = mTimingDate;
                }
            } else {
                date = mTimingDate;
            }
            dateLeftBoundary = new Date(mDefaultValidDate.getTime());
            //            }
            Date targetDate;
            if (DateServiceUtil.isPassedTime(date)) {
                targetDate = change(dateLeftBoundary);
            } else {
                targetDate = date;
            }
            // select time
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(targetDate);

            dateIndex = DateServiceUtil.getDateDeltaV4(new Date(), targetDate);
            hourIndex = calendar.get(Calendar.HOUR_OF_DAY);

            if (mUpdateMinuteType == UPDATE_MINUTE_NORMALLY) {
                minuteIndex = calendar.get(Calendar.MINUTE) / PICK_MINUTE_GAP;
            } else {
                for (int i = 0; i < mMinuteDisplayValues.length; i++) {
                    int sInt = Integer.parseInt(mMinuteDisplayValues[i]);
                    if (sInt % PICK_MINUTE_GAP != 0) {
                        minuteIndex = i;
                    }
                }
            }
            //            }

            if (LogUtil.LOGGABLE) {
                LogUtil.e(TAG,
                        "selUserSettingTime,dateIndex:" + dateIndex
                                + ",dmax:" + dateNumberPicker.getMaxValue()
                                + ",hourIndex:" + hourIndex
                                + ",hmax:" + hourNumberPicker.getMaxValue()
                                + ",minuteIndex:" + minuteIndex
                                + ",mmax:" + miniuteNumberPicker.getMaxValue());
                LogUtil.e(TAG,
                        "selUserSettingTime,date:" + DateServiceUtil.formatDate(date));
                LogUtil.e(TAG, "selUserSettingTime,dateLeftBoundary:" + DateServiceUtil
                        .formatDate(dateLeftBoundary));
                LogUtil.e(TAG,
                        "selUserSettingTime,targetDate:" + DateServiceUtil
                                .formatDate(targetDate));
            }

        } catch (Exception e) {
            dateIndex = 0;
            hourIndex = 0;
            minuteIndex = 0;
        }
        ensureIndex();
        // 设置选中
        selectIndexDirectly(dateIndex, hourIndex, minuteIndex);
        // 不展示过去的时间
        setIgnoreStartIndex();
        dateNumberPicker.invalidate();
        hourNumberPicker.invalidate();
        miniuteNumberPicker.invalidate();
    }

    private void ensureIndex() {
        if (dateIndex > dateNumberPicker.getMaxValue()) {
            dateIndex = dateNumberPicker.getMaxValue();
            hourIndex = hourNumberPicker.getMaxValue();
            minuteIndex = miniuteNumberPicker.getMaxValue();
            return;
        }
        if (dateIndex < dateNumberPicker.getMinValue()) {
            dateIndex = dateNumberPicker.getMinValue();
            hourIndex = hourNumberPicker.getMinValue();
            minuteIndex = miniuteNumberPicker.getMinValue();
            return;
        }
        if (hourIndex > hourNumberPicker.getMaxValue()) {
            hourIndex = hourNumberPicker.getMaxValue();
        }
        if (hourIndex < hourNumberPicker.getMinValue()) {
            hourIndex = hourNumberPicker.getMinValue();
        }
        if (minuteIndex > miniuteNumberPicker.getMaxValue()) {
            minuteIndex = miniuteNumberPicker.getMaxValue();
        }
        if (minuteIndex < miniuteNumberPicker.getMinValue()) {
            minuteIndex = miniuteNumberPicker.getMinValue();
        }
    }

    private void setIgnoreStartIndex() {
        int targetDateIndex =
                DateServiceUtil.getDateDeltaV4(new Date(), mDefaultValidDate);
        if (LogUtil.LOGGABLE) {
            LogUtil.e(TAG,
                    "setIgnoreStartIndex,mDefaultValidDate:" + DateServiceUtil
                            .formatDate(mDefaultValidDate));
            LogUtil.e(TAG,
                    "setIgnoreStartIndex,cur:" + DateServiceUtil.formatDate(new Date()));
            LogUtil.e(TAG,
                    "setIgnoreStartIndex,mCurSelDateIndex:" + mCurSelDateIndex
                            + ",targetDateIndex:" + targetDateIndex);
        }
        dateNumberPicker.setIgnoreStartIndex(targetDateIndex);
        if (mCurSelDateIndex > targetDateIndex) {
            miniuteNumberPicker.setIgnoreStartIndex(-1);
            hourNumberPicker.setIgnoreStartIndex(-1);
        } else {
            setIgnoreStartIndexSpecial();
        }
        dateNumberPicker.invalidate();
        hourNumberPicker.invalidate();
        miniuteNumberPicker.invalidate();
    }

    private void setIgnoreStartIndexSpecial() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mDefaultValidDate);

        int hourIndex = calendar.get(Calendar.HOUR_OF_DAY);
        int minuteIndex = calendar.get(Calendar.MINUTE) / PICK_MINUTE_GAP;
        if (LogUtil.LOGGABLE) {
            LogUtil.e(TAG,
                    "setIgnoreStartIndexSpecial,hourIndex:" + hourIndex + ",minuteIndex:"
                            + minuteIndex);
        }
        hourNumberPicker.setIgnoreStartIndex(hourIndex);
        if (mCurSelHourIndex > hourIndex) {
            miniuteNumberPicker.setIgnoreStartIndex(-1);
        } else {
            miniuteNumberPicker.setIgnoreStartIndex(minuteIndex);
        }
    }

    private void setIgnoreStartIndexOnToday() {
        Date dateNow = new Date();
        Date dateNowFormat = change(dateNow);
        Calendar calendarNow = Calendar.getInstance();
        calendarNow.setTime(dateNow);

        Calendar calendarNowFormat = Calendar.getInstance();
        calendarNowFormat.setTime(dateNowFormat);
        int dateIndex =
                calendarNowFormat.get(Calendar.DAY_OF_YEAR) - calendarNow.get(Calendar.DAY_OF_YEAR);
        int hourIndex = calendarNowFormat.get(Calendar.HOUR_OF_DAY);
        int minuteIndex = calendarNowFormat.get(Calendar.MINUTE) / PICK_MINUTE_GAP;
        if (LogUtil.LOGGABLE) {
            LogUtil.e(TAG, "setIgnoreStartIndex,dateIndex:" + dateIndex
                    + ",hourIndex:" + hourIndex
                    + ",minuteIndex:" + minuteIndex);
        }
        dateNumberPicker.setIgnoreStartIndex(dateIndex);
        hourNumberPicker.setIgnoreStartIndex(hourIndex);
        if (mCurSelHourIndex > hourIndex) {
            miniuteNumberPicker.setIgnoreStartIndex(-1);
        } else {
            miniuteNumberPicker.setIgnoreStartIndex(minuteIndex);
        }
    }

    public String[] formatNumbers(int num) {
        String[] minute = new String[num];
        for (int i = 0; i < minute.length; i++) {
            if (i < 10) {
                String m = "0" + i;
                minute[i] = m;
            } else {
                minute[i] = i + "";
            }
        }
        return minute;
    }

    private void ensureValidOnScroll(int scrollType, boolean needRespond) {
        Date dateNow = new Date();
        Calendar calendarNow = Calendar.getInstance();
        calendarNow.setTime(dateNow);
        if (mUpdateMinuteType == UPDATE_MINUTE_NORMALLY) { // 当前是正常模式，保证滑动时无非法时间
            ensureValid(needRespond, calendarNow,
                    mDefaultValidDate); // TODO 此处mDefaullValidDate是否需要换成实时系统时间
        } else {
            Calendar timingCalendar = Calendar.getInstance();
            timingCalendar.setTime(mTimingDate);
            int hour = timingCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = timingCalendar.get(Calendar.MINUTE) / PICK_MINUTE_GAP + 1;
            int date =
                    timingCalendar.get(Calendar.DAY_OF_YEAR) - calendarNow
                            .get(Calendar.DAY_OF_YEAR);
            if (date != mCurSelDateIndex || hour != mCurSelHourIndex) {
                if (mMinuteDisplayValues.length > PICK_MINUTE_MAX_COUNT) {
                    updateDisplayMinuteNormally();
                    selectIndexDirectly(mCurSelDateIndex, mCurSelHourIndex, 0);
                }
                ensureValid(needRespond, calendarNow, mDefaultValidDate);
            } else {
                if (mMinuteDisplayValues.length == PICK_MINUTE_MAX_COUNT) {
                    updateDisplayMinuteSpecially();
                    if (scrollType != SCROLL_MINUTE) {
                        miniuteNumberPicker
                                .smoothScrollToValueQuick(miniuteNumberPicker.getValue(), minute,
                                        false,
                                        true);
                    }
                }
                ensureValid(needRespond, calendarNow, mDefaultValidDate);
            }
        }

    }

    private void ensureValid(boolean needRespond, Calendar calendarNow, Date dateIn) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateIn);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE) / PICK_MINUTE_GAP;
        int date = calendar.get(Calendar.DAY_OF_YEAR) - calendarNow.get(Calendar.DAY_OF_YEAR);
        if (mCurSelDateIndex < date) {
            dateNumberPicker
                    .smoothScrollToValueQuick(dateNumberPicker.getValue(), date,
                            needRespond,
                            true);
        }

        if (mCurSelDateIndex <= date) {
            if (mCurSelHourIndex < hour) {
                hourNumberPicker
                        .smoothScrollToValueQuick(hourNumberPicker.getValue(), hour,
                                needRespond,
                                true);
            }
        }
        if (mCurSelDateIndex <= date && mCurSelHourIndex <= hour
                && mCurSelMinuteIndex < minute) {
            miniuteNumberPicker
                    .smoothScrollToValueQuick(miniuteNumberPicker.getValue(), minute,
                            needRespond,
                            true);
        }
    }

    private boolean isCurSelTimeBeforeCurRealTime() {
        Calendar calendar = Calendar.getInstance();
        int curHour = calendar.get(Calendar.HOUR_OF_DAY);
        int curMinute = calendar.get(Calendar.MINUTE);
        if (mCurSelDateIndex == 1) {
            int panelMinutesCount = ((mCurSelHourIndex - 1) * 60 + (mCurSelMinuteIndex - 1) * 30);
            int realMinutesCount = (curHour * 60 + curMinute);
            if (panelMinutesCount < realMinutesCount) {
                return true;
            }
            if (mCurSelHourIndex == 0 || mCurSelMinuteIndex == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 确保不能选择早与当前时间
     */
    private void ensureTimeValid() {
        selectDefaultSettingTime(false, false, false);
    }

    public void selectCurTimeDirectly() {
        selectDefaultSettingTime(false, true, true);

    }

    /**
     * @param needRespond     是否需要滚动反馈
     * @param direct          是否直跳
     * @param isOnPanelUnFold 是否为展开面板时执行此处
     */
    public void selectDefaultSettingTime(boolean needRespond, boolean direct,
                                         boolean isOnPanelUnFold) {
        Calendar calendar = Calendar.getInstance();
        int curHour = calendar.get(Calendar.HOUR_OF_DAY);
        int curMinute = calendar.get(Calendar.MINUTE);
        int dateIndex = 0; // 今天
        int hourIndex = curHour;

        int panelMinutesCount = ((mCurSelHourIndex) * 60 + (mCurSelMinuteIndex) * 15);
        int realMinutesCount = (curHour * 60 + curMinute);
        if (LogUtil.LOGGABLE) {
            LogUtil.e(TAG,
                    "setCurTimeOnTodaySelected，curHour:" + curHour + ",curMinute:" + curMinute);
            LogUtil.e(TAG, "setCurTimeOnTodaySelected，mCurSelHourIndex:" + mCurSelHourIndex
                    + ",mCurSelMinuteIndex:"
                    + mCurSelMinuteIndex);
            LogUtil.e(TAG, "setCurTimeOnTodaySelected，realMinutesCount:" + realMinutesCount
                    + ",panelMinutesCount:"
                    + panelMinutesCount);
        }
        if (isOnPanelUnFold) {
            if (panelMinutesCount >= realMinutesCount && panelMinutesCount - realMinutesCount < 15
                    && mCurSelDateIndex == 0) {
                if (LogUtil.LOGGABLE) {
                    LogUtil.e(TAG, "setCurTimeOnTodaySelected，time valid,just return ");
                }
                return;
            }
        } else {
            if (panelMinutesCount > realMinutesCount && mCurSelHourIndex >= 0
                    && mCurSelMinuteIndex >= 0) {
                return;
            }
        }
        int minuteIndex;
        if (curMinute <= 15) {
            minuteIndex = 1;
        } else if (curMinute <= 30) {
            minuteIndex = 2;
        } else if (curMinute <= 45) {
            minuteIndex = 3;
        } else {
            minuteIndex = 0;
            if (mHourDisplayValues[hourIndex].equalsIgnoreCase("23")) {
                hourIndex = 0;
                dateIndex = 1;
            } else {
                hourIndex += 1;
            }
        }
        if (direct) {
            selectIndexDirectly(dateIndex, hourIndex, minuteIndex);
        } else {
            selectIndex(dateIndex, hourIndex, minuteIndex, needRespond);
        }

    }

    private void selectIndexDirectly(int dateIndex, int hourIndex, int minuteIndex) {
        if (LogUtil.LOGGABLE) {
            LogUtil.e(TAG,
                    "selectIndexDirectly,dateIndex:" + dateIndex
                            + ",hourIndex:" + hourIndex
                            + ",minuteIndex:" + minuteIndex);
        }
        dateNumberPicker.setValue(dateIndex);
        hourNumberPicker.setValue(hourIndex);
        miniuteNumberPicker.setValue(minuteIndex);
        mCurSelDateIndex = dateIndex;
        mCurSelHourIndex = hourIndex;
        mCurSelMinuteIndex = minuteIndex;
        dateNumberPicker.postInvalidate();
        hourNumberPicker.postInvalidate();
        miniuteNumberPicker.postInvalidate();
    }

    private void selectIndex(int dateIndex, int hourIndex, int minuteIndex, boolean needRespond) {
        dateNumberPicker
                .smoothScrollToValueQuick(dateNumberPicker.getValue(), dateIndex, needRespond,
                        true);
        hourNumberPicker
                .smoothScrollToValueQuick(hourNumberPicker.getValue(), hourIndex, needRespond,
                        true);
        miniuteNumberPicker
                .smoothScrollToValueQuick(miniuteNumberPicker.getValue(), minuteIndex, needRespond,
                        true);
        if (!needRespond) {
            mCurSelDateIndex = dateIndex;
            mCurSelHourIndex = hourIndex;
            mCurSelMinuteIndex = minuteIndex;
        }
    }

    public interface ActionListener {
        void onClickCancelBtn(String time, Date date, int... args);

        void onClickConfirmBtn(String time, Date date, int... args);

        void onShow();

        void onHide();
    }
}
