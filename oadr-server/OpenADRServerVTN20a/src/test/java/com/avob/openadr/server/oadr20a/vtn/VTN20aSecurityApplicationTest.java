package com.avob.openadr.server.oadr20a.vtn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.avob.openadr.server.common.vtn.VtnDatabaseContainer;

import javax.sql.DataSource;

@EnableJms
@Configuration
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class })
@ComponentScan(basePackages = { "com.avob.openadr.server.oadr20a.vtn" })
@EnableJpaRepositories({ "com.avob.openadr.server.oadr20a.vtn" })
@EntityScan(basePackages = { "com.avob.openadr.server.oadr20a.vtn" })
@ActiveProfiles({ "test" })
public class VTN20aSecurityApplicationTest {

	/**
	 * 도커로 띄운 PostgreSQL 을 가리킨다. VTNCommon 과 같은 컨테이너를 재사용한다.
	 *
	 * 예전에는 H2 임베디드였는데, 운영이 PostgreSQL 이라 Hibernate 가 만드는 DDL 이 달라진다.
	 * 테스트만 통과하고 실제 배포에서 깨지는 걸 막으려고 같은 DB 를 쓴다.
	 *
	 * 커넥션 풀을 쓰지 않는다. 테스트 클래스마다 컨텍스트가 새로 뜨는데
	 * 컨텍스트마다 풀을 잡으면 컨테이너의 max_connections 를 금방 넘긴다.
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
		SpringApplication.run(VTN20aSecurityApplicationTest.class, args);
	}

}
