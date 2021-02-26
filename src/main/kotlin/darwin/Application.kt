package darwin

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

import com.mongodb.MongoClient
import org.springframework.data.mongodb.config.AbstractMongoConfiguration

@springfox.documentation.swagger2.annotations.EnableSwagger2
@org.springframework.boot.autoconfigure.SpringBootApplication
open class Application : org.springframework.web.servlet.config.annotation.WebMvcConfigurer {
  override fun addResourceHandlers(
      registry: org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry) {
    registry.addResourceHandler("/**").addResourceLocations("classpath:/static/")
    registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/")
  }
  companion object { @JvmStatic fun main(args: Array<String>) {
    org.springframework.boot.SpringApplication.run(Application::class.java, *args)
  }}
}

class ResouceReader {
  companion object {
    fun getResourceAsStream(resource: String) =
      Thread.currentThread().getContextClassLoader()
        .getResourceAsStream(resource)
  }
}
