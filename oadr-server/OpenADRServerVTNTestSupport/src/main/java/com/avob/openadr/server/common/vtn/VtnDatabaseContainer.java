package com.avob.openadr.server.common.vtn;

import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * 테스트용 PostgreSQL 컨테이너.
 *
 * 운영이 PostgreSQL 이라 테스트도 같은 DB 를 쓴다. H2 를 쓰면 Hibernate 가 만드는 DDL 이
 * 운영과 달라져서, 테스트가 통과해도 컨테이너에서 깨지는 경우가 생긴다.
 *
 * 스프링 컨텍스트는 테스트 클래스마다 새로 뜨지만 컨테이너는 JVM 당 하나만 띄워 재사용한다.
 * 컨테이너 기동이 수 초 걸리기 때문에 컨텍스트마다 띄우면 빌드가 느려진다.
 * 정리는 testcontainers 의 ryuk 이 JVM 종료 시점에 하므로 stop 을 직접 걸지 않는다.
 */
public final class VtnDatabaseContainer {

	// 도커 스택(docker/postgres)과 같은 메이저 버전을 쓴다
	private static final PostgreSQLContainer INSTANCE = new PostgreSQLContainer("postgres:18-alpine");

	static {
		INSTANCE.start();
	}

	private VtnDatabaseContainer() {
	}

	public static PostgreSQLContainer getInstance() {
		return INSTANCE;
	}
}
