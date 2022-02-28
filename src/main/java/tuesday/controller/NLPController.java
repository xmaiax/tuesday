package tuesday.controller;

@io.swagger.annotations.Api(value = "NLP API")
@org.springframework.web.bind.annotation.RestController
@org.springframework.web.bind.annotation.RequestMapping(path = { "/api/v1/nlp", },
  produces = { org.springframework.http.MediaType.APPLICATION_JSON_VALUE, })
public class NLPController {

  @org.springframework.beans.factory.annotation.Autowired
  private tuesday.service.impl.NLPService service;

  @io.swagger.annotations.ApiOperation(value = "Analyze given sentence(s).")
  @org.springframework.web.bind.annotation.PostMapping(path = { "/text-analysis", },
    consumes = { org.springframework.http.MediaType.TEXT_PLAIN_VALUE, })
  public tuesday.model.Text analyze(@io.swagger.annotations.ApiParam(value = "Plain text to be analysed",
      required = true) @org.springframework.web.bind.annotation.RequestBody String text) {
    return this.service.analyze(text);
  }

}
