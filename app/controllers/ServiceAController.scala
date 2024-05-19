/**
Root Route (/):

GET / (no logLevel parameter provided, defaults to None)
GET /?logLevel=warn (provided logLevel parameter with value warn)


Other Service Route (/other/:serviceName):

GET /other/serviceA (no logLevel parameter provided, defaults to None)
GET /other/serviceA?logLevel=warn (provided logLevel parameter with value warn)


GET     /cause-gc                       causes GC for the service
GET     /cause-oom                      Causes OOM 
**/
package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.ws._
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.ExecutionContext
import java.time.LocalDateTime
import scala.collection.mutable.ListBuffer

@Singleton
class SampleController @Inject()(cc: ControllerComponents, ws: WSClient)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  private val serviceName: String = sys.env.getOrElse("SERVICE_NAME", "defaultService")
  private val logger: Logger = LoggerFactory.getLogger(getClass)
  private val serviceCustomLogger: Logger = LoggerFactory.getLogger(s"${serviceName}-custom-logger")

  // To simulate memory usage
  private val memoryHog = ListBuffer[Array[Byte]]()

  def callRoot(logLevel: Option[String]) = Action { request =>
    val currentTime = LocalDateTime.now
    val message = s"Service: $serviceName, Time: $currentTime"

    logLevel.getOrElse("info").toLowerCase match {
      case "error" =>
        logger.error(message)
        serviceCustomLogger.error(message)
      case "warn" =>
        logger.warn(message)
        serviceCustomLogger.warn(message)
      case _ =>
        logger.info(message)
        serviceCustomLogger.info(message)
    }

    Ok(message)
  }

  def callOtherServiceRoot(service: String, logLevel: Option[String]) = Action.async { request =>
    val serviceUrl = sys.env.getOrElse(s"${service}_URL", throw new RuntimeException(s"Service URL for $service not found in environment variables"))
    val resolvedLogLevel = logLevel.getOrElse("info").toLowerCase

    logger.info(s"Calling $service at $serviceUrl with log level $resolvedLogLevel")
    serviceCustomLogger.info(s"Calling $service at $serviceUrl with log level $resolvedLogLevel")

    ws.url(serviceUrl).withQueryStringParameters("logLevel" -> resolvedLogLevel).get().map { response =>
      val responseBody = response.body
      resolvedLogLevel match {
        case "error" =>
          logger.error(responseBody)
          serviceCustomLogger.error(responseBody)
        case "warn" =>
          logger.warn(responseBody)
          serviceCustomLogger.warn(responseBody)
        case _ =>
          logger.info(responseBody)
          serviceCustomLogger.info(responseBody)
      }
      Ok(responseBody).withHeaders("Called-From" -> serviceName)
    }.recover {
      case e: Exception =>
        logger.error(s"Error calling $service at $serviceUrl", e)
        serviceCustomLogger.error(s"Error calling $service at $serviceUrl", e)
        InternalServerError(s"Error calling $service")
    }
  }

  // API to cause garbage collection
  def causeGC() = Action { request =>
    logger.info("Triggering garbage collection")
    System.gc()
    Ok("Garbage collection triggered")
  }

  // API to cause OutOfMemoryError
  def causeOOM() = Action { request =>
    logger.info("Simulating OutOfMemoryError")
    try {
      while (true) {
        // Allocate 10MB of memory repeatedly
        memoryHog += new Array[Byte](10 * 1024 * 1024)
      }
    } catch {
      case e: OutOfMemoryError =>
        logger.error("OutOfMemoryError occurred", e)
        InternalServerError("OutOfMemoryError occurred")
    }
    Ok("OutOfMemoryError simulation complete")
  }
}
