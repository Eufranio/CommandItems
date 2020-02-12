package io.github.eufranio.commanditems.storage;

import com.google.common.collect.Lists;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.misc.BaseDaoEnabled;
import com.j256.ormlite.table.TableUtils;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Created by Frani on 04/12/2018.
 */
public class Persistable<T extends BaseDaoEnabled<T, ID>, ID> {

    private static List<JdbcConnectionSource> sources = Lists.newArrayList();

    public Dao<T, ID> objDao;
    private JdbcConnectionSource src;

    public static <T extends BaseDaoEnabled<T, ID>, ID> Persistable<T, ID> create(Class<T> clazz, String url) {
        return new Persistable<>(url, clazz);
    }

    @SuppressWarnings("unchecked")
    private Persistable(String url, Class<T> clazz) {
        try {
            Optional<JdbcConnectionSource> source = sources.stream()
                    .filter(s -> s.getUrl().equals(url))
                    .findFirst();
            if (source.isPresent()) {
                src = source.get();
            } else {
                if (url.contains("sqlite")) {
                    src = new JdbcConnectionSource(url);
                } else {
                    src = new JdbcPooledConnectionSource(url);
                }
                sources.add(src);
            }
            this.objDao = DaoManager.createDao(src, clazz);
            TableUtils.createTableIfNotExists(src, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(T obj) {
        try {
            if (obj.getDao() == null) {
                obj.setDao(this.objDao);
            }
            this.objDao.createOrUpdate(obj);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(T obj) {
        try {
            this.objDao.delete(obj);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public T get(ID id) {
        try {
            return this.objDao.queryForId(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public T getOrCreate(ID id) {
        try {
            T obj = this.get(id);
            if (obj == null) {
                obj = this.objDao.getDataClass().newInstance();
                Field f = obj.getClass().getDeclaredField("uuid");
                f.set(obj, id);
                this.save(obj);
            }
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}