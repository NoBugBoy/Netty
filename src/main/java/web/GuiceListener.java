package web;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.servlet.GuiceServletContextListener;

public class GuiceListener extends GuiceServletContextListener {
    @Provides
    @Override
    protected Injector getInjector() {
        return Guice.createInjector(new MyModel());
    }
}