package com.futureworkshops.android.autocapture;

import com.futureworkshops.datacap.common.CoreApplication;


public class AutocaptureApplication extends CoreApplication {

    @Override
    protected void setupDagger() {
        DaggerAppComponent.builder()
                .application(this)
                .build()
                .inject(this);
    }
}
