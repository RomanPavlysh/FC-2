package com.natage.luxbatterysaver.di.module;

import android.content.Context;
import android.support.annotation.NonNull;


import com.natage.luxbatterysaver.Application;
import com.natage.luxbatterysaver.helper.Permissions;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {

    private final Application application;

    public ApplicationModule(@NonNull Application myApplication) {
        this.application = myApplication;
    }

    @Provides
    @Singleton
    public Context provideContext() {
        return application;
    }


    @Provides
    @Singleton
    public Permissions providePermissions(Context context){
        return new Permissions(context);
    }
}
