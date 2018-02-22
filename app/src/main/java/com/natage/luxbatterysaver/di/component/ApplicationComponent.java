package com.natage.luxbatterysaver.di.component;


import com.natage.luxbatterysaver.Application;
import com.natage.luxbatterysaver.FastCharger;
import com.natage.luxbatterysaver.MainActivity;
import com.natage.luxbatterysaver.di.module.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {
    void inject(Application application);

    void inject(MainActivity mainActivity);

    void inject(FastCharger fastCharger);
}
