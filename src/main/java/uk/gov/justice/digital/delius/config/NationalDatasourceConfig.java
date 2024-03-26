package uk.gov.justice.digital.delius.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        basePackages = "uk.gov.justice.digital.delius.jpa.national.repository",
        entityManagerFactoryRef = "nationalEntityManager",
        transactionManagerRef = "nationalTransactionManager"
)
public class NationalDatasourceConfig {
    @Autowired
    private Environment env;

    @Bean(name = "nationalEntityManager")
    public LocalContainerEntityManagerFactoryBean nationalEntityManager(@Qualifier("nationalDataSource") DataSource nationalDataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(nationalDataSource);
        em.setPackagesToScan("uk.gov.justice.digital.delius.jpa.national.entity");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setPersistenceUnitName("national");
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto",
                env.getProperty("hibernate.hbm2ddl.auto"));
        final var dialect = env.getProperty("hibernate.dialect", "org.hibernate.dialect.OracleDialect");
        properties.put("hibernate.dialect", dialect);
        em.setJpaPropertyMap(properties);

        return em;
    }

    @Bean(name = "nationalDataSource")
    public DataSource nationalDataSource(DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    public PlatformTransactionManager nationalTransactionManager(@Qualifier("nationalEntityManager") LocalContainerEntityManagerFactoryBean nationalEntityManager) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(nationalEntityManager.getObject());
        return transactionManager;
    }
}
