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

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import com.stratio.tikitakka.common.message._
import com.stratio.tikitakka.common.model.discovery.DiscoveryAppInfo

import scalaz.Reader

class ServiceActor(discoveryActor: ActorRef, orchestrator: ActorRef) extends Actor with ActorLogging {

  var status: Option[DiscoveryAppInfo] = None
  var appName: String = "UNKNOWN"

  override def receive: Receive = {
    case ManageApp(name) =>
      appName = name
      log.debug(s"[$appName] trigger discovery check")
      discoveryActor ! DiscoverService(appName)
      context.become(ready)

    case _ => log.debug(s"Unknown message")
  }

  def ready: Receive = {
    case AppDiscovered(Some(appDiscovered)) =>
      log.info(s"[${appDiscovered.name}] discovered with id: ${appDiscovered.id}")

      if(Option(appDiscovered) != status) {
        status = Option(appDiscovered)
        orchestrator ! RegisterApplication(appDiscovered)
      } else {
        log.debug(s"[${appDiscovered.name} discovered status has no changes]")
      }

    case AppDiscovered(None) =>
      log.debug(s"[${status.map(_.name).getOrElse("UNKNOWN")}] not discovered sending message to killing myself")
      context.parent ! AppUnDiscovered(appName)
      status.foreach(st => orchestrator ! UnregisterApplication(st))

    case _ => log.debug(s"Unknown message")
  }

}

object ServiceActor {

  def props = Reader {
    (dependencies: Dependencies) =>
      Props(classOf[ServiceActor], dependencies.discoveryActorRef, dependencies.orchestratorActorRef)
  }

}
