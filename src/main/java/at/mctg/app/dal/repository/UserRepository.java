package at.mctg.app.dal.repository;

import at.mctg.app.dal.UnitOfWork;
import at.mctg.app.model.User;

import java.util.Collection;
import java.util.List;

public class UserRepository implements RepositoryInterface<Integer, User> {
    private UnitOfWork unitOfWork;

    public UserRepository(UnitOfWork unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    @Override
    public User findByID(Integer integer) {
        return null;
    }

    @Override
    public Collection<User> findAll() {
        return List.of();
    }

    @Override
    public User save(User object) {
        return null;
    }

    @Override
    public User delete(Integer integer) {
        return null;
    }

    @Override
    public User update(User object) {
        return null;
    }
}

