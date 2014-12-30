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
package org.apache.sqoop.integration.server;

import org.apache.sqoop.client.ClientError;
import org.apache.sqoop.common.Direction;
import org.apache.sqoop.common.SqoopException;
import org.apache.sqoop.connector.hdfs.configuration.ToFormat;
import org.apache.sqoop.driver.DriverError;
import org.apache.sqoop.model.MLink;
import org.apache.sqoop.model.MConfigList;
import org.apache.sqoop.model.MJob;
import org.apache.sqoop.test.testcases.ConnectorTestCase;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Ensure that server will reject starting job when either job itself
 * or corresponding link is disabled.
 */
@RunWith(Parameterized.class)
public class SubmissionWithDisabledModelObjectsTest extends ConnectorTestCase {

  @Parameterized.Parameters(name = "con({0}) job({1})")
  public static Iterable<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {true, false},
        {false, true},
        {false, false},
    });
  }

  private boolean enabledLink;
  private boolean enabledJob;

  public SubmissionWithDisabledModelObjectsTest(boolean enabledLink, boolean enabledJob) {
    this.enabledLink = enabledLink;
    this.enabledJob = enabledJob;
  }

  @Test
  public void testWithDisabledObjects() throws Exception {
    createAndLoadTableCities();

    // RDBMS link
    MLink rdbmsLink = getClient().createLink("generic-jdbc-connector");
    fillRdbmsLinkConfig(rdbmsLink);
    saveLink(rdbmsLink);

    // HDFS link
    MLink hdfsLink = getClient().createLink("hdfs-connector");
    saveLink(hdfsLink);

    // Job creation
    MJob job = getClient().createJob(rdbmsLink.getPersistenceId(), hdfsLink.getPersistenceId());

    // rdms "FROM" config
    MConfigList fromConfig = job.getJobConfig(Direction.FROM);
    fromConfig.getStringInput("fromJobConfig.tableName").setValue(provider.escapeTableName(getTableName()));
    fromConfig.getStringInput("fromJobConfig.partitionColumn").setValue(provider.escapeColumnName("id"));

    // hdfs "TO" config
    fillHdfsToConfig(job, ToFormat.TEXT_FILE);

    // driver config
    MConfigList driverConfig = job.getDriverConfig();
    //driverConfig.getIntegerInput("throttlingConfig.extractors").setValue(3);

    saveJob(job);

    // Disable model entities as per parameterized run
    getClient().enableLink(rdbmsLink.getPersistenceId(), enabledLink);
    getClient().enableJob(job.getPersistenceId(), enabledJob);

    // Try to execute the job and verify that the it was not executed
    try {
      executeJob(job);
      fail("Expected exception as the model classes are disabled.");
    } catch(SqoopException ex) {
      // Top level exception should be CLIENT_0001
      assertEquals(ClientError.CLIENT_0001, ex.getErrorCode());

      // We can directly verify the ErrorCode from SqoopException as client side
      // is not rebuilding SqoopExceptions per missing ErrorCodes. E.g. the cause
      // will be generic Throwable and not SqoopException instance.
      Throwable cause = ex.getCause();
      assertNotNull(cause);

      if(!enabledJob) {
        assertTrue(cause.getMessage().startsWith(DriverError.DRIVER_0009.toString()));
      } else if(!enabledLink) {
        assertTrue(cause.getMessage().startsWith(DriverError.DRIVER_0010.toString()));
      } else {
        fail("Unexpected expception retrieved from server " + cause);
      }
    } finally {
      dropTable();
    }
  }
}
