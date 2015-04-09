package controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.security.authentication.{UsernamePasswordAuthenticationToken, BadCredentialsException, AuthenticationProvider}
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority

import scala.language.existentials

import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

import messageConverters.JerksonHttpMessageConverter
import org.springframework.context.annotation.{Configuration, ComponentScan}
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.bind.annotation._
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import scala.collection.JavaConversions._
import grpc.polls.PollsGRPCServer


import models.{Poll, ModUpdater, Moderator}


/**
 * Created by rmohan on 2/19/15.
 * Controller part of the app and all the Spring Framework Configs
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
@RestController
@RequestMapping(value = Array("/api/v1"))
class MainController extends WebMvcConfigurerAdapter {

  val grpcServer:PollsGRPCServer = new PollsGRPCServer()
  grpcServer.start()

  @Override
  override def configureMessageConverters(converters: java.util.List[HttpMessageConverter[_]]) {
    val converter = new JerksonHttpMessageConverter()
    converters.add(converter)
  }

  //1: Create moderator:
  //curl -H "Content-Type: application/json" -X POST -d '{"name":"ramp", "email":"ramp@abc.com", "password":"dumb"}'
  //    http://localhost:8080/api/v1/moderators
  @RequestMapping(value=Array("/moderators"), method=Array(RequestMethod.POST))
  @ResponseBody
  def moderatorsCreate(@Valid @RequestBody moderator: Moderator) = {
    if (moderator.id != -1) {
      new ResponseEntity[Moderator](moderator, HttpStatus.CREATED)
    } else {
      new ResponseEntity("Error: 400 - Moderator already present", HttpStatus.BAD_REQUEST)
    }
  }

  //2: View moderator:
  //curl -u ramp -H "Accept: application/json" -X GET http://localhost:8080/api/v1/moderators/1
  @RequestMapping(produces=Array("application/json"), method=Array(RequestMethod.GET), value=Array("/moderators/{id}"))
  @ResponseBody
  def moderatorsGet(@PathVariable id: Int, httpServletRequest: HttpServletRequest) = {
    val moderator = Moderator.search(id)
    if (moderator != null)
      new ResponseEntity(moderator, HttpStatus.OK)
    else
      new ResponseEntity("Error: 404 - Moderator Not Found", HttpStatus.NOT_FOUND)
  }

  //3: Update moderator:
  //curl -u ramp -H "Content-Type: application/json" -X PUT -d '{"email":"diego@san.com","password":"dumb"}'
  //    http://localhost:8080/api/v1/moderators/1
  @RequestMapping(produces=Array("application/json"), method=Array(RequestMethod.PUT), value=Array("/moderators/{id}"))
  @ResponseBody
  def moderatorsPut(@RequestBody updateMod: ModUpdater, @PathVariable id: Int) = {
    val moderator = Moderator.search(id)
    if (moderator != null) {
      moderator.update(updateMod.email, updateMod.password)
      new ResponseEntity(moderator, HttpStatus.CREATED)
    } else {
      new ResponseEntity("Error: 400 - Moderator Not Found", HttpStatus.BAD_REQUEST)
    }
  }

  //4: Create poll:
  //curl -u ramp -H "Content-Type: application/json" -X POST -d '{"question":"iPhone or Android",
  //                                                              "started_at":"2015-02-23T13:00:00.000Z",
  //                                                              "expired_at" : "2015-02-24T13:00:00.000Z",
  //                                                              "choice": ["android", "iphone"] }'
  // http://localhost:8080/api/v1/moderators/1/polls
  @RequestMapping(value=Array("/moderators/{id}/polls"), method=Array(RequestMethod.POST))
  @ResponseBody
  def pollCreate(@PathVariable id:Int,  @RequestBody poll: Poll) = {
    val moderator = Moderator.search(id)
    if (moderator == null)
      new ResponseEntity[String]("HTTP Error 404: Moderator for Moderator id not found", HttpStatus.BAD_REQUEST)
    else {
      if (poll.id != "-1") {
        poll.mapToModerator(moderator)
        new ResponseEntity[Poll](poll, HttpStatus.CREATED)
      } else {
        new ResponseEntity[String]("HTTP Error 400: Invalid Time format in JSON", HttpStatus.BAD_REQUEST)
      }
    }
  }

  //5: View poll without result
  //curl -H "Accept: application/json" -X GET http://localhost:8080/api/v1/polls/7a3uej0hcsrp4v3youq
  @RequestMapping(produces=Array("application/json"), method=Array(RequestMethod.GET), value=Array("/polls/{id}"))
  @ResponseBody
  def pollGet(@PathVariable id: String) = {
    val poll = Poll.search(id)
    if (poll != null)
      new ResponseEntity(poll, HttpStatus.OK)
    else
      new ResponseEntity("Error: 404 - Poll " + id + " Not Found ", HttpStatus.NOT_FOUND)
  }

  //6: View poll with result
  //curl -u ramp:dumb -H "Accept: application/json"
  //     -X GET http://localhost:8080/api/v1/moderators/1/polls/2f998uxoh1ozeooowg6k
  @RequestMapping(produces=Array("application/json"), method=Array(RequestMethod.GET), value=Array("/moderators/{mod_id}/polls/{id}"))
  @ResponseBody
  def pollWithResultGet(@PathVariable id: String, @PathVariable mod_id: Int):ResponseEntity[Any] = {
    val mod = Moderator.search(mod_id)
    if (mod == null)
      return new ResponseEntity("Error: 404 - Moderator " + mod_id + " Not Found", HttpStatus.NOT_FOUND)
    val poll = Poll.searchForResultView(id, mod)
    if (poll != null)
      new ResponseEntity(poll, HttpStatus.OK)
    else
      new ResponseEntity("Error: 404 - Poll " + id + " Not Found under given moderator", HttpStatus.NOT_FOUND)
  }

  //7: List all polls for a given moderator:
  //curl -u ramp:dumb -H "Accept: application/json" -X GET http://localhost:8080/api/v1/moderators/1/polls
  @RequestMapping(produces=Array("application/json"), method=Array(RequestMethod.GET), value=Array("/moderators/{mod_id}/polls"))
  @ResponseBody
  def pollsForMod(@PathVariable mod_id: Int) = {
    val mod = Moderator.search(mod_id)
    if (mod == null)
      new ResponseEntity("Error: 404 - Moderator " + mod_id + " Not Found", HttpStatus.NOT_FOUND)
    else
      new ResponseEntity(Poll.pollsForMod(mod), HttpStatus.OK)
  }

  //8: Delete a poll:
  //curl -u ramp:dumb -H "Accept: application/json"
  //     -X DELETE http://localhost:8080/api/v1/moderators/1/polls/2f998uxoh1ozeooowg6k
  @RequestMapping(produces=Array("application/json"), method=Array(RequestMethod.DELETE), value=Array("/moderators/{mod_id}/polls/{id}"))
  @ResponseBody
  def pollDelete(@PathVariable id: String, @PathVariable mod_id: Int):ResponseEntity[String] = {
    val poll = Poll.search(id)
    val mod = Moderator.search(mod_id)

    val poll_t = Poll.searchForResultView(id, mod)
    if (poll_t == null)
      return new ResponseEntity("Poll not under given moderator", HttpStatus.BAD_REQUEST)

    if (mod == null)
      return new ResponseEntity("Error: 400 - Moderator " + mod_id + " Not Found", HttpStatus.BAD_REQUEST)

    if (poll != null) {
      Poll.deletePoll(poll, mod)
      new ResponseEntity("Poll " + id + " Deleted", HttpStatus.NO_CONTENT)
    }
    else
      new ResponseEntity("Error: 400 - Poll " + id + " Not Found ", HttpStatus.BAD_REQUEST)
  }

  //9: Vote in a poll:
  //curl -X PUT http://localhost:8080/api/v1/polls/11kyyde3j16t1?choice=2
  @RequestMapping(value=Array("/polls/{id}"), params=Array("choice"), method=Array(RequestMethod.PUT))
  @ResponseBody
  def pollVote(@PathVariable id: String, @RequestParam(value="choice") choice_index: Int) = {
    val poll = Poll.search(id)
    if (poll != null) {
      if (poll.vote(choice_index) == -1)
        new ResponseEntity("Error: 400 - Invalid choice", HttpStatus.BAD_REQUEST)
      else
        new ResponseEntity("Your vote has been recorded!", HttpStatus.NO_CONTENT)
    }
    else {
      new ResponseEntity("Error: 400 - Poll " + id + " Not Found ", HttpStatus.BAD_REQUEST)
    }
  }
}

@Configuration
class MyWebSecurityConfigurer extends WebSecurityConfigurerAdapter {

  println("In MyWebSecurityConfigurer")
  override def configure(auth: AuthenticationManagerBuilder) =
    auth.authenticationProvider(CustomAuthenticationProvider)

  override def configure(http: HttpSecurity) = {
    println("Access details being configured")
    http.csrf().disable()
    http.authorizeRequests()
      .antMatchers("/api/v1/moderators").permitAll()
      .antMatchers("/api/v1/polls**").permitAll()
      .antMatchers("/api/v1/polls/**").permitAll()
      .anyRequest().authenticated()
    //http.formLogin().permitAll()
    http.httpBasic()
  }
}

@Autowired
object CustomAuthenticationProvider extends AuthenticationProvider {

  override def authenticate(auth: Authentication):Authentication = {
    val eml:String = auth.getName
    val password:String = auth.getCredentials.toString
    if ((eml.length() <= 0) || (password.length() <= 0)) {
      throw new BadCredentialsException("Invalid user/pass")
    }
    val mod = Moderator.search(eml, password)
    if (mod != null) {
      new UsernamePasswordAuthenticationToken(eml, password, Set(new SimpleGrantedAuthority("ROLE_USER")))
    }
    // FIXME: This is a very bad requirement for this assignment. Ideally, 
    // the credentials that should be used is the one used when the moderator is    // created. VERY INSECURE!!!
    else if (eml == "foo" && password == "bar") {
      new UsernamePasswordAuthenticationToken(eml, password, Set(new SimpleGrantedAuthority("ROLE_USER")))
    }
    else {
      throw new BadCredentialsException("Wrong username/password")
    }
  }

  override def supports(auth:Class[_]) = {
    auth.equals(classOf[UsernamePasswordAuthenticationToken])
  }
}

object SpringWebApplication {
  def main (args: Array[String]): Unit = {
    SpringApplication.run(classOf[MainController])
  }
}

