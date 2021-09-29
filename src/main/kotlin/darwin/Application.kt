package darwin

import org.springframework.beans.factory.annotation.Value

@springfox.documentation.swagger2.annotations.EnableSwagger2
@org.springframework.boot.autoconfigure.SpringBootApplication
open class Application : org.springframework.web.servlet.config.annotation.WebMvcConfigurer {
  override fun addResourceHandlers(
      registry: org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry) {
    registry.addResourceHandler("/**").addResourceLocations("classpath:/static/")
    registry.addResourceHandler("swagger-ui.html")
      .addResourceLocations("classpath:/META-INF/resources/") }
  companion object { @JvmStatic fun main(args: Array<String>) {
    org.springframework.boot.SpringApplication.run(Application::class.java, *args) }
    val LOGGER: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(Application::class.java) }
  @Value("\${spring.application.name}") var appName: String? = null
  @Value("\${server.port}") var serverPort: String? = null
  @Value("\${server.servlet.context-path}") var webContextPath: String? = null
  @javax.annotation.PostConstruct
  fun startingMessage() = LOGGER.info("Starting '${this.appName}' on port ${
    this.serverPort} (Web Context: ${this.webContextPath}), please wait...")
}
class ResouceReader {
  companion object {
    fun getResourceAsStream(resource: String) =
      Thread.currentThread().getContextClassLoader()
        .getResourceAsStream(resource) }
}

@org.springframework.stereotype.Component
open class CustomSwaggerDocket(
  @Value("\${info.app.name}") val name: String,
  @Value("\${info.app.description}") val description: String,
  @Value("\${info.app.version}") val version: String
): springfox.documentation.spring.web.plugins.Docket(
    springfox.documentation.spi.DocumentationType.SWAGGER_2) {
  init {
    this.select().apis(
      springfox.documentation.builders.RequestHandlerSelectors.basePackage(
        Application::class.java.getPackage().getName())
    ).paths(springfox.documentation.builders.PathSelectors.any())
    .build().apiInfo(springfox.documentation.builders.ApiInfoBuilder()
      .title(this.name)
      .version(this.version)
      .description(this.description.trim())
      .build())
  }
}
