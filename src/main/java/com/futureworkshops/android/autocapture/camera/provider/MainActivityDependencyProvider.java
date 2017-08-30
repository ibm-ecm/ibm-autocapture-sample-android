package com.futureworkshops.android.autocapture.camera.provider;

import com.futureworkshops.android.autocapture.camera.view.MainActivity;
import com.futureworkshops.android.autocapture.camera.view.MainCameraView;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class MainActivityDependencyProvider {

    @Binds
    abstract MainCameraView providesBaseCameraView(MainActivity mainActivity);
}
