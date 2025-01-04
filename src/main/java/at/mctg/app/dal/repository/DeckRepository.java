package at.mctg.app.dal.repository;

import at.mctg.app.dal.DataAccessException;
import at.mctg.app.dal.UnitOfWork;
import at.mctg.app.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class DeckRepository {
    private final UnitOfWork unitOfWork;

    public DeckRepository(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    //REturns a collection/list of card IDs that the specific user has in their deck
    public Collection<UUID> getDeckByUser(String username) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                    select card_id from deck
                    where username = ?
                """))
        {
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();
            Collection<UUID> resultDeck = new ArrayList<>();

            while(resultSet.next()) {
                resultDeck.add((UUID)resultSet.getObject("card_id"));
            }

            return resultDeck;

        } catch (SQLException e) {
            throw new DataAccessException("Select nicht erfolgreich", e);
        }
    }

    //"Clears" the deck, by deleting all rows for a specific user
    public void clearDeck(String username) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                DELETE FROM deck
                WHERE username = ?
            """))
        {
            preparedStatement.setString(1, username);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Delete nicht erfolgreich", e);
        }

    }

    //Adds one card to the user's deck
    public void addCardToDeck(String username, UUID cardId) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                INSERT INTO deck (username, card_id)
                VALUES (?, ?)
            """))
        {
            preparedStatement.setString(1, username);
            preparedStatement.setObject(2, cardId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Insert operation failed", e);
        }

    }
}
