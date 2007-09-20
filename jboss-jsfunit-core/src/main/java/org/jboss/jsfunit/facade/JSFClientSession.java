/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.jsfunit.facade;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.SubmitButton;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebLink;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import org.jboss.jsfunit.framework.WebConversationFactory;
import org.xml.sax.SAXException;

/**
 * The JSFClientSession provides a simplified API that wraps HttpUnit.  With a
 * single JSFClientSession object, you can simulate an entire user session as you
 * set parameters and submit data using submit() and clickCommandLink() methods.
 * 
 * 
 * @author Stan Silvert
 */
public class JSFClientSession
{
   private WebConversation webConversation;
   private WebResponse webResponse;
   private ClientIDs clientIDs;
   private WebRequestFactory requestFactory;
   
   /**
    * Creates a new client interface for testing the JSF application.   
    * This will also clear the HttpSession.
    * 
    * Note that the initialPage param should be something that maps into the FacesServlet.
    * In the case where the FacesServlet is extension mapped in web.xml, this param will be something
    * like "/index.jsf" or "/index.faces".  If the FacesServlet is path-mapped then the
    * initialPage param will be something like "/faces/index.jsp".
    * 
    * @param initialPage The page used to start a client session with JSF.  Example: "/index.jsf"
    *
    * @throws MalformedURLException If the initialPage cannot be used to create a URL for the JSF app
    * @throws IOException If there is an error calling the JSF app
    * @throws SAXException If the response from the JSF app cannot be parsed as HTML
    */
   public JSFClientSession(String initialPage) throws MalformedURLException, IOException, SAXException
   {
      this.webConversation = WebConversationFactory.makeWebConversation();
      doInitialRequest(initialPage);
   }
   
   /**
    * Creates a new client interface for testing a JSF application using a
    * customized WebConversation.  To use this constructor, first get a
    * WebConversation from org.jboss.jsfunit.framework.WebConversationFactory.
    * 
    * Example:
    * <code>
    * WebConversation webConv = WebConversationFactory.makeWebConversation();
    * webConv.setAuthorization("myuser", "mypassword");
    * webConv.setHeaderField("Accept-Language", "es-mx,es"); 
    * JSFClientSession client = new JSFClientSession(webConv, "/index.jsf");
    * </code>
    * 
    * Note that the initialPage param should be something that maps into the FacesServlet.
    * In the case where the FacesServlet is extension mapped in web.xml, this param will be something
    * like "/index.jsf" or "/index.faces".  If the FacesServlet is path-mapped then the
    * initialPage param will be something like "/faces/index.jsp".
    * 
    * 
    * @param webConversation A WebConversation object with "custom" attributes.
    * @param initialPage The page used to start a client session with JSF.  Example: "/index.jsf"
    * @throws IllegalArgumentException if the WebConversation did not come from the
    *                                  WebConversationFactory.
    * @throws MalformedURLException If the initialPage cannot be used to create a URL for the JSF app
    * @throws IOException If there is an error calling the JSF app
    * @throws SAXException If the response from the JSF app cannot be parsed as HTML
    */
   public JSFClientSession(WebConversation webConversation, String initialPage) 
        throws MalformedURLException, IOException, SAXException
   {
      if (!WebConversationFactory.isJSFUnitWebConversation(webConversation))
      {
          throw new IllegalArgumentException("WebConversation was not created with WebConversationFactory.");
      }
      
      this.webConversation = webConversation;
      doInitialRequest(initialPage);
   }
   
   // common code for constructors
   private void doInitialRequest(String initialPage)
           throws MalformedURLException, IOException, SAXException
   {
      WebRequest req = new GetMethodWebRequest(WebConversationFactory.getWARURL() + initialPage);
      doWebRequest(req);
      this.requestFactory = new WebRequestFactory(this);
   }
   
   /**
    * Package-private method used by ServerFacade
    */
   ClientIDs getClientIDs()
   {
      return this.clientIDs;
   }
   
