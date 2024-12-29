package at.mctg.app.service.users;

import at.mctg.app.model.User;
//import at.mctg.db.DbConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDummyDAL {
    private static  List<User> userData = new ArrayList<>();

    public UserDummyDAL() {
            //userData.add(new User("Marc", "1234"));
            //userData.add(new User("John", "1234"));
            //userData.add(new User("Jane", "1234"));
    }

    // GET /users/:username
    public User getUser(String username) {
        return userData.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    // GET /users
    /*
    public List<User> getUser() throws SQLException {
        String query = "SELECT * FROM person";
        Connection connection = DbConnection.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            System.out.println(resultSet.getString(1));
            System.out.println(resultSet.getString(2));
        }
        return userData;
    }
    */

    // POST /users
    public void addUser(User user) {

        userData.add(user);
        System.out.println("User added: " + user.getUsername());
    }

    // DELETE /users/:username
    public void removeUser(String username) {
        userData.removeIf(user -> user.getUsername().equals(username));
    }

}