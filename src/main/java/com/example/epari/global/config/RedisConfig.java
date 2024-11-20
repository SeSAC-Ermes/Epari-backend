package com.example.epari.global.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정 클래스
 * Redis 캐시 및 템플릿 설정을 관리
 */
@Configuration
@EnableCaching
public class RedisConfig {

	/**
	 * Redis 템플릿 빈 설정
	 * Redis 데이터 접근을 위한 템플릿을 구성
	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		// 문자열 및 JSON 직렬화 도구 생성
		StringRedisSerializer stringSerializer = new StringRedisSerializer();
		GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

		// Redis key는 문자열로 직렬화 (가독성과 디버깅을 위해)
		template.setKeySerializer(stringSerializer);
		template.setHashKeySerializer(stringSerializer);

		// 값은 JSON 형식으로 직렬화 (객체 저장을 위해)
		template.setValueSerializer(jsonSerializer);
		template.setHashValueSerializer(jsonSerializer);

		// 기본 직렬화 도구 설정 (명시되지 않은 타입을 위해)
		template.setDefaultSerializer(jsonSerializer);

		// 트랜잭션 지원 활성화
		template.setEnableTransactionSupport(true);
		template.afterPropertiesSet();

		return template;
	}

	/**
	 * Redis 캐시 매니저 빈 설정
	 * 캐시 동작 방식과 설정을 정의
	 */
	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
				.serializeKeysWith(
						RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
				)
				.serializeValuesWith(
						RedisSerializationContext.SerializationPair.fromSerializer(
								new GenericJackson2JsonRedisSerializer())
				)
				.entryTtl(Duration.ofMinutes(30))  // 캐시 데이터 유효 시간: 30분
				.disableCachingNullValues()        // null 값 캐시 방지
				.prefixCacheNameWith("epari::");   // 키 충돌 방지를 위한 접두사

		return RedisCacheManager.builder(connectionFactory)
				.cacheDefaults(config)
				.transactionAware()  // 트랜잭션 인지 활성화
				.build();
	}

	/**
	 * 캐시별 개별 설정을 위한 커스터마이저
	 * 각 캐시마다 다른 TTL 등을 설정할 수 있습니다.
	 */
	@Bean
	public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
		return (builder) -> builder.build();
	}

}
