package darwin

import io.swagger.annotations.ApiOperation
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController @RequestMapping(
  path = arrayOf("/api/v1/assistant"),
  produces = arrayOf(MediaType.APPLICATION_JSON_VALUE),
  consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
open class AssistantController() {

  @PostMapping("/message")
  @ApiOperation(value = "Simple message request.")
  fun message(
    //@PathVariable instance: String,
    /*@RequestHeader(name = "Token", required = false)
      jwtToken: String? = null,*/
    @RequestBody body: Any
  ): Map<String, Any> {
    System.err.println(body)
    return mapOf("status" to "ok")
  }

}
