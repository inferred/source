/*
 * Copyright 2015 Google Inc. All rights reserved.
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

/**
 * Utility methods related to dependencies being relocated as part of shading. The following
 * assumptions are made: <ul>
 * <li>if any class is shaded, this one is;
 * <li>all packages are shaded into a single, common shade package; and
 * <li>the original package name is completely preserved.
 * </ul>
 *
 * <p>For example, if {@code com.google.common.collect.ImmutableList} is shaded to
 * {@code org.example.shaded.com.google.common.collect.ImmutableList}, {@link #unshadedName}
 * will reverse the shading, allowing {@code ImmutableList.class} to be used as a clean reference
 * within code generation.
 */
public class Shading {

  private static final String SHADE_PACKAGE;
  static {
    String shadedPackageName = Shading.class.getPackage().getName();
    String unshadedPackageName = "org." + "inferred." + "internal." + "source";
    if (!shadedPackageName.endsWith(unshadedPackageName)) {
      throw new AssertionError(String.format(
          "Invalid shading: '%s' must end with '%s'", shadedPackageName, unshadedPackageName));
    }
    SHADE_PACKAGE = shadedPackageName.substring(
        0, shadedPackageName.length() - unshadedPackageName.length());
  }

  public static String unshadedName(String qualifiedName) {
    if (qualifiedName.startsWith(Shading.SHADE_PACKAGE)) {
      return qualifiedName.substring(Shading.SHADE_PACKAGE.length());
    } else {
      return qualifiedName;
    }
  }
}
