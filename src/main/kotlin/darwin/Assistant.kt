package darwin

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiOperation
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Base64

data class MessageInput(val text: String)
data class MessageRequest(val input: MessageInput,
  @JsonProperty("alternate_intents") val alternateIntents: Boolean,
  val context: Map<String, Any>)

data class JWTToken(val header: String,
  val payload: String, val signature: String)

fun JWTToken(token: String): JWTToken? {
  if(token.startsWith("Bearer ")) {
    val splittedJwtToken = token.substring("Bearer ".length, token.length).split(".")
    return JWTToken(
      String(Base64.getDecoder().decode(splittedJwtToken[0])),
      String(Base64.getDecoder().decode(splittedJwtToken[1])),
      splittedJwtToken[2]
    )
  }
  return null
}

/*

Method: POST
Headers:
    accept:application/json
    content-type:application/json
    user-agent:watson-developer-cloud-nodejs-3.15.0;
    host:localhost:8888
    accept-encoding:gzip, deflate
    content-length:74
    connection:close
URI: /assistant/api/v1/workspaces/xuruvisWorkspaceId/message
Token: Bearer eyJraWQiOiIyMDIwMTIyMTE4MzQiLCJhbGciOiJSUzI1NiJ9.eyJpYW1faWQiOiJpYW0tU2VydmljZUlkLTAyNWQ1NzBjLTJhNWQtNGFjNC05ODcwLTA4OGZkZmNmZGYyOSIsImlkIjoiaWFtLVNlcnZpY2VJZC0wMjVkNTcwYy0yYTVkLTRhYzQtOTg3MC0wODhmZGZjZmRmMjkiLCJyZWFsbWlkIjoiaWFtIiwianRpIjoiZmM0NzA0MDMtYTBhMi00YmFkLTkyZWUtMjcwOTQxNGIwNjgwIiwiaWRlbnRpZmllciI6IlNlcnZpY2VJZC0wMjVkNTcwYy0yYTVkLTRhYzQtOTg3MC0wODhmZGZjZmRmMjkiLCJuYW1lIjoiQXV0by1nZW5lcmF0ZWQgc2VydmljZSBjcmVkZW50aWFscyIsInN1YiI6IlNlcnZpY2VJZC0wMjVkNTcwYy0yYTVkLTRhYzQtOTg3MC0wODhmZGZjZmRmMjkiLCJzdWJfdHlwZSI6IlNlcnZpY2VJZCIsInVuaXF1ZV9pbnN0YW5jZV9jcm5zIjpbImNybjp2MTpibHVlbWl4OnB1YmxpYzpjb252ZXJzYXRpb246ZXUtZGU6YS8wMzkwMzRlMjFjNTZiMTVjOTZjZWNjMGJhNzRiNzE1NjoyNjFjNWM3MS02ZDU2LTQ3OWUtOTQ5Mi01YWUzNDcyZDNhNTI6OiJdLCJhY2NvdW50Ijp7InZhbGlkIjp0cnVlLCJic3MiOiIwMzkwMzRlMjFjNTZiMTVjOTZjZWNjMGJhNzRiNzE1NiIsImZyb3plbiI6dHJ1ZX0sImlhdCI6MTYxMDM5NzI4OSwiZXhwIjoxNjEwNDAwODg5LCJpc3MiOiJodHRwczovL2lhbS5ibHVlbWl4Lm5ldC9pZGVudGl0eSIsImdyYW50X3R5cGUiOiJ1cm46aWJtOnBhcmFtczpvYXV0aDpncmFudC10eXBlOmFwaWtleSIsInNjb3BlIjoiaWJtIG9wZW5pZCIsImNsaWVudF9pZCI6ImJ4IiwiYWNyIjoxLCJhbXIiOlsicHdkIl19.YzwW9voSAbe2T7YSqv7m38lPhUvLmp5myL_8JtC1VATuPvWzB_KU8vJUHf_2AkFZ8lDDNCK_rpFUxVvWg877XK90Yiavgxc2BqRMDHkbYIC1zo5FJnup3GY4Eh4YmlKzRgIHP0MlQBtikmIu6QSzuH-t9R4AyEfOXTUffnDBO5wf5Syj2dw2SnF4XbSFqA3ElIb7G1A9mVFuUEzyzLLeuOXga9jZIJsf-KqtFDs2D_mELatzseH7nmc6dJ4Gg9ohSnxTIl_x8VVTyjqZNN21kEc2pMYgUsQ9NhW4OJMzRAgyK8lvGaeNArQHVVGBwsN0ps_LDj7I22Q9-EexWlLYaQ
Body: {"input":{"text":"Saldo e Extrato"},"alternate_intents":true,"context":{}}

JWTToken(
  header={"kid":"202012211834","alg":"RS256"},
  payload={"iam_id":"iam-ServiceId-025d570c-2a5d-4ac4-9870-088fdfcfdf29","id":"iam-ServiceId-025d570c-2a5d-4ac4-9870-088fdfcfdf29","realmid":"iam","jti":"fc470403-a0a2-4bad-92ee-2709414b0680","identifier":"ServiceId-025d570c-2a5d-4ac4-9870-088fdfcfdf29","name":"Auto-generated service credentials","sub":"ServiceId-025d570c-2a5d-4ac4-9870-088fdfcfdf29","sub_type":"ServiceId","unique_instance_crns":["crn:v1:bluemix:public:conversation:eu-de:a/039034e21c56b15c96cecc0ba74b7156:261c5c71-6d56-479e-9492-5ae3472d3a52::"],"account":{"valid":true,"bss":"039034e21c56b15c96cecc0ba74b7156","frozen":true},"iat":1610397289,"exp":1610400889,"iss":"https://iam.bluemix.net/identity","grant_type":"urn:ibm:params:oauth:grant-type:apikey","scope":"ibm openid","client_id":"bx","acr":1,"amr":["pwd"]},
  signature=YzwW9voSAbe2T7YSqv7m38lPhUvLmp5myL_8JtC1VATuPvWzB_KU8vJUHf_2AkFZ8lDDNCK_rpFUxVvWg877XK90Yiavgxc2BqRMDHkbYIC1zo5FJnup3GY4Eh4YmlKzRgIHP0MlQBtikmIu6QSzuH-t9R4AyEfOXTUffnDBO5wf5Syj2dw2SnF4XbSFqA3ElIb7G1A9mVFuUEzyzLLeuOXga9jZIJsf-KqtFDs2D_mELatzseH7nmc6dJ4Gg9ohSnxTIl_x8VVTyjqZNN21kEc2pMYgUsQ9NhW4OJMzRAgyK8lvGaeNArQHVVGBwsN0ps_LDj7I22Q9-EexWlLYaQ
)

*/

@RestController @RequestMapping(
  path = arrayOf("/assistant/api/v1"),
  produces = arrayOf(MediaType.APPLICATION_JSON_VALUE),
  consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
open class AssistantController() {

  @PostMapping("/workspaces/{instance}/message")
  @ApiOperation(value = "Simple message request.")
  fun message(
    @PathVariable instance: String,
    @RequestHeader("Token") jwtToken: String,
    @RequestBody body: MessageRequest
  ) {

    val token = JWTToken(jwtToken)
    System.err.println("""
Instance: ${instance}
Body: ${body}
Token: ${token}
""")

  }

}
