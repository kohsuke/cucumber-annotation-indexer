package org.kohsuke.cukes;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.When;
import cucumber.runtime.java.StepDefAnnotation;
import org.jvnet.hudson.annotation_indexer.AnnotationProcessorImpl;
import org.kohsuke.MetaInfServices;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static javax.lang.model.SourceVersion.*;
import static javax.tools.StandardLocation.*;

/**
 * @author Kohsuke Kawaguchi
 */
@SupportedSourceVersion(RELEASE_6)
@SupportedAnnotationTypes("*")
@SuppressWarnings({"Since16", "Since15"})
@MetaInfServices(Processor.class)
public class CukeAnnotationIndexer extends AnnotationProcessorImpl {
    /**
     * Step definition annotations like {@link When} whose use we've encountered.
     */
    private Set<TypeElement> stepDefTypes = new HashSet<TypeElement>();

    @Override
    protected void execute(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        super.execute(annotations, roundEnv);

        try {
            Set<String> contents = new LinkedHashSet<String>();
            try {
                FileObject in = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/cucumber-annotations");
                // Read existing content, for incremental compilation.
                BufferedReader r = new BufferedReader(new InputStreamReader(in.openInputStream(),"UTF-8"));
                try {
                    String line;
                    while ((line=r.readLine())!=null) {
                        contents.add(line);
                    }
                } finally {
                    r.close();
                }
            } catch (FileNotFoundException x) {
                // this file is created for the first time
            }

            FileObject out = processingEnv.getFiler().createResource(CLASS_OUTPUT,
                    "", "META-INF/cucumber-annotations",
                    stepDefTypes.toArray(new Element[stepDefTypes.size()]));

            PrintWriter w = new PrintWriter(new OutputStreamWriter(out.openOutputStream(),"UTF-8"));
            try {
                for (TypeElement ann : stepDefTypes)
                    contents.add(ann.getQualifiedName().toString());
                for (String content : contents)
                    w.println(content);
            } finally {
                w.close();
            }
        } catch (IOException x) {
            processingEnv.getMessager().printMessage(Kind.ERROR, x.toString());
        }

    }

    @Override
    protected boolean isIndexing(TypeElement ann) {
        if (ann.getQualifiedName().contentEquals(Before.class.getName())
         || ann.getQualifiedName().contentEquals(After.class.getName()))
            return true;

        boolean b = findAnnotationOn(ann, STEP_DEF_ANNOTATION) != null;
        if (b)  stepDefTypes.add(ann);
        return b;
    }

    private static final String STEP_DEF_ANNOTATION = StepDefAnnotation.class.getName();
}