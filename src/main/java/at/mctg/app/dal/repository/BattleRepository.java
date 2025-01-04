package at.mctg.app.dal.repository;

import at.mctg.app.dal.DataAccessException;
import at.mctg.app.dal.UnitOfWork;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BattleRepository {

    private final UnitOfWork unitOfWork;

    public BattleRepository(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    public int saveBattle(String challenger, String opponent, String result, String log) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                INSERT INTO battles (challenger, opponent, result, log)
                VALUES (?, ?, ?, ?)
                RETURNING battle_id;
            """))
        {
            preparedStatement.setString(1, challenger);
            preparedStatement.setString(2, opponent);
            preparedStatement.setString(3, result);
            preparedStatement.setString(4, log);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int battleId = resultSet.getInt("battle_id");
                System.out.println("Battle saved with ID = " + battleId);
                return battleId;
            }
            return -1;
        } catch (SQLException e) {
            throw new DataAccessException("Insert battle failed", e);
        }
    }


}
