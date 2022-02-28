package tuesday.model;

@lombok.experimental.Accessors(chain = true)
@io.swagger.annotations.ApiModel @lombok.Data
public class Text {
  private String input;
  private Long processingTimeInMillis = -1L;
  private java.util.List<Sentence> output;
}
