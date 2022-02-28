package tuesday;

@lombok.extern.slf4j.Slf4j
@org.springframework.boot.autoconfigure.SpringBootApplication
public class Application implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {

  @org.springframework.beans.factory.annotation.Value("${server.port}")
  private Integer webServerPort;
  
  public static void main(String[] args) {
    org.springframework.boot.SpringApplication.run(Application.class, args);
  }

  @Override
  public void addResourceHandlers(
      org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    registry.addResourceHandler("swagger-ui.html")
      .addResourceLocations("classpath:/META-INF/resources/");
  }
  
  @javax.annotation.PostConstruct
  public void startedMessage() {
    log.info("Rest service starting in port: {}", this.webServerPort);
  }

  @org.springframework.context.annotation.Bean
  public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
    return new com.fasterxml.jackson.databind.ObjectMapper()
      .configure(com.fasterxml.jackson.databind.DeserializationFeature
        .FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE)
      .setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
      .setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY);
  }

}
