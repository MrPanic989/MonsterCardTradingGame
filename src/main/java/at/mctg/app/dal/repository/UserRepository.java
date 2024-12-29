package at.mctg.app.dal.repository;

import at.mctg.app.dal.DataAccessException;
import at.mctg.app.dal.UnitOfWork;
import at.mctg.app.dto.UserDTO;
import at.mctg.app.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UserRepository implements RepositoryInterface<String, User> {
    private final UnitOfWork unitOfWork;

    public UserRepository(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId((UUID) rs.getObject("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setAuthtoken(rs.getString("authtoken"));
        user.setAdmin(rs.getBoolean("admin"));
        user.setName(rs.getString("name"));
        user.setBio(rs.getString("bio"));
        user.setImage(rs.getString("image"));
        user.setCoins(rs.getInt("coins"));
        user.setElo(rs.getInt("elo"));
        user.setGamesPlayed(rs.getInt("gamesplayed"));
        user.setWins(rs.getInt("wins"));
        user.setLosses(rs.getInt("losses"));
        return user;
    }
    /*
    public UserDTO findByUsername(String username) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                    select * from person
                    where username = ?
                """))
        {
            //1 steht für das Fragezeichen(das Statement kann ja nach
            //mehreren Parametern abfragen: z.B. where region = ? and country = ?
            //preparedStatement.setString(1, "Europe");
            preparedStatement.setString(1, username);

            //preparedStatement.setDouble(2, 5.0);

            ResultSet resultSet = preparedStatement.executeQuery();

            UserDTO user = null;
            while(resultSet.next())
            {
                user = new UserDTO(
                        resultSet.getString(4),
                        resultSet.getString(5),
                        resultSet.getString(6));
            }
            return user;
        } catch (SQLException e) {
            throw new DataAccessException("Select nicht erfolgreich", e);
        }
    }
    */
    @Override
    public User findByID(String username) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                    select * from person
                    where username = ?
                """))
        {
            //1 steht für das Fragezeichen(das Statement kann ja nach
            //mehreren Parametern abfragen: z.B. where region = ? and country = ?
            //preparedStatement.setString(1, "Europe");
            preparedStatement.setString(1, username);

            //preparedStatement.setDouble(2, 5.0);

            ResultSet resultSet = preparedStatement.executeQuery();
            /*
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

             */
            if(resultSet.next()) {
                return mapResultSetToUser(resultSet);
            }
            return null;
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
            /*
            while(resultSet.next())
            {
                User user = new User(
                        resultSet.getString(2),
                        resultSet.getString(6),
                        resultSet.getString(7),
                        resultSet.getString(8));
                userRows.add(user);
            }

             */
            while(resultSet.next()) {
                userRows.add(mapResultSetToUser(resultSet));
            }
            return userRows;
        } catch (SQLException e) {
            throw new DataAccessException("Select nicht erfolgreich", e);
        }
    }

    @Override
    public User save(User object) {

        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                INSERT INTO person (id, username, password, authtoken, admin, name, bio, image, coins, elo, gamesplayed, wins, losses)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id;
            """))
        {
            UUID uuid = UUID.randomUUID();
            preparedStatement.setObject(1, uuid);
            preparedStatement.setString(2, object.getUsername());
            preparedStatement.setString(3, object.getPassword());
            preparedStatement.setString(4, object.getAuthtoken());
            preparedStatement.setBoolean(5, object.isAdmin());
            preparedStatement.setString(6, object.getName());
            preparedStatement.setString(7, object.getBio());
            preparedStatement.setString(8, object.getImage());
            preparedStatement.setInt(9, object.getCoins());
            preparedStatement.setInt(10, object.getElo());
            preparedStatement.setInt(11, object.getGamesPlayed());
            preparedStatement.setInt(12, object.getWins());
            preparedStatement.setInt(13, object.getLosses());

            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                /*
                UUID generatedId = UUID.fromString(rs.getString(1));
                object.setId(generatedId);

                 */
                object.setId((UUID) rs.getObject("id"));
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
            preparedStatement.setString(1, name);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Record with username " + name + " was deleted successfully.");
            } else {
                System.out.println("No record found with username " + name + ".");
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Delete operation failed", e);
        }

    }

    @Override
    public User update(User object) {
        // Wir machen hier ein Update nach username. Falls es den Eintrag nicht gibt,
        // soll entweder nichts passieren oder null zurückgegeben werden.
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                UPDATE person
                SET name = ?, bio = ?, image = ?, authtoken = ?, admin = ?, coins = ?, elo = ?, gamesplayed = ?, wins = ?, losses = ?
                WHERE username = ?
                RETURNING id, username, password, authtoken, admin, name, bio, image, coins, elo, gamesplayed, wins, losses;
            """))
        {
            preparedStatement.setString(1, object.getName());
            preparedStatement.setString(2, object.getBio());
            preparedStatement.setString(3, object.getImage());
            preparedStatement.setString(4, object.getAuthtoken());
            preparedStatement.setBoolean(5, object.isAdmin());
            preparedStatement.setInt(6, object.getCoins());
            preparedStatement.setInt(7, object.getElo());
            preparedStatement.setInt(8, object.getGamesPlayed());
            preparedStatement.setInt(9, object.getWins());
            preparedStatement.setInt(10, object.getLosses());
            preparedStatement.setString(11, object.getUsername());

            ResultSet rs = preparedStatement.executeQuery();

            // Wenn das Update erfolgreich war, sollte ein Datensatz zurückkommen.
            if (rs.next()) {
                // Aktualisiertes Objekt auslesen (zurückgeben)
                /*
                User updatedUser = new User(
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("bio"),
                        rs.getString("image")
                );

                 */
                return mapResultSetToUser(rs);
            } else {
                // Keine Zeilen gefunden -> kein Update durchgeführt
                return null;
            }

        } catch (SQLException e) {
            throw new DataAccessException("Update operation failed", e);
        }
    }
}

