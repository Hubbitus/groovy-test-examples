package data

@Grapes(
    @Grab(group = 'org.yaml', module = 'snakeyaml', version = '2.2')
)
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.introspector.BeanAccess

/**
 * The very minimal example to send data as webhook:
 * new URL('https://webhook.site/f5b0da6d...?hello=from_groovy').text
 *
 * But we want most comfortable way to do so,
 * Example of usage:
 * new RemoteDebugSend('https://webhook.site/f5b0da6d...')).debug([one: 1, two: '22'])
 **/
public class RemoteDebugSend {
    URL url

    public RemoteDebugSend(String url){
        this.url = new URL(url)
    }

    /**
    * @link by https://bitbucket.org/snakeyaml/snakeyaml/wiki/Documentation#markdown-header-dumping-yaml
    * @param beanAccess also may be BeanAccess.FIELD to try dump also private fields too.
    **/
    static String toYAML(any, dumpPrivateInfo = false) {
        Yaml yaml = new Yaml(new DumperOptions().tap { width = 100000 })
        yaml.setBeanAccess(dumpPrivateInfo ? BeanAccess.FIELD : BeanAccess.DEFAULT)
        return yaml.dump(any)
    }

    /**
    * Map m = [one: 1, two: '22']
    * new URL('https://webhook.site/f5b0da6d-2793-4195-af0c-0b9cdd7f4bcf?q=' + m.encodeURL()).text
    * @link by https://stackoverflow.com/questions/25692515/groovy-built-in-rest-http-client
    **/
    String debug(any, dumpPrivateInfo = false) {
        // HTTP GET:
        //String resp = new URL('https://webhook.site/f5b0da6d-2793-4195-af0c-0b9cdd7f4bcf?debug=' + any.toYML().encodeURL()).text

        // HTTP POST (much more readable):
        String resp
        ((HttpURLConnection)url.openConnection()).with({
            requestMethod = 'POST'
            doOutput = true
            setRequestProperty('Content-Type', 'text/yaml') // Set your content type.
            outputStream.withPrintWriter({ printWriter ->
                printWriter.write(toYAML(any, dumpPrivateInfo)) // Your post data. Could also use withWriter() if you don't want to write a String.
            })
            // Can check 'responseCode' here if you like.
            resp = inputStream.text // Using 'inputStream.text' because 'content' will throw an exception when empty.
            return resp
        })
    }

    static {
        /**
        * Globally register handler as meta-method
        * @link by https://stackoverflow.com/questions/10187344/how-to-encode-url-in-groovy/10187493#10187493 by
        **/
        String.metaClass.encodeURL = {
            URLEncoder.encode((String)delegate, "UTF-8")
        }

        Object.metaClass.encodeURL = {
            delegate.toString().encodeURL()
        }
    }
}

''