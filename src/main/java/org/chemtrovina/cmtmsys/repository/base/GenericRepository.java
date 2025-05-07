package org.chemtrovina.cmtmsys.repository.base;

import java.util.List;

public interface GenericRepository<T> {
    T findById(int id);
    List<T> findAll();
    void add(T entity);
    void update(T entity);
    void delete(int id);
}
