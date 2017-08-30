package com.futureworkshops.android.autocapture.camera.provider;

import com.futureworkshops.android.autocapture.camera.view.CropActivity;
import com.futureworkshops.android.autocapture.camera.view.MainActivity;
import com.futureworkshops.datacap.common.camera.configuration.ConfigurationService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class AndroidComponentBuilder {

    @ContributesAndroidInjector
    public abstract ConfigurationService bindConfigurationService();

    @ContributesAndroidInjector(modules = {MainActivityDependencyProvider.class})
    public abstract MainActivity bindBaseCameraActivity();

    @ContributesAndroidInjector
    public abstract CropActivity bindCropActivity();
}
