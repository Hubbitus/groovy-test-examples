package data

import com.atlassian.jira.issue.fields.NavigableField
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.fields.FieldManager
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.event.issue.IssueEvent

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.config.properties.APKeys

import com.atlassian.jira.issue.RendererManager
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext


import com.atlassian.jira.event.type.EventType

/**
* Various helpful methods to deal with JIRa issues like description formats conversion
* @author Pavel Alexeeev <plalexeev@gid.ru>
**/
class JiraHelpers {
    /**
    * Convert issue description to HTML
    *
    * @link By https://community.atlassian.com/t5/Jira-questions/Get-description-field-of-a-JIRA-issue-as-HTML-using-Groovy/qaq-p/861859
    * @param issue
    * @return Description rendered in HTML
    **/
    public static String getIssueDescriptionInHTML(Issue issue){
        def fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(issue).getFieldLayoutItem("description")
        NavigableField navigableField = (NavigableField)ComponentAccessor.getFieldManager().getField("description")
        return navigableField.getColumnViewHtml(fieldLayoutItem, new HashMap(), issue)
    }

    /**
     * Render WIKI JIRA format into HTML
     * @link by https://stackoverflow.com/questions/70334647/convert-plain-text-wiki-syntax-to-html-with-jira-scriptrunner-show-bullet-poi/70368183#70368183
     * @param jiraMarkup
     * @return HTML markup in string
     */
    public static String renderJiraMarkupToHTML(String jiraMarkup){
        RendererManager rendererManager = ComponentAccessor.getComponentOfType(RendererManager.class)
        JiraRendererPlugin renderer = rendererManager.getRendererForType("atlassian-wiki-renderer");
        return renderer.render(jiraMarkup, null)
    }

    public static String getJiraBaseUrl(){
        ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL)
    }

    /**
    * Return URL to issue in Jira
    * @param issue
    **/
    public static String formatIssueLink(Issue issue){
        "${getJiraBaseUrl()}/browse/${issue.key}"
    }

    /**
    * Format as Markdown issue link
    * @param issue
    **/
    public static String formatIssueMdLink(Issue issue){
        "[${issue.key}](${formatIssueLink(issue)})"
    }

    /**
    * @link by https://community.atlassian.com/t5/Jira-questions/Getting-an-Event-Name-via-Groovy/qaq-p/1118632
    * @param issue
    **/
    public static EventType getEventType(IssueEvent event){
        return ComponentAccessor.getEventTypeManager().getEventTypes().find{ it.id == event.getEventTypeId() }
    }
}

''
