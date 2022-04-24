package tuesday.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import tuesday.model.OutputMessage;
import tuesday.service.WebsocketService;

@Api(value = "Telegram API", hidden = true)
@RestController @RequestMapping(
  path = { "/api/v1/telegram", },
  produces = { MediaType.APPLICATION_JSON_VALUE, }
) public class TelegramController {

  @Autowired
  private WebsocketService websocketService;

  @PostMapping
  public ResponseEntity<Void> webhook(
      @RequestHeader(required = false) Map<String, String> headers,
      @RequestBody(required = false) Map<String, Object> body) {
    System.err.println("Mensagem recebida!");
    this.websocketService.allConnectedSessions().forEach((session) -> {
      this.websocketService.send(new OutputMessage().setSession(session)
        .setPayload(Map.of("headers", headers, "body", body)));
    });
    return ResponseEntity.ok().build();
  }

}
