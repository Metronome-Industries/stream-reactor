/*
 * Copyright 2017 Datamountaineer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.datamountaineer.streamreactor.connect.kudu

import com.datamountaineer.streamreactor.connect.kudu.config.KuduConfigConstants
import com.datamountaineer.streamreactor.connect.kudu.sink.{KuduSinkConnector, KuduSinkTask}

import scala.jdk.CollectionConverters.ListHasAsScala



/**
  * Created by andrew@datamountaineer.com on 24/02/16. 
  * stream-reactor
  */
class TestKuduSinkConnector extends TestBase {
  "Should start a Kudu Connector" in {
    val config = getConfig
    val connector = new KuduSinkConnector()
    connector.start(config)
    val taskConfigs = connector.taskConfigs(1).asScala
    taskConfigs.head.get(KuduConfigConstants.KUDU_MASTER) shouldBe KUDU_MASTER
    taskConfigs.size shouldBe 1
    connector.taskClass() shouldBe classOf[KuduSinkTask]
    connector.stop()
  }
}
