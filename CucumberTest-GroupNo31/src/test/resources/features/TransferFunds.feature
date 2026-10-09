Feature: Transfer Funds

  As a registered ParaBank customer
  I want to transfer money between my accounts
  So that I can manage my funds correctly and securely

  Background:
    Given I am logged in as a ParaBank customer
    And I am on the Transfer Funds page
    And at least two accounts are available for transfer

  @positive
  Scenario Outline: Successfully transfer a valid amount between accounts
    When I transfer "<amount>" from the first account to the second account
    Then I should see that the transfer was completed successfully

    Examples:
      | amount |
      | 10     |
      | 25     |
      | 10.50  |

  @negative
  Scenario: Attempt to transfer a non-numeric amount
    When I attempt to transfer "abc" from the first account to the second account
    Then the transfer should not be completed
    And I should see an internal transfer error

  @negative
  Scenario: Attempt to transfer with a blank amount
    When I attempt to transfer a blank amount
    Then the transfer should not be completed
    And I should see an internal transfer error

  @defect
  Scenario: Attempt to transfer a zero amount
    When I attempt to transfer a zero amount
    Then the zero-value transfer should be rejected

  @defect
  Scenario: Attempt to transfer a negative amount
    When I attempt to transfer the negative amount "-50"
    Then the negative-value transfer should be rejected

  @defect
  Scenario: Attempt to transfer more than the available balance
    When I attempt to transfer more than the source account balance
    Then the over-balance transfer should be rejected

  @defect
  Scenario: Attempt to transfer between the same account
    When I attempt to transfer "10" from an account to the same account
    Then the same-account transfer should be rejected