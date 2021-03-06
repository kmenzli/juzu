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

package org.juzu.impl.compiler;

import org.juzu.impl.utils.ErrorCode;

import javax.lang.model.element.Element;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CompilationException extends RuntimeException
{

   /** . */
   private final ErrorCode code;

   /** . */
   private Object[] arguments;

   /** . */
   private final Element element;

   public CompilationException(Element element, ErrorCode code, Object... arguments)
   {
      this.code = code;
      this.element = element;
      this.arguments = arguments;
   }

   public CompilationException(ErrorCode code, Object... arguments)
   {
      this.code = code;
      this.arguments = arguments;
      this.element = null;
   }

   public CompilationException(Throwable cause, ErrorCode code, Object... arguments)
   {
      super(cause);

      //
      this.code = code;
      this.element = null;
      this.arguments = arguments;
   }

   public CompilationException(Throwable cause, Element element, ErrorCode code, Object... arguments)
   {
      super(cause);

      //
      this.code = code;
      this.element = element;
      this.arguments = arguments;
   }

   public Element getElement()
   {
      return element;
   }

   public ErrorCode getCode()
   {
      return code;
   }

   public Object[] getArguments()
   {
      return arguments;
   }
}
