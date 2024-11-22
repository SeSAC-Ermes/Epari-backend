package com.example.epari.global.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Redis 설정 클래스
 * Redis 캐시 및 템플릿 설정을 관리
 */
@Configuration
@EnableCaching
public class RedisConfig {

	@Value("${spring.data.redis.ssl.enabled:false}")
	private boolean ssl;

	/**
	 * Redis 연결을 위한 ConnectionFactory 빈을 생성합니다.
	 * - 개발 환경: SSL 미사용 (spring.data.redis.ssl=false)
	 * - 운영 환경: SSL 사용 (spring.data.redis.ssl=true)
	 * @param host Redis 서버 호스트 주소
	 * @param port Redis 서버 포트 번호
	 * @return Redis 연결을 위한 LettuceConnectionFactory 인스턴스
	 */
	@Bean
	public RedisConnectionFactory redisConnectionFactory(
			@Value("${spring.data.redis.host}") String host,
			@Value("${spring.data.redis.port}") int port) {

		RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
		redisConfig.setHostName(host);
		redisConfig.setPort(port);

		LettuceClientConfiguration clientConfig = ssl ?
				LettuceClientConfiguration.builder().useSsl().build() :
				LettuceClientConfiguration.builder().build();

		return new LettuceConnectionFactory(redisConfig, clientConfig);
	}

	/**
	 * Java 8 이상 날짜/시간 타입을 위한 Jackson ObjectMapper 설정
	 */
	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());  // JSR-310 모듈 등록
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // ISO-8601 형식으로 직렬화
		return mapper;
	}

	/**
	 * 커스텀 ObjectMapper를 사용하는 JSON 직렬화 도구
	 */
	@Bean
	public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer(ObjectMapper objectMapper) {
		return new GenericJackson2JsonRedisSerializer(objectMapper);
	}

	/**
	 * Redis 템플릿 빈 설정
	 * Redis 데이터 접근을 위한 템플릿을 구성
	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate(
			RedisConnectionFactory connectionFactory,
			GenericJackson2JsonRedisSerializer jsonSerializer) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		// 문자열 직렬화 도구
		StringRedisSerializer stringSerializer = new StringRedisSerializer();

		// Redis key는 문자열로 직렬화 (가독성과 디버깅을 위해)
		template.setKeySerializer(stringSerializer);
		template.setHashKeySerializer(stringSerializer);

		// 값은 커스텀 JSON 직렬화 도구 사용 (객체 저장을 위해)
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
	 * 기본 Redis 캐시 설정을 정의하는 빈
	 */
	@Bean
	public RedisCacheConfiguration defaultCacheConfig(GenericJackson2JsonRedisSerializer jsonSerializer) {
		return RedisCacheConfiguration.defaultCacheConfig()
				.serializeKeysWith(
						RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
				)
				.serializeValuesWith(
						RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
				)
				.entryTtl(Duration.ofMinutes(30))  // 기본 캐시 유효 시간: 30분
				.disableCachingNullValues()        // null 값 캐시 방지
				.prefixCacheNameWith("epari::");   // 키 충돌 방지를 위한 접두사
	}

	/**
	 * 캐시별 설정을 생성하는 메서드
	 */
	private Map<String, RedisCacheConfiguration> createCacheConfigurations(
			RedisCacheConfiguration defaultCacheConfig) {
		Map<String, RedisCacheConfiguration> configMap = new HashMap<>();

		// 캐시별 설정 추가
		configMap.put("curriculums", defaultCacheConfig.entryTtl(CachingTTL.CURRICULUM));
		configMap.put("courses", defaultCacheConfig.entryTtl(CachingTTL.COURSE_LIST));

		return configMap;
	}

	/**
	 * Redis 캐시 매니저 빈 설정
	 */
	@Bean
	public RedisCacheManager cacheManager(
			RedisConnectionFactory connectionFactory,
			RedisCacheConfiguration defaultCacheConfig) {

		return RedisCacheManager.builder(connectionFactory)
				.cacheDefaults(defaultCacheConfig)
				.withInitialCacheConfigurations(createCacheConfigurations(defaultCacheConfig))
				.transactionAware()
				.build();
	}

	/**
	 * 캐시별 TTL 설정을 관리하는 상수
	 */
	private static class CachingTTL {

		public static final Duration CURRICULUM = Duration.ofHours(12);

		public static final Duration COURSE_LIST = Duration.ofHours(3); // 강의 목록 캐시

	}

}
