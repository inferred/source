package org.inferred.internal.source;

import static javax.lang.model.SourceVersion.latestSupported;
import static org.inferred.internal.source.ModelUtils.findAnnotationMirror;
import static org.inferred.internal.source.RoundEnvironments.annotatedElementsIn;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

public abstract class SimpleTypeProcessor implements Processor {

  protected Elements elements;
  protected ProcessingEnvironment env;
  protected Filer filer;
  protected Messager messager;
  protected Types types;

  protected void init() {}

  protected abstract SourceVersion minimumSupportedVersion();

  protected abstract Class<? extends Annotation> annotation();

  protected abstract void processAnnotatedElements(
      Set<? extends Element> annotatedElements, RoundEnvironment roundEnv);

  protected interface Processing {
    void process(TypeElement type) throws CannotGenerateCodeException, IOException;
  }

  /**
   * Runs {@code processing} on every element in {@code types}.
   *
   * <p>Any uncaught exceptions will be printed on the type being processed.
   */
  protected void process(Iterable<? extends TypeElement> types, Processing processing) {
    for (TypeElement type : types) {
      try {
        processing.process(type);
      } catch (CannotGenerateCodeException e) {
        // Thrown to skip writing the builder source; the error will already have been issued.
      } catch (FilerException e) {
        messager.printMessage(
            Kind.WARNING,
            "Error generating type: " + e.getMessage(),
            type,
            findAnnotationMirror(type, annotation()).get());
      } catch (IOException e) {
        messager.printMessage(
            Kind.ERROR,
            "I/O error: " + Throwables.getStackTraceAsString(e),
            type,
            findAnnotationMirror(type, annotation()).get());
      } catch (RuntimeException e) {
        messager.printMessage(
            Kind.ERROR,
            "Internal error: " + Throwables.getStackTraceAsString(e),
            type,
            findAnnotationMirror(type, annotation()).get());
      }
    }
  }

  @Override
  public void init(ProcessingEnvironment processingEnv) {
    elements = processingEnv.getElementUtils();
    env = processingEnv;
    filer = processingEnv.getFiler();
    messager = processingEnv.getMessager();
    types = processingEnv.getTypeUtils();
    init();
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return ImmutableSet.of(annotation().getName());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return Ordering.natural().max(latestSupported(), minimumSupportedVersion());
  }

  @Override
  public Set<String> getSupportedOptions() {
    return ImmutableSet.of();
  }

  @Override
  public Iterable<? extends Completion> getCompletions(
      Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
    return ImmutableSet.of();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    processAnnotatedElements(annotatedElementsIn(roundEnv, annotation()), roundEnv);
    return false;
  }

}