package darwin

import opennlp.tools.util.Span
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

data class Word(val token: String, val tag: Tag,
  val lemma: String?, val chance: Double)

data class Sentence(
  @com.fasterxml.jackson.annotation.JsonProperty("sentence")
  val text: String, val confidence: Double, val words: Array<Word>)

@io.swagger.annotations.ApiModel
data class Text(val input: String, val processingTimeInMillis: Long = -1,
  val output: Array<Sentence>? = null)

@Component
open class RegexTokenizer(
  private val regexPattern: java.util.regex.Pattern =
    java.util.regex.Pattern.compile(
      "(\\.)+|\\,|(\\!)+|\\(|\\)|[\\w\\@\\#\\$\\%\\&\\*\u00c0-\u00ff]*"
    )
) : opennlp.tools.tokenize.Tokenizer {
  override fun tokenize(input: String) =
    Span.spansToStrings(this.tokenizePos(input), input)
  override fun tokenizePos(input: String): Array<Span> {
    val output = mutableListOf<Span>()
    val matcher = this.regexPattern.matcher(input)
    while (matcher.find()) if (!matcher.group().isNullOrEmpty())
      output += Span(matcher.start(), matcher.end())
    return output.toTypedArray()
  }
}

@Component
open class BRSentenceDetector :
  opennlp.tools.sentdetect.SentenceDetectorME(
    opennlp.tools.sentdetect.SentenceModel(
      ResouceReader.getResourceAsStream(
        eu.crydee.uima.opennlp.resources.PtSentModel.path)))

@Component
open class BRLemmatizer(@Value(
    "\${nlp.embedded-dict-resource-path}") val embeddedDictPath: String) :
  opennlp.tools.lemmatizer.DictionaryLemmatizer(
    ResouceReader.getResourceAsStream(embeddedDictPath)) {
  override fun lemmatize(tokens: Array<String>, postags: Array<String>) =
    super.lemmatize(tokens, postags).map { lemm ->
      if (lemm == "O") null else lemm
    }.toTypedArray() }

enum class Tag(val code: String, private val startsWith: Array<String>) {
  DESCONHECIDO("?", arrayOf()),
  ARTIGO("art", arrayOf("art")),
  NUMERAL("num", arrayOf("num")),
  PONTUACAO("punc", arrayOf("pont")),
  SUBSTANTIVO("n", arrayOf("sm", "sf")),
  ADJETIVO("adj", arrayOf("adj")),
  ADVERBIO("adv", arrayOf("adv")),
  NOME_PROPRIO("prop", arrayOf("prop")),
  PREPOSICAO("prp", arrayOf("prp", "prep")),
  INTERJEICAO("in", arrayOf("int")),
  PRONOME_DETERMINATIVO("pron-det", arrayOf("pron")),
  PRONOME_INDEPENDENTE("pron-indp", arrayOf("pron")),
  PRONOME_PESSOAL("pron-pers", arrayOf("pron")),
  VERBO_FINITO("v-fin", arrayOf("v")),
  VERBO_GERUNDIO("v-ger", arrayOf("v")),
  VERBO_INFINITIVO("v-inf", arrayOf("v")),
  VERBO_PARTICIPIO("v-pcp", arrayOf("v")),
  CONJUNCAO_COORDENATIVA("conj-c", arrayOf("conj")),
  CONJUNCAO_SUBORDINATIVA("conj-s", arrayOf("conj")),
  SINTAGMA_EVIDENCIADOR_REL_COORDENACAO("ec", arrayOf("sint", "ec")),
  SINTAGMA_PREPOSICIONAL("pp", arrayOf("sint", "pp")),
  SINTAGMA_VERBAL("vp", arrayOf("sint", "vp")),
  ;companion object {
    fun fromCode(code: String) =
      Tag.values().filter { it.code == code }
        .firstOrNull()?.let { it } ?: run { DESCONHECIDO } }
}

@Component
open class BRPosTagger :
  opennlp.tools.postag.POSTaggerME(opennlp.tools.postag.POSModel(
    ResouceReader.getResourceAsStream(
      eu.crydee.uima.opennlp.resources.PtPosMaxentModel.path)))

@org.springframework.stereotype.Service
open class NLPService(
  @Autowired val tokenizer: RegexTokenizer,
  @Autowired val sentenceDetector: BRSentenceDetector,
  @Autowired val posTagger: BRPosTagger,
  @Autowired val lemmatizer: BRLemmatizer,
  @org.springframework.beans.factory.annotation.Value(
    "\${nlp.embedded-dict-minimal-probability}")
      val embeddedDictMinimalProb: Double) {
  companion object {
    val LOGGER: org.slf4j.Logger =
      org.slf4j.LoggerFactory.getLogger(NLPService::class.java) }
  fun analyzeText(text: Text): Text {
    val startTime = java.util.Calendar.getInstance().getTimeInMillis()
    val output = this.sentenceDetector.sentDetect(text.input).map { sentence ->
      LOGGER.debug("Analysing sentence: ${sentence}")
      val tokens = this.tokenizer.tokenize(sentence)
      val tags = this.posTagger.tag(tokens)
      val probs = this.posTagger.probs()
      val words = this.lemmatizer.lemmatize(tokens, tags)
        .mapIndexedNotNull { i, lemma ->
          var prob = probs[i]
          val tag = if (prob > this.embeddedDictMinimalProb) tags[i]
          else {
            if (arrayOf<String>(
                Tag.PRONOME_DETERMINATIVO.code, Tag.PRONOME_PESSOAL.code,
                Tag.PRONOME_INDEPENDENTE.code, Tag.NOME_PROPRIO.code
              ).contains(tags[i])
            ) tags[i]
            else {
              LOGGER.debug("No dictionary tag found (word: '${
                tokens[i]}', tag: '${Tag.fromCode(tags[i])
                  }', chance: ${probs[i]})")
              tags[i]
            }
          }
          Word(tokens[i], Tag.fromCode(tag), lemma, prob)
        }.toTypedArray()
      val output = Sentence(sentence, words.map { it.chance }.average(), words)
      LOGGER.debug("Complete sentence analysis: ${output}")
      output
    }.toTypedArray()
    return text.copy(output = output, processingTimeInMillis =
      java.util.Calendar.getInstance().getTimeInMillis() - startTime)
  }

}

@org.springframework.web.bind.annotation.RestController
@org.springframework.web.bind.annotation.RequestMapping(
  path = arrayOf("/nlp/api/v1"),
  produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
open class NLPController(@Autowired val nlpService: NLPService) {
  @io.swagger.annotations.ApiOperation(value = "Analyze given sentence(s).")
  @org.springframework.web.bind.annotation.PostMapping(
    path = arrayOf("/text-analysis"),
    consumes = arrayOf(MediaType.TEXT_PLAIN_VALUE)) fun textAnalysis(
      @io.swagger.annotations.ApiParam(value = "Plain text to be analysed",
        required = true)
      @org.springframework.web.bind.annotation.RequestBody(
        required = true) text: String) =
    this.nlpService.analyzeText(Text(text))
}
