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

import org.juzu.PropertyType;
import org.juzu.impl.application.ApplicationContext;
import org.juzu.impl.controller.descriptor.ControllerMethod;
import org.juzu.impl.request.Request;
import org.juzu.impl.spi.request.RequestBridge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class RequestContext
{

   /** Phase type literal. */
   public static class METHOD_ID extends PropertyType<String> {}

   /** Phase type literal instance. */
   public static METHOD_ID METHOD_ID = new METHOD_ID();

   /** . */
   protected final ApplicationContext application;

   /** . */
   protected final ControllerMethod method;

   /** . */
   protected final Request request;

   public RequestContext(Request request, ApplicationContext application, ControllerMethod method)
   {
      this.request = request;
      this.application = application;
      this.method = method;
   }

   public ApplicationContext getApplication()
   {
      return application;
   }

   public ControllerMethod getMethod()
   {
      return method;
   }

   public Map<String, String[]> getParameters()
   {
      return request.getParameters();
   }
   
   public HttpContext getHttpContext()
   {
      return getBridge().getHttpContext();
   }
   
   public SecurityContext getSecurityContext()
   {
      return getBridge().getSecurityContext();
   }
   
   public <T> T getProperty(PropertyType<T> propertyType)
   {
      return getBridge().getProperty(propertyType);
   }

   public abstract Phase getPhase();

   protected abstract RequestBridge getBridge();

}
