package repository
/**
The repository package contains classes related to account data management.
 */
import commonmodel.Account
import com.typesafe.config.ConfigFactory
import scala.concurrent.Future

/**
 * The Repository class provides methods for managing account data.
 *
 * @param accountId The ID of the repository.
 * @param defAmount The default amount for new accounts.
 */
class Repository(val accountId: Int, defAmount: Int){
  var accounts: Map[Int, Account] = Map.empty
  var lastAccountId: Int = ConfigFactory.load().getInt("account.id")
  lastAccountId *= 100

  /**
   * Creates a new account with the generated account ID and default amount.
   *
   * @return A `Future` containing the created account.
   */
  def createAccount(): Future[Account] = {
    val accountId = generateAccountId()
    val account = Account(accountId, defAmount)
    accounts += (accountId -> account)
    Future.successful(account)
  }
  /**
   * Retrieves an account by its ID.
   *
   * @param accountId The ID of the account to retrieve.
   * @return An `Option` containing the account, or `None` if the account is not found.
   */
  def getAccount(accountId: Int): Option[Account] = {
    accounts.get(accountId)
  }
  /**
   * Generates a new account ID.
   *
   * @return The generated account ID.
   */
  private def generateAccountId(): Int = {
    lastAccountId += 1
    lastAccountId
  }
  /**
   * Updates the account with the specified ID by adding the given value to its amount.
   *
   * @param accountId The ID of the account to update.
   * @param value     The value to add to the account's amount.
   * @return A `Future` containing the updated account.
   * @throws Exception if the account with the specified ID is not found.
   */
  def update(accountId: Int, value: Int): Future[Account] = {
    accounts.get(accountId) match {
      case Some(account) =>
        val updatedAccount = account.update(value)
        accounts += (accountId -> updatedAccount)
        Future.successful(updatedAccount)
      case None => Future.failed(new Exception(s"Аккаунт с ID $accountId не найден"))
    }
  }
}