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

package org.juzu.impl.plugin.asset;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.Test;
import org.juzu.test.AbstractHttpTestCase;
import org.juzu.test.UserAgent;
import org.juzu.test.protocol.mock.MockApplication;

import java.util.Arrays;
import java.util.List;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ServerLocationTestCase extends AbstractHttpTestCase
{

   @Test
   public void testSatisfied() throws Exception
   {
      MockApplication<?> app = assertDeploy("plugin", "asset", "location", "server");

      //
      UserAgent ua = assertInitialPage();
      HtmlPage page = ua.getHomePage();

      // Script
      HtmlAnchor trigger = (HtmlAnchor)page.getElementById("trigger");
      trigger.click();
      List<String> alerts = ua.getAlerts(page);
      assertEquals(Arrays.asList("OK MEN"), alerts);

      // CSS
      List<HtmlElement> links = page.getElementsByTagName("link");
      assertEquals(2, links.size());
      HtmlLink link1 = (HtmlLink)links.get(0);
      assertEquals("stylesheet", link1.getRelAttribute());
      assertEquals("/juzu/main.css", link1.getHrefAttribute());
      assertEquals("text/css", link1.getTypeAttribute());
      HtmlLink link2 = (HtmlLink)links.get(1);
      assertEquals("stylesheet", link2.getRelAttribute());
      assertEquals("/juzu/main.less", link2.getHrefAttribute());
      assertEquals("text/less", link2.getTypeAttribute());
   }
}
