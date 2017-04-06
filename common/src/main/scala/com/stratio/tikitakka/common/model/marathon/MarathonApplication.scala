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

package com.stratio.tikitakka.common.model.marathon

import play.api.libs.functional.syntax._
import play.api.libs.json._

import com.stratio.tikitakka.common.model._

case class MarathonApplication(id: String,
                               cpus: Double,
                               mem: Int,
                               instances: Option[Int] = None,
                               user: Option[String] = None,
                               args: Option[List[String]] = None,
                               env: Option[Map[String, String]] = None,
                               container: MarathonContainer,
                               cmd: Option[String] = None,
                               portDefinitions: Option[Seq[MarathonPortDefinition]] = None,
                               requirePorts: Option[Boolean] = None,
                               healthChecks: Option[Seq[MarathonHealthCheck]] = None,
                               labels: Map[String, String] = Map.empty[String, String],
                               ports: Option[Seq[Int]] = None) extends Container {
}

object MarathonApplication {

  def apply(buildApp: CreateApp): MarathonApplication =
    new MarathonApplication(
      id = buildApp.id,
      cpus = buildApp.cpus,
      mem = buildApp.mem,
      instances = buildApp.instances,
      user = buildApp.user,
      args = buildApp.args,
      env = buildApp.env,
      container = MarathonContainer(
        docker = Docker(
          image = buildApp.container.docker.image,
          portMappings = buildApp.container.docker.portMappings.map { ports =>
            ports.map {
              case PortMapping(hostPort, containerPort, servicePort, protocol, labels) =>
                DockerPortMapping(hostPort, containerPort, servicePort, protocol.getOrElse(TcpValue), labels)
            }
          },
          network = buildApp.container.docker.network.getOrElse(BridgeValue),
          forcePullImage = buildApp.container.docker.forcePullImage,
          parameters = buildApp.container.docker.parameters.map { parameters =>
            parameters.map { case Parameter(key, value) => DockerParameter(key, value) }
          }
        ),
        volumes = buildApp.container.docker.volumes.map { volumes =>
          volumes.map { case Volume(containerPath, hostPath, mode) =>
            MarathonVolume(containerPath, hostPath, mode)
          }
        }
      ),
      cmd = buildApp.cmd,
      portDefinitions = buildApp.portDefinitions.map { portDefinitions =>
        portDefinitions.map { portDefinition =>
          MarathonPortDefinition(
            portDefinition.name,
            portDefinition.port,
            portDefinition.protocol.getOrElse(TcpValue),
            portDefinition.labels
          )
        }
      },
      requirePorts = buildApp.requirePorts,
      healthChecks = buildApp.healthChecks.map { healthChecks =>
        healthChecks.map { healthCheck =>
          MarathonHealthCheck(
            healthCheck.protocol,
            healthCheck.path,
            healthCheck.portIndex,
            healthCheck.command.map(cmd => MarathonHealthCheckCommand(cmd.value)),
            healthCheck.gracePeriodSeconds, healthCheck.intervalSeconds, healthCheck.timeoutSeconds,
            healthCheck.maxConsecutiveFailures, healthCheck.ignoreHttp1xx)
        }
      },
      labels = buildApp.labels,
      ports = buildApp.ports
    )

  def fromJson(id: String,
               cpus: Double,
               mem: Int,
               instances: Option[Int],
               user: Option[String],
               args: Option[List[String]],
               env: Option[Map[String, String]],
               container: MarathonContainer,
               cmd: Option[String],
               portDefinitions: Option[Seq[MarathonPortDefinition]],
               requirePorts: Option[Boolean],
               healthChecks: Option[Seq[MarathonHealthCheck]],
               labels: Map[String, String],
               ports: Option[Seq[Int]]) =
    MarathonApplication(
      id = id.replaceFirst("^/", ""),
      cpus = cpus,
      mem = mem,
      instances = instances,
      user = user,
      args = args,
      env = env,
      container = container,
      cmd = cmd,
      portDefinitions = portDefinitions,
      requirePorts = requirePorts,
      healthChecks = healthChecks,
      labels = labels,
      ports = ports
    )

  // Literals
  val idLiteral = "id"
  val cpusLiteral = "cpus"
  val memLiteral = "mem"
  val instancesLiteral = "instances"
  val userLiteral = "user"
  val argsLiteral = "args"
  val envLiteral = "env"
  val containerLiteral = "container"
  val cmdLiteral = "cmd"
  val portDefinitionsLiteral = "portDefinitions"
  val requirePortsLiteral = "requirePorts"
  val healthChecksLiteral = "healthChecks"
  val labelsLiteral = "labels"
  val portsLiteral = "ports"

