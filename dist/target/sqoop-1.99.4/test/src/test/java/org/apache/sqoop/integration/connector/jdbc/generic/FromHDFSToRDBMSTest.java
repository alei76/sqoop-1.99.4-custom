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
package org.apache.sqoop.integration.connector.jdbc.generic;

import org.apache.sqoop.common.Direction;
import org.apache.sqoop.test.testcases.ConnectorTestCase;
import org.apache.sqoop.model.MDriverConfig;
import org.apache.sqoop.model.MLink;
import org.apache.sqoop.model.MConfigList;
import org.apache.sqoop.model.MJob;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class FromHDFSToRDBMSTest extends ConnectorTestCase {

  @Test
  public void testBasic() throws Exception {
    createTableCities();
    createFromFile("input-0001",
      "1,'USA','San Francisco'",
      "2,'USA','Sunnyvale'",
      "3,'Czech Republic','Brno'",
      "4,'USA','Palo Alto'"
    );

    // RDBMS link
    MLink rdbmsLink = getClient().createLink("generic-jdbc-connector");
    fillRdbmsLinkConfig(rdbmsLink);
    saveLink(rdbmsLink);

    // HDFS link
    MLink hdfsLink = getClient().createLink("hdfs-connector");
    saveLink(hdfsLink);

    // Job creation
    MJob job = getClient().createJob(hdfsLink.getPersistenceId(), rdbmsLink.getPersistenceId());

    // set hdfs "FROM" config for the job, since the connector test case base class only has utilities for hdfs!
    fillHdfsFromConfig(job);

    // set the rdms "TO" config here
    MConfigList toConfig = job.getJobConfig(Direction.TO);
    toConfig.getStringInput("toJobConfig.tableName").setValue(provider.escapeTableName(getTableName()));

    // driver config
    MDriverConfig driverConfig = job.getDriverConfig();
    driverConfig.getIntegerInput("throttlingConfig.numExtractors").setValue(3);
    saveJob(job);

    executeJob(job);

    assertEquals(4L, rowCount());
    assertRowInCities(1, "USA", "San Francisco");
    assertRowInCities(2, "USA", "Sunnyvale");
    assertRowInCities(3, "Czech Republic", "Brno");
    assertRowInCities(4, "USA", "Palo Alto");

    // Clean up testing table
    dropTable();
  }

}
