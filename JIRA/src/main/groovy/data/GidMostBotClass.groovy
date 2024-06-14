package data

import data.yaml.GidHtmlConverterCoreNodeRendererFactoryClass
import data.yaml.GidHtmlConverterCoreNodeRendererFactory

// Warn! Both GidHtmlConverterCoreNodeRendererClass GidHtmlConverterCoreNodeRenderer must be imported by some reason (TODO why?)
import data.yaml.GidHtmlConverterCoreNodeRendererClass
import data.yaml.GidHtmlConverterCoreNodeRenderer

import data.RemoteDebugSendClass
import data.RemoteDebugSend

import groovy.util.logging.Log4j

import static data.yaml.GidHtmlConverterCoreNodeRenderer.escapeMarkdown

import data.JiraHelpersClass
import data.JiraHelpers
import static data.JiraHelpers.formatIssueMdLink
import static data.JiraHelpers.getIssueDescriptionInHTML
import static data.JiraHelpers.renderJiraMarkupToHTML
import static data.JiraHelpers.getEventType

import com.atlassian.jira.issue.Issue
import com.atlassian.jira.event.issue.IssueEvent
import com.atlassian.jira.event.type.EventType

@Grapes([
    @Grab(group='org.slf4j', module='slf4j-api', version='2.0.13'), // version 1.7.28 from dependencies did not found!
    @Grab(group='ru.mail.im', module='bot-api', version='1.2.3'),
    // Unspecified in bot deps, but required at runtime:
    @Grab(group='com.google.code.gson', module='gson', version='2.11.0'),
    @Grab(group='com.squareup.okhttp3', module='okhttp', version='4.12.0'),
    // Yaml conversion:
    @Grab(group='com.vladsch.flexmark', module='flexmark-html2md-converter', version='0.64.8')
])
import ru.mail.im.botapi.BotApiClient
import ru.mail.im.botapi.BotApiClientController
import ru.mail.im.botapi.api.entity.SendTextRequest
import ru.mail.im.botapi.response.MessageResponse

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import com.vladsch.flexmark.util.data.MutableDataSet

/**
* Implement minimal GidMost client to handle HTML test, markdown conversion and so on
* @author Pavel Alexeeev <plalexeev@gid.ru>
**/
@Log4j
class GidMostBot {
    public final static String BOT_BASE_URL = 'https://api.most.gid.ru/'

    private String botToken
    private String chatId

    GidMostBot(String botToken, String chatId){
        this.botToken = botToken
        this.chatId   = chatId
    }

    static String formatMdHeaderCreate(IssueEvent event){
        """*Новый алерт ${formatIssueMdLink(event.issue)}*: «__${escapeMarkdown(event.issue.summary)}__»
_Labels_: ${event.issue.labels.collect{"`${escapeMarkdown(it as String)}`"}.join(', ')}
--------------------
"""
    }

    static String formatMdHeaderChange(IssueEvent event){
        """*Изменение алерта ${formatIssueMdLink(event.issue)}*: «__${escapeMarkdown(event.issue.summary)}__»
_Изменения_ (${getEventType(event)?.name}) от ${event?.user?.displayName}"""
    }

    static String formatMdIssueChangeDetails(IssueEvent event){
        """:
${// By https://stackoverflow.com/questions/36668135/how-to-get-a-list-of-modified-fields-with-scriptrunner-on-issueupdated-event/36729350#36729350
    event.getChangeLog()?.getRelated('ChildChangeItem')?.collect { change ->
        log.warn("Issue change: ${change}")
        "- __${escapeMarkdown(change['field'] as String)}__: «${escapeMarkdown(change['oldstring'] as String)?.trim()}» ➫ «${escapeMarkdown(change['newstring'] as String)?.trim()}»"
    }?.join('\n')
}"""
    }

    static String formatMdCommentChange(IssueEvent event, String header='Новый комментарий'){
        """*${header} от _${event?.user?.displayName}_ в ${formatIssueMdLink(event.issue)}*: «__${escapeMarkdown(event.issue.summary)}__»:
"""
    }

