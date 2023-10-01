package com.testcontainers.demo;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerControllerTest {

  /**
   * Inject the random port on which the Spring Boot application starts
   */
  @LocalServerPort
  private Integer port;

  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
    "postgres:15-alpine"
  );

  /**
   * Postgres database runs on port 5432 inside the PostgreSQLContainer
   */
  @BeforeAll
  static void beforeAll() {
    postgres.start();
  }

  @AfterAll
  static void afterAll() {
    postgres.stop();
  }

  /**
   * Register the database connection properties dynamically, from the PostgreSQLContainer
   * @param registry
   */
  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired
  CustomerRepository customerRepository;

  /**
   * Check the random port in which spring boot context runs, deleting all existing customer entries
   */
  @BeforeEach
  void setUp() {
    //
    RestAssured.baseURI = "http://localhost:" + port;
    customerRepository.deleteAll();
  }

  /**
   * Get all the customers existing in the database, via the REST API endpoint
   */
  @Test
  void shouldGetAllCustomers() {
    List<Customer> customers = List.of(
      new Customer(null, "John", "john@mail.com"),
      new Customer(null, "Dennis", "dennis@mail.com")
    );
    customerRepository.saveAll(customers);

    given()
      .contentType(ContentType.JSON)
      .when()
      .get("/api/customers")
      .then()
      .statusCode(200)
      .body(".", hasSize(2));
  }
}
