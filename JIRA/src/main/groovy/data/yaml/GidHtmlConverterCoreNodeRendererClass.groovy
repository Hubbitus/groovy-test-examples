package data.yaml

import com.vladsch.flexmark.ast.Reference
import com.vladsch.flexmark.html.renderer.LinkType
import com.vladsch.flexmark.html.renderer.ResolvedLink
@Grab(group='com.vladsch.flexmark', module='flexmark-html2md-converter', version='0.64.8')
import com.vladsch.flexmark.html2md.converter.*
import com.vladsch.flexmark.html2md.converter.internal.HtmlConverterCoreNodeRenderer
import com.vladsch.flexmark.util.data.DataHolder
import org.jsoup.nodes.Element

/**
* Class to handle JIRA html to Markdown conversions
* @author Pavel Alexeeev <plalexeev@gid.ru>
**/
public class GidHtmlConverterCoreNodeRenderer extends HtmlConverterCoreNodeRenderer {
    final private HtmlConverterOptions myHtmlConverterOptions

    GidHtmlConverterCoreNodeRenderer(DataHolder options) {
        super(options)
        myHtmlConverterOptions = new HtmlConverterOptions(options)
    }

    @Override
    Set<HtmlNodeRendererHandler<?>> getHtmlNodeRendererHandlers() {
        return new HashSet<>(Arrays.asList(
            new HtmlNodeRendererHandler<>(FlexmarkHtmlConverter.A_NODE, Element.class, this::processA),
            new HtmlNodeRendererHandler<>(FlexmarkHtmlConverter.CODE_NODE, Element.class, this::processCode),
            new HtmlNodeRendererHandler<>(FlexmarkHtmlConverter.PRE_NODE, Element.class, this::processCode),
            new HtmlNodeRendererHandler<>(FlexmarkHtmlConverter.INS_NODE, Element.class, this::processCodeInline),
            new HtmlNodeRendererHandler<>(FlexmarkHtmlConverter.B_NODE, Element.class, this::processStrong),
            new HtmlNodeRendererHandler<>(FlexmarkHtmlConverter.STRONG_NODE, Element.class, this::processStrong)
        ))
    }

    /**
    * Override base implementation to use ``` as code delimiter, and not single backtrack.
    * Also we need to escape all * symbols to do not get "Format error"
    **/
    private void processCode(Element element, HtmlNodeConverterContext context, HtmlMarkdownWriter out) {
        context.processConditional(myHtmlConverterOptions.extInlineCode, element, () -> {
            context.inlineCode(() -> {
//                context.processTextNodes(element, true, '```', '') // Simple as 1 line if not need to deal with escaping!
                String str = context.processTextNodes(element)
                context.pushState(element)
//                out.append("```\n${escapeMarkdown(str)}\n```")
                out.append("```\n${str}\n```")
                context.popState(out)
            })
        })
    }

    public static String escapeMarkdown(String str) {
        str?.replaceAll(/(?m)(?<!\\\\)([\*_\[\]])/, /\\$1/)?.
            replaceAll('&#91;', "\\\\[")?.
                replaceAll('&#93;', "\\\\]")
    }

    /**
    * Process <ins> tag as inline `code`
    **/
    private void processCodeInline(Element element, HtmlNodeConverterContext context, HtmlMarkdownWriter out) {
        context.processConditional(myHtmlConverterOptions.extInlineCode, element, () -> {
            context.inlineCode(() -> {
                String str = context.processTextNodes(element)
                context.pushState(element)
//                out.append("`${escapeMarkdown(str)}`")
                out.append("`${str}`")
                context.popState(out)
            })
        })
    }

