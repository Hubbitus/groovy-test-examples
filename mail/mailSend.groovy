//http://www.brookstevens.org/2009/09/sending-email-using-groovy.html

import javax.mail.internet.*;
import javax.mail.*
import javax.activation.*

message = "test groovy mail"
subject = "groovy sent this"
toAddress = "pahan@hubbitus.info"
fromAddress = "p.alexeev@spb.ant-inform.ru"
host = "mail.spb.ant-inform.ru"
port = "25"

Properties mprops = new Properties();
mprops.setProperty("mail.transport.protocol","smtp");
mprops.setProperty("mail.host",host);
mprops.setProperty("mail.smtp.port",port);

Session lSession = Session.getDefaultInstance(mprops,null);
MimeMessage msg = new MimeMessage(lSession);


//tokenize out the recipients in case they came in as a list
StringTokenizer tok = new StringTokenizer(toAddress,";");
ArrayList emailTos = new ArrayList();
while(tok.hasMoreElements()){
emailTos.add(new InternetAddress(tok.nextElement().toString()));
}
InternetAddress[] to = new InternetAddress[emailTos.size()];
to = (InternetAddress[]) emailTos.toArray(to);
msg.setRecipients(MimeMessage.RecipientType.TO,to);
InternetAddress fromAddr = new InternetAddress(fromAddress);
msg.setFrom(fromAddr);
msg.setFrom(new InternetAddress(fromAddress));
msg.setSubject(subject);
msg.setText(message)

Transport transporter = lSession.getTransport("smtp");
transporter.connect();
transporter.send(msg);

println ("Done")
