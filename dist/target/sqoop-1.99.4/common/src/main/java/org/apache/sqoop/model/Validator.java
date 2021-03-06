/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sqoop.model;

import org.apache.sqoop.validation.validators.AbstractValidator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for specifying validators
 *
 * Usage without any parameters:
 * @Validator(ClassName.class)
 *
 * To specify string parameter call:
 * @Validator(value = ClassName.class, strArg = "Hello World!")
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Validator {
  /**
   * Validator implementation that should be executed.
   */
  Class<? extends AbstractValidator> value();

  /**
   * Optional argument that should be given to the validator before execution.
   */
  String strArg() default AbstractValidator.DEFAULT_STRING_ARGUMENT;
}
