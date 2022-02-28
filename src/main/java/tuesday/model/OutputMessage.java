package tuesday.model;

@lombok.experimental.Accessors(chain = true)
@io.swagger.annotations.ApiModel @lombok.Data
public class OutputMessage {
  private String session;
  private Object payload;
}
