package com.testcontainers.demo;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for Customer
 */
interface CustomerRepository extends JpaRepository<Customer, Long> {}
