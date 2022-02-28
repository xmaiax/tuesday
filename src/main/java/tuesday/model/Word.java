package tuesday.model;

@lombok.experimental.Accessors(chain = true)
@io.swagger.annotations.ApiModel @lombok.Data
public class Word {
  private String token;
  private tuesday.configuration.NLPConfiguration.Tag tag;
  private String lemma;
  private Double chance;
}
