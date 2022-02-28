package tuesday.configuration;

import org.springframework.beans.factory.annotation.Value;

@org.springframework.stereotype.Component
@springfox.documentation.swagger2.annotations.EnableSwagger2
public class SwaggerConfiguration extends springfox.documentation.spring.web.plugins.Docket {

  public static final String NO_DESC = "No description.";

  public SwaggerConfiguration(
      @Value("${info.app.name}") String name,
      @Value("${info.app.description:" + NO_DESC + "}") String description,
      @Value("${info.app.version}") String version) {
    super(springfox.documentation.spi.DocumentationType.SWAGGER_2);
    this.select().apis(springfox.documentation.builders.RequestHandlerSelectors.basePackage(
        tuesday.Application.class.getPackage().getName()))
      .paths(springfox.documentation.builders.PathSelectors.any())
      .build().apiInfo(new springfox.documentation.builders.ApiInfoBuilder().title(name).version(version)
        .description(description == null || !description.isBlank() ? description.trim() : NO_DESC).build());
  }

}
