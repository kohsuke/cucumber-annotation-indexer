package cucumber.runtime.java;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.runtime.Backend;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.DuplicateStepDefinitionException;
import cucumber.runtime.Glue;
import cucumber.runtime.Reflections;
import cucumber.runtime.UnreportedStepExecutor;
import cucumber.runtime.Utils;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.snippets.FunctionNameSanitizer;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;
import org.jvnet.hudson.annotation_indexer.Index;
import org.kohsuke.cukes.StepDefFinder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Improved version of {@link JavaBackend} that uses indexed annotations to find glues.
 *
 * @author Kohsuke Kawaguchi
 */
public class BetterJavaBackend implements Backend {
    private final ClassLoader classLoader;
    private final ObjectFactory objectFactory;
    private SnippetGenerator snippetGenerator = new SnippetGenerator(new JavaSnippet());

    /**
     * For automatic instantiation from Cucumber runtime.
     */
    public BetterJavaBackend(ResourceLoader resourceLoader) {
        classLoader = Thread.currentThread().getContextClassLoader();
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        objectFactory = loadObjectFactory(classFinder);
    }

    public BetterJavaBackend(ObjectFactory objectFactory, ClassLoader classLoader) {
        this.objectFactory = objectFactory;
        this.classLoader = classLoader;
    }

    public void setUnreportedStepExecutor(UnreportedStepExecutor executor) {
        //Not used here yet
    }

    public void buildWorld() {
        objectFactory.start();
    }

    public void disposeWorld() {
        objectFactory.stop();
    }

    public String getSnippet(Step step, FunctionNameSanitizer functionNameSanitizer) {
        return snippetGenerator.getSnippet(step, functionNameSanitizer);
    }

    /**
     * Find cucumber annotated step definitions and hook through index
     */
    public void loadGlue(Glue glue, List<String> gluePaths) {
        // ignore user-specified glues (such as ones that IDE tell us) and just find them on our own
        try {

            for (Method method : StepDefFinder.list(classLoader)) {
                for (Annotation a : method.getAnnotations()) {
                    if (a.annotationType().isAnnotationPresent(StepDefAnnotation.class)) {
                        addStepDefinition(glue, a, method);
                    }
                }
            }

            for (Method method : Index.list(Before.class, classLoader, Method.class)) {
                addHook(glue, method.getAnnotation(Before.class), method);
            }

            for (Method method : Index.list(After.class, classLoader, Method.class)) {
                addHook(glue, method.getAnnotation(After.class), method);
            }
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    void addStepDefinition(Glue glue, Annotation annotation, Method method) {
        try {
            objectFactory.addClass(method.getDeclaringClass());
            glue.addStepDefinition(new JavaStepDefinition(method, pattern(annotation), timeoutMillis(annotation), objectFactory));
        } catch (DuplicateStepDefinitionException e) {
            throw e;
        } catch (Throwable e) {
            throw new CucumberException(e);
        }
    }

    public static ObjectFactory loadObjectFactory(ClassFinder classFinder) {
        ObjectFactory objectFactory;
        try {
            Reflections reflections = new Reflections(classFinder);
            objectFactory = reflections.instantiateExactlyOneSubclass(ObjectFactory.class, "cucumber.runtime", new Class[0], new Object[0]);
        } catch (CucumberException ce) {
            objectFactory = new DefaultJavaObjectFactory();
        }
        return objectFactory;
    }

    private Pattern pattern(Annotation annotation) throws Throwable {
        Method regexpMethod = annotation.getClass().getMethod("value");
        String regexpString = (String) Utils.invoke(annotation, regexpMethod, 0);
        return Pattern.compile(regexpString);
    }

    private long timeoutMillis(Annotation annotation) throws Throwable {
        Method regexpMethod = annotation.getClass().getMethod("timeout");
        return (Long) Utils.invoke(annotation, regexpMethod, 0);
    }

    void addHook(Glue glue, Annotation annotation, Method method) {
        objectFactory.addClass(method.getDeclaringClass());

        if (annotation.annotationType().equals(Before.class)) {
            String[] tagExpressions = ((Before) annotation).value();
            long timeout = ((Before) annotation).timeout();
            glue.addBeforeHook(new JavaHookDefinition(method, tagExpressions, ((Before) annotation).order(), timeout, objectFactory));
        } else {
            String[] tagExpressions = ((After) annotation).value();
            long timeout = ((After) annotation).timeout();
            glue.addAfterHook(new JavaHookDefinition(method, tagExpressions, ((After) annotation).order(), timeout, objectFactory));
        }
    }
}