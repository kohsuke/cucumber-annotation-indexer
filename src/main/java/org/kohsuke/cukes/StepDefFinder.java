package org.kohsuke.cukes;

import org.jvnet.hudson.annotation_indexer.Index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Lists up all the step methods
 *
 * @author Kohsuke Kawaguchi
 */
public class StepDefFinder {
    public static List<Method> list(final ClassLoader cl) throws IOException {
        List<Method> methods = new ArrayList<Method>();

        final Enumeration<URL> res = cl.getResources("META-INF/cucumber-annotations");
        while (res.hasMoreElements()) {
            URL url = res.nextElement();
            InputStream is = url.openStream();
            try {
                BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String typeName;
                while ((typeName = r.readLine()) != null) {
                    try {
                        for (Method m : Index.list(cl.loadClass(typeName).asSubclass(Annotation.class), cl, Method.class))
                            methods.add(m);
                    } catch (ClassNotFoundException e) {
                        // this cucumber annotation existed at compile time but not at runtime
                    }
                }
            } finally {
                is.close();
            }
        }

        return methods;
    }

}
