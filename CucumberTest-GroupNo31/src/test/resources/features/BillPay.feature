Feature: Bill Pay

  As a registered ParaBank customer
  I want to pay bills from my account
  So that I can manage my bill payments securely

  Background:
    Given I am logged in as a ParaBank customer
    And I am on the Bill Pay page
    And a valid source account is available for bill payment

  @positive
  Scenario Outline: Successfully make a valid bill payment
    Given I have entered valid complete payee details
    When I submit a bill payment of "<amount>"
    Then I should see that the bill payment was completed successfully

    Examples:
      | amount |
      | 10     |
      | 10.50  |

  @validation
  Scenario: Submit bill payment with all required payee details blank
    When I submit a bill payment with all payee details blank and amount "20"
    Then I should see the required payee field validation errors

  @validation
  Scenario: Submit bill payment without account numbers
    Given I have entered valid payee contact details
    When I submit a bill payment with blank account numbers and amount "20"
    Then I should see the account number required validation errors

  @validation
  Scenario: Submit bill payment with a blank amount
    Given I have entered valid complete payee details
    When I submit a bill payment with a blank amount
    Then I should see the amount empty validation message

  @validation
  Scenario: Submit bill payment with a non-numeric amount
    Given I have entered valid complete payee details
    When I submit a bill payment with the non-numeric amount "sdyd"
    Then I should see the invalid amount validation message

  @defect
  Scenario: Attempt to make a zero-value bill payment
    Given I have entered valid complete payee details
    When I attempt to make a zero-value bill payment
    Then the zero-value bill payment should be rejected

  @defect
  Scenario: Attempt to make a negative bill payment
    Given I have entered valid complete payee details
    When I attempt to make a bill payment of "-100"
    Then the negative bill payment should be rejected

  @defect
  Scenario: Attempt to make a bill payment greater than the available balance
    Given I have entered valid complete payee details
    When I attempt to make a bill payment greater than the source account balance
    Then the over-balance bill payment should be rejected