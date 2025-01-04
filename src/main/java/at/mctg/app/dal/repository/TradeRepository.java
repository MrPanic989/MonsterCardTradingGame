package at.mctg.app.dal.repository;

import at.mctg.app.dal.DataAccessException;
import at.mctg.app.dal.UnitOfWork;
import at.mctg.app.model.TradeDeal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradeRepository {
    private final UnitOfWork unitOfWork;

    public TradeRepository(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    //Helper method to map a Databank row to TradeDeal Object
    private TradeDeal mapRow(ResultSet rs) throws SQLException {
        TradeDeal deal = new TradeDeal();
        deal.setId((UUID) rs.getObject("trade_id"));
        deal.setCardToTradeId((UUID) rs.getObject("card_id"));
        deal.setRequiredCardType(rs.getString("required_type"));
        deal.setMinimumDamage(rs.getDouble("required_damage"));
        deal.setUsername(rs.getString("username"));
        return deal;
    }

    public void createDeal(TradeDeal deal) {

        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                INSERT INTO trades (trade_id, card_id, required_type, required_damage, username)
                VALUES (?, ?, ?, ?, ?)
            """))
        {
            preparedStatement.setObject(1, deal.getId());
            preparedStatement.setObject(2, deal.getCardToTradeId());
            preparedStatement.setString(3, deal.getRequiredCardType());
            preparedStatement.setDouble(4, deal.getMinimumDamage());
            preparedStatement.setString(5, deal.getUsername());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Insert operation failed", e);
        }

    }


    public List<TradeDeal> findAllDeals() {

        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                    SELECT * FROM trades
                """))
        {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<TradeDeal> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(mapRow(resultSet));
            }

            return result;

        } catch (SQLException e) {
            throw new DataAccessException("Select all deals failed", e);
        }

    }

    public TradeDeal findDealById(UUID tradeId) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                    SELECT * FROM trades
                    WHERE trade_id = ?
                """))
        {
            preparedStatement.setObject(1, tradeId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()) {
                return mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Select deal by id failed", e);
        }
    }

    public void deleteDeal(UUID tradeId) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                DELETE FROM trades
                WHERE trade_id = ?
            """))
        {
            preparedStatement.setObject(1, tradeId);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Delete operation failed", e);
        }
    }

}
