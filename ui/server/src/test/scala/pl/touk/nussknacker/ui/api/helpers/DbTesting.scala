package pl.touk.nussknacker.ui.api.helpers

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.scalalogging.LazyLogging
import db.migration.DefaultJdbcProfile
import org.scalatest.{BeforeAndAfterEach, Suite}
import pl.touk.nussknacker.ui.db.{DatabaseInitializer, DbConfig}
import slick.jdbc.JdbcBackend

import scala.util.Try

object DbTesting extends LazyLogging {
  val db: DbConfig = DbConfig(JdbcBackend.Database.forURL(
    url = s"jdbc:hsqldb:mem:esp;sql.syntax_ora=true",
    driver = "org.hsqldb.jdbc.JDBCDriver",
    user = "SA",
    password = ""
  ), DefaultJdbcProfile.profile)

  new DatabaseInitializer(db.db).initDatabase()

  def cleanDB(): Try[Unit] = {
    Try {
      val session = db.db.createSession()
      session.prepareStatement("""delete from "process_attachments"""").execute()
      session.prepareStatement("""delete from "process_comments"""").execute()
      session.prepareStatement("""delete from "process_deployment_info"""").execute()
      session.prepareStatement("""delete from "process_versions"""").execute()
      session.prepareStatement("""delete from "tags"""").execute()
      session.prepareStatement("""delete from "environments"""").execute()
      session.prepareStatement("""delete from "processes"""").execute()
    }
  }
}

trait WithDbTesting { self: Suite with BeforeAndAfterEach =>

  val db: DbConfig = DbTesting.db

  override protected def afterEach(): Unit = {
    DbTesting.cleanDB().failed.foreach { e =>
      throw new InternalError("Error during cleaning test resources", e) //InternalError as scalatest swallows other exceptions in afterEach
    }
  }

}