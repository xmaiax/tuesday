package tuesday.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import redis.embedded.RedisServer;

@lombok.extern.slf4j.Slf4j
@org.springframework.context.annotation.Configuration
public class RedisConfiguration {

  public static final String REGEX_BINDED_REDIS_PORT = "(# Creating Server TCP listening socket )(\\w.)*(: bind:)";

  @Value("${redis.host}") private String host;

  @Value("${redis.port}") private Integer port;

  private RedisServer redisServer;

  @Bean
  public RedisServer redisServer() {
    this.redisServer = RedisServer.builder().port(this.port).build();
    boolean noException = Boolean.TRUE;
    try { this.redisServer.start(); }
    catch(RuntimeException rex) {
      noException = Boolean.FALSE;
      if(!java.util.regex.Pattern.compile(REGEX_BINDED_REDIS_PORT,
          java.util.regex.Pattern.CASE_INSENSITIVE).matcher(rex.getMessage()).find()) {
        log.error("Error while starting embedded Redis. {}", rex.getMessage(), rex);
        System.exit(-1);
      }
    }
    if(noException) {
      log.info("Redis not found in port '{}', running embedded version.", this.port);
    }
    return this.redisServer;
  }

  @javax.annotation.PreDestroy
  public void stopRedisServer() {
    if(this.redisServer != null && this.redisServer.isActive()) {
      log.info("Stopping embedded Redis server...");
      this.redisServer.stop();
    }
  }

  @Bean
  public JedisConnectionFactory jedisConnectionFactory() {
    return new JedisConnectionFactory(new org.springframework.data.redis.connection
      .RedisStandaloneConfiguration(this.host, this.port));
  }

  @Bean
  public org.springframework.data.redis.core.StringRedisTemplate redisTemplate(
      JedisConnectionFactory jedisConnectionFactory) {
    return new org.springframework.data.redis.core.StringRedisTemplate(jedisConnectionFactory);
  }

}
