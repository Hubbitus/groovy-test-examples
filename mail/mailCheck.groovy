/**
* Got from: http://pleac.sourceforge.net/pleac_groovy/internetservices.html#AEN990
*/

import org.apache.commons.net.pop3.POP3Client
server = '192.168.100.202'
username = 'testesb_pasha@spb.ant-inform.ru'
password = 'qwerty12'
timeoutMillis = 30000

def printMessageInfo(reader, id) {
    def from, subject
    reader.eachLine{ line ->
        lower = line.toLowerCase()
        if (lower.startsWith("from: ")) from = line[6..-1].trim()
        else if (lower.startsWith("subject: ")) subject = line[9..-1].trim()
    }
    println "$id From: $from, Subject: $subject"
}

pop3 = new POP3Client()
pop3.setDefaultTimeout(timeoutMillis)
pop3.connect(server)

if (!pop3.login(username, password)) {
    System.err.println("Could not login to server. Check password.")
    pop3.disconnect()
    System.exit(1)
}
messages = pop3.listMessages()
if (!messages) System.err.println("Could not retrieve message list.")
else if (messages.length == 0) println("No messages")
else {
    messages.each{ message ->
        reader = pop3.retrieveMessageTop(message.number, 0)
        if (!reader) {
            System.err.println("Could not retrieve message header. Skipping...")
        }
        printMessageInfo(new BufferedReader(reader), message.number)
    }
}

pop3.logout()
pop3.disconnect()
