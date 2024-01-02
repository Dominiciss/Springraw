package com.dominicis.springraw;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dominicis.springraw.Config.AppCtxConfig;
import com.dominicis.springraw.Dao.UserDao;

@Component
public class DbFetcher implements ApplicationRunner {
    private final ApplicationContext ctx = new AnnotationConfigApplicationContext(AppCtxConfig.class);
    private final UserDao userDao = ctx.getBean(UserDao.class);
    private Boolean isFirstRun = true;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Double time = (double) System.nanoTime();
        userDao.instantiate();
        time = (System.nanoTime() - time) * /* Convert nanoseconds to seconds */ 0.000000001;
        System.out.println(String.format("%s - Database initialization complete! Took %ss", new Date().toString(), Double.toString(time).substring(0, 4)));
        
        synchronized(this) {
            this.notifyAll();
        }
        
        isFirstRun = false;
    }

    @Scheduled(fixedDelay = 20000)
    public void reload() {
        if (isFirstRun == true) {
            return;
        }

        Double time = (double) System.nanoTime();
        userDao.instantiate();
        time = (System.nanoTime() - time) * /* Convert nanoseconds to seconds */ 0.000000001;
        System.out.println(String.format("%s - Database reload complete! Took %ss", new Date().toString(), Double.toString(time).substring(0, 4)));
    }

    public UserDao userDao() {
        return userDao;
    }
}
