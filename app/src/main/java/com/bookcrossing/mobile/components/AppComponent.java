package com.bookcrossing.mobile.components;

import com.bookcrossing.mobile.modules.ApiModule;
import com.bookcrossing.mobile.modules.App;
import com.bookcrossing.mobile.modules.AppModule;
import com.bookcrossing.mobile.modules.LocationModule;
import com.bookcrossing.mobile.modules.PrefModule;
import com.bookcrossing.mobile.presenters.MainPresenter;
import com.bookcrossing.mobile.util.FirebaseWrapper;
import com.bookcrossing.mobile.util.SystemServicesWrapper;
import dagger.BindsInstance;
import dagger.Component;
import javax.inject.Singleton;

/**
 * (c) 2016 Andrey Mukamolow aka fobo66 <fobo66@protonmail.com>
 * Created by fobo66 on 15.11.2016.
 */
@Singleton @Component(modules = {
    AppModule.class, PrefModule.class, LocationModule.class, ApiModule.class
}) public interface AppComponent {
  @Component.Builder interface Builder {
    @BindsInstance Builder application(App application);

    Builder appModule(AppModule appModule);

    Builder prefModule(PrefModule prefModule);

    Builder apiModule(ApiModule apiModule);

    Builder locationModule(LocationModule locationModule);

    AppComponent build();
  }

  void inject(App app);

  void inject(FirebaseWrapper firebaseWrapper);

  void inject(SystemServicesWrapper systemServicesWrapper);

  void inject(MainPresenter presenter);
}
