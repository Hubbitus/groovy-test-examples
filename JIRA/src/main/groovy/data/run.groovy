
// Will work in Java application (and in JIRA ScriptRunner)
// But for running as *scripts* said in idea we need machinery like:
//import RemoteDebugSendClass

// WORKS, but require call newInstance, instead of traditional new
//Class RemoteDebugSendClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(new File('RemoteDebugSendClass.groovy'))
//println(RemoteDebugSendClass.newInstance('https://webhook.site/f5b0da6d-2793-4195-af0c-0b9cdd7f4bcf').debug([one:  1, two:  '22']))

/**
* Dynamic scripts load
* Example of usage:
* _new_('data.RemoteDebugSend', 'https://webhook.site/f5b0da6d-2793-4195-af0c-0b9cdd7f4bcf').debug([one:  1, two:  '24'])
*
* @param className
* @param args
* @return
**/
def _new_ (String className, ...args){
    GroovyClassLoader gos = new GroovyClassLoader(getClass().getClassLoader())
    gos.parseClass(
        new File("${className.replaceAll(/data\./, '').replaceAll(/\./, '/')}Class.groovy")
    )

    return gos.loadClass(className).newInstance(args)
}
//_new_('data.RemoteDebugSend', 'https://webhook.site/f5b0da6d-2793-4195-af0c-0b9cdd7f4bcf').debug([one:  1, two:  '24'])


// Can't convert outside JIRA (got from log):
String jiraCommentHTML = '''<p>Коммент 23_4<br/>
Нам поможет запрос:</p>
<div class="code panel" style="border-width: 1px;"><div class="codeContent panelContent">
<pre class="code-sql code_sql">
<span class="code-keyword">SELECT</span> *
<span class="code-keyword">FROM</span> <span class="code-keyword">table</span>
</pre>
</div></div>'''

println("jiraCommentHTML: ${jiraCommentHTML}")

@Grab(group='com.vladsch.flexmark', module='flexmark-html2md-converter', version='0.64.8')
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import com.vladsch.flexmark.util.data.MutableDataSet

MutableDataSet mdOptions = new MutableDataSet()
mdOptions.set(FlexmarkHtmlConverter.THEMATIC_BREAK, '--------------------')
mdOptions.set(FlexmarkHtmlConverter.UNORDERED_LIST_DELIMITER, (char)'-')

// Prepare HTML from JIRA. Sometimes {{ }} does not converted in any code, we want inline it
jiraCommentHTML = jiraCommentHTML.replaceAll(/\{\{/, '<ins>').replaceAll(/\}\}/, '</ins>')


//import data.yaml.GidHtmlConverterCoreNodeRendererClass
//import data.yaml.GidHtmlConverterCoreNodeRenderer
renderer = _new_('data.yaml.GidHtmlConverterCoreNodeRenderer')
//jiraCommentHTML = GidHtmlConverterCoreNodeRenderer.escapeMarkdown(jiraCommentHTML)
jiraCommentHTML = renderer.escapeMarkdown(jiraCommentHTML)
println("jiraCommentHTML after escape: ${jiraCommentHTML}")

//import data.yaml.GidHtmlConverterCoreNodeRendererFactoryClass
//import data.yaml.GidHtmlConverterCoreNodeRendererFactory

FlexmarkHtmlConverter.Builder builder = FlexmarkHtmlConverter.builder(mdOptions)
//builder.htmlNodeRendererFactory(new GidHtmlConverterCoreNodeRendererFactory())
builder.htmlNodeRendererFactory(_new_('data.yaml.GidHtmlConverterCoreNodeRendererFactory'))
FlexmarkHtmlConverter converter = builder.build()
String markdown = converter.convert(jiraCommentHTML)
log.warn("Most Markdown: ${markdown}")


//import data.GidMostBotClass
//import data.GidMostBot
//GidMostBot bot = new GidMostBot('001....', 'ApAGBrO8bgC7vGw')
def bot = _new_('data.GidMostBot', '001.....', 'ApAGBrO8bgC7vGw')
println(bot.sendMarkdownText(markdown))