    /**
    * GidMost (VkTeams) bot markdown uses single * for mark text as bold
    **/
    private void processStrong(Element element, HtmlNodeConverterContext context, HtmlMarkdownWriter out) {
        context.processConditional(myHtmlConverterOptions.extInlineStrong, element, () -> {
            if (!myHtmlConverterOptions.preCodePreserveEmphasis && out.isPreFormatted()) {
                context.wrapTextNodes(element, "", false);
            } else {
                context.wrapTextNodes(element, myHtmlConverterOptions.extInlineStrong.isTextOnly() ? "" : "*", element.nextElementSibling() != null);
            }
        })
    }

    /**
     * Mostly Copy/Paste of original tag implementation, include title into link text, not URL (bug??)
     * @param element
     * @param context
     * @param out
     */
    private void processA(Element element, HtmlNodeConverterContext context, HtmlMarkdownWriter out) {
        // see if it is an anchor or a link
        if (element.hasAttr("href")) {
            LinkConversion conv = myHtmlConverterOptions.extInlineLink;
            if (conv.isSuppressed()) return;

            String href = element.attr("href");
            ResolvedLink resolvedLink = context.resolveLink(LinkType.LINK, href, false);
            String useHref = resolvedLink.getUrl();

            if (out.isPreFormatted()) {
                // in preformatted text links convert to URLs
                int slashIndex = useHref.lastIndexOf('/');
                if (slashIndex != -1) {
                    int hashIndex = useHref.indexOf('#', slashIndex);
                    if (hashIndex != -1 && slashIndex + 1 == hashIndex) {
                        // remove trailing / from page ref
                        useHref = useHref.substring(0, slashIndex) + useHref.substring(hashIndex);
                    }
                }
                out.append(useHref);
            } else if (conv.isParsed()) {
                context.pushState(element);
                String textNodes = context.processTextNodes(element);
                String text = textNodes.trim();
                String title = element.hasAttr("title") ? element.attr("title") : null;

                if (!text.isEmpty() || !useHref.contains("#")
                        || !isHeading(element.parent()) && !useHref.equals("#") && (context.getState() == null || context.getState().getAttributes().get("id") == null || context.getState().getAttributes().get("id").getValue().isEmpty())) {
                    if (myHtmlConverterOptions.extractAutoLinks && href.equals(text) && (title == null || title.isEmpty())) {
                        if (myHtmlConverterOptions.wrapAutoLinks) out.append('<');
                        out.append(useHref);
                        if (myHtmlConverterOptions.wrapAutoLinks) out.append('>');
                        context.transferIdToParent();
                    } else if (!conv.isTextOnly() && !useHref.startsWith("javascript:")) {
                        boolean handled = false;

                        if (conv.isReference() && !hasChildrenOfType(element, explicitLinkTextTags)) {
                            // need reference
                            Reference reference = context.getOrCreateReference(useHref, text, title);
                            if (reference != null) {
                                handled = true;
                                if (reference.getReference().equals(text)) {
                                    out.append('[').append(text).append("][]");
                                } else {
                                    out.append('[').append(text).append("][").append(reference.getReference()).append(']');
                                }
                            }
                        }

                        if (!handled) {
                            out.append('[');
                            out.append(text);
                            if (title != null)
                                out.append(" «").append(title.replace("\n", myHtmlConverterOptions.eolInTitleAttribute).replace("\"", "\\\"")).append('»');
                            out.append(']');
                            out.append('(').append(useHref);
                            out.append(")");
                        }
                    } else {
                        if (href.equals(text)) {
                            out.append(useHref);
                        } else {
                            out.append(text);
                        }
                    }

                    context.excludeAttributes("href", "title");
                    context.popState(out);
                } else {
                    context.transferIdToParent();
                    context.popState(null);
                }
            } else if (!conv.isSuppressed()) {
                context.processWrapped(element, null, true);
            }
        } else {
            boolean stripIdAttribute = false;
            if (element.childNodeSize() == 0 && element.parent().tagName().equals("body")) {
                // these are GitHub dummy repeats of heading anchors
                stripIdAttribute = true;
            }

            context.processTextNodes(element, stripIdAttribute);
        }
    }
}

''
