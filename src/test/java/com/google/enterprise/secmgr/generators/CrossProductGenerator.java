// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.secmgr.generators;

import com.google.common.base.Preconditions;

/**
 * A generator that supplies a "cross product" of some given generators.  This
 * means that it generates a sequence of arrays, each element of which comes
 * from the corresponding generator.  All possible combinations of elements are
 * generated, so the length of the sequence generated by this generator is the
 * product of the lengths of all the input sequences.  Each element of the
 * output sequence is an array whose length is the same as the number of given
 * generators.  So if this is a cross product of three generators, the output
 * sequence will consist of arrays of length three.
 */
final class CrossProductGenerator implements Generator {
  private static final Generator NULL_SET = Generators.of(new Object[0]);
  private final Generator[] generators;

  private CrossProductGenerator(Generator[] generators) {
    this.generators = generators;
  }

  static Generator make(Generator... generators) {
    for (Generator generator : generators) {
      Preconditions.checkNotNull(generator);
    }
    return (generators.length > 0)
        ? new CrossProductGenerator(generators)
        : NULL_SET;
  }

  @Override
  public Generator.Iterator iterator() {
    return new LocalIterator(generators);
  }

  // Made package-local so that JoiningGenerator can extend it.
  static class LocalIterator extends Generator.Iterator {
    protected final Generator[] generators;
    protected Generator.Iterator[] iterators;

    public LocalIterator(Generator[] generators) {
      this.generators = generators;
      iterators = new Generator.Iterator[generators.length];
      for (int i = 0; i < generators.length; i++) {
        iterators[i] = generators[i].iterator();
      }
    }

    @Override
    public boolean hasNext() {
      for (int i = 0; i < generators.length; i++) {
        if (!iterators[i].hasNext()) {
          return false;
        }
      }
      return true;
    }

    @Override
    public Object next() {
      Object result = peek();
      int i = generators.length;
      while (true) {
        iterators[--i].next();
        if (iterators[i].hasNext() || i <= 0) {
          break;
        }
        iterators[i] = generators[i].iterator();
      }
      return result;
    }

    @Override
    public Object peek() {
      Object[] results = new Object[generators.length];
      for (int i = 0; i < generators.length; i++) {
        results[i] = iterators[i].peek();
      }
      return results;
    }
  }
}