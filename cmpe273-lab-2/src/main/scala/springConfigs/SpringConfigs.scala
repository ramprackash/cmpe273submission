package springConfigs

import controllers.MainController
import messageConverters.JerksonHttpMessageConverter
import models.Moderator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.{SpringBootApplication, EnableAutoConfiguration}
import org.springframework.context.annotation.{ComponentScan, Configuration}
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.security.authentication.{UsernamePasswordAuthenticationToken, BadCredentialsException, AuthenticationProvider}
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.{EnableWebSecurity, WebSecurityConfigurerAdapter}
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import scala.collection.JavaConversions._
/**
 * Created by rprakash on 3/1/15.
 * Contains all spring configurations
 */

//
//@Configuration
//@ComponentScan
//class MyWebMvcConfigurer extends WebMvcConfigurerAdapter {
//
//  print("Replacing MessageConverter with Jerkson... ")
//  @Autowired
//  override def configureMessageConverters(converters: java.util.List[HttpMessageConverter[_]]) {
//    val converter = new JerksonHttpMessageConverter()
//    converters.add(converter)
//    println("Done!")
//  }
//
//}

//@Autowired
//object CustomAuthenticationProvider extends AuthenticationProvider {
//
//  override def authenticate(auth: Authentication):Authentication = {
//    val eml:String = auth.getName
//    val password:String = auth.getCredentials.toString
//    if ((eml.length() <= 0) || (password.length() <= 0)) {
//      throw new BadCredentialsException("Invalid user/pass")
//    }
//    val mod = Moderator.search(eml, password)
//    if (mod != null) {
//      new UsernamePasswordAuthenticationToken(eml, password, Set(new SimpleGrantedAuthority("ROLE_USER")))
//    }
//    else {
//      throw new BadCredentialsException("Wrong username/password")
//    }
//  }

//  override def supports(auth:Class[_]) = {
//    auth.equals(classOf[UsernamePasswordAuthenticationToken])
//  }
//}

//@Configuration
//@ComponentScan
//@EnableWebSecurity
//class MyWebSecurityConfigurer extends WebSecurityConfigurerAdapter {

//  println("In MyWebSecurityConfigurer")
//  @Autowired
//  override def configure(auth: AuthenticationManagerBuilder) =
//    auth.authenticationProvider(CustomAuthenticationProvider)

//  @Autowired
//  override def configure(http: HttpSecurity) = {
//    println("Access details being configured")
//    http.csrf().disable()
//    http.authorizeRequests()
//      .antMatchers("/api/v1/moderators").permitAll()
//      .antMatchers("/api/v1/polls**").permitAll()
//      .antMatchers("/api/v1/polls/**").permitAll()
//      .anyRequest().authenticated()
//    //http.formLogin().permitAll()
//    http.httpBasic()
//  }
//}

//@SpringBootApplication
//object SpringWebApplication {
//  def main (args: Array[String]): Unit = {
//    SpringApplication.run(classOf[MainController])
//  }
//}
