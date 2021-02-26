/*package darwin

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@ApiModel open class ConnectionInfo(
  @ApiModelProperty val endpoint: String = WebsocketConfiguration.WS_ENDPOINT,
  @ApiModelProperty val listener: String = "/secured" +
    WebsocketConfiguration.BROKER + WebsocketConfiguration.CLIENT_LISTENER,
  @ApiModelProperty val messageBroker: String =
    WebsocketConfiguration.APP_DEST_PREFIX + "/secured/channel"
) { companion object { val DEFAULT = ConnectionInfo() } }
@ApiModel data class InputMessage(val message: String? = null)
@ApiModel data class OutputMessage(val session: String = "", val payload: Any = "",
  val timestamp: Long = java.util.Calendar.getInstance().getTimeInMillis())
@ApiModel data class ErrorMessage(val message: String, val details: String?,
  @JsonIgnore val httpStatusCode: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR)

open class NoWebsocketSessionException(val session: String):
  Exception("Session '${session}' not found... Is it still active?")

@Configuration
@EnableWebSocketMessageBroker
open class WebsocketConfiguration: WebSocketMessageBrokerConfigurer {

  companion object {
    val BROKER = "/conversation"
    val APP_DEST_PREFIX = "/queue"
    val WS_ENDPOINT = "/chat"
    val CLIENT_LISTENER = "/attendance"
  }

  override fun configureMessageBroker(config: MessageBrokerRegistry) {
    config.enableSimpleBroker(BROKER)
    config.setApplicationDestinationPrefixes(APP_DEST_PREFIX)
    config.setUserDestinationPrefix("/secured")
  }

  override fun registerStompEndpoints(registry: StompEndpointRegistry) {
    registry.addEndpoint(WS_ENDPOINT).withSockJS()
  }

}

@Service
open class WebsocketService(@Autowired val simpTemplate: SimpMessagingTemplate) {

  companion object {
    private val logger: Logger =
      LoggerFactory.getLogger(WebsocketService::class.java)
  }

  private val connectedSessions = mutableSetOf<String>()
  fun getAllConnectedSessions() = this.connectedSessions.toTypedArray().clone()
  fun newSessionListener(event: SessionConnectEvent) =
    StompHeaderAccessor.wrap(event.getMessage())
      .getSessionId()?.let { session ->
        logger.debug("Connected: ${session}")
        this.connectedSessions += session
      } ?: run { throw IllegalArgumentException("No session found!") }
  fun deleteSessionListener(event: SessionDisconnectEvent) =
    StompHeaderAccessor.wrap(event.getMessage())
      .getSessionId()?.let { session ->
        logger.debug("Disconnected: ${session}")
        this.connectedSessions -= session
      } ?: run { throw IllegalArgumentException("No session found!") }

  fun receiveMessage(session: String, inputMessage: InputMessage) =
    logger.info("Received message from '${session}': ${inputMessage}")

  fun sendMessage(outputMessage: OutputMessage) =
    if(this.getAllConnectedSessions().contains(outputMessage.session)) {
      logger.info("Sending message to '${outputMessage.session}': ${outputMessage.payload}")
      this.simpTemplate.convertAndSend(
        "/conversation/attendance-user${outputMessage.session}",
        outputMessage.payload)
      outputMessage
    } else throw NoWebsocketSessionException(outputMessage.session)

}

@Controller
open class WebsocketBroker(@Autowired val websocketService: WebsocketService) {

  @EventListener(SessionConnectEvent::class)
  fun onNewConnection(event: SessionConnectEvent) =
    this.websocketService.newSessionListener(event)

  @EventListener(SessionDisconnectEvent::class)
  fun onDisconnect(event: SessionDisconnectEvent) =
    this.websocketService.deleteSessionListener(event)

  @MessageMapping("/secured/channel")
  fun messageReceived(@Header("simpSessionId") session: String,
      @Payload inputMessage: InputMessage) =
    this.websocketService.receiveMessage(session, inputMessage)

}

@RestController @CrossOrigin @Api(value = "Websocket API")
@RequestMapping(value = arrayOf("/api/v1/websocket"),
  produces = arrayOf(org.springframework.http.MediaType.APPLICATION_JSON_VALUE))
open class WebsocketController(@Autowired val websocketService: WebsocketService) {

  @ResponseBody
  @ApiOperation(value = "Connection info to start a client-side conversation channel.")
  @GetMapping(value = arrayOf("/connection-info"))
  fun connectionInfo() = ConnectionInfo.DEFAULT

  @ResponseBody
  @ApiOperation(value = "Retrieve all active sessions.")
  @GetMapping(value = arrayOf("/all-sessions"))
  fun allSessions() = this.websocketService.getAllConnectedSessions()

  @ResponseBody
  @ApiOperation(value = "Send message to an active session through Rest request.")
  @PostMapping(value = arrayOf("/message-{session}"))
  fun sendMessage(@RequestBody payload: Any,
      @PathVariable session: String) =
    try { this.websocketService.sendMessage(OutputMessage(session, payload)) }
    catch(e: NoWebsocketSessionException) {
      val response = ErrorMessage("Error sending message", e.message, HttpStatus.NOT_FOUND)
      ResponseEntity(response, response.httpStatusCode)
    }

}
*/
