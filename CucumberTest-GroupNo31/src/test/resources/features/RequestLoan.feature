Feature: Request Loan

  As a registered ParaBank customer
  I want to request a loan
  So that invalid loan requests are rejected and valid requests are processed correctly

  Background:
    Given I am logged in as a ParaBank customer
    And I am on the Request Loan page
    And a valid source account is available for the loan request

  @positive
  Scenario: Request a valid loan with zero down payment
    When I request a loan amount of "1000" with a down payment of "0"
    Then the loan request should be processed successfully
    And the loan should be approved

  @negative
  Scenario: Request a zero-value loan with zero down payment
    When I request a loan amount of "0" with a down payment of "0"
    Then I should see an internal loan request error

  @negative
  Scenario: Submit a loan request with a blank loan amount
    When I request a loan with a blank loan amount and down payment "0"
    Then I should see an internal loan request error

  @negative
  Scenario: Submit a loan request with a blank down payment
    When I request a loan amount of "1000" with a blank down payment
    Then I should see an internal loan request error

  @negative
  Scenario: Submit a loan request with a non-numeric loan amount
    When I request a loan amount of "abc" with a down payment of "0"
    Then I should see an internal loan request error

  @negative
  Scenario: Submit a loan request with a non-numeric down payment
    When I request a loan amount of "1000" with a down payment of "abc"
    Then I should see an internal loan request error

  @negative
  Scenario: Request a negative loan amount
    When I request a loan amount of "-1000" with a down payment of "0"
    Then the negative loan request should be denied
    And I should see the invalid loan amount denial message

  @defect
  Scenario: Request a loan using a negative down payment
    When I request a loan amount of "1000" with a down payment of "-100"
    Then the negative down payment should be rejected

  @defect
  Scenario: Request a loan where the down payment is greater than the loan amount
    When I request a loan with a down payment greater than the loan amount but within the available balance
    Then the down payment greater than loan amount should be rejected

  @negative
  Scenario: Request a loan with a down payment greater than the available account balance
    When I request a loan with a down payment greater than the available account balance
    Then the loan request should be denied
    And I should see the insufficient funds for down payment message