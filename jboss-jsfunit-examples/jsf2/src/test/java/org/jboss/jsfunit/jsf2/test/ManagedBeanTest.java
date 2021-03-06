/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.jsfunit.jsf2.test;

import java.io.IOException;
import javax.faces.application.ProjectStage;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.cactus.ServletTestCase;
import org.jboss.jsfunit.jsfsession.JSFServerSession;
import org.jboss.jsfunit.jsfsession.JSFSession;

/**
 *
 * @author Stan Silvert
 */
public class ManagedBeanTest extends ServletTestCase 
{
   
   private JSFSession jsfSession;
   private JSFServerSession server;
   
   public void setUp() throws IOException
   {
      jsfSession = new JSFSession("/index.jsf");
      server = jsfSession.getJSFServerSession();
   }
   
   /**
    * @return the suite of tests being tested
    */
   public static Test suite()
   {
      return new TestSuite( ManagedBeanTest.class    );
   }

   /**
    */
   public void testManagedBeans() throws IOException
   {
      
      assertEquals("request", server.getManagedBeanValue("#{requestBean.scope}"));
      assertEquals("view", server.getManagedBeanValue("#{viewBean.scope}"));
      assertEquals("session", server.getManagedBeanValue("#{sessionBean.scope}"));
      assertEquals("application", server.getManagedBeanValue("#{applicationBean.scope}"));
   } 
   
   public void testProjectStage() throws IOException
   {
      assertEquals(ProjectStage.Development,
                   server.getManagedBeanValue("#{facesContext.application.projectStage}"));
   }
   
}
