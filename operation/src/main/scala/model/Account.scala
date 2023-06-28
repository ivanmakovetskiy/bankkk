package model
/**

The Account case class represents an account with an ID and an amount.
@param id The ID of the account.
@param amount The amount in the account.
 **/
case class Account(id: Int, amount: Int) {
  def update(value: Int) = this.copy(amount = amount + value)
}

/**
 * The Command trait represents a command for account operations.
 */
trait Command
/**
 * The AccountUpdate case class represents an account update command.
 *
 * @param operationId The ID of the operation.
 * @param accountId The ID of the account to update.
 * @param destinationId The optional ID of the destination account (if applicable).
 * @param value The value to update the account with.
 * @param category The optional category of the update.
 */
case class AccountUpdate(operationId: Int, accountId: Int, destinationId: Option[Int], value: Int, category: Option[String])
/**
 * The Event trait represents an event related to account operations.
 */
trait Event
/**
 * The AccountUpdated case class represents an account updated event.
 *
 * @param operationId The ID of the operation.
 * @param accountId The ID of the account that was updated.
 * @param destinationId The optional ID of the destination account (if applicable).
 * @param value The updated value of the account.
 * @param category The optional category of the update.
 */
case class AccountUpdated(operationId: Int, accountId: Int, destinationId: Option[Int], value: Int, category: Option[String])