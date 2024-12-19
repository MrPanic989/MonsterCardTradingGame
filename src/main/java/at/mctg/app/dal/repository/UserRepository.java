package at.mctg.app.dal.repository;

import at.mctg.app.dal.DataAccessException;
import at.mctg.app.dal.UnitOfWork;
import at.mctg.app.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserRepository implements RepositoryInterface<String, User> {
    private UnitOfWork unitOfWork;

    public UserRepository(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    public User findByID(String name) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                    select * from person
                    where id = ?
                """))
        {
            //1 steht für das Fragezeichen(das Statement kann ja nach
            //mehreren Parametern abfragen: z.B. where region = ? and country = ?
            //preparedStatement.setString(1, "Europe");
            preparedStatement.setString(1, name);

            //preparedStatement.setDouble(2, 5.0);

            ResultSet resultSet = preparedStatement.executeQuery();

            User user = null;
            while(resultSet.next())
            {
                user = new User(
                        resultSet.getString(2),
                        resultSet.getString(4),
                        resultSet.getString(5),
                        resultSet.getString(6));
            }
            return user;
        } catch (SQLException e) {
            throw new DataAccessException("Select nicht erfolgreich", e);
        }
    }

    @Override
    public Collection<User> findAll() {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                    select * from person
                    
                """))
        {
            //1 steht für das Fragezeichen(das Statement kann ja nach
            //mehreren Parametern abfragen: z.B. where region = ? and country = ?
            // preparedStatement.setString(1, "Europe");
            //preparedStatement.setInt();
            //preparedStatement.setDouble(2, 5.0);

            ResultSet resultSet = preparedStatement.executeQuery();
            Collection<User> userRows = new ArrayList<>();
            while(resultSet.next())
            {
                User user = new User(
                        resultSet.getString(2),
                        resultSet.getString(6),
                        resultSet.getString(7),
                        resultSet.getString(8));
                userRows.add(user);
            }
            return userRows;
        } catch (SQLException e) {
            throw new DataAccessException("Select nicht erfolgreich", e);
        }
    }

    @Override
    public User save(User object) {
        // Nur region, city, temperature sind hier relevant.
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                INSERT INTO person (username, password)
                VALUES (?, ?)
                RETURNING id;
            """))
        {
            preparedStatement.setString(1, object.getUsername());
            preparedStatement.setString(2, object.getPassword());

            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                object.setId(generatedId);
            }

            return object;
        } catch (SQLException e) {
            throw new DataAccessException("Insert operation failed", e);
        }
    }

    @Override
    public User delete(String name) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                DELETE FROM person
                WHERE username = ?
            """))
        {
            preparedStatement.setString(2, name);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Record with username " + name + " was deleted successfully.");
            } else {
                System.out.println("No record found with username " + name + ".");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Delete operation failed", e);
        }
        return null;
    }

    @Override
    public User update(User object) {
        // Wir machen hier ein Update nach username. Falls es den Eintrag nicht gibt,
        // soll entweder nichts passieren oder null zurückgegeben werden.
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                UPDATE person
                SET name = ?, bio = ?, image = ?
                WHERE username = ?
                RETURNING username, name, bio, image;
            """))
        {

            preparedStatement.setString(6, object.getName());
            preparedStatement.setString(7, object.getBio());
            preparedStatement.setString(8, object.getImage());
            preparedStatement.setString(2, object.getUsername());

            ResultSet rs = preparedStatement.executeQuery();

            // Wenn das Update erfolgreich war, sollte ein Datensatz zurückkommen.
            if (rs.next()) {
                // Aktualisiertes Objekt auslesen (zurückgeben)
                User updatedUser = new User(
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("bio"),
                        rs.getString("image")
                );
                return updatedUser;
            } else {
                // Keine Zeilen gefunden -> kein Update durchgeführt
                return null;
            }

        } catch (SQLException e) {
            throw new DataAccessException("Update operation failed", e);
        }
    }
}

