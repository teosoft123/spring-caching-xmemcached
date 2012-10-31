package net.remmirath.spring.caching;

import net.remmirath.dao.CachedCrudDao;
import net.remmirath.dao.CrudDaoInterface;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({SpringCachingFrameworkTestContext.class})
public class CachedCrudDaoTestContext {

    @Bean
    public CrudDaoInterface dao() {
        return new CachedCrudDao();
    }

    @Bean
    public CrudAlertCollator crudAlertCollator() {
        return new CrudAlertCollator();
    }

}