   /**
    * Package-private method to get the WebForm that contains the given 
    * component.
    *
    * @param componentID The ID of the component contained by the form, or the
    *                    ID of the form itself.
    *
    * @return The WebForm.
    *
    * @throws SAXException if the current response page can not be parsed
    * @throws ComponentIDNotFoundException if the component can not be found
    * @throws DuplicateClientIDException if more than one client ID matches the 
    *                                    componentID suffix
    * @throws FormNotFoundException if no form parameter can be found matching 
    *                               the componentID
    */
   WebForm getForm(String componentID) throws SAXException
   {
      String clientID = this.clientIDs.findClientID(componentID);
      WebForm[] forms = getWebResponse().getForms();
      if (forms.length == 0) throw new FormNotFoundException(componentID);
      
      for (int i=0; i < forms.length; i++)
      {
         if (clientID.startsWith(forms[i].getID())) return forms[i];
      }
      
      throw new FormNotFoundException(componentID);
   } 
   
   /**
    * The method submits the WebRequest to the server using the WebConversation
    * of this JSFClientSession instance.  
    * 
    * At the end of this method, a new view from the server will be loaded so 
    * that you can continue to use this JSFClientSession instance to make further 
    * requests.
    * 
    * 
    * @param request The WebRequest
    * @throws IOException If there is an error calling the JSF app
    * @throws SAXException If the response from the JSF app cannot be parsed as 
    *                      HTML
    */
   public void doWebRequest(WebRequest request) throws SAXException, IOException
   {
      this.webResponse = this.webConversation.getResponse(request);
      this.clientIDs = new ClientIDs();
   }
   
   /**
    * Get the HttpUnit WebResponse object from the latest request.
    *
    * @return The HttpUnit WebResponse.
    */
   public WebResponse getWebResponse()
   {
      return this.webResponse;
   }
   
   /**
    * Get the HttpUnit WebConversation used by this instance.
    *
    * @return The WebConversation
    */
   public WebConversation getWebConversation()
   { 
      return this.webConversation;
   }
   
   /**
    * Set a parameter value on a form.
    *
    * @param componentID The JSF component ID or a suffix of the client ID.
    * @param value The value to set before the form is submitted.
    *
    * @throws SAXException if the current response page can not be parsed
    * @throws ComponentIDNotFoundException if the component can not be found 
    * @throws DuplicateClientIDException if more than one client ID matches the 
    *                                    componentID suffix
    */
   public void setParameter(String componentID, String... value) throws SAXException
   {
      String clientID = this.clientIDs.findClientID(componentID);
      getForm(clientID).setParameter(clientID, value);
   }
   
   /**
    * Set a checkbox value on a form.  This method is needed because
    * setParameter can not "uncheck" a checkbox.
    *
    * @param componentID The JSF component ID or a suffix of the client ID.
    * @param value The value to set before the form is submitted.
    *
    * @throws SAXException if the current response page can not be parsed
    * @throws ComponentIDNotFoundException if the component can not be found 
    * @throws DuplicateClientIDException if more than one client ID matches the 
    *                                    componentID suffix
    * @throws IllegalArgumentException if the componentID does not resolve to a 
    *                                  checkbox control
    */
   public void setCheckbox(String componentID, boolean state) throws SAXException
   {
      String clientID = this.clientIDs.findClientID(componentID);
      getForm(clientID).setCheckbox(clientID, state);
   }
   
   /**
    * Finds the lone form on the page and submits the form.
    *
    * At the end of this method call, the new view will be loaded so you can
    * perform tests on the next page.
    *
    * @throws IllegalStateException if page does not contain exactly one form.
    * @throws IOException if there is a problem submitting the form.
    * @throws SAXException if the response page can not be parsed
    */
   public void submit() throws SAXException, IOException
   {
      WebForm[] forms = getWebResponse().getForms();
      if (forms.length != 1) 
         throw new IllegalStateException("For this method, page must contain" +
                                         " only one form.  Use another " +
                                         "version of the submit() method.");
      
      this.webResponse = forms[0].submit();
      this.clientIDs = new ClientIDs();
   }
   
