package darwin

import javax.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.MediaType

@RestController @RequestMapping(
  path = arrayOf("/test/**"),
  produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
open class TestController() {

  @RequestMapping
  fun allRequests(request: HttpServletRequest) {
    System.err.println("""
Method: ${request.getMethod()}
Headers: ${request.getHeaderNames().toList()
    .filter { it != "authorization" }.map { headerName ->
  request.getHeader(headerName)?.let { "${headerName}:${it}" }}}
URI: ${request.getRequestURI()}
Token: ${request.getHeader("authorization")}
Body: ${request.getReader().lines().collect(java.util.stream.
  Collectors.joining(System.lineSeparator()))}
""")
  }

}

/*

import org.apache.commons.codec.binary.Base64;
@Test
public void testDecodeJWT(){
  String jwtToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0Iiwicm9sZXMiOiJST0xFX0FETUlOIiwiaXNzIjoibXlzZWxmIiwiZXhwIjoxNDcxMDg2MzgxfQ.1EI2haSz9aMsHjFUXNVz2Z4mtC0nMdZo6bo3-x-aRpw";
  System.out.println("------------ Decode JWT ------------");
  String[] split_string = jwtToken.split("\\.");
  String base64EncodedHeader = split_string[0];
  String base64EncodedBody = split_string[1];
  String base64EncodedSignature = split_string[2];

  System.out.println("~~~~~~~~~ JWT Header ~~~~~~~");
  Base64 base64Url = new Base64(true);
  String header = new String(base64Url.decode(base64EncodedHeader));
  System.out.println("JWT Header : " + header);

  System.out.println("~~~~~~~~~ JWT Body ~~~~~~~");
  String body = new String(base64Url.decode(base64EncodedBody));
  System.out.println("JWT Body : "+body);
}

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

*/

/*@RestController @RequestMapping(
  path = arrayOf("/assistant/api/v1"),
  produces = arrayOf(MediaType.APPLICATION_JSON_VALUE),
  consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
open class AssistantController() {

  @PostMapping("/workspaces/xuruvisWorkspaceId/message")
  @ApiOperation(value = "Simple message request.")
  fun message() {

  }

}*/

