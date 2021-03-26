package darwin

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiOperation
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class JWTToken(val header: String,
    val payload: String, val signature: String) {
  companion object {
    val PREFIX = "Bearer "
    val SEPARATOR = "."
  }
}
fun JWTToken(token: String?) = token?.let {
  if(it.startsWith(JWTToken.PREFIX)) {
    val splittedJwtToken = it.substring(JWTToken.PREFIX.length,
      it.length).split(JWTToken.SEPARATOR)
    JWTToken(
      String(java.util.Base64.getDecoder().decode(splittedJwtToken[0])),
      String(java.util.Base64.getDecoder().decode(splittedJwtToken[1])),
      splittedJwtToken[2])
  } else null }

data class MessageInput(val text: String)
data class MessageRequest(val input: MessageInput,
  @JsonProperty("alternate_intents") val alternateIntents: Boolean,
  val context: Map<String, Any>)

enum class ResponseType {
  TEXT,
  ;@com.fasterxml.jackson.annotation.JsonValue
  override fun toString() = this.name.toLowerCase()
}
data class GenericResponseDTO(
  @JsonProperty("response_type") val responseType: ResponseType,
  @JsonProperty("text") val dialogNodeIdentifier: String)
data class OutputDTO(val nodeName: String,
  val generic: Array<GenericResponseDTO>,
  @JsonProperty("text") val dialogNodeIdentifiers: Array<String>,
  @JsonProperty("nodes_visited") val visitedDialogNodes: Array<String> = arrayOf(),
  val endFlow: Boolean = true, val humanAgent: Boolean = false,
  val notHumanAgent: Boolean = false, val endConversation: Boolean = false,
  val beginConversation: Boolean = false,
  @JsonProperty("log_messages") val logMessages: Array<Any> = arrayOf()
)

data class IntentDTO(val intent: String, val confidence: Double)
data class MessageResponse(val intents: Array<IntentDTO>,
  val entities: Array<Any>, val input: MessageInput,
  val output: OutputDTO, val context: Map<String, Any>,
  @JsonProperty("alternate_intents") val alternateIntents: Boolean,
  @JsonProperty("user_id") val userId: String = java.util.UUID.randomUUID().toString()
)

@RestController @RequestMapping(
  path = arrayOf("/assistant/api"),
  produces = arrayOf(MediaType.APPLICATION_JSON_VALUE),
  consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
open class AssistantController() {

  @PostMapping("/v1/workspaces/{instance}/message")
  @ApiOperation(value = "Simple message request.")
  fun message(
    @PathVariable instance: String,
    @RequestHeader(name = "Token", required = false)
      jwtToken: String? = null,
    @RequestBody body: MessageRequest
  ): MessageResponse {
    System.err.println("""Instance: ${instance}
Body: ${body}
Token: ${JWTToken(jwtToken)}
""")
    return MessageResponse(
      intents = arrayOf(),
      entities = arrayOf(),
      input = body.input,
      context = mapOf(),
      alternateIntents = body.alternateIntents,
      output = OutputDTO("xuruvis",
        arrayOf(GenericResponseDTO(ResponseType.TEXT, "xxxyyy")), arrayOf())
    )
  }

  /*@RequestMapping
  fun untracked() {

  }*/

}
