package model

import java.util.UUID

case class Account(id: UUID = UUID.randomUUID(), username: String, sum: Int = 0)
case class AddAccount(username: String, sum: Int)
case class UpdateAccount(id: UUID, username: Option[String] = None, sum: Option[Int] = None)