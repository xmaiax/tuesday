package tuesday.configuration;

import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class NLPConfiguration {

  @io.swagger.annotations.ApiModel
  @lombok.Getter public static enum Tag {
    ARTIGO("art",  new String[] { "art", }),
    NUMERAL("num", new String[] { "num", }),
    PONTUACAO("punc", new String[] { "pont", }),
    SUBSTANTIVO("n", new String[] { "sm", "sf", }),
    ADJETIVO("adj", new String[] { "adj", }),
    ADVERBIO("adv", new String[] { "adv", }),
    NOME_PROPRIO("prop", new String[] { "prop", }),
    PREPOSICAO("prp", new String[] { "prp", "prep", }),
    INTERJEICAO("in", new String[] { "int", }),
    PRONOME_DETERMINATIVO("pron-det", new String[] { "pron", }),
    PRONOME_INDEPENDENTE("pron-indp", new String[] { "pron", }),
    PRONOME_PESSOAL("pron-pers", new String[] { "pron", }),
    VERBO_FINITO("v-fin", new String[] { "v", }),
    VERBO_GERUNDIO("v-ger", new String[] { "v", }),
    VERBO_INFINITIVO("v-inf", new String[] { "v", }),
    VERBO_PARTICIPIO("v-pcp", new String[] { "v", }),
    CONJUNCAO_COORDENATIVA("conj-c", new String[] { "conj", }),
    CONJUNCAO_SUBORDINATIVA("conj-s", new String[] { "conj", }),
    SINTAGMA_EVIDENCIADOR_REL_COORDENACAO("ec", new String[] { "sint", "ec", }),
    SINTAGMA_PREPOSICIONAL("pp", new String[] { "sint", "pp", }),
    SINTAGMA_VERBAL("vp", new String[] { "sint", "vp", }),
    DESCONHECIDO("?", new String[] { });
    private String code;
    private java.util.List<String> startsWith;
    private Tag(String code, String[] startsWith) {
      this.code = code;
      this.startsWith = java.util.Collections.unmodifiableList(java.util.Arrays.asList(startsWith));
    }
    public static Tag fromCode(String code) {
      return java.util.Arrays.asList(Tag.values()).stream()
        .filter(t -> t.getCode().equals(code) || t == DESCONHECIDO).findFirst().get();
    }
  }

  @Bean
  public opennlp.tools.tokenize.Tokenizer regexTokenizer() {
    return new opennlp.tools.tokenize.Tokenizer() {
      private java.util.regex.Pattern regexPattern = java.util.regex.Pattern.compile(
        "(\\.)+|\\,|(\\!)+|\\(|\\)|[\\w\\@\\#\\$\\%\\&\\*\u00c0-\u00ff]*");
      @Override
      public String[] tokenize(String input) {
        return opennlp.tools.util.Span.spansToStrings(this.tokenizePos(input), input);
      }
      @Override
      public opennlp.tools.util.Span[] tokenizePos(String input) {
        final java.util.List<opennlp.tools.util.Span> output =
          new java.util.ArrayList<opennlp.tools.util.Span>();
        final java.util.regex.Matcher matcher = this.regexPattern.matcher(input);
        while(matcher.find()) {
          final String match = matcher.group();
          if(match != null && !match.isBlank())
            output.add(new opennlp.tools.util.Span(matcher.start(), matcher.end()));
        }
        return output.toArray(opennlp.tools.util.Span[]::new);
      }
    };
  }

  @Bean
  public opennlp.tools.sentdetect.SentenceDetectorME brSentenceDetector() throws java.io.IOException {
    return new opennlp.tools.sentdetect.SentenceDetectorME(
      new opennlp.tools.sentdetect.SentenceModel(tuesday.utils.ResouceReader.getInstance()
        .getResourceAsStream(eu.crydee.uima.opennlp.resources.PtSentModel.path)));
  }

  @org.springframework.beans.factory.annotation.Value("${nlp.embedded-dict-resource-path}")
  private String embeddedDictPath;

  @Bean
  public opennlp.tools.lemmatizer.DictionaryLemmatizer brLemmatizer() throws java.io.IOException {
    return new opennlp.tools.lemmatizer.DictionaryLemmatizer(tuesday.utils.ResouceReader
        .getInstance().getResourceAsStream(this.embeddedDictPath)) {
      @Override
      public String[] lemmatize(final String[] tokens, final String[] postags) {
        return java.util.Arrays.asList(super.lemmatize(tokens, postags)).stream()
          .map(lemma -> "O".equals(lemma) ? null : lemma).toArray(String[]::new);
      }
    };
  }

  @Bean
  public opennlp.tools.postag.POSTaggerME brPosTagger() throws java.io.IOException {
    return new opennlp.tools.postag.POSTaggerME(new opennlp.tools.postag.POSModel(
      tuesday.utils.ResouceReader.getInstance().getResourceAsStream(
        eu.crydee.uima.opennlp.resources.PtPosMaxentModel.path)));
  }

}
