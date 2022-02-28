package tuesday.controller;

import static tuesday.configuration.WebsocketConfiguration.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;
import tuesday.model.OutputMessage;

@io.swagger.annotations.Api(value = "Websocket API")
@org.springframework.web.bind.annotation.CrossOrigin
@org.springframework.web.bind.annotation.RestController
@org.springframework.web.bind.annotation.RequestMapping(value = { "/api/v1/websocket", },
  produces = { org.springframework.http.MediaType.APPLICATION_JSON_VALUE, })
public class WebsocketController {

  @org.springframework.beans.factory.annotation.Autowired
  private tuesday.service.WebsocketService service;

  @lombok.Getter public static class ConnectionInfo {
    private String endpoint = WS_ENDPOINT;
    private String[] listeners = { CLIENT_SECURED_PREFIX + BROKER +
      CLIENT_LISTENER, BROKER + CLIENT_LISTENER };
    private String serverMessageChannel = APP_DEST_PREFIX +
      CLIENT_SECURED_PREFIX + SERVER_CHANNEL_SUFFIX;
    public static final ConnectionInfo DEFAULT = new ConnectionInfo();
    private ConnectionInfo() { }
  }

  @GetMapping(value = { "/connection-info", })
  @ApiOperation(value = "Connection info to start a client-side conversation channel.")
  public ConnectionInfo connectionInfo() { return ConnectionInfo.DEFAULT; }

  @ApiIgnore @org.springframework.context.event.EventListener({
    org.springframework.web.socket.messaging.SessionConnectEvent.class,
    org.springframework.web.socket.messaging.SessionDisconnectEvent.class,
    org.springframework.web.socket.messaging.SessionSubscribeEvent.class,
    org.springframework.web.socket.messaging.SessionUnsubscribeEvent.class,
  })
  public void newEvent(org.springframework.web.socket.messaging.AbstractSubProtocolEvent event) {
    this.service.sessionListener(event); }

  @ApiIgnore @org.springframework.messaging.handler.annotation.MessageMapping(
    CLIENT_SECURED_PREFIX + SERVER_CHANNEL_SUFFIX) public void newMessage(
      @org.springframework.messaging.handler.annotation.Header("simpSessionId") String session,
      @org.springframework.messaging.handler.annotation.Payload tuesday.model.InputMessage message) {
    this.service.receive(message.setSession(session));
  }

  @GetMapping(value = { "/all-sessions", })
  @ApiOperation(value = "Retrieve all active sessions.")
  public java.util.Set<String> allSessions() {
    return this.service.allConnectedSessions();
  }

  @PostMapping(value = { "/{session}/message", })
  @ApiOperation(value = "Send message to an active session.")
  public ResponseEntity<OutputMessage> send(@RequestBody Object payload,
      @org.springframework.web.bind.annotation.PathVariable String session) {
    final OutputMessage message = this.service.send(new OutputMessage()
      .setPayload(payload).setSession(session));
    return ResponseEntity.status(message.getSession() != null && !message.getSession().isEmpty() ?
      HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR).body(message);
  }

  @PostMapping(value = { "/broadcast", })
  @ApiOperation(value = "Send message to all active sessions.")
  public ResponseEntity<Object> broadcast(@RequestBody Object payload) {
    final Boolean success = this.service.broadcast(payload);
    return ResponseEntity.status(success ? HttpStatus.OK :
      HttpStatus.INTERNAL_SERVER_ERROR).body(payload);
  }

}
