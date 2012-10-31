package net.remmirath.spring.caching;

import static org.junit.Assert.assertNull;
import net.remmirath.spring.caching.SpringCachingServiceBean.K9;
import net.remmirath.spring.caching.SpringCachingServiceBean.KeyBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/**
 * @author oleg
 *
 */
@ContextConfiguration(classes = { SpringCachingFrameworkTestContext.class })
public class SpringCachingFrameworkTest extends AbstractTestNGSpringContextTests {
    final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AlertCollator alertCollator;

    @Autowired
    private SpringCachingServiceBean service;

    private KeyBase mustWorkWithSpELKey = new KeyBase(3,7);

    @BeforeMethod
    public void setup() {
        alertCollator.alertCount = 0;
        service.cacheEvictingMethod(mustWorkWithSpELKey);
    }

    @Test
    public void mustWorkWithSpEL() {
        AssertJUnit.assertEquals(0, alertCollator.alertCount);
        int result = service.cachedMethod(mustWorkWithSpELKey);
        AssertJUnit.assertEquals(1, alertCollator.alertCount);
        int result2 = service.cachedMethod(mustWorkWithSpELKey);
        AssertJUnit.assertEquals(1, alertCollator.alertCount);
        AssertJUnit.assertEquals(42, result);
        AssertJUnit.assertEquals(result, result2);
        mustEvict(mustWorkWithSpELKey);
    }

    public void mustEvict(KeyBase key) {
        service.cacheEvictingMethod(key);
        int result = service.cachedMethod(key);
        AssertJUnit.assertEquals(2, alertCollator.alertCount);
        AssertJUnit.assertEquals(42, result);
        int result2 = service.cachedMethod(key);
        AssertJUnit.assertEquals(2, alertCollator.alertCount);
        AssertJUnit.assertEquals(result, result2);
    }

    @Test
    public void mustSupportNullKey() {
        K9 key = null;
        Integer result = service.cachedMethodThatReturnsNull(key);
        assertNull(result);
        key = new K9(null);
        result = service.cachedMethodThatReturnsNull(key);
    }

    @Test(expectedExceptions=RuntimeException.class)
    public void mustCatchSpELErrors() {
        KeyBase key = new KeyBase(7,11);
        try {
            service.cachedMethodWrongSpEL(key);
        } catch (RuntimeException e) {
            log.debug("Exception 1!", e);
            throw e;
        }
        try {
            service.cachedMethodWrongSpEL2(key);
        } catch (RuntimeException e) {
            log.debug("Exception 2!", e);
            throw e;
        }
    }

}
