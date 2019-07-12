package web;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import web.service.ConnService;
import web.service.ConnServiceImpl;

public class MyModel extends AbstractModule{

    @Override
    protected void configure() {
        install(new ServletModule(){
            @Override
            protected void configureServlets() {
                serve("/sendToAll").with(MessageController.class);
                bind(ConnService.class).to(ConnServiceImpl.class).in(Scopes.SINGLETON);
            }
        });
    }
}
