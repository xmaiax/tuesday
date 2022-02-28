package tuesday.service.impl;

import org.springframework.beans.factory.annotation.Autowired;

import tuesday.configuration.NLPConfiguration.Tag;

@lombok.extern.slf4j.Slf4j
@org.springframework.stereotype.Service
public class NLPService {

  public static final java.util.List<Tag> PRONOUNS_TAGS = java.util.Collections.unmodifiableList(
    java.util.Arrays.asList(new Tag[] {
      Tag.PRONOME_DETERMINATIVO, Tag.PRONOME_PESSOAL,
      Tag.PRONOME_INDEPENDENTE, Tag.NOME_PROPRIO, }));

  @Autowired private opennlp.tools.tokenize.Tokenizer tokenizer;
  @Autowired private opennlp.tools.sentdetect.SentenceDetectorME sentenceDetector;
  @Autowired private opennlp.tools.lemmatizer.DictionaryLemmatizer lemmatizer;
  @Autowired private opennlp.tools.postag.POSTaggerME posTagger;

  @org.springframework.beans.factory.annotation.Value("${nlp.embedded-dict-minimal-probability}")
  private Double embeddedDictMinimalProb;

  private Tag[] calculateTag(String token, Tag tag, Double probability) {
    if(probability > this.embeddedDictMinimalProb || PRONOUNS_TAGS.contains(tag))
      return new Tag[] { tag, };
    //TODO: Buscar todas as possíveis Tag's em um dicionário independente.
    // A primeira sempre será indicada como correspondente.
    log.warn("Unidentified token: {}", token);
    return new Tag[] { tag, };
  }

  private Double calculateProbability(Double probability, Tag[] possibleTags) {
    return probability + (possibleTags.length == java.math.BigDecimal.ONE.intValue() ?
      java.math.BigDecimal.ZERO.doubleValue() :
        java.math.BigDecimal.ONE.doubleValue() / possibleTags.length);
  }

  public tuesday.model.Text analyze(String text) {
    final long startTime = java.util.Calendar.getInstance().getTimeInMillis();
    final java.util.List<tuesday.model.Sentence> sentences = java.util.Arrays.asList(
        this.sentenceDetector.sentDetect(text)).stream().map(sentence -> {
      log.info("Analysing sentence: {}", sentence);
      final String[] tokens = this.tokenizer.tokenize(sentence);
      final String[] tags = this.posTagger.tag(tokens);
      final double[] probs = this.posTagger.probs();
      final String[] lemmas = this.lemmatizer.lemmatize(tokens, tags);
      final java.util.List<tuesday.model.Word> words = java.util.stream.IntStream.range(
        java.math.BigInteger.ZERO.intValue(), lemmas.length).mapToObj(i -> {
          final Tag[] possibleTags = this.calculateTag(tokens[i], Tag.fromCode(tags[i]), probs[i]);
          return new tuesday.model.Word().setChance(this.calculateProbability(probs[i], possibleTags))
            .setToken(tokens[i]).setLemma(lemmas[i]).setTag(
              possibleTags[java.math.BigInteger.ZERO.intValue()]);
        }).collect(java.util.stream.Collectors.toList());
      return new tuesday.model.Sentence().setText(sentence).setWords(words)
        .setConfidence(words.stream().mapToDouble(w -> w.getChance()).average()
          .orElse(java.math.BigDecimal.ZERO.doubleValue()));
    }).collect(java.util.stream.Collectors.toList());
    return new tuesday.model.Text().setInput(text).setOutput(sentences)
      .setProcessingTimeInMillis(java.util.Calendar.getInstance().getTimeInMillis() - startTime);
  }

}
