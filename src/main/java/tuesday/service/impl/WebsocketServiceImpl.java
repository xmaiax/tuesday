package tuesday.service.impl;

import static tuesday.configuration.WebsocketConfiguration.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;

import tuesday.model.OutputMessage;

@lombok.extern.slf4j.Slf4j
@org.springframework.stereotype.Service
public class WebsocketServiceImpl implements tuesday.service.WebsocketService {

  @Autowired
  private org.springframework.messaging.simp.SimpMessagingTemplate simpTemplate;

  @Autowired
  private org.springframework.data.redis.core.StringRedisTemplate strRedisTemplate;

  @Autowired
  private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

  @Override
  public java.util.Set<String> allConnectedSessions() {
    return this.strRedisTemplate.keys("*");
  }

  private void registerConnectedClient(String session) {
    try {
      this.strRedisTemplate.opsForValue().set(session, this.objectMapper.writeValueAsString(
        java.util.Collections.singletonMap("connectedSince", tuesday.utils.DateUtils.getInstance()
          .formatDate(java.util.Calendar.getInstance().getTime()))));
    }
    catch(com.fasterxml.jackson.core.JsonProcessingException jpe) {
      log.error("Unable to register session '{}': ", session, jpe.getMessage());
    }
  }

  private void registerdDisconnectedClient(String session) {
    this.strRedisTemplate.delete(session);
  }

  @SuppressWarnings("unchecked")
  private void registerUnSubscription(AbstractSubProtocolEvent event, String session, Boolean isSub) {
    final String subUnsunDesc = isSub ? "subscribed" : "unsubscribed";
    try {
      final List<String> channels = (List<String>)((java.util.Map<String, Object>) event.getMessage()
        .getHeaders().get("nativeHeaders")).get("destination");
      log.info("Client '{}' {} the following channels: {}", session, subUnsunDesc, channels);
    }
    catch(Exception ex) {
      if(ex instanceof ClassCastException)
        log.error("Cannot find '{}' {} channels: {}", session, subUnsunDesc, ex.getMessage());
      else throw ex;
    }
  }

  @Override
  public void sessionListener(AbstractSubProtocolEvent event) {
    final String session = org.springframework.messaging.simp.stomp.StompHeaderAccessor
      .wrap(event.getMessage()).getSessionId();
    if(session == null || session.isBlank()) return;
    final StompCommand eventType = (StompCommand) event.getMessage().getHeaders().get("stompCommand");
    log.debug("New websocket event '{}' from session '{}': {}",
      eventType, session, event.getMessage().getHeaders());
    switch(eventType) {
      case CONNECT: this.registerConnectedClient(session); break;
      case DISCONNECT: this.registerdDisconnectedClient(session); break;
      case SUBSCRIBE: this.registerUnSubscription(event, session, Boolean.TRUE); break;
      case UNSUBSCRIBE: this.registerUnSubscription(event, session, Boolean.FALSE); break;
      default: log.debug("Unhandled event: {}", eventType);
    }
  }

  //TODO: Processamento de mensagens recebidas deve ser realizado por fila.
  @Override
  public void receive(tuesday.model.InputMessage message) {
    log.info("Received message from '{}': {}", message.getSession(), message.getMessage());
  }

  @Override
  public OutputMessage send(OutputMessage message) {
    if(this.strRedisTemplate.hasKey(message.getSession())) {
      log.info("Sending message to '{}': {}", message.getSession(), message.getPayload());
      this.simpTemplate.convertAndSend(String.format("%s%s-user%s", BROKER,
        CLIENT_LISTENER, message.getSession()), message.getPayload());
      return message;
    }
    else return message.setPayload(java.util.Collections.singletonMap("error",
      String.format("Session '%s' not found.", message.getSession()))).setSession(null);
  }

  @Override
  public Boolean broadcast(Object message) {
    log.info("Broadcasting message: {}", message);
    try { this.simpTemplate.convertAndSend(BROKER + CLIENT_LISTENER, message); }
    catch(org.springframework.messaging.MessagingException mex) {
      log.error("Unable to broadcast message '{}': {}", message, mex.getMessage());
      return Boolean.FALSE;
    }
    return Boolean.TRUE;
  }

}
