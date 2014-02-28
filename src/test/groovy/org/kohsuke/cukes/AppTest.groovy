package org.kohsuke.cukes

import com.google.common.collect.Iterables
import cucumber.api.java.After
import cucumber.api.java.Before
import org.junit.Assert
import org.junit.Test
import org.jvnet.hudson.annotation_indexer.Index

public class AppTest extends Assert {
    @Test
    public void test() throws Exception {
        def cl = getClass().classLoader;

        def symbols = StepDefFinder.list(cl).collect { it.declaringClass.name+':'+it.name }

        assert symbols as Set == [SampleSteps.class.getName()+":eat", SampleSteps.class.getName()+":feelingGreat"] as Set

        assert "[public void org.kohsuke.cukes.SampleHooks.foo()]" == Iterables.toString(Index.list(Before.class, cl))
        assert "[public void org.kohsuke.cukes.SampleHooks.bar()]" == Iterables.toString(Index.list(After.class, cl))
    }
}
