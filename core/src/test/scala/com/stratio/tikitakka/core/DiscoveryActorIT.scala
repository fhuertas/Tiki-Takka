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
package com.stratio.tikitakka.core

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.ActorMaterializerSettings
import akka.testkit.ImplicitSender
import akka.testkit.TestActorRef
import akka.testkit.TestKit
import akka.testkit.TestProbe
import com.stratio.tikitakka.columbus.consul.ConsulComponent
import com.stratio.tikitakka.columbus.test.utils.consul.AgentService
import com.stratio.tikitakka.columbus.test.utils.consul.ConsulUtils
import com.stratio.tikitakka.columbus.test.utils.consul.UnregisterService
import com.stratio.tikitakka.common.message.AppsDiscovered
import com.stratio.tikitakka.common.message.DiscoverServices
import com.stratio.tikitakka.common.util.ConfigComponent
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.ShouldMatchers
import org.scalatest.WordSpecLike
import org.scalatest.junit.JUnitRunner
import scala.concurrent.duration._

import scala.concurrent.Await

@RunWith(classOf[JUnitRunner])
class DiscoveryActorIT extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with WordSpecLike
  with ShouldMatchers
  with BeforeAndAfterAll
  with ConsulUtils{

  implicit val testSystem = ActorSystem("test")
  implicit val actorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))
  implicit val uri = ConfigComponent.config.getString(ConsulComponent.uriField)

  val services = (0 to 4).map(_ => AgentService.randomObject.copy(Tags = List[String]("theTag")))
  val servicesMap = services.map(s => s.Service -> s.Tags)
  val datacenter = Await.result(getDatacenter, 3 seconds)
  val nodeCatalog = Await.result(getNode, 3 seconds)
  val catalogServices = services.map { service => service.toCatalogService(datacenter, nodeCatalog)}
  val unregisterServiceModels = catalogServices.map { c => UnregisterService(c.Datacenter, c.Node, c.Service.ID)}
  val timeout = 3 seconds

  override def beforeAll(): Unit = {
    Await.result(registerServices(catalogServices.toList), timeout)
  }

  "Discovery Actor" should {

    "Discover new services" in {

      val orchestratorActor = TestProbe()
      val service = ConsulComponent(system, actorMaterializer)
      val servicesActor = TestActorRef[ServicesActor](new ServicesActor(orchestratorActor.ref))
      val discoveryActor = TestActorRef[DiscoveryActor](new DiscoveryActor(service, servicesActor))

      discoveryActor ! DiscoverServices(List("theTag"))
      expectMsg(AppsDiscovered(servicesMap.toMap))
      Thread.sleep(1000)
      servicesActor.underlyingActor.services.keys should contain theSameElementsAs servicesMap.toMap.keySet
      servicesMap.toMap.keySet.foreach(k => servicesActor.underlyingActor.context.child(k) should not be None)
    }
  }

  override def afterAll(): Unit = {
    Await.result(unregisterServices(unregisterServiceModels.toList), timeout)
  }
}
