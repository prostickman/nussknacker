package pl.touk.esp.ui.api

import akka.http.scaladsl.server.Directives
import pl.touk.esp.ui.config.FeatureTogglesConfig
import pl.touk.esp.ui.security.LoggedUser
import pl.touk.http.argonaut.Argonaut62Support

import scala.concurrent.ExecutionContext

class SettingsResources(config: FeatureTogglesConfig)(implicit ec: ExecutionContext)
  extends Directives with Argonaut62Support {

  import argonaut.ArgonautShapeless._

  val route = (user: LoggedUser) =>
    pathPrefix("settings") {
      get {
        complete {
          ToggleFeaturesOptions(
            counts = config.counts.isDefined,
            search = config.search,
            metrics = config.metrics,
            migration = config.migration.map(c => MigrationConfig(c.environmentId)),
            environmentAlert = config.environmentAlert
          )
        }
      }
    }
}

case class GrafanaSettings(url: String, dashboard: String, env: String)
case class KibanaSettings(url: String)
case class MigrationConfig(targetEnvironmentId: String)

case class ToggleFeaturesOptions(counts: Boolean,
                                 search: Option[KibanaSettings],
                                 metrics: Option[GrafanaSettings],
                                 migration: Option[MigrationConfig],
                                 environmentAlert:Option[String]
                                )