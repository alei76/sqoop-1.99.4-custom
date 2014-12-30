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
package org.apache.sqoop.repository.derby;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.sqoop.model.MBooleanInput;
import org.apache.sqoop.model.MConfig;
import org.apache.sqoop.model.MConnector;
import org.apache.sqoop.model.MDriver;
import org.apache.sqoop.model.MEnumInput;
import org.apache.sqoop.model.MInput;
import org.apache.sqoop.model.MIntegerInput;
import org.apache.sqoop.model.MLink;
import org.apache.sqoop.model.MLinkConfig;
import org.apache.sqoop.model.MMapInput;
import org.apache.sqoop.model.MPersistableEntity;
import org.apache.sqoop.model.MStringInput;
import org.junit.Before;
import org.junit.Test;

/**
 * Test proper support of all available model types.
 */
public class TestInputTypes extends DerbyTestCase {

  DerbyRepositoryHandler handler;

  @Before
  public void setUp() throws Exception {
    super.setUp();

    handler = new DerbyRepositoryHandler();
    // We always needs schema for this test case
    createOrUpgradeSchemaForLatestVersion();
  }

  /**
   * Ensure that metadata with all various data types can be successfully
   * serialized into repository and retrieved back.
   */
  @Test
  public void testEntitySerialization() throws Exception {
    MConnector connector = getConnector();

    // Serialize the connector with all data types into repository
    handler.registerConnector(connector, getDerbyDatabaseConnection());

    // Successful serialization should update the ID
    assertNotSame(connector.getPersistenceId(), MPersistableEntity.PERSISTANCE_ID_DEFAULT);

    // Retrieve registered connector
    MConnector retrieved = handler.findConnector(connector.getUniqueName(), getDerbyDatabaseConnection());
    assertNotNull(retrieved);

    // Original and retrieved connectors should be the same
    assertEquals(connector, retrieved);
  }

  /**
   * Test that serializing actual data is not an issue.
   */
  @Test
  public void testEntityDataSerialization() throws Exception {
    MConnector connector = getConnector();
    MDriver driver = getDriver();

    // Register objects for everything and our new connector
    handler.registerConnector(connector, getDerbyDatabaseConnection());
    handler.registerDriver(driver, getDerbyDatabaseConnection());

    // Inserted values
    Map<String, String> map = new HashMap<String, String>();
    map.put("A", "B");

    // Connection object with all various values
    MLink link = new MLink(connector.getPersistenceId(), connector.getLinkConfig());
    MLinkConfig forms = link.getConnectorLinkConfig();
    forms.getStringInput("f.String").setValue("A");
    forms.getMapInput("f.Map").setValue(map);
    forms.getIntegerInput("f.Integer").setValue(1);
    forms.getBooleanInput("f.Boolean").setValue(true);
    forms.getEnumInput("f.Enum").setValue("YES");

    // Create the link in repository
    handler.createLink(link, getDerbyDatabaseConnection());
    assertNotSame(link.getPersistenceId(), MPersistableEntity.PERSISTANCE_ID_DEFAULT);

    // Retrieve created link
    MLink retrieved = handler.findLink(link.getPersistenceId(), getDerbyDatabaseConnection());
    forms = retrieved.getConnectorLinkConfig();
    assertEquals("A", forms.getStringInput("f.String").getValue());
    assertEquals(map, forms.getMapInput("f.Map").getValue());
    assertEquals(1, (int)forms.getIntegerInput("f.Integer").getValue());
    assertEquals(true, (boolean)forms.getBooleanInput("f.Boolean").getValue());
    assertEquals("YES", forms.getEnumInput("f.Enum").getValue());
  }

  /**
   * Overriding parent method to get forms with all supported data types.
   *
   * @return Forms with all data types
   */
  @Override
  protected List<MConfig> getConfigs() {
    List<MConfig> forms = new LinkedList<MConfig>();

    List<MInput<?>> inputs;
    MInput input;

    inputs = new LinkedList<MInput<?>>();

    input = new MStringInput("f.String", false, (short)30);
    inputs.add(input);

    input = new MMapInput("f.Map", false);
    inputs.add(input);

    input = new MIntegerInput("f.Integer", false);
    inputs.add(input);

    input = new MBooleanInput("f.Boolean", false);
    inputs.add(input);

    input = new MEnumInput("f.Enum", false, new String[] {"YES", "NO"});
    inputs.add(input);

    forms.add(new MConfig("f", inputs));
    return forms;
  }
}
