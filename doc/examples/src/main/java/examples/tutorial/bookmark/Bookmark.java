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

package examples.tutorial.bookmark;

import org.juzu.Action;
import org.juzu.Path;
import org.juzu.View;
import org.juzu.template.Template;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class Bookmark
{

   private static Map<String, String> bookmarks = new LinkedHashMap<String, String>();

   static
   {
      bookmarks.put("Google", "http://www.google.com");
      bookmarks.put("Yahoo", "http://www.yahoo.com");
   }

   @Path("index.gtmpl")
   @Inject
   Template index;

   @View
   public void index() throws IOException
   {
      index.render(Collections.singletonMap("bookmarks", bookmarks));
   }

   @Action
   public void add(String name, String url)
   {
      bookmarks.put(name, url);
   }
}