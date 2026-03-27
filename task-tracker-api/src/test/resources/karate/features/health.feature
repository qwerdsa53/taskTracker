Feature: readiness

  Scenario: actuator health
    Given url baseUrl
    And path '/actuator/health'
    When method GET
    Then status 200
    And match response.status == 'UP'
