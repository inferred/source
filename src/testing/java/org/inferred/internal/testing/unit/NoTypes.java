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

import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

/**
 * Fake implementation of {@link NoType} for unit tests.
 */
public abstract class NoTypes implements NoType {

  public static final NoType NONE = Partial.of(NoTypes.class, TypeKind.NONE);

  private final TypeKind kind;

  NoTypes(TypeKind kind) {
    this.kind = kind;
  }

  @Override
  public TypeKind getKind() {
    return kind;
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitNoType(this, p);
  }

  @Override
  public String toString() {
    return getKind().toString().toLowerCase();
  }
}

