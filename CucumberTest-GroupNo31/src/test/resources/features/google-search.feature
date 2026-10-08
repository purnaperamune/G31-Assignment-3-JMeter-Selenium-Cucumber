Feature: Google Search

  Scenario: Google search
    Given I launch chrome browser
    When I open Google home page
    Then I check the Google home page title
    Then I enter the search item
    And Close the browser