  //Fixed Values
  val TcpValue = "tcp"
  val BridgeValue = "BRIDGE"

  implicit val writes: Writes[MarathonApplication] = Json.writes[MarathonApplication]
  implicit val reads: Reads[MarathonApplication] = (
    (__ \ idLiteral).read[String] and
      (__ \ cpusLiteral).read[Double] and
      (__ \ memLiteral).read[Int] and
      (__ \ instancesLiteral).readNullable[Int] and
      (__ \ userLiteral).readNullable[String] and
      (__ \ argsLiteral).readNullable[List[String]] and
      (__ \ envLiteral).readNullable[Map[String, String]] and
      (__ \ containerLiteral).read[MarathonContainer] and
      (__ \ cmdLiteral).readNullable[String] and
      (__ \ portDefinitionsLiteral).readNullable[Seq[MarathonPortDefinition]] and
      (__ \ requirePortsLiteral).readNullable[Boolean] and
      (__ \ healthChecksLiteral).readNullable[Seq[MarathonHealthCheck]] and
      (__ \ labelsLiteral).read[Map[String, String]] and
      (__ \ portsLiteral).readNullable[Seq[Int]]
    ) (MarathonApplication.fromJson _)
}

case class MarathonContainer(docker: Docker, `type`: String = "DOCKER", volumes: Option[Seq[MarathonVolume]] = None)

object MarathonContainer {

  implicit val writes: Writes[MarathonContainer] = Json.writes[MarathonContainer]
  implicit val reads: Reads[MarathonContainer] = Json.reads[MarathonContainer]
}

case class Docker(image: String,
                  portMappings: Option[Seq[DockerPortMapping]] = None,
                  network: String = "HOST",
                  privileged: Option[Boolean] = None,
                  parameters: Option[Seq[DockerParameter]] = None,
                  forcePullImage: Option[Boolean] = None)

object Docker {

  implicit val writes: Writes[Docker] = Json.writes[Docker]
  implicit val reads: Reads[Docker] = Json.reads[Docker]
}

case class DockerParameter(key: String, value: String)

object DockerParameter {

  implicit val writes: Writes[DockerParameter] = Json.writes[DockerParameter]
  implicit val reads: Reads[DockerParameter] = Json.reads[DockerParameter]
}

case class MarathonVolume(containerPath: String, hostPath: String, mode: String)

object MarathonVolume {

  implicit val writes: Writes[MarathonVolume] = Json.writes[MarathonVolume]
  implicit val reads: Reads[MarathonVolume] = Json.reads[MarathonVolume]
}

case class DockerPortMapping(hostPort: Int,
                             containerPort: Int,
                             servicePort: Option[Int] = None,
                             protocol: String = MarathonApplication.TcpValue,
                             labels: Option[Map[String, String]] = None)

object DockerPortMapping {

  implicit val writes: Writes[DockerPortMapping] = Json.writes[DockerPortMapping]
  implicit val reads: Reads[DockerPortMapping] = Json.reads[DockerPortMapping]
}

case class MarathonPortDefinition(name: Option[String] = None,
                                  port: Int,
                                  protocol: String = MarathonApplication.TcpValue,
                                  labels: Option[Map[String, String]] = None)

object MarathonPortDefinition {

  implicit val writes: Writes[MarathonPortDefinition] = Json.writes[MarathonPortDefinition]
  implicit val reads: Reads[MarathonPortDefinition] = Json.reads[MarathonPortDefinition]
}

case class MarathonHealthCheck(protocol: String,
                               path: Option[String] = None,
                               portIndex: Option[Int] = None,
                               command: Option[MarathonHealthCheckCommand] = None,
                               gracePeriodSeconds: Int,
                               intervalSeconds: Int,
                               timeoutSeconds: Int,
                               maxConsecutiveFailures: Int,
                               ignoreHttp1xx: Option[Boolean] = None)

object MarathonHealthCheck {

  implicit val writes: Writes[MarathonHealthCheck] = Json.writes[MarathonHealthCheck]
  implicit val reads: Reads[MarathonHealthCheck] = Json.reads[MarathonHealthCheck]
}

case class MarathonHealthCheckCommand(value: String)

object MarathonHealthCheckCommand {

  implicit val writes: Writes[MarathonHealthCheckCommand] = Json.writes[MarathonHealthCheckCommand]
  implicit val reads: Reads[MarathonHealthCheckCommand] = Json.reads[MarathonHealthCheckCommand]
}
