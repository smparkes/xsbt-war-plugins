//
// Copyright 2011, Steven Parkes
//
// Apache 2.0 License
// Please see README.md, LICENSE and NOTICE
//

package net.usersource.jettyembed.jetty7;

import java.io.File;
import java.net.URL;
import java.security.ProtectionDomain;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;

public class StartupXML {
  public static void main(String[] args) {
    Server server = new Server();

    ProtectionDomain protectionDomain = StartupXML.class.getProtectionDomain();
    URL location = protectionDomain.getCodeSource().getLocation();
 
    WebAppContext context = new WebAppContext();
    context.setServer(server);
    context.setContextPath("/");
    context.setWar(location.toExternalForm());
    try {
      context.preConfigure();
    } catch (Exception e) {
      System.err.println(e);
      System.exit(-1);
    }
    System.err.println(context.getTempDirectory());
    String jetty_home = System.getProperty("jetty.home");
    if (jetty_home == null) {
      jetty_home = "" + context.getTempDirectory() +
        File.separatorChar + "webapp";
      System.setProperty("jetty.home", jetty_home);
    }
    String jetty_xml = jetty_home +         
      File.separatorChar + "etc" +
      File.separatorChar + "jetty.xml";

    try {
      (new XmlConfiguration(new File(jetty_xml).toURI().toURL())).configure(server);
    } catch (Exception e) {
      System.err.println(e);
      System.exit(-1);
    }

    
    HandlerCollection handlers = (HandlerCollection)server.getChildHandlerByClass(HandlerCollection.class);
    if (handlers == null) {
      handlers = new HandlerCollection();
      server.setHandler(handlers);
    }

    ContextHandlerCollection contexts =
      (ContextHandlerCollection) handlers.getChildHandlerByClass(ContextHandlerCollection.class);
    if (contexts == null) {
      contexts = new ContextHandlerCollection();
      handlers.addHandler(contexts);
    }

    contexts.setHandlers(new Handler[] {context});

    DefaultHandler defaultHandler = handlers.getChildHandlerByClass(DefaultHandler.class);
    if (defaultHandler == null) {
      defaultHandler = new DefaultHandler();
      handlers.addHandler(defaultHandler);
    }

    RequestLogHandler rlh = handlers.getChildHandlerByClass(RequestLogHandler.class);
    if (rlh == null) {
      rlh = new RequestLogHandler();
      handlers.addHandler(rlh);
      rlh.setRequestLog(new org.eclipse.jetty.server.NCSARequestLog());
      handlers.addHandler(rlh);
    }

    try {
      server.start();
      if (false) {
          System.in.read();
          server.stop();
      }
      server.join();
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(100);
    }
  }
}
