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
package org.apache.sqoop.handler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.apache.sqoop.audit.AuditLoggerManager;
import org.apache.sqoop.common.SqoopException;
import org.apache.sqoop.connector.ConnectorManager;
import org.apache.sqoop.json.ConnectorBean;
import org.apache.sqoop.json.ConnectorsBean;
import org.apache.sqoop.json.JsonBean;
import org.apache.sqoop.model.MConnector;
import org.apache.sqoop.server.RequestContext;
import org.apache.sqoop.server.RequestContext.Method;
import org.apache.sqoop.server.RequestHandler;
import org.apache.sqoop.server.common.ServerError;

public class ConnectorRequestHandler implements RequestHandler {

  private static final Logger LOG = Logger.getLogger(ConnectorRequestHandler.class);

  private static final String CONNECTORS_PATH = "connectors";

  public ConnectorRequestHandler() {
    LOG.info("ConnectorRequestHandler initialized");
  }

  @Override
  public JsonBean handleEvent(RequestContext ctx) {
    // connector only support GET requests
    if (ctx.getMethod() != Method.GET) {
      throw new SqoopException(ServerError.SERVER_0002, "Unsupported HTTP method for connector:"
          + ctx.getMethod());
    }

    List<MConnector> connectors;
    Map<Long, ResourceBundle> configParamBundles;
    Locale locale = ctx.getAcceptLanguageHeader();
    String cIdentifier = ctx.getLastURLElement();

    LOG.info("ConnectorRequestHandler handles cid: " + cIdentifier);

    if (ctx.getPath().contains(CONNECTORS_PATH) || cIdentifier.equals("all")) {
      connectors = ConnectorManager.getInstance().getConnectorConfigurables();
      configParamBundles = ConnectorManager.getInstance().getResourceBundles(locale);
      AuditLoggerManager.getInstance().logAuditEvent(ctx.getUserName(),
          ctx.getRequest().getRemoteAddr(), "get", "connectors", "all");
      return new ConnectorsBean(connectors, configParamBundles);

    } else {
      // NOTE: we now support using unique name as well as the connector id
      // NOTE: connectorId is a fallback for older sqoop clients if any, since we want to primarily use unique conenctorNames
      boolean cIdNameIdentfierUsed = true;
      Long cId = ConnectorManager.getInstance().getConnectorId(cIdentifier);
      if (cId == null) {
        // support for cId in the query
        cIdNameIdentfierUsed = false;
        cId = Long.parseLong(cIdentifier);
      }
      // Check that user is not asking for non existing connector id or non
      // existing unique connector name
      if (!cIdNameIdentfierUsed && !ConnectorManager.getInstance().getConnectorIds().contains(cId)) {
        throw new SqoopException(ServerError.SERVER_0004, "Invalid connector id " + cId);
      }

      connectors = new LinkedList<MConnector>();
      configParamBundles = new HashMap<Long, ResourceBundle>();

      connectors.add(ConnectorManager.getInstance().getConnectorConfigurable(cId));
      configParamBundles.put(cId, ConnectorManager.getInstance().getResourceBundle(cId, locale));

      AuditLoggerManager.getInstance().logAuditEvent(ctx.getUserName(),
          ctx.getRequest().getRemoteAddr(), "get", "connector", String.valueOf(cIdentifier));
      return new ConnectorBean(connectors, configParamBundles);
    }
  }
}