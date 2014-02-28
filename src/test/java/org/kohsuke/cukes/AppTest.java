package org.kohsuke.cukes;

import com.google.common.collect.Iterables;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import org.junit.Assert;
import org.junit.Test;
import org.jvnet.hudson.annotation_indexer.Index;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppTest extends Assert {
    @Test
    public void test() throws Exception {
        ClassLoader cl = getClass().getClassLoader();

        List<Method> methods = StepDefFinder.list(cl);

        Set<String> symbols = new HashSet<String>();
        for (Method m : methods) {
            symbols.add(m.getDeclaringClass().getName()+":"+m.getName());
        }

        assertEquals(symbols, new HashSet<String>(Arrays.asList(
                SampleSteps.class.getName()+":eat",
                SampleSteps.class.getName()+":feelingGreat"
        )));

        assertEquals("[public void org.kohsuke.cukes.SampleHooks.foo()]", Iterables.toString(Index.list(Before.class, cl)));
        assertEquals("[public void org.kohsuke.cukes.SampleHooks.bar()]", Iterables.toString(Index.list(After.class, cl)));
    }
}
