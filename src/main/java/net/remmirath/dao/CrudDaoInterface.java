package net.remmirath.dao;

public interface CrudDaoInterface {

    public abstract Integer create(Long data);

    public abstract Long read(final int id);

    public abstract void update(final int id, Long newData);

    public abstract void delete(final int id);

}
