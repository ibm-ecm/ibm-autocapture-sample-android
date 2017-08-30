package com.futureworkshops.android.autocapture.camera.provider;

import android.content.Context;

import com.futureworkshops.datacap.common.camera.configuration.Configuration;
import com.ibm.datacap.sdk.api.DatacapApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApiProvider {

    @Provides
    @Singleton
    DatacapApi providesDatacapApi(Context context) {
        return new DatacapApi.Builder()
                .endpoint(Configuration.ENDPOINT)
                .serviceType(Configuration.DATACAP_SERVICE)
                .withContext(context)
                .logLevel(1) // debug
                .boxClientID(Configuration.CLIENT_ID)
                .boxClientSecret(Configuration.CLIENT_SECRET)
                .build();
    }
}