   /**
    * Finds the named submit button on a form and submits the form.  
    *
    * At the end of this method call, the new view will be loaded so you can
    * perform tests on the next page.
    *
    * @param componentID The JSF component id (or a suffix of the client ID) of 
    *                    the submit button to be "pressed".
    *
    * @throws IOException if there is a problem submitting the form.
    * @throws SAXException if the response page can not be parsed
    * @throws ComponentIDNotFoundException if the component can not be found 
    * @throws DuplicateClientIDException if more than one client ID matches the 
    *                                    componentID suffix
    */
   public void submit(String componentID) throws SAXException, IOException
   {
      String clientID = this.clientIDs.findClientID(componentID);
      WebForm form = getForm(clientID);
      SubmitButton button = form.getSubmitButtonWithID(clientID);
      this.webResponse = form.submit(button);
      this.clientIDs = new ClientIDs();
   }
   
   /**
    * Submits a form without pressing a button.  This is useful for testing
    * forms that are submitted via javascript.
    *
    * At the end of this method call, the new view will be loaded so you can
    * perform tests on the next page.
    *
    * @param componentID The JSF component id (or a suffix of the client ID) of 
    *                    a component on the form to be submitted.  This can also
    *                    be the ID of the form itself.
    *
    * @throws IOException if there is a problem submitting the form.
    * @throws SAXException if the response page can not be parsed
    * @throws ComponentIDNotFoundException if the component can not be found 
    * @throws DuplicateClientIDException if more than one client ID matches the 
    *                                    componentID suffix
    */
   public void submitNoButton(String componentID) 
         throws SAXException, IOException
   {
      String clientID = this.clientIDs.findClientID(componentID);
      WebForm form = getForm(clientID);
      this.webResponse = form.submitNoButton();
      this.clientIDs = new ClientIDs();
   }
   
   /**
    * Finds the named link and clicks it.  This method is used to click static
    * links such as those produced by h:outputLink.  If you need to submit
    * a form using an h:commandLink, use clickCommandLink() instead.
    * 
    * At the end of this method call, a new page will be loaded.  So you can
    * use this JSFClientSession instance to do tests on the page.  However, static 
    * links typically do not call into the JSF servlet.  Therefore, you have
    * exited the realm of JSF.  In that case you will probably need a new 
    * JSFClientSession instance to do more JSF testing.
    * 
    * 
    * @param componentID The JSF component id (or a suffix of the client ID) of 
    *                    the link to be "clicked".
    * @throws IOException if there is a problem clicking the link.
    * @throws SAXException if the response page can not be parsed.
    * @throws ComponentIDNotFoundException if the component can not be found
    * @throws DuplicateClientIDException if more than one client ID matches the 
    *                                    componentID suffix
    */
   public void clickLink(String componentID) throws SAXException, IOException
   {
      String clientID = this.clientIDs.findClientID(componentID);
      WebLink link = this.webResponse.getLinkWithID(clientID);
      if (link == null) throw new ComponentIDNotFoundException(componentID);
      this.webResponse = link.click();
      this.clientIDs = new ClientIDs();
   }
   
   /**
    * Finds the named command link and uses the link to submit its form.
    *
    * At the end of this method call, the new view will be loaded so you can
    * perform tests on the next page.
    *
    * @param componentID The JSF component id (or a suffix of the client ID) of 
    *                    the link to be "clicked".
    *
    * @throws IOException if there is a problem clicking the link.
    * @throws SAXException if the response page can not be parsed.
    * @throws ComponentIDNotFoundException if the component can not be found 
    * @throws DuplicateClientIDException if more than one client ID matches the 
    *                                    componentID suffix
    */
   public void clickCommandLink(String componentID) 
         throws SAXException, IOException
   {
      
      WebRequest req = this.requestFactory.buildRequest(componentID);
      setCmdLinkParam(req, componentID);
      doWebRequest(req);
   }
   
   private void setCmdLinkParam(WebRequest req, String componentID) 
         throws SAXException, IOException
   {
      String clientID = this.clientIDs.findClientID(componentID);
      WebForm form = getForm(componentID);
      
      String myFaces115CmdLink = form.getID() + ":" + "_idcl";
      if (form.hasParameterNamed(myFaces115CmdLink))
      {
         req.setParameter(myFaces115CmdLink, clientID);
         return;
      }
      
      String myFaces114CmdLink = form.getID() + ":" + "_link_hidden_";
      if (form.hasParameterNamed(myFaces114CmdLink))
      {
         req.setParameter(myFaces114CmdLink, clientID);
         return;
      }
      
      req.setParameter(clientID, clientID); // for the RI
   }
   
}