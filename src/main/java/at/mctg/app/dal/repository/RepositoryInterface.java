package at.mctg.app.dal.repository;

import java.util.Collection;

public interface RepositoryInterface<ID, Type> {
    public Type findByID(ID id);
    public Collection<Type> findAll();
    public Type save(Type object);
    public Type delete(ID id);
    public Type update(Type object);
}
