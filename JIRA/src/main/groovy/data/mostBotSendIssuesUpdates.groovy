package data

import data.GidMostBotClass
import com.onresolve.scriptrunner.parameters.annotation.ShortTextInput

/**
* Script to send creation, update, comments of issues into GidMost (VkTeams) channels
* @author Pavel Alexeeev <plalexeev@gid.ru>
**/

@ShortTextInput(label='CHAT_BOT_TOKEN', description='Enter GidMost (VkTeams) chat-bot secret token (password)')
String CHAT_BOT_TOKEN
@ShortTextInput(label='CHAT_ID', description='Enter GidMost (VkTeams) chat ID (last part from the URL after /)')
String CHAT_ID

log.warn("CHAT_ID=[${CHAT_ID}]")

GidMostBot bot = new GidMostBot(CHAT_BOT_TOKEN, CHAT_ID)
bot.processIssueChanges(event, bundle)
