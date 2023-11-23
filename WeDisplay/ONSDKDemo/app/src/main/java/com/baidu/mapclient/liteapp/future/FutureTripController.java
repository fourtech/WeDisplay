package com.baidu.mapclient.liteapp.future;

import java.util.Date;

import com.baidu.mapclient.liteapp.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

/**
 * Author: duanpeifeng
 * Data: 2021/10/25
 */
public class FutureTripController {

    private final FutureTripDateTimePickerView mDateTimePickerView;
    private FutureTripDateTimePickerView.ActionListener mActionListener;

    private boolean mShowing;
    private final ViewGroup mPanelContainerView;

    public FutureTripController(Context context, ViewGroup viewGroup,
                                FutureTripDateTimePickerView.ActionListener listener) {
        mActionListener = listener;
        mPanelContainerView = viewGroup;
        mDateTimePickerView = new FutureTripDateTimePickerView(context) {
            @Override
            public int getLayoutId() {
                return R.layout.nsdk_future_trip_date_time_picker_layout;
            }

            @Override
            public void setTitle(View view, String title) {
                ((TextView) view.findViewById(R.id.title)).setText(title);
            }

            @Override
            public void setTitle(View view, int title) {
                ((TextView) view.findViewById(R.id.title)).setText(title);
            }
        };
        mDateTimePickerView.setFunctionBtnListener(listener);
    }

    /**
     * 展开面板之前调用
     */
    public void config( final Config config) {
        mDateTimePickerView.setEntryType(config.entryType);
        mDateTimePickerView.setCurShowingDate(config.entryDate);
        mDateTimePickerView.setDefaultValidDate(config.defaultValidDate);
        mDateTimePickerView.setTitle(mDateTimePickerView, config.titleStrResId);
        mDateTimePickerView.build();
    }

    public final boolean show() {
        if (mShowing) {
            return false;
        }

        if (mPanelContainerView == null || mDateTimePickerView == null) {
            return false;
        }
        if (!addView()) {
            return false;
        }
        mPanelContainerView.setVisibility(View.VISIBLE);
        mDateTimePickerView.scrollToPosOnInit();

        mShowing = true;
        if (mActionListener != null) {
            mActionListener.onShow();
        }
        return true;
    }

    private boolean addView() {
        if (mDateTimePickerView != null && mPanelContainerView != null && (
                mDateTimePickerView.getParent()
                        == mPanelContainerView)) {
            return true;
        }

        if (mDateTimePickerView != null && mPanelContainerView != null) {
            if (mDateTimePickerView.getParent() != null) {
                ((ViewGroup) mDateTimePickerView.getParent()).removeAllViews();
            }
            mPanelContainerView.setVisibility(View.GONE);
            mPanelContainerView.removeAllViews();
            mPanelContainerView.addView(mDateTimePickerView,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
            return true;
        } else {
            return false;
        }
    }

    public final void hide() {
        if (mPanelContainerView != null && mDateTimePickerView != null) {
            if (!mShowing) {
                mPanelContainerView.setVisibility(View.GONE);
                return;
            }
            mPanelContainerView.setVisibility(View.GONE);

            mShowing = false;
            if (mActionListener != null) {
                mActionListener.onHide();
            }
        }
    }

    public static class Config {
        public static final int ENTRY_TYPE_DEPART = 0;
        public int entryType;
        @NonNull
        public String title;
        @StringRes
        public int titleStrResId;
        public String subTitle;
        public Date entryDate;
        public Date defaultValidDate;

        public static int getTitleByEntryType(int entryType) {
            if (entryType == ENTRY_TYPE_DEPART) {
                return R.string.nsdk_future_trip_time_picker_depart_title;
            }
            return R.string.nsdk_future_trip_time_picker_arrive_title;
        }
    }

}
