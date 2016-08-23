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
import snap.SnapException;

public class Mailer
{
  final Logger log = LoggerFactory.getLogger(Mailer.class);

  private Session mSession;
  private Transport mTransport;

  /**
   * This is builder is used to create messages which use Rythm templates as the
   * body. It automatically generates multipart email for both HTML and plain
   * text viewing.
   *
   * In snap.properties you should specify the "snap.mail.templatepath" to set
   * the email path. This template path is relative to the webroot.
   */
  public class MessageBuilder
  {
    final Logger log = LoggerFactory.getLogger(MessageBuilder.class);

    MessageBuilder()
    {
      recipients = new ArrayList<>();
    }

    /**
     * Set the filename to use as the template for this message. The mailer will
     * append ".html" and ".txt" to the file name and expects both files to be
     * present.
     *
     * @param templateName
     *          the filename relative to the "snap.mail.templatepath" property
     * @return This builder for chaining calls.
     */
    public MessageBuilder setTemplateName(String templateName)
    {
      this.templateName = templateName;
      return this;
    }

    /**
     * Set the sender. Most mail servers require that the From is the same as
     * the login user. (See property mail.smtp.user) However if your mail server
     * supports anonymous sending then you can enter anything here.
     *
     * @param address
     *          The from email address: "name@example.com" or
     *          "name &lt;name@example.com&gt;"
     * @return This builder for chaining calls.
     * @throws AddressException
     *           When the address is invalid
     */
    public MessageBuilder setFrom(String address) throws AddressException
    {
      from = createAddress(address, null);
      return this;
    }

    /**
     * Set the sender. Most mail servers require that the From is the same as
     * the login user. (See property mail.smtp.user) However if your mail server
     * supports anonymous sending then you can enter anything here.
     *
     * @param address
     *          The from email address "name@example.com"
     * @param name
     *          The name. May be null. combined with address to form
     *          "name &lt;name@example.com&gt;"
     * @return This builder for call chaining
     * @throws AddressException
     *           if the address was invalid
     */
    public MessageBuilder setFrom(String address, String name) throws AddressException
    {

      from = createAddress(address, name);

      return this;
    }

    /**
     * Adds a recipient to the list. If the name parameter is specified as
     * "John Doe" then the recipient will be in the form of
     * "John Doe &lt;name@example.com&gt;"
     *
     * @param address
     *          The internet email address in the form of "name@example.com" or
     *          "name &lt;name@example.com&gt;" if name is specified then you
     *          should use rthe first address form.
     * @param name
     *          The name of the recipient. May be null. Combined with address to
     *          form "name &lt;name@example.com&gt;"
     * @return This builder for chaining calls.
     * @throws AddressException
     *           Is thrown when the address is invalid.
     */
    public MessageBuilder addRecipient(String address, String name) throws AddressException
    {
      recipients.add(createAddress(address, name));
      return this;
    }

    /**
     * Adds a list of recipients.
     *
     * @param recipients
     *          The recpients.
     * @return This builder for chaining calls.
     */
    public MessageBuilder addRecipients(List<InternetAddress> recipients)
    {
      this.recipients.addAll(recipients);
      return this;
    }

    /**
     * Convenience function to create an InternetAddress. Can be used with
     * addRecipients
     *
     * @param address
     *          The address: "name@example.com"
     * @param name
     *          The name to combine with address or null. name
     *          <name@example.com>
     * @return The internet address
     * @throws AddressException
     *           throws when address is invalid
     */
    private InternetAddress createAddress(String address, String name) throws AddressException
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

        log.error("Invalid recipient address: " + name + " <" + address + ">", e);
        throw e;
      }
      catch (UnsupportedEncodingException e)
      {
        log.error("JVM doesn't support encoding", e);
        return null;
      }
    }

    /**
     * Get the list of recipients as an array
     *
     * @return The recipients
     */
    public Address[] getRecipients()
    {
      Address[] address = new InternetAddress[recipients.size()];
      return this.recipients.toArray(address);
    }

    /**
     * The the message subject
     *
     * @param subject
     *          The subject line. Officially 7-bit ascii only.
     * @return This builder for chaining calls
     */
    public MessageBuilder setSubject(String subject)
    {
      this.subject = subject;
      return this;
    }

    /**
     * Build the message with the Rythm context for the template that will be
     * used to generate this email.
     *
     * @param context
     *          The Rythm parameter list context.
     * @return The built message to pass to Mailer.
     * @throws MessagingException
     *           if an error occurred
     */
    public MimeMessage build(Map<String, Object> context) throws MessagingException
    {
      try
      {
        MimeMessage msg = new MimeMessage(mSession);

        Multipart multiPart = new MimeMultipart("alternative");

        // Set Text part of message
        MimeBodyPart textPart = new MimeBodyPart();
        // TODO: check if value is present

        File txtFile = new File(
            Settings.rootPath + "/" + Settings.emailTemplatePath + "/" + templateName + ".txt");
        if (!txtFile.exists())
        {
          throw new SnapException("Missing template file: " + txtFile);
        }
        String text = Rythm.render(txtFile, context);
        textPart.setText(text, "UTF-8");

        // Set html Part
        MimeBodyPart htmlPart = new MimeBodyPart();
        File htmlFile = new File(
            Settings.rootPath + "/" + Settings.emailTemplatePath + "/" + templateName + ".html");
        if (!htmlFile.exists())
        {
          throw new SnapException("Missing template file: " + htmlFile);
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
        log.error("Can't create message: " + from.getAddress() + " :: " + subject, e);
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

  /**
   * Get a Builder to construct a message
   *
   * @return
   */
  public MessageBuilder getMessageBuilder()
  {
    return new MessageBuilder();
  }

  /**
   * Opens the connection to the mail transport and creates a session. This
   * normally means connecting to an SMTP server. Should be paired with a
   * close() call.
   *
   * @throws MessagingException
   *           When a connection can't be made
   */
  public void open() throws MessagingException
  {
    mTransport = mSession.getTransport("smtp");
    mTransport.connect(mSession.getProperty("mail.smtp.user"), mSession.getProperty("mail.smtp.password"));
  }

  /**
   * Send a message.
   *
   * @param msg
   *          The message to send.
   * @throws MessagingException
   *           Thrown when the message can't be sent.
   */
  public void send(Message msg) throws MessagingException
  {
    if (mTransport == null)
      throw new IllegalStateException("You must call open() to create a transport.");
    if (!mTransport.isConnected())
      throw new IllegalStateException("Mail transport created but not open. Can't send");

    mTransport.sendMessage(msg, msg.getAllRecipients());
  }

  /**
   * Close the transport connection.
   */
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
