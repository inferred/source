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

import static com.google.common.collect.Lists.newArrayList;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.INTERFACE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.inferred.internal.testing.Partial;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

/**
 * Fake implementation of {@link DeclaredType} for unit tests.
 */
public abstract class ClassTypeImpl implements DeclaredType {

  private final Element enclosingElement;
  private final TypeMirror enclosingType;
  private final String simpleName;
  private final ElementKind kind;
  private final ImmutableSet<Modifier> modifiers;
  private final ImmutableList<TypeMirror> interfaces;

  public static class Builder {
    Element enclosingElement = PackageElementImpl.create("org.example");
    TypeMirror enclosingType = NoTypes.NONE;
    String simpleName;
    ElementKind kind = CLASS;
    EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
    List<TypeMirror> interfaces = newArrayList();

    private Builder(String simpleName) {
      this.simpleName = simpleName;
    }

    public Builder inPackage(String qualifiedName) {
      this.enclosingElement = PackageElementImpl.create(qualifiedName);
      this.enclosingType = NoTypes.NONE;
      return this;
    }

    public Builder nestedIn(TypeElement enclosingElement) {
      this.enclosingElement = enclosingElement;
      this.enclosingType = NoTypes.NONE;
      return this;
    }

    public Builder innerClassIn(DeclaredType enclosingType) {
      this.enclosingElement = enclosingType.asElement();
      this.enclosingType = enclosingType;
      return this;
    }

    public Builder implementing(TypeMirror iface) {
      interfaces.add(iface);
      return this;
    }

    public Builder implementing(TypeElement iface) {
      interfaces.add(iface.asType());
      return this;
    }

    public ClassTypeImpl asMirror() {
      return Partial.of(ClassTypeImpl.class, this);
    }

    public TypeElement asElement() {
      return asMirror().asElement();
    }
  }

  /**
   * Returns a {@link Builder} initially configured to create a {@link DeclaredType} for a
   * top-level class called {@code simpleName}, in package {@code org.example}, with no
   * supertype or interfaces.
   */
  public static Builder type(String simpleName) {
    return new Builder(simpleName);
  }

  /**
   * Returns a {@link Builder} initially configured to create a {@link DeclaredType} for a
   * top-level interface called {@code simpleName}, in package {@code org.example}, with no
   * super-interfaces.
   */
  public static Builder iface(String simpleName) {
    Builder builder = new Builder(simpleName);
    builder.kind = INTERFACE;
    builder.modifiers.add(Modifier.ABSTRACT);
    return builder;
  }

  public static ClassTypeImpl newTopLevelClass(String qualifiedName) {
    String pkg = qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
    String simpleName = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
    return type(simpleName).inPackage(pkg).asMirror();
  }

  public static ClassTypeImpl newNestedClass(TypeElement enclosingType, String simpleName) {
    return type(simpleName).nestedIn(enclosingType).asMirror();
  }

  public static ClassTypeImpl newInnerClass(DeclaredType enclosingType, String simpleName) {
    return type(simpleName).innerClassIn(enclosingType).asMirror();
  }

  ClassTypeImpl(Builder builder) {
    this.enclosingElement = builder.enclosingElement;
    this.enclosingType = builder.enclosingType;
    this.simpleName = builder.simpleName;
    this.kind = builder.kind;
    this.modifiers = ImmutableSet.copyOf(builder.modifiers);
    this.interfaces = ImmutableList.copyOf(builder.interfaces);
  }

  @Override
  public TypeKind getKind() {
    return TypeKind.DECLARED;
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitDeclared(this, p);
  }

  @Override
  public ClassElementImpl asElement() {
    return Partial.of(ClassElementImpl.class, this);
  }

  @Override
  public TypeMirror getEnclosingType() {
    return enclosingType;
  }

  @Override
  public List<? extends TypeMirror> getTypeArguments() {
    return ImmutableList.of();
  }

  @Override
  public String toString() {
    final String prefix;
    if (enclosingElement.getKind() == ElementKind.PACKAGE) {
      prefix = ((PackageElement) enclosingElement).getQualifiedName() + ".";
    } else {
      prefix = ((TypeElement) enclosingElement).getQualifiedName() + ".";
    }
    return prefix + simpleName;
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof ClassTypeImpl) && toString().equals(o.toString());
  }

  /**
   * Fake implementation of {@link TypeElement} for unit tests.
   */
  public abstract class ClassElementImpl implements TypeElement {

    @Override
    public ClassTypeImpl asType() {
      return ClassTypeImpl.this;
    }

    @Override
    public ElementKind getKind() {
      return kind;
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
      return v.visitType(this, p);
    }

    @Override
    public NestingKind getNestingKind() {
      return (enclosingElement.getKind() == ElementKind.PACKAGE)
          ? NestingKind.TOP_LEVEL : NestingKind.MEMBER;
    }

    @Override
    public Name getQualifiedName() {
      return new NameImpl(ClassTypeImpl.this.toString());
    }

    @Override
    public Name getSimpleName() {
      return new NameImpl(simpleName);
    }

    @Override
    public TypeMirror getSuperclass() {
      return NoTypes.NONE;
    }

    @Override
    public ImmutableList<TypeMirror> getInterfaces() {
      return interfaces;
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters() {
      return ImmutableList.of();
    }

    @Override
    public Element getEnclosingElement() {
      return enclosingElement;
    }

    @Override
    public Set<Modifier> getModifiers() {
      return modifiers;
    }

    @Override
    public String toString() {
      return kind.toString().toLowerCase() + " " + ClassTypeImpl.this.toString();
    }

    @Override
    public int hashCode() {
      return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
      return (o instanceof ClassElementImpl) && toString().equals(o.toString());
    }
  }
}
