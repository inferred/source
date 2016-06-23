/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.inferred.internal.source;

import static com.google.common.base.Preconditions.checkArgument;
import static org.inferred.internal.source.AnnotationSource.addSource;

import org.inferred.internal.source.feature.Feature;
import org.inferred.internal.source.feature.FeatureSet;
import org.inferred.internal.source.feature.FeatureType;
import org.inferred.internal.source.feature.StaticFeatureSet;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * A {@link SourceBuilder} that writes to a {@link StringBuilder}.
 */
public final class SourceStringBuilder implements SourceBuilder {

  private final TypeShortener shortener;
  private final StringBuilder destination = new StringBuilder();
  private final FeatureSet features;

  /**
   * Returns a {@link SourceStringBuilder} that always shortens types, even if that causes
   * conflicts.
   */
  public static SourceBuilder simple(Feature<?>... features) {
    return new SourceStringBuilder(
        new TypeShortener.AlwaysShorten(), new StaticFeatureSet(features));
  }

  SourceStringBuilder(TypeShortener shortener, FeatureSet features) {
    this.shortener = shortener;
    this.features = features;
  }

  @Override
  public SourceBuilder add(Excerpt excerpt) {
    excerpt.addTo(this);
    return this;
  }

  @Override
  public SourceBuilder add(String fmt, Object... args) {
    Object[] substituteArgs = new Object[args.length];
    for (int i = 0; i < args.length; i++) {
      substituteArgs[i] = substitute(args[i]);
    }
    destination.append(String.format(fmt, substituteArgs));
    return this;
  }

  @Override
  public SourceBuilder addLine(String fmt, Object... args) {
    return add(fmt + "\n", args);
  }

  @Override
  public SourceStringBuilder subBuilder() {
    return new SourceStringBuilder(shortener, features);
  }

  @Override
  public <T extends Feature<T>> T feature(FeatureType<T> feature) {
    return features.get(feature);
  }

  /** Returns the source code written so far. */
  @Override
  public String toString() {
    return destination.toString();
  }

  private Object substitute(Object arg) {
    if (arg instanceof Excerpt) {
      SourceBuilder excerptBuilder = subBuilder();
      ((Excerpt) arg).addTo(excerptBuilder);
      return excerptBuilder.toString();
    } else if (arg instanceof Package) {
      return ((Package) arg).getName();
    } else if (arg instanceof Element) {
      ElementKind kind = ((Element) arg).getKind();
      if (kind == ElementKind.PACKAGE) {
        return ((PackageElement) arg).getQualifiedName();
      } else if (kind.isClass() || kind.isInterface()) {
        return shortener.shorten(QualifiedName.of((TypeElement) arg));
      } else {
        return arg;
      }
    } else if (arg instanceof Class<?>) {
      return shortener.shorten(QualifiedName.of((Class<?>) arg));
    } else if ((arg instanceof TypeMirror) && (((TypeMirror) arg).getKind() == TypeKind.DECLARED)) {
      DeclaredType mirror = (DeclaredType) arg;
      checkArgument(isLegalType(mirror), "Cannot write unknown type %s", mirror);
      return shortener.shorten(mirror);
    } else if (arg instanceof QualifiedName) {
      return shortener.shorten((QualifiedName) arg);
    } else if (arg instanceof AnnotationMirror) {
      SourceBuilder excerptBuilder = subBuilder();
      addSource(excerptBuilder, (AnnotationMirror) arg);
      return excerptBuilder.toString();
    } else {
      return arg;
    }
  }

  private static boolean isLegalType(TypeMirror mirror) {
    return !(new IsInvalidTypeVisitor().visit(mirror));
  }
}
