package messageConverters

import java.nio.charset.Charset

import com.codahale.jerkson.{ParsingException, Json}
import com.fasterxml.jackson.core.JsonParseException
import org.springframework.http.converter.{AbstractHttpMessageConverter, HttpMessageNotReadableException, HttpMessageNotWritableException}
import org.springframework.http.{HttpInputMessage, HttpOutputMessage, MediaType}
import scala.language.reflectiveCalls

/**
 * Created by rprakash on 2/23/15.
 *
 * This is a helper class to serialize/deserialize Json using Jerkson
 * "inspired" from http://blog.caseylucas.com/2011/08/17/jerkson-spring-scala/
 */
class JerksonHttpMessageConverter extends AbstractHttpMessageConverter[Object] (new MediaType("application", "json", Charset.forName("UTF-8"))) {

  /*
  val json = new Json
  {
    def canWrite(clazz : Class[_]) = mapper.canSerialize(clazz)
    def canDeserialize(clazz: Class[_]) = mapper.canDeserialize(mapper.constructType(clazz))
  }
  */

  override def writeInternal(o: Object, outputMessage: HttpOutputMessage) = {
    try {
      Json.generate(o, outputMessage.getBody)
    } catch {
      case ex: Exception => throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage, ex);
    }
  }

  override def readInternal(clazz: Class[_ <: Object], inputMessage: HttpInputMessage) =  {
    try {
      Json.parse(inputMessage.getBody)(Manifest.classType(clazz))
    } catch {
      case ex: JsonParseException =>
        throw new HttpMessageNotReadableException("Could not read JSON: " + ex.getMessage, ex)
      case ex: ParsingException =>
        throw new HttpMessageNotReadableException("Could not read JSON: " + ex.getMessage, ex)
    }
  }

  override def supports(clazz: Class[_]): Boolean = {
    true
  }

  override def canRead(clazz: Class[_], mediaType: MediaType): Boolean = {
    canRead(mediaType)
  }

  override def canWrite(clazz : Class[_], mediaType : MediaType) : Boolean = {
    canWrite(mediaType)
  }
}