    static String formatMdHeaderDelete(IssueEvent event){
        """*Удалён алерт ${formatIssueMdLink(event.issue)}*: «__${escapeMarkdown(event.issue.summary)}__»
_Labels_: ${event.issue.labels.collect{"`${escapeMarkdown(it as String)}`"}.join(', ')}
--------------------
"""
    }

    /**
    * Main horse: process various event changes.
    * @param event
    * @param bundle
    **/
    def processIssueChanges(IssueEvent event, def bundle){
        log.warn("Process issue [${event.issue}] event: ${event} with type [${getEventType(event)}]")
//        log.warn("Events bundle:\n[${RemoteDebugSend.toYAML(bundle, true)}]") // TODO why not work?
        bundle.events.each{ includedEvent ->
            log.warn("\tBundle event: ${event} with type [${getEventType(event)}]")
        } as List<IssueEvent>

        //noinspection GroovyFallthrough
        switch (event.getEventTypeId()){ // https://docs.atlassian.com/software/jira/docs/api/7.0.8/com/atlassian/jira/event/type/EventType.html
            case EventType.ISSUE_CREATED_ID:
                sendMarkdownTextWithFallback(
                    formatMdHeaderCreate(event),
                    convertHtmlToMostMarkdown(getIssueDescriptionInHTML(event.issue)),
                    '*Error convert issue description for GidMost*'
                )
                break

            case EventType.ISSUE_UPDATED_ID:
            case EventType.ISSUE_ASSIGNED_ID:
            case EventType.ISSUE_RESOLVED_ID:
            case EventType.ISSUE_CLOSED_ID:
            case EventType.ISSUE_REOPENED_ID:
            case EventType.ISSUE_MOVED_ID:
            case EventType.ISSUE_ARCHIVED_ID:
            case EventType.ISSUE_GENERICEVENT_ID:
                sendMarkdownTextWithFallback(
                    formatMdHeaderChange(event),
                    formatMdIssueChangeDetails(event),
                    '*Error process changes of issue in rich format for GidMost*'
                )
                break

            case EventType.ISSUE_DELETED_ID:
                sendMarkdownTextWithFallback(
                    formatMdHeaderDelete(event),
                    convertHtmlToMostMarkdown(getIssueDescriptionInHTML(event.issue)),
                    '*Error convert issue description for GidMost*'
                )
                break

            case EventType.ISSUE_COMMENTED_ID:
                String comment = renderJiraMarkupToHTML(event?.comment?.body).trim()
                if (comment) {
                    log.warn("Issue comment HTML: ${comment}")
                    sendMarkdownTextWithFallback(
                        formatMdCommentChange(event, 'Добавлен комментарий'),
                        convertHtmlToMostMarkdown(renderJiraMarkupToHTML(event?.comment?.body)),
                        '*Error convert comment to rich format for GidMost*'
                    )
                }
                else {
                    /**
                    * Due to the JIRA bugs: https://jira.atlassian.com/browse/JRASERVER-62351 and https://jira.atlassian.com/browse/JRASERVER-59999 IssueComment is not triggered automatically.
                    * Such manual event is workaround (we will ignore empty comments in listener).
                    **/
                    log.warn('Issue comment is empty, skipping')
                }
                break
            case EventType.ISSUE_COMMENT_DELETED_ID:
                sendMarkdownTextWithFallback(
                    formatMdCommentChange(event, 'Комментарий удалён'),
                    convertHtmlToMostMarkdown(renderJiraMarkupToHTML(event?.comment?.body)),
                    '*Error convert comment to rich format for GidMost*'
                )
                break
            case EventType.ISSUE_COMMENT_EDITED_ID:
                log.warn("Issue comment HTML: ${renderJiraMarkupToHTML(event?.comment?.body)}")
                sendMarkdownTextWithFallback(
                    formatMdCommentChange(event, 'Комментарий отредактирован'),
                    convertHtmlToMostMarkdown(renderJiraMarkupToHTML(event?.comment?.body)),
                    '*Error convert comment to rich format for GidMost*'
                )
                break

            default:
                sendMarkdownTextWithFallback("Unsupported event type [${escapeMarkdown(getEventType(event) as String)}]", "Unsupported event")
                throw new IllegalArgumentException("Unsupported event: ${event}")
        }
    }

