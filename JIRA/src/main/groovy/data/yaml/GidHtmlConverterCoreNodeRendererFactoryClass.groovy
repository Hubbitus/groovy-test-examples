package data.yaml

import data.yaml.GidHtmlConverterCoreNodeRenderer

@Grab(group='com.vladsch.flexmark', module='flexmark-html2md-converter', version='0.64.8')
import com.vladsch.flexmark.html2md.converter.HtmlNodeRenderer
import com.vladsch.flexmark.html2md.converter.HtmlNodeRendererFactory
import com.vladsch.flexmark.util.data.DataHolder

/**
* Class to handle JIRA html to Markdown conversions - factory
* @author Pavel Alexeeev <plalexeev@gid.ru>
**/
public class GidHtmlConverterCoreNodeRendererFactory implements HtmlNodeRendererFactory {
    @Override
    public HtmlNodeRenderer apply(DataHolder options) {
        return new GidHtmlConverterCoreNodeRenderer(options)
    }
}

''