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
package org.inferred.internal.testing.unit;

import org.inferred.internal.testing.Partial;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

public abstract class PackageElementImpl implements PackageElement {

  public static PackageElementImpl create(String qualifiedName) {
    return Partial.of(PackageElementImpl.class, qualifiedName);
  }

  private final String qualifiedName;

  PackageElementImpl(String qualifiedName) {
    this.qualifiedName = qualifiedName;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof PackageElementImpl)
        && qualifiedName.equals(((PackageElementImpl) obj).qualifiedName);
  }

  @Override
  public int hashCode() {
    return qualifiedName.hashCode();
  }

  @Override
  public String toString() {
    return "package " + qualifiedName;
  }

  @Override
  public TypeMirror asType() {
    return Partial.of(PackageTypeImpl.class, qualifiedName);
  }

  @Override
  public ElementKind getKind() {
    return ElementKind.PACKAGE;
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitPackage(this, p);
  }

  @Override
  public Name getQualifiedName() {
    return new NameImpl(qualifiedName);
  }

  @Override
  public Name getSimpleName() {
    String simpleName = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
    return new NameImpl(simpleName);
  }

  @Override
  public Element getEnclosingElement() {
    return null;
  }

  @Override
  public boolean isUnnamed() {
    return false;
  }

  abstract static class PackageTypeImpl implements NoType {

    private final String qualifiedName;

    private PackageTypeImpl(String qualifiedName) {
      this.qualifiedName = qualifiedName;
    }

    @Override
    public TypeKind getKind() {
      return TypeKind.PACKAGE;
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p) {
      return v.visitNoType(this, p);
    }

    @Override
    public boolean equals(Object obj) {
      return (obj instanceof PackageTypeImpl)
        && toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
      return qualifiedName.hashCode();
    }

    @Override
    public String toString() {
      return "package " + qualifiedName;
    }
  }
}

