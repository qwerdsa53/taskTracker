Feature: register, login, list users, create habit

  Scenario: full flow
    * def email = 'karate-' + java.util.UUID.randomUUID() + '@example.com'
    * def password = 'Secret123!'

    Given url baseUrl
    And path '/api/v1/users'
    And request { email: '#(email)', username: 'karate-user', password: '#(password)', timezone: 'Europe/Moscow' }
    When method POST
    Then status 201
    * def userId = response.id

    Given url baseUrl
    And path '/api/v1/auth/login'
    And request { email: '#(email)', password: '#(password)' }
    When method POST
    Then status 200
    And match response.tokenType == 'Bearer'
    * def token = response.accessToken

    Given url baseUrl
    And path '/api/v1/users'
    And header Authorization = 'Bearer ' + token
    When method GET
    Then status 200

    Given url baseUrl
    And path '/api/v1/users', userId, 'habits'
    And header Authorization = 'Bearer ' + token
    And request
      """
      {
        "title": "Morning run",
        "description": "Karate",
        "color": "#336699",
        "iconKey": "run",
        "archived": false,
        "schedule": {
          "type": "EVERY_DAY",
          "targetPerWeek": 1,
          "activeWeekdays": []
        }
      }
      """
    When method POST
    Then status 201
    And match response.title == 'Morning run'
