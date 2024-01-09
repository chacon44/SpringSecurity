package com.epam.esm.database;

import java.sql.Connection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.logging.Logger;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"!h2"})
public class DatabaseConfiguration {
  private static final Logger LOGGER = Logger.getLogger(DatabaseConfiguration.class.getName());

  @Value("${spring.datasource.url}")
  private String url;

  @Value("${spring.datasource.username}")
  private String username;

  @Value("${spring.datasource.password}")
  private String password;

  @Value("${spring.datasource.driver-class-name}")
  private String driverClassName;

  @Bean
  public DataSource dataSource() throws SQLException {
    DataSource dataSource = DataSourceBuilder.create()
        .url(url)
        .username(username)
        .password(password)
        .driverClassName(driverClassName)
        .build();

    try (Connection conn = dataSource.getConnection()) {
      if(conn != null) {
        LOGGER.info("Successfully connected to the database");
      } else {
        LOGGER.info("Failed to establish a connection to the database");
      }
    }

    return dataSource;
  }
}
