package net.remmirath.spring.caching;

import net.remmirath.dao.CrudAlertListener;

public class CrudAlertCollator implements CrudAlertListener {

    public volatile int creates = 0;
    public volatile int reads = 0;
    public volatile int updates = 0;
    public volatile int deletes = 0;

    @Override
    public void alertCreate() {
        ++creates;
    }

    @Override
    public void alertRead() {
        ++reads;
    }

    @Override
    public void alertUpdate() {
        ++updates;
    }

    @Override
    public void alertDelete() {
        ++deletes;
    }

    public void reset() {
        creates = reads = updates = deletes = 0;
    }

}
