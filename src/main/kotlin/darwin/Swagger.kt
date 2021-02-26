package darwin

import org.springframework.beans.factory.annotation.Value

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
        CustomSwaggerDocket::class.java.getPackage().getName())
    ).paths(springfox.documentation.builders.PathSelectors.regex("/.*"))
    .build().apiInfo(springfox.documentation.builders.ApiInfoBuilder()
      .title(this.name)
      .version(this.version)
      .description(this.description.trim())
      .build())
  }
}
