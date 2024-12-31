package at.mctg.app.dal.repository;

import at.mctg.app.dal.DataAccessException;
import at.mctg.app.dal.UnitOfWork;
import at.mctg.app.model.CardPackage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PackageRepository {
    private final UnitOfWork unitOfWork;

    public PackageRepository(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    public void createPackage(CardPackage newPackage) {
        try (PreparedStatement preparedStatement =
             this.unitOfWork.prepareStatement("""
                         INSERT INTO packages (package_id, purchased)
                         VALUES (?, ?)
                     """))
        {
            preparedStatement.setObject(1, newPackage.getPackageId());
            preparedStatement.setBoolean(2, newPackage.isPurchased());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Insert operation failed", e);
        }
    }

    public CardPackage findById(UUID packageId) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                    select * from packages
                    where package_id = ?
                """))
        {
            preparedStatement.setObject(1, packageId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                CardPackage pack = new CardPackage();
                pack.setPackageId((UUID) resultSet.getObject("package_id"));
                pack.setPurchased(resultSet.getBoolean("purchased"));
                return pack;
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Select nicht erfolgreich", e);
        }
    }

    public void markPackagePurchased(UUID packageId) {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                UPDATE packages
                SET purchased = true
                WHERE package_id = ?
            """))
        {
            preparedStatement.setObject(1, packageId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Update operation failed", e);
        }
    }

    public CardPackage findAnyUnpurchasedPackage() {
        try (PreparedStatement preparedStatement =
                     this.unitOfWork.prepareStatement("""
                    select package_id, purchased from packages
                    where purchased = false
                    LIMIT 1
                """))
        {
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                CardPackage pack = new CardPackage();
                pack.setPackageId((UUID) resultSet.getObject("package_id"));
                pack.setPurchased(resultSet.getBoolean("purchased"));
                return pack;
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Select nicht erfolgreich", e);
        }
    }

}
