package com.dominicis.springraw;

import java.util.Date;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dominicis.springraw.Config.AppCtxConfig;
import com.dominicis.springraw.Dao.UserDao;

@Component
public class DbFetcher implements /* Runs in application start */ ApplicationRunner {
    // Gets application contexts (config)
    private final ApplicationContext ctx = new AnnotationConfigApplicationContext(AppCtxConfig.class);
    // Instantiates UserDao with the bean created in AppCtxConfig.java
    private final UserDao userDao = ctx.getBean(UserDao.class);
    // Looks if method run(args) has already run, so reload() can start working
    private Boolean isFirstRun = true;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Double time = (double) System.nanoTime();
        userDao.instantiate();
        time = (System.nanoTime() - time) * /* Convert nanoseconds to seconds */ 0.000000001;
        System.out.println(String.format("%s - Database initialization complete! Took %ss", new Date().toString(), Double.toString(time).substring(0, 4)));
        
        // Notifies the dbFetcher.wait() that it can work again. Used to wait for data to be fetched
        synchronized(this) {
            this.notifyAll();
        }
        
        isFirstRun = false;
    }

    @Scheduled(fixedDelay = 20000)
    // Runs over time in another thread
    public void reload() {
        if (isFirstRun == true) {
            return;
        }

        Double time = (double) System.nanoTime();
        userDao.instantiate();
        time = (System.nanoTime() - time) * /* Convert nanoseconds to seconds */ 0.000000001;
        System.out.println(String.format("%s - Database reload complete! Took %ss", new Date().toString(), Double.toString(time).substring(0, 4)));
    }

    // Gets userDao
    public UserDao userDao() {
        return userDao;
    }
}
