package models

import org.joda.time.DateTime

import scala.util.Random
import scala.math.abs
import scala.collection.JavaConversions._
import java.util.ArrayList


/**
 * Created by rmohan on 2/20/15.
 * Contains the model code for the poll
 */

// Poll implementation
case class Poll(var question:String, var started_at:String, var expired_at:String, var choice:List[String]) {
  var id = java.lang.Long.toString(Poll.getId,36)
  try {
    DateTime.parse(started_at)
    DateTime.parse(expired_at)
  } catch {
    case e: Exception =>
      println("Invalid time format")
      id = "-1"
  }

  def getId = {
          this.id
  }

  def this(question:String, started_at:String, expired_at:String, 
                  choice:java.util.ArrayList[String]) {

    this(question, started_at, expired_at, Poll.j2sList(choice))
  }

  def mapToModerator(moderator: Moderator) = {
    Poll.storePoll(new PollWithResult(id, question, started_at, expired_at, choice, List.fill(choice.length){0}, this), moderator)
  }

  def mapToModeratorId(modId: Int) = {
    val moderator = Moderator.search(modId)
    if (moderator != null)
            mapToModerator(moderator)
  }

  def vote(choiceIndex: Int):Int = {
    Poll.vote(id, choiceIndex)
  }
}

object Poll {
  var count = 0
  val POLL_ID_START:Long = Random.nextLong()
  var polls:List[PollModeratorMapping] = List()

  def j2sList(jl:java.util.ArrayList[String]):List[String] = {
    var sl = List[String]()
    jl.foreach { i => sl = sl :+ i }
    sl
  }

  def getId = {
    count += 1
    abs(count + POLL_ID_START)
  }

  def vote(id:String, choiceIndex:Int):Int = {
    val pollResult = searchForResult(id)
    if (choiceIndex >= pollResult.results.length || choiceIndex < 0) {
      return -1
    }

    val resultIndex = choiceIndex
    //val resultIndex = choiceIndex - 1
    val newCount =  pollResult.results(resultIndex) + 1
    pollResult.results = pollResult.results.patch(resultIndex, Seq(newCount), 1)
    0
  }

  def storePoll(poll:PollWithResult, mod:Moderator) = {
    val polMod = new PollModeratorMapping(poll, mod)
    polls = polls ::: List(polMod)
  }

  def deletePoll(poll:Poll, mod:Moderator) = {
    var toRem:List[PollModeratorMapping] = List()
    for(e <- polls if e.pollWithResult.poll == poll && e.mod == mod)
      toRem = toRem ::: List(e)
    polls = polls diff toRem
  }

  def pollsForMod(mod:Moderator):List[PollWithResultView] = {
    var list:List[PollWithResultView] = List()
    for(e <- polls if e.mod == mod)
      list = list ::: List(new PollWithResultView(e.pollWithResult.id, e.pollWithResult.question,
                                                   e.pollWithResult.started_at, e.pollWithResult.expired_at,
                                                   e.pollWithResult.choice, e.pollWithResult.results))
    list
  }

  def search(id:String) : Poll = {
    for (e <- polls if e.pollWithResult.id == id)
      return e.pollWithResult.poll
    null
  }

  def searchForResultView(id:String, moderator: Moderator):PollWithResultView = {
    for (e <- polls if e.pollWithResult.id == id && e.mod == moderator)
      return new PollWithResultView(e.pollWithResult.id, e.pollWithResult.question,
                                    e.pollWithResult.started_at, e.pollWithResult.expired_at, e.pollWithResult.choice, e.pollWithResult.results)
    null
  }

  def searchForResult(id:String):PollWithResult = {
    for (e <- polls if e.pollWithResult.id == id)
      return e.pollWithResult
    null
  }
}

class PollModeratorMapping(val pollWithResult: PollWithResult, val mod:Moderator)

case class PollWithResult(id:String, question:String, started_at:String, expired_at:String, choice:List[String], var results:List[Int], poll:Poll)
case class PollWithResultView(id:String, question:String, started_at:String, expired_at:String, choice:List[String], result:List[Int])
