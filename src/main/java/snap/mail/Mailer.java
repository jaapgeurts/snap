package snap.mail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.rythmengine.Rythm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snap.Settings;

public class Mailer
{
  final Logger log = LoggerFactory.getLogger(Mailer.class);

  private Session mSession;
  private Transport mTransport;

  public class MessageBuilder
  {
    final Logger log = LoggerFactory.getLogger(MessageBuilder.class);

    MessageBuilder()
    {
      recipients = new ArrayList<>();
    }

    public MessageBuilder setTemplateName(String templateName)
    {
      this.templateName = templateName;
      return this;
    }

    public MessageBuilder setFrom(String address) throws AddressException
    {
      from = createAddress(address, null);
      return this;
    }

    public MessageBuilder setFrom(String name, String address)
        throws AddressException
    {

      from = createAddress(address, name);

      return this;
    }

    public MessageBuilder addRecipient(String address) throws AddressException
    {
      recipients.add(createAddress(address, null));
      return this;
    }

    public MessageBuilder addRecipient(String address, String name)
        throws AddressException
    {
      recipients.add(createAddress(address, name));
      return this;
    }

    public MessageBuilder addRecipients(List<InternetAddress> recipients)
    {
      this.recipients.addAll(recipients);
      return this;
    }

    private InternetAddress createAddress(String address, String name)
        throws AddressException
    {
      try
      {
        InternetAddress inetAddress;
        if (name == null)
          inetAddress = new InternetAddress(address);
        else
          inetAddress = new InternetAddress(address, name);
        return inetAddress;
      }
      catch (AddressException e)
      {

        log.error("Invalid recipient address: " + name + " <" + address + ">",
            e);
        throw e;
      }
      catch (UnsupportedEncodingException e)
      {
        log.error("JVM doesn't support encoding", e);
        return null;
      }
    }

    public Address[] getRecipients()
    {
      Address[] address = new InternetAddress[recipients.size()];
      return this.recipients.toArray(address);
    }

    public MessageBuilder setSubject(String subject)
    {
      this.subject = subject;
      return this;
    }

    public MimeMessage build(Map<String, Object> context)
        throws MessagingException
    {
      try
      {
        MimeMessage msg = new MimeMessage(mSession);

        Multipart multiPart = new MimeMultipart("alternative");

        // Set Text part of message
        MimeBodyPart textPart = new MimeBodyPart();
        // TODO: check if value is present

        File txtFile = new File(Settings.rootPath + "/"
            + Settings.emailTemplatePath + "/" + templateName + ".txt");
        if (!txtFile.exists())
        {
          log.error("Missing template file: " + txtFile);
          return null;
        }
        String text = Rythm.render(txtFile, context);
        textPart.setText(text, "UTF-8");

        // Set html Part
        MimeBodyPart htmlPart = new MimeBodyPart();
        File htmlFile = new File(Settings.rootPath + "/"
            + Settings.emailTemplatePath + "/" + templateName + ".html");
        if (!htmlFile.exists())
        {
          log.error("Missing template file: " + htmlFile);
          return null;
        }
        String html = Rythm.render(htmlFile, context);
        htmlPart.setContent(html, "text/html; charset=utf-8");

        // add the parts to the mail
        multiPart.addBodyPart(textPart);
        multiPart.addBodyPart(htmlPart);
        msg.setContent(multiPart);

        // set the message delivery parameters

        msg.setFrom(from);
        msg.setRecipients(Message.RecipientType.TO, getRecipients());
        msg.setSubject(subject);
        msg.setSentDate(new Date());

        return msg;

      }
      catch (MessagingException e)
      {
        log.error("Can't create message: " + from.getAddress() + " :: "
            + subject, e);
        throw e;
      }
    }

    private List<InternetAddress> recipients;

    private String subject;

    private String templateName;

    private InternetAddress from;
  }

  public Mailer()
  {
    mSession = Session.getInstance(Settings.asProperties(), null);
  }

  public MessageBuilder getBuilder()
  {
    return new MessageBuilder();
  }

  public void open() throws MessagingException
  {
    mTransport = mSession.getTransport("smtp");
    mTransport.connect();
  }

  /**
   * Send messages stored previously. If you do not wish to receive error
   * reports, set e1 to null
   * 
   * @param el
   * @throws MessagingException
   */
  public void send(Message msg) throws MessagingException
  {
    if (!mTransport.isConnected())
      throw new IllegalStateException("Mail connection not open. Can't send");

    mTransport.sendMessage(msg, msg.getAllRecipients());
  }

  public void close()
  {
    try
    {
      mTransport.close();
    }
    catch (MessagingException e)
    {
      log.error("Can't close SMTP transport", e);
    }
  }

}