    static String convertHtmlToMostMarkdown(String html){
        MutableDataSet mdOptions = new MutableDataSet()
        mdOptions.set(FlexmarkHtmlConverter.THEMATIC_BREAK, '--------------------')
        mdOptions.set(FlexmarkHtmlConverter.UNORDERED_LIST_DELIMITER, (char)'-')
        mdOptions.set(FlexmarkHtmlConverter.SKIP_CHAR_ESCAPE, true)

        // Prepare HTML from JIRA. Sometimes {{ }} does not converted in any code, we want inline it
        html = html.replaceAll(/\{\{/, '<ins>').replaceAll(/}}/, '</ins>')
        html = escapeMarkdown(html)

        FlexmarkHtmlConverter.Builder builder = FlexmarkHtmlConverter.builder(mdOptions)
        builder.htmlNodeRendererFactory(new GidHtmlConverterCoreNodeRendererFactory())
        FlexmarkHtmlConverter converter = builder.build()
        return converter.convert(html).trim()
    }

    /**
    * Wrapper around {@see #sendMarkdownText}. GidMost sending is very fragile about provided data forms. And you may easy get "Format error" responce.
    * So, in that case second attempt performed to send only fallback value.
    * @see #sendMarkdownText
    *
    * @param markdownDesired Markdown to send. Most probably that converted comment or description from the issue. If some conversion performed incorrectly blind "Format error" happened
    * @param markdownMinimal Fallback markdown. Please provide there minimal formating and ensure it is correct!
    * @return MessageResponse
    **/
    MessageResponse sendMarkdownTextWithFallback(String markdownDesired, String markdownMinimal){
        MessageResponse res = sendMarkdownText(markdownDesired)
        if (!res.ok){
            // Fallback to send just event, without issue description
            res = sendMarkdownText(markdownMinimal)
        }
        return res
    }

    /**
    * Handy wrapper to use single header instead of call like:
    * <code>
    * String header = formatMdCommentChange(event, 'Комментарий отредактирован'),
    * sendMarkdownTextWithFallback(
    *   header + convertHtmlToMostMarkdown(renderJiraMarkupToHTML(event?.comment?.body)),
    *   header + '*Error convert comment to rich format for GidMost*'
    * )
    * </code>
    * @param markdownHeader
    * @param markdownDesiredAddon
    * @param markdownMinimalAddon
    * @return message sent result
    **/
    MessageResponse sendMarkdownTextWithFallback(String markdownHeader, String markdownDesiredAddon, String markdownMinimalAddon){
        return sendMarkdownTextWithFallback(markdownHeader + markdownDesiredAddon, markdownHeader + markdownMinimalAddon)
    }

    /**
    * Send markdown text to chat.
    * No need to maintain the session - we connect, send message and then perform disconnection
    * @see #sendMarkdownTextWithFallback
    * @param markdown text to send
    * @return MessageResponse object. Please check at least `ok` field of that for the result. Really sending is very fragile and you frequently may got "Format error"
    **/
    MessageResponse sendMarkdownText(String markdown){
        log.warn("Sending markdown: \n${markdown}")
        BotApiClient client = new BotApiClient(BOT_BASE_URL, botToken)
        BotApiClientController controller = BotApiClientController.startBot(client)

        MessageResponse resp = controller.sendTextMessage(
            new SendTextRequest()
              .setChatId(chatId)
              .setParseMode('MarkdownV2').setText(markdown)
        )

        if (!resp.ok){
            log.error("GidMost failed to send message: ${resp.description}")
        }
        client.stop() // stop when work done
        return resp
    }
}

''