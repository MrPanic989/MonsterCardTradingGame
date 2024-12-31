package at.mctg.app.dal.repository;

import at.mctg.app.dal.DataAccessException;
import at.mctg.app.dal.UnitOfWork;
import at.mctg.app.model.Card;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import java.util.UUID;

public class CardRepository {
    private final UnitOfWork unitOfWork;

    public CardRepository(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    //Helper method to map a DB row to a Card object
    private Card mapResultSetToCard(ResultSet rs) throws SQLException {
        Card card = new Card();
        card.setCardId((UUID) rs.getObject("card_id"));
        card.setName(rs.getString("name"));
        card.setDamage(rs.getDouble("damage"));
        card.setElementType(rs.getString("element_type"));
        card.setCardType(rs.getString("card_type"));
        card.setOwnerUsername(rs.getString("owner"));
        card.setPackageId((UUID) rs.getObject("package_id"));
        return card;
    }


    public Card findByID(UUID uuid) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                    select * from cards
                    where card_id = ?
                """))
        {
            preparedStatement.setObject(1, uuid);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()) {
                return mapResultSetToCard(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Select nicht erfolgreich", e);
        }
    }

    //Returns all cards that belong to a specific user (owner)
    public Collection<Card> findCardsByOwner(String ownerUsername) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                    select * from cards
                    where owner = ?
                """))
        {
            preparedStatement.setString(1, ownerUsername);


            ResultSet resultSet = preparedStatement.executeQuery();
            Collection<Card> cardRows = new ArrayList<>();
            while(resultSet.next()) {
                cardRows.add(mapResultSetToCard(resultSet));
            }
            return cardRows;
        } catch (SQLException e) {
            throw new DataAccessException("Select nicht erfolgreich", e);
        }
    }
    //Returns all cards from a package
    public Collection<Card> findCardsByPackageId(UUID packageId) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                    select * from cards
                    where package_id = ?
                """))
        {
            preparedStatement.setObject(1, packageId);

            ResultSet resultSet = preparedStatement.executeQuery();
            Collection<Card> cardRows = new ArrayList<>();
            while (resultSet.next()) {
                cardRows.add(mapResultSetToCard(resultSet));
            }
            return cardRows;
        } catch (SQLException e) {
            throw new DataAccessException("Select (findCardsByPackageId) failed", e);
        }
    }


    public void insertCard(Card object) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                INSERT INTO cards (card_id, name, damage, element_type, card_type, owner, package_id)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """))
        {
            preparedStatement.setObject(1, object.getCardId());
            preparedStatement.setString(2, object.getName());
            preparedStatement.setDouble(3, object.getDamage());
            preparedStatement.setString(4, object.getElementType());
            preparedStatement.setString(5, object.getCardType());
            preparedStatement.setString(6, object.getOwnerUsername());
            preparedStatement.setObject(7, object.getPackageId()); //which package does it belong to

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Insert operation failed", e);
        }
    }

    public void updateCard(Card object) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                UPDATE cards
                SET name = ?, damage = ?, element_type = ?, card_type = ?, owner = ?, package_id = ?
                WHERE card_id = ?
            """))
        {
            preparedStatement.setString(1, object.getName());
            preparedStatement.setDouble(2, object.getDamage());
            preparedStatement.setString(3, object.getElementType());
            preparedStatement.setString(4, object.getCardType());
            preparedStatement.setString(5, object.getOwnerUsername());
            preparedStatement.setObject(6, object.getPackageId());
            preparedStatement.setObject(7, object.getCardId());

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Update operation failed", e);
        }
    }
}

