import data.GidMostBot
import spock.lang.Specification

class FormattingCheckSpec extends Specification {
    def 'Base convert HTML To Most markdown'(){
        given:
            String html = '''Hello <b>world</b>!'''
        when:
            String markdown = GidMostBot.convertHtmlToMostMarkdown(html)
        then:
            'Hello *world*!' == markdown
    }

    def 'Base convert HTML To Most markdown with underscore'(){
        given:
            String html = '''Hello <b>world</b>! 2 * 3 = 6'''
        when:
            String markdown = GidMostBot.convertHtmlToMostMarkdown(html)
        then:
            'Hello *world*! 2 \\* 3 = 6' == markdown
    }

    def 'Convert HTML To Most markdown with underscore _'(){
        given:
            String html = '''<p>Коммент 23_4<br/>
Нам поможет запрос:</p>
<div class="code panel" style="border-width: 1px;"><div class="codeContent panelContent">
<pre class="code-sql code_sql">
<span class="code-keyword">SELECT</span> *
<span class="code-keyword">FROM</span> <span class="code-keyword">table</span>
</pre>
</div></div>'''

        when:
            String markdown = GidMostBot.convertHtmlToMostMarkdown(html).trim()
        then:
            markdown == '''Коммент 23\\_4  

Нам поможет запрос:  
```
SELECT \\*
FROM table
```'''
    }

    def 'Convert issue link message'() {
        given:
            String html = '''<div class="panel" style="background-color: #a3f1b9;border-style: dashed;border-width: 1px;"><div class="panelHeader" style="border-bottom-width: 1px;border-bottom-style: dashed;background-color: #77c386;"><b>Добавлена ссылка (<em>auto comment</em>)</b></div><div class="panelContent" style="background-color: #a3f1b9;">
<p><b><a href="https://jira-lab.gid.team/browse/DATA-9820" title="Завершился ошибкой DAG alert_test - 2024-05-27 - 119" class="issue-link" data-issue-key="DATA-9820"><del>DATA-9820</del></a></b> <tt>blocks</tt> <a href="https://jira-lab.gid.team/browse/DATA-9830" title="[DQ-issue][auto]Airflow Alert - 14354" class="issue-link" data-issue-key="DATA-9830">DATA-9830</a> "<span class="error">&#91;DQ-issue&#93;</span><span class="error">&#91;auto&#93;</span>Airflow Alert - 14354"</p>
</div></div>'''

        when:
            String markdown = GidMostBot.convertHtmlToMostMarkdown(html).trim()

        then:
            markdown ==  '''*Добавлена ссылка (*auto comment*)*  
*[~~DATA-9820~~ «Завершился ошибкой DAG alert\\_test - 2024-05-27 - 119»](https://jira-lab.gid.team/browse/DATA-9820)* blocks [DATA-9830 «\\[DQ-issue\\]\\[auto\\]Airflow Alert - 14354»](https://jira-lab.gid.team/browse/DATA-9830) "\\[DQ-issue\\]\\[auto\\]Airflow Alert - 14354"'''
    }
}
