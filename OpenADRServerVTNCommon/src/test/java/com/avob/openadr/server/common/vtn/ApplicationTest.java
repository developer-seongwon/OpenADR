package com.avob.openadr.server.common.vtn;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;

@EnableJms
@Configuration
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class })
@ComponentScan({ "com.avob.openadr.server.common.vtn" })
@EnableJpaRepositories({ "com.avob.openadr.server.common.vtn" })
@EntityScan({ "com.avob.openadr.server.common.vtn" })
@ActiveProfiles("test")
@PropertySource("classpath:application-test.properties")
public class ApplicationTest {

	/**
	 * 도커로 띄운 PostgreSQL 을 가리킨다.
	 *
	 * 커넥션 풀을 쓰지 않는다. 테스트 클래스마다 컨텍스트가 새로 뜨는데 컨텍스트마다 풀을 잡으면
	 * 컨테이너의 max_connections 를 금방 넘긴다.
	 */
	@Bean
	public DataSource dataSource() {
		PostgreSQLContainer db = VtnDatabaseContainer.getInstance();
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(db.getDriverClassName());
		dataSource.setUrl(db.getJdbcUrl());
		dataSource.setUsername(db.getUsername());
		dataSource.setPassword(db.getPassword());
		return dataSource;
	}

	public static void main(String[] args) {
		SpringApplication.run(ApplicationTest.class, args);
	}

}
