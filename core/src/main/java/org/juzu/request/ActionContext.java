/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.juzu.request;

import org.juzu.Response;
import org.juzu.impl.application.ApplicationContext;
import org.juzu.impl.controller.descriptor.ControllerMethod;
import org.juzu.impl.request.Request;
import org.juzu.impl.spi.request.ActionBridge;
import org.juzu.impl.utils.ParameterMap;

import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ActionContext extends RequestContext
{

   /** . */
   private ActionBridge bridge;

   public ActionContext(Request request, ApplicationContext application, ControllerMethod method, ActionBridge bridge)
   {
      super(request, application, method);

      //
      this.bridge = bridge;
   }

   @Override
   protected ActionBridge getBridge()
   {
      return bridge;
   }

   @Override
   public Phase getPhase()
   {
      return Phase.ACTION;
   }

   public Response.Update createResponse(ControllerMethod method) throws IllegalStateException
   {
      Response.Update update = new Response.Update();
      update.setProperty(RequestContext.METHOD_ID, method.getId());
      return update;
   }

   public Response.Update createResponse(ControllerMethod method, Object arg) throws IllegalStateException
   {
      Response.Update response = createResponse(method);
      if (arg != null)
      {
         // Yeah OK nasty cast, we'll see later
         method.setArgs(new Object[]{arg}, (ParameterMap)response.getParameters());
      }
      return response;
   }

   public Response.Update createResponse(ControllerMethod method, Object[] args) throws IllegalStateException
   {
      Response.Update response = createResponse(method);
      // Yeah OK nasty cast, we'll see later
      method.setArgs(args, (ParameterMap)response.getParameters());
      return response;
   }
}
