package com.dominicis.springraw.Controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.dominicis.springraw.DbFetcher;
import com.dominicis.springraw.Config.AppCtxConfig;
import com.dominicis.springraw.Dao.UserDao;
import com.dominicis.springraw.Entity.User;
import com.dominicis.springraw.Exception.UserException;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private DbFetcher dbFetcher;

    @GetMapping("")
    public Object getAll() {
        if (dbFetcher.userDao().findAll().get(0).getId() == -1) {
            synchronized (dbFetcher) {
                try {
                    dbFetcher.wait();
                } catch (InterruptedException e) {
                    System.err.println(e);
                }
            }
        }
        return dbFetcher.userDao().findAll();
    }

    @GetMapping("/search")
    public Object getByName(@RequestParam String username) {
        return dbFetcher.userDao().findByName(username);
    }

    @GetMapping("/create")
    public ResponseEntity<Object> create(@RequestParam Integer id, @RequestParam String username,
            @RequestParam String password) {
        if (dbFetcher.userDao().findAll().get(0).getId() == -1) {
            synchronized (dbFetcher) {
                try {
                    dbFetcher.wait();
                } catch (InterruptedException e) {
                    System.err.println(e);
                }
            }
        }

        try {
            dbFetcher.userDao().create(id, username, password, new Date());
        } catch (UserException e) {
            return new ResponseEntity<Object>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Object>(new User(id, username, password, new Date()), HttpStatus.OK);
    }
}
