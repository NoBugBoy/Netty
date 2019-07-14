package web;

import server.NettyServer;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class StartAddDataListener  implements ServletContextListener {

    private static final Logger log = Logger.getLogger(StartAddDataListener.class);
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        log.info("开始启动Netty...");
        new Thread(() -> {
            try {
                new NettyServer(8089).start();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }).start();
        log.info("Netty启动完成...");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}