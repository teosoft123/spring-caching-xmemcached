package net.remmirath.spring.caching;

import java.util.Arrays;
import java.util.Iterator;

import net.remmirath.dao.CrudDaoInterface;
import net.remmirath.spring.caching.SpringCachingFrameworkTestContext.MemcachedClientController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.thimbleware.jmemcached.LocalCacheElement;
import com.thimbleware.jmemcached.MemCacheDaemon;

@ContextConfiguration(classes = { CachedCrudDaoTestContext.class })
public class CachedCrudTest extends AbstractTestNGSpringContextTests {

    @Autowired
    CrudDaoInterface dao;

    @Autowired
    CrudAlertCollator alertCollator;

    @Autowired
    MemCacheDaemon<LocalCacheElement> memcachedDaemon;

    @Autowired
    MemcachedClientController memcachedClientController;

    @Test(dataProvider = "dataProvider")
    public void mustCRUDWithCache(Control cc, Iterator<Integer> expected) throws Exception {
        cc.control();
        int id = dao.create(33L);
        AssertJUnit.assertNotNull(id);
        Long data = dao.read(id);
        AssertJUnit.assertEquals(new Long(33L), data);
        data = dao.read(id);
        AssertJUnit.assertEquals(new Long(33L), data);
        dao.update(id, 44L);
        data = dao.read(id);
        AssertJUnit.assertEquals(new Long(44L), data);
        data = dao.read(id);
        AssertJUnit.assertEquals(new Long(44L), data);
        dao.delete(id);
        data = dao.read(id);
        AssertJUnit.assertNull(data);
        AssertJUnit.assertEquals(expected.next().intValue(), alertCollator.creates);
        AssertJUnit.assertEquals(expected.next().intValue(), alertCollator.reads);
        AssertJUnit.assertEquals(expected.next().intValue(), alertCollator.updates);
        AssertJUnit.assertEquals(expected.next().intValue(), alertCollator.deletes);
    }

    interface Control {
        void control() throws Exception;
    }

    @DataProvider
    public Object[][] dataProvider() {
        return new Object[][] {
          new Object[] {
                  new Control() {
                    @Override
                    public String toString() {
                        return "Cache ON";
                    }
                    @Override
                      public void control() throws InterruptedException {
                          alertCollator.reset();
                      }
                  },
                  Arrays.asList(1, 3, 1, 1).iterator()
            },

            new Object[] {
                  new Control() {
                      @Override
                      public String toString() {
                          return "Cache OFF";
                      }
                      @Override
                      public void control() throws Exception {
                          alertCollator.reset();
                          memcachedClientController.removeAllServers();
                      }
                  }, Arrays.asList(1, 5, 1, 1).iterator() },
            };
    }

}
