package models

import org.joda.time.DateTime

/**
 * Created by rprakash on 2/26/15.
 * Contains MOderator model
 */


case class ModUpdater(var email:String, var password:String)

case class Moderator(var name: String, var email: String, var password: String) {

  var id = 0
  val created_at = DateTime.now.toString()

  // For PUT operations (Updating email/password for existing Moderator
  def this(email:String, password: String) {
    this("", email, password)
  }

  def update(eml:String, pw: String) = {
    email = eml
    password = pw
  }

  if (alreadyExists(name, email)) {
    id = -1
  } else
  {
    id = Moderator.getId
    Moderator.storeModerator(this)
  }

  def alreadyExists(name:String, email: String): Boolean = {
    for (e <- Moderator.moderators if (e.name == name) && (e.email == email)) {
      return true
    }
    false
  }
}

object Moderator {
  var count = 0
  var moderators:List[Moderator] = List()
  def getId = {
    count = count + 1
    count
  }

  def storeModerator(obj:Moderator) = {
    moderators = moderators.:::(List(obj))
  }

  def search(eml:String, pw:String) : Moderator = {
    for (e <- moderators if ((e.email == eml) && (e.password == pw)) ||
      ((e.name == eml) && (e.password == pw)))
      return e
    null
  }

  def search(id:Int) : Moderator = {
    for (e <- moderators if e.id == id)
      return e
    null
  }
}
