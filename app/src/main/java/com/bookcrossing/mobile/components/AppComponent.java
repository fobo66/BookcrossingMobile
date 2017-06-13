package com.bookcrossing.mobile.components;

import com.bookcrossing.mobile.modules.ApiModule;
import com.bookcrossing.mobile.modules.AppModule;
import com.bookcrossing.mobile.modules.PrefModule;
import com.bookcrossing.mobile.util.FirebaseWrapper;
import com.bookcrossing.mobile.util.SystemServicesWrapper;
import dagger.Component;
import javax.inject.Singleton;

/**
 * (c) 2016 Andrey Mukamolow aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 15.11.2016.
 */
@Singleton @Component(modules = {
    AppModule.class, PrefModule.class, ApiModule.class
}) public interface AppComponent {
  void inject(FirebaseWrapper firebaseWrapper);

  void inject(SystemServicesWrapper systemServicesWrapper);
}
