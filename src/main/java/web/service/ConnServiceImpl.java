package web.service;

import com.google.inject.Singleton;
import org.apache.log4j.Logger;

@Singleton
public class ConnServiceImpl implements ConnService {
    private Logger LOGGER = Logger.getLogger(ConnServiceImpl.class);
    @Override
    public void addConn(String id) {
        LOGGER.info("addconn" + id);
    }
}
