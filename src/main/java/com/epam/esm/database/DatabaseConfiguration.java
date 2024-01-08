package esm.database;

import static com.epam.esm.logs.LogMessages.CREATING_DATABASE;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@Slf4j
@ComponentScan(basePackages = "com.epam.esm.database")
@PropertySource("classpath:application.properties")
public class DatabaseConfiguration {

    @Value("${db.driver}")
    private String DRIVER;
    @Value("${db.url}")
    private String URL;
    @Value("${user}")
    private String USERNAME;
    @Value("${password}")
    private String PASSWORD;

    @Bean
    public DataSource dataSource() throws IllegalStateException {

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(DRIVER);
        dataSource.setUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        Resource resource = new ClassPathResource("database.sql");
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator(resource);
        databasePopulator.execute(dataSource);
        log.debug(CREATING_DATABASE);

        return dataSource;
    }


    @Bean
    public DataSourceTransactionManager dataSourceTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {

        return new JdbcTemplate(dataSource());
    }

}

