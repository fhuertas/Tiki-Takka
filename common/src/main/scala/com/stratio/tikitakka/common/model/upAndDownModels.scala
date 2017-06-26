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

package com.stratio.tikitakka.common.model

import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.Writes

trait Container

case class CreateApp(id: String,
                     cpus: Double,
                     mem: Int,
                     instances: Option[Int] = None,
                     user: Option[String] = None,
                     args: Option[List[String]] = None,
                     env: Option[Map[String, String]] = None,
                     container: ContainerInfo,
                     cmd: Option[String] = None,
                     portDefinitions: Option[Seq[PortDefinition]] = None,
                     requirePorts: Option[Boolean] = None,
                     healthChecks: Option[Seq[HealthCheck]] = None,
                     labels: Map[String, String] = Map.empty[String, String],
                     ports: Option[Seq[Int]] = None,
                     constraints: Option[Seq[Seq[String]]] = None,
                     ipAddress: Option[IpAddress] = None
                    ) extends Container

case class ContainerId(id: String)

case class ContainerInfo(docker: DockerContainerInfo)

case class DockerContainerInfo(image: String,
                               portMappings: Option[Seq[PortMapping]] = None,
                               volumes: Option[Seq[Volume]] = None,
                               network: Option[String] = None,
                               forcePullImage: Option[Boolean] = None,
                               privileged: Option[Boolean] = None,
                               parameters: Option[Seq[Parameter]] = None)

case class Parameter(key: String, value: String)

object Parameter {

  implicit val writes: Writes[Parameter] = Json.writes[Parameter]
  implicit val reads: Reads[Parameter] = Json.reads[Parameter]
}

object ContainerInfo {

  implicit val writes: Writes[ContainerInfo] = Json.writes[ContainerInfo]
  implicit val reads: Reads[ContainerInfo] = Json.reads[ContainerInfo]
}

object DockerContainerInfo {

  implicit val writes: Writes[DockerContainerInfo] = Json.writes[DockerContainerInfo]
  implicit val reads: Reads[DockerContainerInfo] = Json.reads[DockerContainerInfo]
}

object CreateApp {

  implicit val writes: Writes[CreateApp] = Json.writes[CreateApp]
  implicit val reads: Reads[CreateApp] = Json.reads[CreateApp]
}

case class Volume(containerPath: String, hostPath: String, mode: String)

object Volume {

  implicit val writes: Writes[Volume] = Json.writes[Volume]
  implicit val reads: Reads[Volume] = Json.reads[Volume]
}

case class PortMapping(hostPort: Int,
                       containerPort: Int,
                       servicePort: Option[Int] = None,
                       protocol: Option[String] = None,
                       labels: Option[Map[String, String]] = None)

object PortMapping {

  implicit val writes: Writes[PortMapping] = Json.writes[PortMapping]
  implicit val reads: Reads[PortMapping] = Json.reads[PortMapping]
}

case class PortDefinition(name: Option[String] = None,
                          port: Int,
                          protocol: Option[String] = None,
                          labels: Option[Map[String, String]] = None)

object PortDefinition {

  implicit val writes: Writes[PortDefinition] = Json.writes[PortDefinition]
  implicit val reads: Reads[PortDefinition] = Json.reads[PortDefinition]
}

case class HealthCheck(
                        protocol: String,
                        path: Option[String] = None,
                        portIndex: Option[Int] = None,
                        timeoutSeconds: Int,
                        gracePeriodSeconds: Int,
                        intervalSeconds: Int,
                        maxConsecutiveFailures: Int,
                        command: Option[HealthCheckCommand] = None,
                        ignoreHttp1xx: Option[Boolean] = None)

object HealthCheck {

  implicit val writes: Writes[HealthCheck] = Json.writes[HealthCheck]
  implicit val reads: Reads[HealthCheck] = Json.reads[HealthCheck]
}

case class HealthCheckCommand(value: String)

object HealthCheckCommand {

  implicit val writes: Writes[HealthCheckCommand] = Json.writes[HealthCheckCommand]
  implicit val reads: Reads[HealthCheckCommand] = Json.reads[HealthCheckCommand]
}

case class IpAddress(
                      networkName: Option[String] = None,
                      discovery: Option[DiscoveryInfo] = None,
                      groups: Option[Seq[String]] = None,
                      labels: Option[Map[String, String]] = None
                    )

object IpAddress {

  implicit val writes: Writes[IpAddress] = Json.writes[IpAddress]
  implicit val reads: Reads[IpAddress] = Json.reads[IpAddress]
}

case class DiscoveryInfo(ports: Seq[PortAddressDefinition])

object DiscoveryInfo {

  implicit val writes: Writes[DiscoveryInfo] = Json.writes[DiscoveryInfo]
  implicit val reads: Reads[DiscoveryInfo] = Json.reads[DiscoveryInfo]
}

case class PortAddressDefinition(number: Int,
                                 name: String,
                                 protocol: String)

object PortAddressDefinition {

  implicit val writes: Writes[PortAddressDefinition] = Json.writes[PortAddressDefinition]
  implicit val reads: Reads[PortAddressDefinition] = Json.reads[PortAddressDefinition]
}

