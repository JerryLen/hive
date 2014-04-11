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

package org.apache.hadoop.hive.ql.lockmgr;

import org.apache.hadoop.hive.ql.metadata.DummyPartition;
import org.apache.hadoop.hive.ql.metadata.Partition;
import org.apache.hadoop.hive.ql.metadata.Table;

public class HiveLockObject {
  String [] pathNames = null;

  public static class HiveLockObjectData {

    private String queryId;  // queryId of the command
    private String lockTime; // time at which lock was acquired
    // mode of the lock: EXPLICIT(lock command)/IMPLICIT(query)
    private String lockMode;
    private String queryStr;
    private String clientIp;

    /**
     * Constructor
     *
     * Note: The parameters are used to uniquely identify a HiveLockObject.
     * The parameters will be stripped off any ':' characters in order not
     * to interfere with the way the data is serialized (':' delimited string).
     */
    public HiveLockObjectData(String queryId,
                              String lockTime,
                              String lockMode,
                              String queryStr) {
      this.queryId = removeDelimiter(queryId);
      this.lockTime = removeDelimiter(lockTime);
      this.lockMode = removeDelimiter(lockMode);
      this.queryStr = removeDelimiter(queryStr.trim());
    }

    /**
     * Constructor
     *
     * @param data String of the form "queryId:lockTime:lockMode:queryStr".
     * No ':' characters are allowed in any of the components.
     */
    public HiveLockObjectData(String data) {
      if (data == null) {
        return;
      }

      String[] elem = data.split(":");
      queryId  = elem[0];
      lockTime = elem[1];
      lockMode = elem[2];
      queryStr = elem[3];
    }

    public String getQueryId() {
      return queryId;
    }

    public String getLockTime() {
      return lockTime;
    }

    public String getLockMode() {
      return lockMode;
    }

    public String getQueryStr() {
      return queryStr;
    }

    @Override
    public String toString() {
      return queryId + ":" + lockTime + ":" + lockMode + ":" + queryStr + ":"
          + clientIp;
    }

    public String getClientIp() {
      return this.clientIp;
    }

    public void setClientIp(String clientIp) {
      this.clientIp = clientIp;
    }
  }

  /* user supplied data for that object */
  private HiveLockObjectData data;

  public HiveLockObject() {
    this.data = null;
  }

  public HiveLockObject(String path, HiveLockObjectData lockData) {
    this.pathNames = new String[1];
    this.pathNames[0] = path;
    this.data = lockData;
  }

  public HiveLockObject(String[] paths, HiveLockObjectData lockData) {
    this.pathNames = paths;
    this.data = lockData;
  }

  public HiveLockObject(Table tbl, HiveLockObjectData lockData) {
    this(new String[] {tbl.getDbName(), tbl.getTableName()}, lockData);
  }

  public HiveLockObject(Partition par, HiveLockObjectData lockData) {
    this(new String[] { par.getTable().getDbName(),
        par.getTable().getTableName(), par.getName() }, lockData);
  }

  public HiveLockObject(DummyPartition par, HiveLockObjectData lockData) {
    this(new String[] { par.getName() }, lockData);
  }

  public String getName() {
    if (this.pathNames == null) {
      return null;
    }
    String ret = "";
    boolean first = true;
    for (int i = 0; i < pathNames.length; i++) {
      if (!first) {
        ret = ret + "/";
      } else {
        first = false;
      }
      ret = ret + pathNames[i];
    }
    return ret;
  }

  public String getDisplayName() {
    if (this.pathNames == null) {
      return null;
    }
    if (pathNames.length == 1) {
      return pathNames[0];
    }
    else if (pathNames.length == 2) {
      return pathNames[0] + "@" + pathNames[1];
    }

    String ret = pathNames[0] + "@" + pathNames[1] + "@";
    boolean first = true;
    for (int i = 2; i < pathNames.length; i++) {
      if (!first) {
        ret = ret + "/";
      } else {
        first = false;
      }
      ret = ret + pathNames[i];
    }
    return ret;
  }

  public HiveLockObjectData getData() {
    return data;
  }

  public void setData(HiveLockObjectData data) {
    this.data = data;
  }

  private static String removeDelimiter(String in) {
    if (in == null) {
      return null;
    }
    return in.replaceAll(":","");
  }
}
