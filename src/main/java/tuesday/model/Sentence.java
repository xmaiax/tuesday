package tuesday.model;

@lombok.experimental.Accessors(chain = true)
@io.swagger.annotations.ApiModel @lombok.Data
public class Sentence {
  @com.fasterxml.jackson.annotation.JsonProperty("sentence") private String text;
  private Double confidence;
  private java.util.List<Word> words;
}
