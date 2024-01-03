package com.dominicis.springraw.Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.dominicis.springraw.Entity.User;
import com.dominicis.springraw.Exception.UserException;

import jakarta.servlet.http.HttpServletRequest;

// Indicates that it is a spring component
@Component
// DAO: Data access object
public class UserDao {
    // DataSource of database, where all info is fetched from
    private DataSource dataSource;
    // User list pre-loaded for a better and faster client experience
    private List<User> users = new ArrayList<User>();

    // Instantiate the datasource and a "unknown" user to have as reference
    public UserDao(DataSource dataSource) {
        this.dataSource = dataSource;
        users.add(new User(-1, null, null, null));
    }

    // Initialization of user list
    public void instantiate() {
        try (Connection connection = dataSource.getConnection()) {
            Statement selectStatement = connection.createStatement();
            ResultSet userResult = selectStatement.executeQuery("SELECT id, username, password, create_time FROM user");

            users.clear();
            while (userResult.next()) {
                Integer id = userResult.getInt("id");
                String name = userResult.getString("username");
                String pass = userResult.getString("password");
                Date date = userResult.getDate("create_time");
                users.add(new User(id, name, pass, date));
            }

            userResult.close();
            selectStatement.close();
            connection.close();
        } catch (SQLException e) {
            System.err.println(e);
        }
    }

    public List<User> findAll() {
        return users;
    }

    public User findByName(String name) {
        return users.stream().filter((user) -> user.getUsername().equals(name)).findFirst().orElse(null);
    }

    public User create(Integer id, String username, String password, Date date) throws UserException {
        // Looks if the user id has already been used
        if (users.stream().filter((user) -> user.getId().equals(id)).count() > 0) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest();
            String ip = request.getHeader("X-FORWARDED-FOR");

            if (ip == null || ip.isEmpty()) {
                ip = request.getRemoteAddr();
            }

            System.err.println(String.format(
                    "%s tried to create the user {id: %s, username: %s, password: %s, date: %s}, but id was duplicated",
                    ip, id, username, password, new java.sql.Date(date.getTime())));
            throw new UserException("Duplicated id, user cannot use an id that has already been used");
        }

        // Excecutes the update to the database in another thread, so the user has better loading times
        CompletableFuture.runAsync(() -> {
            Double time = (double) System.nanoTime();
            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement selectStatement = connection
                        .prepareStatement(
                                "INSERT INTO user (id, create_time, username, password) VALUES (?, ?, ?, ?)");
                selectStatement.setInt(1, id);
                selectStatement.setDate(2, new java.sql.Date(date.getTime()));
                selectStatement.setString(3, username);
                selectStatement.setString(4, password);
                Integer result = selectStatement.executeUpdate();

                selectStatement.close();
                connection.close();

                time = (System.nanoTime() - time) * /* Convert nanoseconds to seconds */ 0.000000001;
                if (result == 0) {
                    System.err.println(String.format("%s - Could not save user. Took %ss", new Date().toString(),
                            Double.toString(time).substring(0, 4)));
                } else {
                    System.out.println(String.format("%s - User was saved succesfully! Took %ss", new Date().toString(),
                            Double.toString(time).substring(0, 4)));

                    users.add(new User(id, username, password, new java.sql.Date(date.getTime())));
                }
            } catch (SQLException e) {
                System.err.println(e);
            }
        });

        return null;
    }

    public User update(Integer id, String username, String password) throws UserException {
        if (users.stream().filter((user) -> user.getId().equals(id)).count() < 1) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest();
            String ip = request.getHeader("X-FORWARDED-FOR");

            if (ip == null || ip.isEmpty()) {
                ip = request.getRemoteAddr();
            }

            System.err.println(String.format(
                    "%s tried to update user {id: %s}, but id was not found",
                    ip, id));
            throw new UserException("Id not found, no user had that id");
        }

        CompletableFuture.runAsync(() -> {
            Double time = (double) System.nanoTime();
            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement selectStatement = connection
                        .prepareStatement(
                                "UPDATE user SET username = ?, password = ? WHERE id = ?");
                selectStatement.setString(1, username);
                selectStatement.setString(2, password);
                selectStatement.setInt(3, id);
                Integer result = selectStatement.executeUpdate();

                selectStatement.close();
                connection.close();

                time = (System.nanoTime() - time) * /* Convert nanoseconds to seconds */ 0.000000001;
                if (result == 0) {
                    System.err.println(String.format("%s - Could not update user. Took %ss", new Date().toString(),
                            Double.toString(time).substring(0, 4)));
                } else {
                    System.out.println(String.format("%s - User was updated succesfully! Took %ss", new Date().toString(),
                            Double.toString(time).substring(0, 4)));

                    User user = users.stream().filter((e) -> e.getId() == id).findFirst().orElse(null);
                    users.remove(user);
                    user.setUsername(username);
                    user.setPassword(password);
                    users.add(user);
                }
            } catch (SQLException e) {
                System.err.println(e);
            }
        });

        return null;
    }

    public User delete(Integer id) throws UserException {
        if (users.stream().filter((user) -> user.getId().equals(id)).count() < 1) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest();
            String ip = request.getHeader("X-FORWARDED-FOR");

            if (ip == null || ip.isEmpty()) {
                ip = request.getRemoteAddr();
            }

            System.err.println(String.format(
                    "%s tried to delete user {id: %s}, but id was not found",
                    ip, id));
            throw new UserException("Id not found, no user had that id");
        }

        CompletableFuture.runAsync(() -> {
            Double time = (double) System.nanoTime();
            try (Connection connection = dataSource.getConnection()) {
                PreparedStatement selectStatement = connection
                        .prepareStatement(
                                "DELETE FROM user WHERE id = ?");
                selectStatement.setInt(1, id);
                Integer result = selectStatement.executeUpdate();

                selectStatement.close();
                connection.close();

                time = (System.nanoTime() - time) * /* Convert nanoseconds to seconds */ 0.000000001;
                if (result == 0) {
                    System.err.println(String.format("%s - Could not delete user. Took %ss", new Date().toString(),
                            Double.toString(time).substring(0, 4)));
                } else {
                    System.out.println(String.format("%s - User was removed succesfully! Took %ss", new Date().toString(),
                            Double.toString(time).substring(0, 4)));

                    users.remove(users.stream().filter((user) -> user.getId() == id).findFirst().orElse(null));
                }
            } catch (SQLException e) {
                System.err.println(e);
            }
        });

        return null;
    }
}
