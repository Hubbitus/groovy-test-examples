@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.6')
import groovy.json.JsonBuilder
import groovyx.net.http.RESTClient
import groovyx.net.http.ContentType

class LersJsonClient{
    String authCookie;

    public def query(JsonBuilder query, String path) {
        if (!authCookie && '/Api.asmx/Login' != path) auth();
    
        RESTClient client = new RESTClient('http://tgu-s.ru')
        client.headers['Cookie'] = authCookie;
        def resp = client.post(
            contentType: ContentType.JSON,
            requestContentType: ContentType.JSON,
            path: path,
            body: query.toString()
        );
        return resp;
    }
    
    public def auth(){
        JsonBuilder jb = new JsonBuilder()
        jb {
            contentType: 'application/json; charset=utf-8'
            userName 'ГАЗ'
            password '123'
        }
        def resp = query(jb, '/Api.asmx/Login')
        authCookie = resp.getHeaders('Set-Cookie').value[0];
    }
}

LersJsonClient lc = new LersJsonClient();

JsonBuilder jb = new JsonBuilder()
jb {}
lc.query(jb, '/Api.asmx/GetMeasurePointList').data.d.MeasurePointList

/*
jb{
    measurePointId 160
}
lc.query(jb, '/Api.asmx/GetMeasurePointById').data.d
*/

use(groovy.json.JsonOutput){
    jb{
        contentType 'application/json; charset=utf-8'
        measurePointId 160
        startDate (new Date() - 2).toJson()
        endDate (new Date()).toJson()
    }
}
lc.query(jb, '/Api.asmx/ExportMeasurePointDataToXml80020').data.d