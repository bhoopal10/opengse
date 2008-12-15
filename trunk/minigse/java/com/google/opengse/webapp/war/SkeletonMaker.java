// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
/**
 *
 * @author jennings
 * Date: Nov 19, 2008
 */
package com.google.opengse.webapp.war;

import com.google.opengse.util.PropertiesUtil;
import com.google.opengse.webapp.WebAppConfigurationBuilder;
import com.google.opengse.configuration.webxml.WebXmlDump;

import java.util.Properties;
import java.io.*;

/**
 * This class generates webapp skeletons.
 *
 */
class SkeletonMaker {

  static void createWebApp(Properties props) throws IOException {
    File webapp = PropertiesUtil.getFile(props, "webapp");
    if (webapp == null) {
      System.err.println("Need to supply a --webapp parameter");
      return;
    }
    String swebapp = PropertiesUtil.getAliasedProperty(props, "webapp", null);
    if (swebapp != null && swebapp.equals("true")) {
      System.err.println("Sorry, can't create a webapp in a directory named 'true'");
      return;
    }

    File parent = webapp.getParentFile();
    if (!parent.isDirectory()) {
      System.err.println("'" + parent + "' is not a directory");
      return;
    }
    if (!parent.canWrite()) {
      System.err.println("Cannot write to directory '" + parent + "'");
      return;
    }
    String context = PropertiesUtil.getAliasedProperty(props, "context", null);
    if (context == null) {
      System.err.println("Need a --context parameter");
      return;
    }
    if (webapp.exists()) {
      System.err.println(webapp + " already exists. Please delete it.");
      return;
    }
    webapp.mkdirs();
    File webinf = new File(webapp, "WEB-INF");
    webinf.mkdirs();
    if (!webinf.exists()) {
      System.err.println("Can't create '" + webinf +"' for some reason.");
      return;
    }
    File classes = new File(webinf, "classes");
    classes.mkdirs();
    if (!classes.exists()) {
      System.err.println("Can't create '" + classes + "' for some reason.");
      return;
    }
    WebAppConfigurationBuilder wxmlb = new WebAppConfigurationBuilder();
    wxmlb.addContextParam("global.foo1", "global.bar1");
    wxmlb.addContextParam("global.foo2", "global.bar2");
    wxmlb.unsafe_addServlet("myservlet", "com.google.opengse.SomeServlet", "*.cgi", "chocolate", "good");
    File webxmlfile = new File(webinf, "web.xml");
    PrintWriter webxml = new PrintWriter(webxmlfile);
    try {
      WebXmlDump.dump(wxmlb.getConfiguration(), webxml);
    } finally {
      webxml.close();
    }
    System.err.println("Created '" + webxmlfile + "'");
    File propsfile = new File(parent, context + ".properties");
    Properties wprops = new Properties();
    wprops.setProperty("webapp", "${basedir}/" + webapp.getName());
    wprops.setProperty("port", PropertiesUtil.getAliasedProperty(props, "port", "8080"));
    wprops.setProperty("context", context);
    OutputStream os = new FileOutputStream(propsfile);
    try {
      wprops.store(os, "auto-generated by OpenGSE");
    } finally {
      os.close();
    }
    System.err.println("Created " + propsfile);
    System.err.println("Use 'java -jar opengse.jar --props=/path/to/" + propsfile.getName() + "' to deploy");
  }

}