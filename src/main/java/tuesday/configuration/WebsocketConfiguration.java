package tuesday.configuration;

@org.springframework.context.annotation.Configuration
@org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
public class WebsocketConfiguration 
    implements org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer {

  public static final String
    BROKER = "/conversation",
    APP_DEST_PREFIX = "/queue",
    WS_ENDPOINT = "/chat",
    SERVER_CHANNEL_SUFFIX = "/channel",
    CLIENT_SECURED_PREFIX = "/secured",
    CLIENT_LISTENER = "/attendance";

  @Override
  public void configureMessageBroker(
      org.springframework.messaging.simp.config.MessageBrokerRegistry config) {
    config.enableSimpleBroker(BROKER);
    config.setApplicationDestinationPrefixes(APP_DEST_PREFIX);
    config.setUserDestinationPrefix(CLIENT_SECURED_PREFIX);
  }

  @Override
  public void registerStompEndpoints(
      org.springframework.web.socket.config.annotation.StompEndpointRegistry registry) {
    registry.addEndpoint(WS_ENDPOINT).withSockJS();
  }

}
