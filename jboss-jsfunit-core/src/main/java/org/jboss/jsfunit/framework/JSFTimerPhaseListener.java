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

package org.jboss.jsfunit.framework;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

/**
 * This PhaseListener provides time stamps for each phase of the JSF lifecycle.
 * To use this class, you must enable it in faces-config.xml.
 *
 * @author Stan Silvert
 * @since 1.0
 * @see org.jboss.jsfunit.framework.JSFTimer
 */
public class JSFTimerPhaseListener implements PhaseListener
{
   public PhaseId getPhaseId()
   {
      return PhaseId.ANY_PHASE;
   }

   public void beforePhase(PhaseEvent phaseEvent)
   {
      PhaseId phaseId = phaseEvent.getPhaseId();
      JSFTimer timer = JSFTimer.getTimer();
      timer.beforePhase(phaseId);
   }

   public void afterPhase(PhaseEvent phaseEvent)
   {
      JSFTimer.getTimer().afterPhase(phaseEvent.getPhaseId());
   }
   
}
