package com.dominicis.springraw.Config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.dominicis.springraw.Dao.UserDao;
import com.mysql.cj.jdbc.MysqlDataSource;

// App Context Configuration
@Configuration
// Enables @Schedule, an async clock that runs a method multiple times
@EnableScheduling
// Scan for @Component
@ComponentScan
public class AppCtxConfig {
    
    @Bean
    @Scope("singleton")
    /*
     * singleton: Only one instance created between multiple devices. (DEFAULT)
     * prototype: Every time the method is called, there will be a new instance.
     * session: One bean created for every HTTP session.
     * request: One bean created for every HTTP request.
     * application: One bean created for every lifecycle of a ServletContext. (+)
     * websocket: One bean created for every lifecycle of a WebSocket. (+)
     + Only valid in the context of a web-aware Spring ApplicationContext.
     */
     public DataSource dataSource() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setUser("springrawuser");
        dataSource.setPassword("springrawpass");
        dataSource.setURL("jdbc:mysql://db4free.net:3306/springraw");
        return dataSource;
     }

     @Bean
     public UserDao userDao() {
      return new UserDao(dataSource());
     }
}
