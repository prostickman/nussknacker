package pl.touk.esp.engine.process

import java.util.Date

import org.scalatest.{FlatSpec, Matchers}
import pl.touk.esp.engine.api._
import pl.touk.esp.engine.build.GraphBuilder
import pl.touk.esp.engine.graph.EspProcess
import pl.touk.esp.engine.graph.service.{Parameter, ServiceRef}
import pl.touk.esp.engine.graph.variable.Field
import pl.touk.esp.engine.process.KeyValueTestHelper.KeyValue
import pl.touk.esp.engine.process.ProcessTestHelpers.{MockService, SimpleRecord, processInvoker}
import pl.touk.esp.engine.spel

import scala.collection.JavaConverters._
import scala.concurrent.duration._

class FlinkProcessRegistrarSpec extends FlatSpec with Matchers {

  import spel.Implicits._

  it should "aggregate records" in {
    val process = EspProcess(
      MetaData("proc1"),
      GraphBuilder.source("source", "simple-keyvalue")
        .aggregate(
          id = "aggregate", aggregatedVar = "input", keyExpression = "#input.key",
          duration = 5 seconds, step = 1 second
        )
        .processorEnd("service", ServiceRef("mock", List(Parameter("input", "#sum(#input.![value])"))))
    )
    val data = List(
      KeyValue("a", 1, new Date(0)),
      KeyValue("a", 1, new Date(1000))
    )

    KeyValueTestHelper.processInvoker.invoke(process, data)

    KeyValueTestHelper.MockService.data shouldEqual List(1, 2, 2, 2, 2, 1)
  }

  it should "aggregate and filter records" in {
    val process = EspProcess(MetaData("proc1"),
      GraphBuilder.source("id", "input")
        .aggregate("agg", "input", "#input.id", 5 seconds, 1 second)
        .filter("filter1", "#sum(#input.![value1]) > 24")
        .processor("proc2", ServiceRef("logService", List(Parameter("all", "#distinct(#input.![value2])"))))
        .sink("out", "monitor"))
    val data = List(
      SimpleRecord("1", 12, "a", new Date(0)),
      SimpleRecord("1", 15, "b", new Date(1000)),
      SimpleRecord("2", 12, "c", new Date(2000)),
      SimpleRecord("1", 23, "d", new Date(5000))
    )

    processInvoker.invoke(process, data)

    MockService.data shouldNot be('empty)
    MockService.data(0) shouldBe Map("all" -> Set("a", "b").asJava)
  }

  it should "aggregate nested records" in {
    val process = EspProcess(MetaData("proc1"),
      GraphBuilder.source("id", "input")
        .aggregate("agg", "input", "#input.id", 5 seconds, 1 second)
        .buildVariable("newInput", "newInput",
          Field("id", "#input[0].id"),
          Field("sum", "#sum(#input.![value1])")
        )
        .filter("filter1", "#newInput[sum] > 24")
        .aggregate("agg2", "newInput", "#newInput[id]", 5 seconds, 1 second)
        .processor("proc2", ServiceRef("logService", List(Parameter("all", "#distinct(#newInput.![[sum]])"))))
        .sink("out", "monitor"))
    val data = List(
      SimpleRecord("1", 12, "a", new Date(0)),
      SimpleRecord("1", 15, "b", new Date(1000)),
      SimpleRecord("2", 12, "c", new Date(2000)),
      SimpleRecord("1", 23, "d", new Date(5000))
    )

    processInvoker.invoke(process, data)

    MockService.data shouldNot be('empty)
    MockService.data(0) shouldBe Map("all" -> Set(27L).asJava)
  }

}