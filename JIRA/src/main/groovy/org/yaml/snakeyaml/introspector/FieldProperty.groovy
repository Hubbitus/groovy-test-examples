/**
 * Copyright (c) 2008, SnakeYAML
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.yaml.snakeyaml.introspector


import java.lang.annotation.Annotation;
import java.lang.reflect.Field

import org.yaml.snakeyaml.util.ArrayUtils

import java.lang.reflect.InaccessibleObjectException;

/**
* Hack-workaround of the bug: https://bitbucket.org/snakeyaml/snakeyaml/issues/1092/inaccessibleobjectexception-in-jdk-9
* if we can't get fields value - we just return empty Object().
* At least we provide info about readable fields (most non-JDK should be open
* TODO Strange, but this does not work in ScriptRunner. Probably due to the ClassLoading issue.
**/
public class FieldProperty extends GenericProperty {

  public static class FieldUnreadable {}

  private final Field field;

  public FieldProperty(Field field) {
    super(field.getName(), field.getType(), field.getGenericType());
    this.field = field;
    try {
      field.setAccessible(true);
    }
    catch (InaccessibleObjectException ignored) {}
  }

  @Override
  public void set(Object object, Object value) throws Exception {
    field.set(object, value);
  }

  @Override
  public Object get(Object object) {
    try {
      return field.get(object);
    } catch (Exception e) {
//*      throw new YAMLException(
//*          "Unable to access field " + field.getName() + " on object " + object + " : " + e);
      return new FieldUnreadable();
    }
  }

  @Override
  public List<Annotation> getAnnotations() {
    return ArrayUtils.toUnmodifiableList(field.getAnnotations());
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return field.getAnnotation(annotationType);
  }
}
