package darwin

import org.springframework.http.HttpStatus
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.bind.annotation.ResponseBody
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PathVariable
import io.swagger.annotations.ApiModel
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RestController
import io.swagger.annotations.Api
import org.springframework.web.bind.annotation.RequestMapping
import io.swagger.annotations.ApiModelProperty
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.http.ResponseEntity
import org.springframework.context.event.EventListener
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.stereotype.Controller
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent
import org.springframework.messaging.simp.stomp.StompCommand

@ApiModel open class ConnectionInfo(
  @ApiModelProperty val endpoint: String = WebsocketConfiguration.WS_ENDPOINT,
  @ApiModelProperty val listeners: Array<String> = arrayOf(WebsocketConfiguration.CLIENT_SECURED_PREFIX +
    WebsocketConfiguration.BROKER + WebsocketConfiguration.CLIENT_LISTENER,
    WebsocketConfiguration.BROKER + WebsocketConfiguration.CLIENT_LISTENER),
  @ApiModelProperty val serverMessageChannel: String = WebsocketConfiguration.APP_DEST_PREFIX +
    WebsocketConfiguration.CLIENT_SECURED_PREFIX + WebsocketConfiguration.SERVER_CHANNEL_SUFFIX
) { companion object { val DEFAULT = ConnectionInfo() } }
@ApiModel data class InputMessage(val message: String? = null)
@ApiModel data class OutputMessage(val session: String, val payload: Any)
@ApiModel data class ErrorMessage(val message: String, val details: String?,
  @JsonIgnore val httpStatusCode: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR)
open class NoWebsocketSessionException(val session: String):
  Exception("Session '${session}' not found... Is it still active?")

@Configuration
@EnableWebSocketMessageBroker
open class WebsocketConfiguration: org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer {
  companion object {
    const val BROKER = "/conversation"
    const val APP_DEST_PREFIX = "/queue"
    const val WS_ENDPOINT = "/chat"
    const val SERVER_CHANNEL_SUFFIX = "/channel"
    const val CLIENT_SECURED_PREFIX = "/secured"
    const val CLIENT_LISTENER = "/attendance"
  }
  override fun configureMessageBroker(config: org.springframework.messaging.simp.config.MessageBrokerRegistry) {
    config.enableSimpleBroker(BROKER)
    config.setApplicationDestinationPrefixes(APP_DEST_PREFIX)
    config.setUserDestinationPrefix(CLIENT_SECURED_PREFIX)
  }
  override fun registerStompEndpoints(registry: org.springframework.web.socket.config.annotation.StompEndpointRegistry) {
    registry.addEndpoint(WS_ENDPOINT).withSockJS() }
}

@Service
open class WebsocketService(@Autowired val simpTemplate: org.springframework.messaging.simp.SimpMessagingTemplate) {

  companion object {
    private val LOGGER = org.slf4j.LoggerFactory.getLogger(WebsocketService::class.java)
  }

  private val connectedSessions = mutableSetOf<String>()
  fun getAllConnectedSessions() = this.connectedSessions.toTypedArray().clone()

  @Suppress("UNCHECKED_CAST")
  private fun subscribeUnsubscribeEventHandler(event: AbstractSubProtocolEvent, session: String, isSubscribe: Boolean) {
    val subUnsunDesc = if(isSubscribe) "subscribed" else "unsubscribed"
    try {
      val channels = (event.getMessage().getHeaders().get("nativeHeaders"
        ) as Map<String, Any>).get("destination") as List<String>?
      LOGGER.info("Client '${session}' ${subUnsunDesc} the following channels: ${channels}")
    }
    catch(ex: Exception) {
      when(ex) {
        is ClassCastException, is TypeCastException -> {
          LOGGER.error("Cannot find '${session}' ${subUnsunDesc} channels: ${ex.message}")
        }
        else -> throw ex
      }
    }
  }

  fun genericSessionListener(event: AbstractSubProtocolEvent) =
    StompHeaderAccessor.wrap(event.getMessage()).getSessionId()?.let { session ->
      val eventType = event.getMessage().getHeaders().get("stompCommand") as StompCommand
      LOGGER.debug("New websocket event '${eventType}' from session '${session}': ${event.getMessage().getHeaders()}")
      when(eventType) {
        StompCommand.CONNECT -> {
          LOGGER.info("New connected client: ${session}")
          this.connectedSessions += session
        }
        StompCommand.DISCONNECT -> {
          LOGGER.info("Disconnected client: ${session}")
          this.connectedSessions -= session
        }
        StompCommand.SUBSCRIBE -> subscribeUnsubscribeEventHandler(event, session, true)
        StompCommand.UNSUBSCRIBE -> subscribeUnsubscribeEventHandler(event, session, false)
        else -> Unit
      }
    } ?: run { throw NoWebsocketSessionException("No session found!") }

  fun receiveMessage(session: String, inputMessage: InputMessage) =
    LOGGER.info("Received message from '${session}': ${inputMessage}")

  fun sendMessage(outputMessage: OutputMessage) =
    if(this.getAllConnectedSessions().contains(outputMessage.session)) {
      LOGGER.info("Sending message to '${outputMessage.session}': ${outputMessage.payload}")
      this.simpTemplate.convertAndSend("${WebsocketConfiguration.BROKER}${
        WebsocketConfiguration.CLIENT_LISTENER}-user${outputMessage.session}",
        outputMessage.payload)
      outputMessage
    } else {
      val ex = NoWebsocketSessionException(outputMessage.session)
      LOGGER.error("Unable to send message...", ex)
      throw ex
    }

  fun broadcastMessage(payload: Any): Any {
    LOGGER.info("Broadcasting message: ${payload.toString()}")
    this.simpTemplate.convertAndSend(WebsocketConfiguration.BROKER +
      WebsocketConfiguration.CLIENT_LISTENER, payload)
    return payload
  }

}

@Controller @CrossOrigin
open class WebsocketBroker(@Autowired val websocketService: WebsocketService) {

  @EventListener(value = arrayOf(
    org.springframework.web.socket.messaging.SessionConnectEvent::class,
    org.springframework.web.socket.messaging.SessionDisconnectEvent::class,
    org.springframework.web.socket.messaging.SessionSubscribeEvent::class,
    org.springframework.web.socket.messaging.SessionUnsubscribeEvent::class
  ))
  fun onNewEvent(event: AbstractSubProtocolEvent) =
    this.websocketService.genericSessionListener(event)

  @MessageMapping(WebsocketConfiguration.CLIENT_SECURED_PREFIX +
    WebsocketConfiguration.SERVER_CHANNEL_SUFFIX)
  fun messageReceived(@Header("simpSessionId") session: String,
      @Payload inputMessage: InputMessage) =
    this.websocketService.receiveMessage(session, inputMessage)

}

@RestController @CrossOrigin @Api(value = "Websocket API")
@RequestMapping(value = arrayOf("/websocket/api/v1"),
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
  @ApiOperation(value = "Send message to an active session.")
  @PostMapping(value = arrayOf("/{session}/message"))
  fun sendMessage(@RequestBody payload: Any,
      @PathVariable session: String) =
    try { this.websocketService.sendMessage(OutputMessage(session, payload)) }
    catch(e: NoWebsocketSessionException) {
      ResponseEntity(ErrorMessage("Error sending message", e.message),
        HttpStatus.NOT_FOUND) }

  @ResponseBody
  @ApiOperation(value = "Send message to all active sessions.")
  @PostMapping(value = arrayOf("/broadcast"))
  fun broadcastMessage(@RequestBody payload: Any) =
    this.websocketService.broadcastMessage(payload)

}
