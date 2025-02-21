package tradetariff

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import io.gatling.http.protocol.HttpProtocolBuilder

class CommoditiesSimulation extends Simulation   {

  val httpProtocol: HttpProtocolBuilder = http
    .baseUrl(sys.env("PERFTESTURL"))

  val commoditiesFeeder = csv("5000-commodities.csv").queue.circular

  val request =
    feed(commoditiesFeeder)
      .exec(
        http("UK Commodity")
          .head("/commodities/#{commodity}")
      )
      .pause(1)
      .exec(
        http("XI Commodity")
          .head("/xi/commodities/#{commodity}")
      )

  val commoditiesScenario = scenario("Commodities").exec(request)

  setUp(
    commoditiesScenario.inject(
      constantConcurrentUsers(1).during(10.seconds), // 1
      rampConcurrentUsers(1).to(30).during(60.seconds),
      constantConcurrentUsers(30).during(830.seconds)
    )
  ).protocols(httpProtocol)
}
