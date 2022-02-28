package tuesday.model;

@lombok.experimental.Accessors(chain = true)
@io.swagger.annotations.ApiModel @lombok.Data
public class InputMessage {
  @com.fasterxml.jackson.annotation.JsonIgnore private String session;
  private String message;
}
