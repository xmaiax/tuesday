package tuesday.configuration;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import redis.embedded.RedisServer;

@lombok.extern.slf4j.Slf4j
@org.springframework.context.annotation.Configuration
public class RedisConfiguration {

  public static final String ENV_VAR_REDIS_URL = "REDIS_URL";
  public static final String REGEX_BINDED_REDIS_PORT = "(# Creating Server TCP listening socket )(\\w.)*(: bind:)";

  @Value("${redis.host}") private String host;

  @Value("${redis.port}") private Integer port;

  private RedisServer redisServer;

  @Bean @org.springframework.context.annotation.Profile({ "local", "heroku", })
  public RedisServer redisServer() {
    final String redisUrl = System.getenv(ENV_VAR_REDIS_URL);
    if(redisUrl != null && !redisUrl.isBlank()) return null;
    else this.redisServer = RedisServer.builder().port(this.port).build();
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
    final String redisUrl = System.getenv(ENV_VAR_REDIS_URL);
    if(redisUrl != null && !redisUrl.isBlank()) {
      try {
        final javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("SSL");
        sslContext.init(null, new javax.net.ssl.TrustManager[] { new javax.net.ssl.X509TrustManager() {
          @Override public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
            String authType) throws java.security.cert.CertificateException { }
          @Override public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
            String authType) throws java.security.cert.CertificateException { }
          @Override public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
        }}, new java.security.SecureRandom());
        final RedisStandaloneConfiguration rsc = new RedisStandaloneConfiguration(
          redisUrl.split("@")[BigInteger.ONE.intValue()].split(":")[BigInteger.ZERO.intValue()],
          Integer.parseInt(redisUrl.split("@")[BigInteger.ONE.intValue()].split(":")[BigInteger.ONE.intValue()]));
        rsc.setPassword(redisUrl.split("@")[BigInteger.ZERO.intValue()].split("//")[
          BigInteger.ONE.intValue()].substring(BigInteger.ONE.intValue()));
        return new JedisConnectionFactory(rsc, JedisClientConfiguration.builder().useSsl()
          .hostnameVerifier((hostname, session) -> true)
          .sslSocketFactory(sslContext.getSocketFactory())
          .sslParameters(sslContext.getDefaultSSLParameters()).build());
      }
      catch (java.security.NoSuchAlgorithmException | java.security.KeyManagementException ex) {
        log.error("Unable to start Redis connection with given URL: {}", redisUrl, ex);
      }
    }
    return new JedisConnectionFactory(new RedisStandaloneConfiguration(this.host, this.port));
  }

  @Bean
  public org.springframework.data.redis.core.StringRedisTemplate redisTemplate(
      JedisConnectionFactory jedisConnectionFactory) {
    return new org.springframework.data.redis.core.StringRedisTemplate(jedisConnectionFactory);
  }

}
