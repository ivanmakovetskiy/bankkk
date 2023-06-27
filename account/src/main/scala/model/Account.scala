package model

import java.util.UUID

case class Account(id: Int, amount: Int) {
  def update(value: Int) = this.copy(amount = amount + value)
}

trait Command
case class AccountUpdate(value: Int)
case class AccountTransfer(targetAccountId: Int, value: Int)

trait Event
case class AccountUpdated(accountId: Int, amount: Int)
case class AccountTransferStarted(sourceAccountId: Int, targetAccountId: Int, value: Int)
case class AccountTransferCompleted(sourceAccountId: Int, targetAccountId: Int, value: Int)
