package com.futureworkshops.android.autocapture;

import android.app.Application;

import com.futureworkshops.android.autocapture.camera.provider.AndroidComponentBuilder;
import com.futureworkshops.android.autocapture.camera.provider.ApiProvider;
import com.futureworkshops.datacap.common.AppModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

@Singleton
@Component(modules = {
        ApiProvider.class,
        AndroidInjectionModule.class,
        AppModule.class,
        AndroidComponentBuilder.class})
public interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }

    void inject(AutocaptureApplication coreApplication);
}
