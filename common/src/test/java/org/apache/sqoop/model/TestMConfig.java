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

public class TestMConfig {

  /**
   * Test for initialization
   */
  @Test
  public void testInitialization() {
    MInput<String> input1 = new MStringInput("sqoopsqoop1", true, (short) 5);
    MInput<String> input2 = new MStringInput("sqoopsqoop2", true, (short) 5);

    List<MInput<?>> list = new ArrayList<MInput<?>>();
    list.add(input1);
    list.add(input2);
    MConfig mConfig = new MConfig("config", list);

    assertEquals("config", mConfig.getName());
    assertEquals(2, mConfig.getInputs().size());
  }

  /**
   * Test for equals method
   */
  @Test
  public void testEquals() {
    MInput<Integer> input1 = new MIntegerInput("sqoopsqoop1", false);
    MInput<Integer> input2 = new MIntegerInput("sqoopsqoop2", false);
    List<MInput<?>> list1 = new ArrayList<MInput<?>>();
    list1.add(input1);
    list1.add(input2);
    MConfig mform1 = new MConfig("config", list1);

    MInput<Integer> input3 = new MIntegerInput("sqoopsqoop1", false);
    MInput<Integer> input4 = new MIntegerInput("sqoopsqoop2", false);
    List<MInput<?>> list2 = new ArrayList<MInput<?>>();
    list2.add(input3);
    list2.add(input4);
    MConfig mform2 = new MConfig("config", list2);
    assertEquals(mform2, mform1);
  }

  @Test
  public void testGetInputs() {
    MIntegerInput intInput = new MIntegerInput("Config.A", false);
    MMapInput mapInput = new MMapInput("Config.B", false);
    MStringInput stringInput = new MStringInput("Config.C", false, (short)3);
    MEnumInput enumInput = new MEnumInput("Config.D", false, new String[] {"I", "V"});

    List<MInput<?>> inputs = new ArrayList<MInput<?>>();
    inputs.add(intInput);
    inputs.add(mapInput);
    inputs.add(stringInput);
    inputs.add(enumInput);

    MConfig config = new MConfig("Config", inputs);
    assertEquals(intInput, config.getIntegerInput("Config.A"));
    assertEquals(mapInput, config.getMapInput("Config.B"));
    assertEquals(stringInput, config.getStringInput("Config.C"));
    assertEquals(enumInput, config.getEnumInput("Config.D"));
  }
}
