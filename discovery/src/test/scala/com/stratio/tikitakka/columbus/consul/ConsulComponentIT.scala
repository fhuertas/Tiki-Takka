/*
 * Copyright (C) 2017 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.tikitakka.columbus.consul

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.ActorMaterializerSettings
import com.stratio.tikitakka.columbus.test.utils.consul.AgentService
import com.stratio.tikitakka.columbus.test.utils.consul.ConsulUtils
import com.stratio.tikitakka.columbus.test.utils.consul.UnregisterService
import com.stratio.tikitakka.common.util.ConfigComponent
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.ShouldMatchers
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class ConsulComponentIT extends WordSpec with ShouldMatchers with BeforeAndAfterAll with ConsulUtils {

  implicit val system = ActorSystem("Actor-Test-System")
  implicit val actorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))
  implicit val uri = ConfigComponent.config.getString(ConsulComponent.uriField)

  val datasourceTags = List[String]("datas")
  val agentTags = List[String]("dgagent")
  val appServiceTags = List[String]("appservices")
  val allTags = datasourceTags ++ agentTags
  val datasourceServices = (0 to 5).map(_ => AgentService.randomObject.copy(Tags = datasourceTags))
  val agentServices = (0 to 5).map(_ => AgentService.randomObject.copy(Tags = agentTags))
  val serviceName = "service"
  val appServices = (0 to 2).map(i => AgentService.randomObject.
    copy(ID = s"$serviceName$i", Service = serviceName, Tags = appServiceTags))
  val services = datasourceServices ++ agentServices ++ appServices
  val datacenter = Await.result(getDatacenter, 3 seconds)
  val nodeCatalog = Await.result(getNode, 3 seconds)
  val catalogServices = services.map { service => service.toCatalogService(datacenter, nodeCatalog)}
  val unregisterServiceModels = catalogServices.map { c => UnregisterService(c.Datacenter, c.Node, c.Service.ID)}
  val datasourceServiceMap = datasourceServices.map(s => s.Service -> s.Tags).toMap
  val agentServiceMap = agentServices.map(s => s.Service -> s.Tags).toMap
  val taggedServicesMap = datasourceServiceMap ++ agentServiceMap
  val appServicesMap = appServices.map(s => s.Service -> s.Tags)
  val servicesMap = taggedServicesMap ++ appServicesMap ++ Map[String, List[String]]("consul" -> List.empty[String])
  val tagList = allTags ++ appServiceTags

  trait ActorTestSystem {

    implicit val system: ActorSystem = ActorSystem("Actor-Test-System")
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))
    val timeout = 3 seconds

  }

  override def beforeAll(): Unit = new ActorTestSystem {
    Await.result(registerServices(catalogServices.toList), timeout)
  }

  "ConsulComponent" should {

    "know if the discovery service is up" in new ConsulComponent with ActorTestSystem {

      Await.result(isUp, timeout) should be(true)

    }

    "know if the discovery service is down" in new ConsulComponent with ActorTestSystem {

      override val uri = "fakeHost"

      Await.result(isUp, timeout) should be(false)

    }

    "get all services and its info from discovery service" in new ConsulComponent with ActorTestSystem {

      Await.result(discover(), timeout).filter(s => s._2.isEmpty || tagList.contains(s._2.head)) should equal (servicesMap)

    }

    "get services and its info filtered by its tags from discovery service" in new ConsulComponent
      with ActorTestSystem {

      Await.result(discover(datasourceTags), timeout) should equal(datasourceServiceMap)
      Await.result(discover(agentTags), timeout) should equal(agentServiceMap)
      Await.result(discover(allTags), timeout) should equal(taggedServicesMap)

    }

    "return all services related to the same app" in new ConsulComponent with ActorTestSystem {

      Await.result(discover(serviceName), timeout).get.services should have (size (appServices.size))

    }

    "return none if service is not found" in new ConsulComponent with ActorTestSystem {

      Await.result(discover("unknown-service"), timeout) shouldBe None

    }

  }

  override def afterAll(): Unit = new ActorTestSystem {
    Await.result(unregisterServices(unregisterServiceModels.toList), timeout)
  }

}
