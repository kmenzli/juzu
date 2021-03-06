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

package org.juzu;

import org.juzu.impl.utils.ParameterHashMap;
import org.juzu.impl.utils.ParameterMap;
import org.juzu.impl.utils.Tools;
import org.juzu.io.Stream;
import org.juzu.io.Streamable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>A response object signalling to the portal the action to take after an interaction. This object is usually returned
 * after the invocation of a controller method and instructs the aggregator the action to take.</p>
 *
 * <h2>Action response</h2>
 *
 * <h3>Redirection response</h3>
 *
 * <p>A <code>Response.Process.Action.Redirect</code> response instructs the aggregator to make a redirection to a valid URL after the
 * interaction, this kind of response is created using the factory method {@link Response#redirect(String)}:
 * <code><pre>
 *    return Response.redirect("http://www.exoplatform.org");
 * </pre></code>
 * </p>
 *
 * <h3>Proceed to render phase</h3>
 *
 * <p>A <code>Response.Update</code> response instructs the aggreator to proceed to the render phase of a valid
 * view controller, this kind of response can be created using an {@link org.juzu.request.ActionContext}, however
 * the the preferred way is to use a controller companion class that carries method factories for creating render
 * responses.</p>
 *
 * <p>Type safe {@link org.juzu.Response.Update} factory method are generated for each view or resource controller methods.
 * The signature of an render factory is obtained by using the same signature of the controller method.</p>
 *
 * <code><pre>
 *    public class MyController {
 *
 *       &#064;Action
 *       public {@link org.juzu.Response.Update} myAction() {
 *          return MyController_.myRender("hello");
 *       }
 *
 *       &#064;View
 *       public void myRender(String param) {
 *       }
 *    }
 * </pre></code>
 *
 * <h2>Mime response</h2>
 * 
 * <p>Mime response are used by the {@link org.juzu.request.Phase#RENDER} and the {@link org.juzu.request.Phase#RESOURCE} phases.
 * Both contains a content to be streamed to the client but still they have some noticeable differences.</p>
 * 
 * <p>The {@link org.juzu.Response.Content} class is the base response class which will work well for the two phases. However the
 * {@link org.juzu.request.Phase#RENDER} can specify an optional title and the {@link org.juzu.request.Phase#RESOURCE} can
 * specify an optional status code for the user agent response.</p>
 * 
 * <p>Responses are created using the {@link Response} factory methods such as</p>
 * 
 * <ul>
 *    <li>{@link Response#content} creates an ok response</li>
 *    <li>{@link Response#notFound} creates a not found response</li>
 * </ul>
 *
 * <p>Response can also created from {@link org.juzu.template.Template} directly:</p>
 *
 * <code><pre>
 *    public class MyController {
 *
 *       &#064;Inject &#064;Path("index.gtmpl") {@link org.juzu.template.Template} index;
 *
 *       &#064;View
 *       public {@link org.juzu.Response.Render} myView() {
 *          return index.render();
 *       }
 *
 *       &#064;Inject &#064;Path("error.gtmpl")  {@link org.juzu.template.Template} error;
 *
 *       &#064;Resource
 *       public {@link org.juzu.Response.Resource} myView() {
 *          return error.notFound();
 *       }
 *    }
 * </pre></code>
 * 
 * <p>The {@link org.juzu.template.Template.Builder} can also create responses:</p>
 *
 * <code><pre>
 *    public class MyController {
 *
 *       &#064;Inject &#064;Path("index.gtmpl") index index;
 *
 *       &#064;View
 *       public {@link org.juzu.Response.Content} myView() {
 *          return index.with().label("hello").render();
 *       }
 *    }
 * </pre></code>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class Response
{

   /** . */
   protected final PropertyMap properties;

   protected Response()
   {
      this.properties = new PropertyMap();
   }

   protected Response(PropertyMap properties)
   {
      this.properties = properties;
   }

   /**
    * Set a property, if the value is null, the property is removed.
    *
    * @param propertyType the property type
    * @param propertyValue the property value
    * @throws NullPointerException if the property type is null
    */
   public <T> Response setProperty(PropertyType<T> propertyType, T propertyValue) throws NullPointerException
   {
      if (propertyType == null)
      {
         throw new NullPointerException("No null property type allowed");
      }
      properties.setValue(propertyType, propertyValue);
      return this;
   }

   public final PropertyMap getProperties()
   {
      return properties;
   }

   /**
    * A response instructing to execute a render phase of a controller method after the current interaction.
    */
   public static class Update extends Response
   {

      /** . */
      private ParameterMap parameterMap;

      public Update()
      {
         this.parameterMap = new ParameterHashMap();
      }

      public Update setParameter(String name, String value) throws NullPointerException
      {
         parameterMap.setParameter(name, value);
         return this;
      }

      public Update setParameter(String name, String[] value) throws NullPointerException, IllegalArgumentException
      {
         parameterMap.setParameter(name, value);
         return this;
      }

      public Update setParameters(Map<String, String[]> parameters) throws NullPointerException, IllegalArgumentException
      {
         parameterMap.setParameters(parameters);
         return this;
      }

      public Map<String, String[]> getParameters()
      {
         return parameterMap;
      }

      @Override
      public <T> Update setProperty(PropertyType<T> propertyType, T propertyValue) throws NullPointerException
      {
         return (Update)super.setProperty(propertyType, propertyValue);
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj == this)
         {
            return true;
         }
         if (obj instanceof Update)
         {
            Update that = (Update)obj;
            return parameterMap.equals(that.parameterMap) && properties.equals(that.properties);
         }
         return false;
      }

      @Override
      public String toString()
      {
         return "Response.Update[parameters" + parameterMap + ",properties=" + properties + "]";
      }
   }

   /**
    * A response instructing to execute an HTTP redirection after the current interaction.
    */
   public static class Redirect extends Response
   {

      /** . */
      private final String location;

      public Redirect(String location)
      {
         this.location = location;
      }

      public String getLocation()
      {
         return location;
      }

      @Override
      public <T> Redirect setProperty(PropertyType<T> propertyType, T propertyValue) throws NullPointerException
      {
         return (Redirect)super.setProperty(propertyType, propertyValue);
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj == this)
         {
            return true;
         }
         if (obj instanceof Redirect)
         {
            Redirect that = (Redirect)obj;
            return location.equals(that.location);
         }
         return false;
      }

      @Override
      public String toString()
      {
         return "Response.Redirect[location" + location + "]";
      }
   }

   public static class Content<S extends Stream> extends Response
   {

      /** Mime type type literal. */
      public static class MIME_TYPE extends PropertyType<String> {}

      /** Mime type literal instance. */
      public static MIME_TYPE MIME_TYPE = new MIME_TYPE();

      /** . */
      private final Class<S> kind;

      /** . */
      private Streamable<S> streamable;

      protected Content(Class<S> kind)
      {
         this.kind = kind;
         this.streamable = null;
      }

      protected Content(Class<S> kind, PropertyMap properties)
      {
         super(properties);

         //
         this.kind = kind;
         this.streamable = null;
      }

      protected Content(Class<S> kind, Streamable<S> streamable)
      {
         this.kind = kind;
         this.streamable = streamable;
      }

      protected Content(Class<S> kind, PropertyMap properties, Streamable<S> streamable)
      {
         super(properties);

         //
         this.kind = kind;
         this.streamable = streamable;
      }

      public Class<S> getKind()
      {
         return kind;
      }

      public Streamable<S> getStreamable()
      {
         return streamable;
      }

      public String getMimeType()
      {
         return properties.getValue(MIME_TYPE);
      }

      public Content<S> withMimeType(String mimeType)
      {
         properties.setValue(MIME_TYPE, mimeType);
         return this;
      }

      @Override
      public <T> Content setProperty(PropertyType<T> propertyType, T propertyValue) throws NullPointerException
      {
         return (Content)super.setProperty(propertyType, propertyValue);
      }

      /**
       * Send the response on the stream argument, Juzu invokes it when it needs to render the content object.
       *
       * @param stream the stream for sending the response
       * @throws IOException any io exception
       */
      public void send(S stream) throws IOException
      {
         streamable.send(stream);
      }
   }

   public static class Render extends Content<Stream.Char>
   {

      /** Script type literal. */
      public static class SCRIPT extends PropertyType<String> {}

      /** Script type literal instance. */
      public static SCRIPT SCRIPT = new SCRIPT();

      /** Stylesheet type literal. */
      public static class STYLESHEET extends PropertyType<String> {}

      /** Stylesheet literal instance. */
      public static STYLESHEET STYLESHEET = new STYLESHEET();

      @Override
      public <T> Render setProperty(PropertyType<T> propertyType, T propertyValue) throws NullPointerException
      {
         return (Render)super.setProperty(propertyType, propertyValue);
      }

      /** Stylesheet type literal. */
      public static class TITLE extends PropertyType<String> {}

      /** Stylesheet literal instance. */
      public static TITLE TITLE = new TITLE();

      public Render()
      {
         super(Stream.Char.class);
      }

      public Render(PropertyMap properties, Streamable<Stream.Char> streamable)
      {
         super(Stream.Char.class, properties, streamable);
      }

      public Render(Streamable<Stream.Char> streamable)
      {
         super(Stream.Char.class, streamable);
      }

      public String getTitle()
      {
         return properties.getValue(TITLE);
      }

      public Render withTitle(String title)
      {
         properties.setValue(TITLE, title);
         return this;
      }

      public Iterable<String> getScripts()
      {
         Iterable<String> scripts = properties.getValues(SCRIPT);
         return scripts != null ? scripts : Tools.<String>emptyIterable();
      }

      public Render addScript(String script) throws NullPointerException
      {
         if (script == null)
         {
            throw new NullPointerException("No null script accepted");
         }
         properties.addValue(SCRIPT, script);
         return this;
      }

      public Iterable<String> getStylesheets()
      {
         Iterable<String> stylesheets = properties.getValues(STYLESHEET);
         return stylesheets != null ? stylesheets : Tools.<String>emptyIterable();
      }

      public Render addStylesheet(String stylesheet) throws NullPointerException
      {
         if (stylesheet == null)
         {
            throw new NullPointerException("No null stylesheet accepted");
         }
         properties.addValue(SCRIPT, stylesheet);
         return this;
      }

      @Override
      public String toString()
      {
         return "Response.Render[]";
      }
   }

   public static class Resource<S extends Stream> extends Content<S>
   {

      /** Mime type type literal. */
      public static class STATUS extends PropertyType<Integer> {}

      /** Mime type literal instance. */
      public static STATUS STATUS = new STATUS();

      public Resource(Class<S> kind)
      {
         super(kind);
      }

      public Resource(Class<S> kind, Streamable<S> streamable)
      {
         super(kind, streamable);
      }

      public Integer getStatus()
      {
         return properties.getValue(STATUS);
      }

      public Resource<S> withStatus(int status)
      {
         properties.setValue(STATUS, status);
         return this;
      }

      @Override
      public Resource<S> withMimeType(String mimeType)
      {
         return (Resource<S>)super.withMimeType(mimeType);
      }

      @Override
      public <T> Resource<S> setProperty(PropertyType<T> propertyType, T propertyValue) throws NullPointerException
      {
         return (Resource<S>)super.setProperty(propertyType, propertyValue);
      }

      @Override
      public String toString()
      {
         return "Response.Resource[]";
      }
   }

   public static Response.Redirect redirect(String location)
   {
      return new Response.Redirect(location);
   }

   public static Content<Stream.Char> content(CharSequence content)
   {
      return new Content<Stream.Char>(Stream.Char.class, new Streamable.CharSequence(content));
   }

   public static Render render(CharSequence content)
   {
      return render(null, content);
   }

   public static Render render(String title, CharSequence content)
   {
      return new Render(new Streamable.CharSequence(content)).withTitle(title);
   }

   public static Resource<?> ok()
   {
      return ok((String)null);
   }

   public static Resource<Stream.Char> ok(CharSequence content)
   {
      return status(200, content);
   }

   public static Resource<Stream.Binary> ok(String mimeType, InputStream content)
   {
      return status(200, mimeType, content);
   }

   public static Resource<Stream.Binary> ok(InputStream content)
   {
      return ok(null, content);
   }

   public static Resource<?> notFound()
   {
      return notFound(null);
   }

   public static Resource<Stream.Char> notFound(CharSequence content)
   {
      return status(404, content);
   }

   public static Resource<?> status(int code)
   {
      return status(code, (String)null);
   }

   public static Resource<Stream.Char> status(int code, CharSequence content)
   {
      return status(code, new Streamable.CharSequence(content));
   }

   public static Resource<Stream.Char> status(int code, Streamable<Stream.Char> content)
   {
      return new Resource<Stream.Char>(Stream.Char.class, content).withStatus(code).withMimeType("text/html");
   }

   public static Resource<Stream.Binary> status(int code, InputStream content)
   {
      return status(code, null, content);
   }

   public static Resource<Stream.Binary> status(int code, String mimeType, InputStream content)
   {
      return new Resource<Stream.Binary>(Stream.Binary.class, new Streamable.InputStream(content)).withStatus(code).withMimeType(mimeType);
   }
}
