@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.6')
import groovy.json.JsonBuilder
import groovyx.net.http.RESTClient
import groovyx.net.http.ContentType


JsonBuilder jb = new JsonBuilder()
jb {
    method 'user.login'
    params {
        user 'p.alexeev'
        password 'test'
    }
    id 1
}
//        log.info("JSON для запроса аутентификации в Zabbix: ${jb.toString()}")
//        jb.content.auth = authData // It is constant part
jb.content.jsonrpc = '2.0' // It is constant part

RESTClient client = new RESTClient("http://192.168.100.182")
def resp = client.post(
    contentType: ContentType.JSON,
    requestContentType: ContentType.JSON,
    path: "/zabbix/api_jsonrpc.php",
    body: jb.toString()
);
return resp.data.result;