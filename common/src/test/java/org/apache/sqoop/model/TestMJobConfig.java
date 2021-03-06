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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestMJobConfig {
  /**
   * Test for class initialization and values
   */
  @Test
  public void testInitialization() {
    List<MConfig> configs = new ArrayList<MConfig>();
    MFromConfig fromJobConfig = new MFromConfig(configs);
    List<MConfig> configs2 = new ArrayList<MConfig>();
    MFromConfig fromJobConfig2 = new MFromConfig(configs2);
    assertEquals(fromJobConfig2, fromJobConfig);
    MConfig c = new MConfig("test", null);
    configs2.add(c);
    assertFalse(fromJobConfig.equals(fromJobConfig2));
  }
}
