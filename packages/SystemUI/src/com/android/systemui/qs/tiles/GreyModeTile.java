/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.service.quicksettings.Tile;
import android.widget.Switch;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.systemui.R;
import com.android.systemui.qs.QSHost;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.qs.SecureSetting;

/** Quick settings tile: enable/disable grey color display **/
public class GreyModeTile extends QSTileImpl<BooleanState> {

    private final AnimationIcon mEnable
            = new AnimationIcon(R.drawable.ic_greymode_enable_animation,
            R.drawable.ic_greymode_disable);
    private final AnimationIcon mDisable
            = new AnimationIcon(R.drawable.ic_greymode_disable_animation,
            R.drawable.ic_greymode_enable);
    private final SecureSetting mSetting;
    private final SecureSetting mDaltonizerMode;

    private boolean mListening;

    public GreyModeTile(QSHost host) {
        super(host);

        mSetting = new SecureSetting(mContext, mHandler,
                Secure.ACCESSIBILITY_DISPLAY_DALTONIZER_ENABLED) {
            @Override
            protected void handleValueChanged(int value, boolean observedChange) {
                handleRefreshState(value);
            }
        };
        mDaltonizerMode = new SecureSetting(mContext, mHandler,
                Secure.ACCESSIBILITY_DISPLAY_DALTONIZER) {
            @Override
            protected void handleValueChanged(int value, boolean observedChange) {
                //handleRefreshState(value);
            }
        };
    }

    @Override
    protected void handleDestroy() {
        super.handleDestroy();
        mSetting.setListening(false);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void handleSetListening(boolean listening) {
        mSetting.setListening(listening);
        mDaltonizerMode.setListening(listening);
    }

    @Override
    protected void handleUserSwitch(int newUserId) {
        mSetting.setUserId(newUserId);
        handleRefreshState(mSetting.getValue());
        //handleRefreshState(mDaltonizerMode.getValue());
    }

    @Override
    public Intent getLongClickIntent() {
        // TODO SAT: Change this
        return new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
    }

    @Override
    protected void handleClick() {
        //TODO SAT: Define constant or write comments
        mSetting.setValue(mState.value ? 0 : 1);
        mDaltonizerMode.setValue(mState.value ? -1 : 0);
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_greymode_label);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        final int value = arg instanceof Integer ? (Integer) arg : mSetting.getValue();
        final boolean enabled = value != 0;
        state.value = enabled;
        state.state = state.value ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE;
        state.label = mContext.getString(R.string.quick_settings_greymode_label);
        state.icon = enabled ? mEnable : mDisable;
        state.expandedAccessibilityClassName = Switch.class.getName();
        state.contentDescription = state.label;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_CUSTOM;
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(
                    R.string.accessibility_quick_settings_greymode_changed_on);
        } else {
            return mContext.getString(
                    R.string.accessibility_quick_settings_greymode_changed_off);
        }
    }
}
