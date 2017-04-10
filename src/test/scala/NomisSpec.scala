import akka.actor.ActorSystem
import akka.http.scaladsl.model.DateTime
import akka.stream.ActorMaterializer
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import gov.uk.justice.digital.pollpush.data.{PullResult, SourceCaseNote}
import gov.uk.justice.digital.pollpush.services.NomisSource
import org.json4s.NoTypeHints
import org.json4s.native.Serialization
import org.scalatest.{FunSpec, GivenWhenThen, Matchers}
import scala.concurrent.Await
import scala.concurrent.duration._

class NomisSpec extends FunSpec with GivenWhenThen with Matchers {

  implicit val formats = Serialization.formats(NoTypeHints)
  implicit val system = ActorSystem()
  implicit val materialzer = ActorMaterializer()

  describe("Pull from Nomis API") {

    it("GETs case notes from the API") {

      configureFor(8082)
      val api = new WireMockServer(options.port(8082))
      val source = new NomisSource("http://localhost:8082/nomis/casenotes")

      Given("the source API")
      api.start()
      val rightNow = DateTime.now
      val minuteAgo = rightNow.minus(6000)

      When("Case Notes are pulled from the API")
      val result = Await.result(source.pull(minuteAgo, rightNow), 5.seconds)

      Then("the API receives a HTTP GET call and returns the Case Notes")
      verify(getRequestedFor(urlEqualTo(s"/nomis/casenotes?from=$minuteAgo")))
      result shouldBe PullResult(Seq(
        SourceCaseNote("1234", "ABCD", "observation", "This is a case note", "2017-03-13T12:34:56Z", "John Smith", "ABC"),
        SourceCaseNote("5678", "EFGH", "admin", "More case notes", "2017-03-14T09:00:00Z", "Dave Jones", "DEF"),
        SourceCaseNote("9876", "IJKL", "regular", "Even more notes", "2017-03-15T23:45:12Z", "Fred Baker", "GHI"),
        SourceCaseNote("5432", "MNOP", "regular", "Yet more notes", "2017-03-16T13:45:12Z", "Jimmy Jones", "JKL")
      ), Some(minuteAgo), Some(rightNow), None)

      api.stop()
      system.terminate()
    }
  }
}
