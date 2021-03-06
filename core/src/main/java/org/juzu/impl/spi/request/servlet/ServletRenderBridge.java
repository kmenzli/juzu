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

package org.juzu.impl.spi.request.servlet;

import org.juzu.Response;
import org.juzu.impl.asset.Asset;
import org.juzu.impl.inject.ScopedContext;
import org.juzu.impl.spi.request.RenderBridge;
import org.juzu.io.AppendableStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServletRenderBridge extends ServletMimeBridge implements RenderBridge
{

   ServletRenderBridge(
      ServletBridgeContext context,
      HttpServletRequest req,
      HttpServletResponse resp,
      String methodId,
      Map<String, String[]> parameters)
   {
      super(context, req, resp, methodId, parameters);
   }

   public void setTitle(String title)
   {
   }

   public void end(Response response) throws IllegalStateException, IOException
   {
      if (response instanceof Response.Render)
      {
         Response.Content.Render render = (Response.Render)response;
         Iterable<Asset> scripts;
         Iterable<Asset> stylesheets;
         try
         {
            scripts = context.scriptManager.resolveAssets(render.getScripts());
            stylesheets = context.scriptManager.resolveAssets(render.getStylesheets());
         }
         catch (IllegalArgumentException e)
         {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return;
         }

         //
         resp.setContentType(render.getMimeType());

         //
         PrintWriter writer = resp.getWriter();

         //
         writer.println("<!DOCTYPE html>");
         writer.println("<html>");

         writer.println("<head>");
         for (Asset stylesheet : stylesheets)
         {
            String path = stylesheet.getValue();
            int pos = path.lastIndexOf('.');
            String ext = pos == -1 ? "css" : path.substring(pos + 1);
            writer.print("<link rel=\"stylesheet\" type=\"text/");
            writer.print(ext);
            writer.print("\" href=\"");
            renderAssetURL(stylesheet, writer);
            writer.println("\"></link>");
         }

         //
         for (Asset script : scripts)
         {
            writer.print("<script type=\"text/javascript\" src=\"");
            renderAssetURL(script, writer);
            writer.println("\"></script>");
         }

         //
         writer.println("</head>");
         writer.println("<body>");

         // Send response
         render.send(new AppendableStream(writer));

         //
         writer.println("</body>");
         writer.println("</html>");
      }
   }

   private void renderAssetURL(Asset asset, Appendable appendable) throws IOException
   {
      switch (asset.getLocation())
      {
         case SERVER:
         case CLASSPATH:
            appendable.append(req.getContextPath());
            appendable.append(asset.getValue());
            break;
         case EXTERNAL:
            appendable.append(asset.getValue());
            break;
      }
   }

   private String getAssetURL(String url)
   {
      if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("/"))
      {
         return url;
      }
      else
      {
         return req.getContextPath() + "/" + url;
      }
   }

   @Override
   public void close()
   {
      ScopedContext context = getFlashContext(false);
      if (context != null)
      {
         context.close();
      }
   }
}
