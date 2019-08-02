/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Created on: 26th March 2012
 */
package notifiers

import org.apache.commons.mail._

import java.util.concurrent.Future
import java.lang.reflect._
import javax.mail.internet.InternetAddress

import scala.collection.JavaConversions._

import play.api._
import play.api.Configuration._

import views._

import javax.inject.Inject

/**
 * this class providers a wrapper for sending email in Play! 2.0
 * based on the EmailNotifier trait by Aishwarya Singhal
 * 
 * @author Justin Long
 * 
 * make sure to include Apache Commons Mail in dependencies
 * "org.apache.commons" % "commons-mail" % "1.2"
 */
trait Mailloy {
  
  var notifications = new ThreadLocal[MailloyContext]
  
  val conf = play.api.Play.current.configuration
  val smtpHost = conf.get[String]("smtp.host")
  val smtpPort = conf.get[Int]("smtp.port")
  val smtpSsl  = conf.get[Boolean]("smtp.ssl")
  val smtpUser = conf.get[String]("smtp.user")
  val smtpPass = conf.get[String]("smtp.pass")

  /**
   * Sets a subject for this email. It enables formatting of the providing string using Java's
   * string formatter.
   *
   * @param subject
   * @param args
   */
  def setSubject(subject: String, args: AnyRef*) = {
    current.subject = String.format(subject, args: _*)
  }
  
  /**
   * Defines the sender of this email("from" address).
   *
   * @param from
   */
  def addFrom(from: String) = {
    current.from = from
  }

  /**
   * Adds an email recipient in CC.
   *
   * @param ccRecipients
   */
  def addCc(ccRecipients: String*) = {
    current.ccRecipients :::= List(ccRecipients: _*)
  }

  /**
   * Adds an email recipient in BCC.
   *
   * @param bccRecipients
   */
  def addBcc(bccRecipients: String*) = {
    current.bccRecipients :::= List(bccRecipients: _*)
  }
  
  /**
   * Adds an email recipient ("to" addressee).
   *
   * @param recipients
   */
  def addRecipient(recipients: String*) = {
    current.recipients :::= List(recipients: _*)
  }
  
  /**
   * Defines the "reply to" email address.
   *
   * @param replyTo
   */
  def setReplyTo(replyTo: String) = {
    current.replyTo = replyTo
  }
  
  /**
   * Sets the charset for this email.
   *
   * @param charset
   */
  def setCharset(charset: String) = {
    current.charset = charset
  }

  /**
   * Sets the content type for the email. If none is set, by default it is assumed to be "UTF-8".
   * @param contentType
   */
  def setContentType(contentType: String) = {
    current.contentType = contentType
  }
  
  /**
   * Adds a request header to this email message.
   *
   * @param key
   * @param value
   */
  def addHeader(key: String, value: String) = {
    current.headers += (key -> value)
  }
  
  /**
   * Sends an email based on the provided data. It also validates and ensures completeness of
   * this object before attempting a send.
   *
   * @param bodyText : pass a string or use a Play! text template to generate the template
   *  like view.Mails.templateText(tags).
   * @param bodyHtml : pass a string or use a Play! HTML template to generate the template 
   * like view.Mails.templateHtml(tags).
   * @return
   */
  def send(bodyText: String, bodyHtml: String) = {

    // Content type
    ensureContentTypeDefined(bodyHtml)

    var email: MultiPartEmail = getEmail(bodyText, bodyHtml)

		email.setCharset(current.charset)
		
		setAddress(current.from) { (address, name) => email.setFrom(address, name) }
		setAddress(current.replyTo) { (address, name) => email.addReplyTo(address, name) }
		current.recipients.foreach(setAddress(_) { (address, name) => email.addTo(address, name) })
		current.ccRecipients.foreach(setAddress(_) { (address, name) => email.addCc(address, name) })
		current.bccRecipients.foreach(setAddress(_) { (address, name) => email.addBcc(address, name) })
		
		email.setSubject(current.subject)
		current.headers foreach ((entry) => email.addHeader(entry._1, entry._2))
		
		// do the work to prepare sending on SMTP
		email.setHostName(smtpHost);
		email.setSmtpPort(smtpPort);
		//email.setSSL(smtpSsl)
        email.setSSLOnConnect(smtpSsl)
		email.setAuthenticator(new DefaultAuthenticator(smtpUser, smtpPass));
		email.setDebug(false);
      
    email.send
		
		// now flush the stored context and send
		notifications.remove
  }
  
  /**
   * Extracts an email address from the given string and passes to the enclosed method.
   *
   * @param emailAddress
   * @param setter
   */
  private def setAddress(emailAddress: String)(setter: (String, String) => Unit) = {

    if (emailAddress != null) {
      try {
        val iAddress = new InternetAddress(emailAddress);
        val address = iAddress.getAddress()
        val name = iAddress.getPersonal()

        setter(address, name)
      } catch {
        case e: Exception =>
          setter(emailAddress, null)
      }
    }
  }

  /**
   * Creates an appropriate email object based on the content type.
   *
   * @param bodyText
   * @param bodyHtml
   * @return
   */
  private def getEmail(bodyText: String, bodyHtml: String): MultiPartEmail = {
    var email: MultiPartEmail = null
    if (bodyHtml == null) {
      email = new MultiPartEmail();
      email.setMsg(bodyText);
    } else {
      email = new HtmlEmail();
      email.asInstanceOf[HtmlEmail].setHtmlMsg(bodyHtml);
      if (bodyText != null) {
        email.asInstanceOf[HtmlEmail].setTextMsg(bodyText);
      }
    }
    return email
  }

  /**
   * Sets a content type if none is defined.
   *
   * @param bodyHtml
   */
  private def ensureContentTypeDefined(bodyHtml: String) = {
    if (current.contentType == null) {
      if (bodyHtml != null) {
        current.contentType = "text/html";
      } else {
        current.contentType = "text/plain";
      }
    }
  }

  /**
   * Gets the current notification context as stored.
   * @return
   */
  private def current = {
    var notification = notifications.get
    if (notification == null) {
      notification = new MailloyContext
      notifications.set(notification)
    }

    notification
  }

}

class MailloyContext {

  var subject: String = null
  var recipients: List[String] = List[String]()
  var from: String = null
  var ccRecipients: List[String] = List[String]()
  var bccRecipients: List[String] = List[String]()
  var contentType: String = null
  var replyTo: String = null
  var charset: String = "utf-8"
  var headers: Map[String, String] = Map[String, String]()

}