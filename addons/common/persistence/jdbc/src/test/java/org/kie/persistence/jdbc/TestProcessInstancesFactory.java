/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.persistence.jdbc;

import javax.sql.DataSource;

import org.kie.kogito.persistence.jdbc.AbstractProcessInstancesFactory;
import org.kie.kogito.persistence.jdbc.JDBCProcessInstances;
import org.kie.kogito.process.Process;

import static org.mockito.Mockito.spy;

public class TestProcessInstancesFactory extends AbstractProcessInstancesFactory {

    public TestProcessInstancesFactory(DataSource dataSource, boolean lock) {
        super(dataSource, lock);
    }

    @Override
    public JDBCProcessInstances createProcessInstances(Process<?> process) {
        return spy(super.createProcessInstances(process));
    }

}
