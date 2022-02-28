package tuesday.service;

import tuesday.model.OutputMessage;

public interface WebsocketService {
  java.util.Set<String> allConnectedSessions();
  void sessionListener(org.springframework.web.socket.messaging.AbstractSubProtocolEvent event);
  void receive(tuesday.model.InputMessage message);
  OutputMessage send(OutputMessage message);
  Boolean broadcast(Object message);
}
