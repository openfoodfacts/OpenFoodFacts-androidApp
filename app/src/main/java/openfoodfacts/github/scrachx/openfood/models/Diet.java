package openfoodfacts.github.scrachx.openfood.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Unique;

import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

/**
 * Created by dobriseb on 2018.10.15.
 */
@Entity(
        indexes = {
                @Index(value = "tag DESC", unique = true)
        }
)
public class Diet {
    @Id(autoincrement = true)
    private Long id;
    @Unique
    private String tag;

    @ToMany(joinProperties = {
        @JoinProperty(name = "tag", referencedName = "dietTag")
    })
    private List<DietName> names;
    private boolean enabled;
/** Used to resolve relations */
@Generated(hash = 2040040024)
private transient DaoSession daoSession;
/** Used for active entity operations. */
@Generated(hash = 1318681666)
private transient DietDao myDao;
@Generated(hash = 7986711)
public Diet(Long id, String tag, boolean enabled) {
    this.id = id;
    this.tag = tag;
    this.enabled = enabled;
}
@Generated(hash = 1084628194)
public Diet() {
}
public Long getId() {
    return this.id;
}
public void setId(Long id) {
    this.id = id;
}
public String getTag() {
    return this.tag;
}
public void setTag(String tag) {
    this.tag = tag;
}
public boolean getEnabled() {
    return this.enabled;
}
public void setEnabled(boolean enabled) {
    this.enabled = enabled;
}
/**
 * To-many relationship, resolved on first access (and after reset).
 * Changes to to-many relations are not persisted, make changes to the target entity.
 */
@Generated(hash = 1388775944)
public List<DietName> getNames() {
    if (names == null) {
        final DaoSession daoSession = this.daoSession;
        if (daoSession == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        DietNameDao targetDao = daoSession.getDietNameDao();
        List<DietName> namesNew = targetDao._queryDiet_Names(tag);
        synchronized (this) {
            if (names == null) {
                names = namesNew;
            }
        }
    }
    return names;
}
/** Resets a to-many relationship, making the next get call to query for a fresh result. */
@Generated(hash = 1832659617)
public synchronized void resetNames() {
    names = null;
}
/**
 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
 * Entity must attached to an entity context.
 */
@Generated(hash = 128553479)
public void delete() {
    if (myDao == null) {
        throw new DaoException("Entity is detached from DAO context");
    }
    myDao.delete(this);
}
/**
 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
 * Entity must attached to an entity context.
 */
@Generated(hash = 1942392019)
public void refresh() {
    if (myDao == null) {
        throw new DaoException("Entity is detached from DAO context");
    }
    myDao.refresh(this);
}
/**
 * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
 * Entity must attached to an entity context.
 */
@Generated(hash = 713229351)
public void update() {
    if (myDao == null) {
        throw new DaoException("Entity is detached from DAO context");
    }
    myDao.update(this);
}
/** called by internal mechanisms, do not call yourself. */
@Generated(hash = 1364662479)
public void __setDaoSession(DaoSession daoSession) {
    this.daoSession = daoSession;
    myDao = daoSession != null ? daoSession.getDietDao() : null;
}
}
