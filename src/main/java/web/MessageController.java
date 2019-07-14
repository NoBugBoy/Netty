package web;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;
import server.ChannelStringList;
import web.service.ConnService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class MessageController extends HttpServlet{
    private static final Logger log = Logger.getLogger(MessageController.class);
    @Inject
    Injector injector;
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String msg = req.getParameter("msg");
        ChannelStringList.sendAll(msg);
        ConnService instance = injector.getInstance(ConnService.class);
        instance.addConn("123");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req,resp);
    }
}
