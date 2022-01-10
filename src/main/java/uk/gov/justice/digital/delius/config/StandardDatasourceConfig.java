package uk.gov.justice.digital.delius.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
        basePackages = {"uk.gov.justice.digital.delius.jpa.standard.repository"},
        entityManagerFactoryRef = "standardEntityManager",
        transactionManagerRef = "standardTransactionManager"
)
public class StandardDatasourceConfig {
    @Autowired
    private Environment env;

    @Bean(name = "standardEntityManager")
    @Primary
    public LocalContainerEntityManagerFactoryBean standardEntityManager(DataSource standardDataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(standardDataSource);
        
        em.setPackagesToScan("uk.gov.justice.digital.delius.jpa.standard.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setPersistenceUnitName("standard");
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto",
                env.getProperty("hibernate.hbm2ddl.auto"));
        final var dialect = env.getProperty("hibernate.dialect");
        properties.put("hibernate.dialect",
            dialect == null ? "org.hibernate.dialect.Oracle12cDialect" : dialect);
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Primary
    @Bean
    public DataSource standardDataSource(DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean(name = "standardTransactionManager")
    public PlatformTransactionManager standardTransactionManager(@Qualifier("standardEntityManager") LocalContainerEntityManagerFactoryBean standardEntityManager) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(standardEntityManager.getObject());
        return transactionManager;
    }
}
