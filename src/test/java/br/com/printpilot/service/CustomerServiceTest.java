package br.com.printpilot.service;

import br.com.printpilot.dto.customer.CustomerRequest;
import br.com.printpilot.dto.customer.CustomerResponse;
import br.com.printpilot.entity.Customer;
import br.com.printpilot.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    @DisplayName("Should create customer successfully")
    void shouldCreateCustomer() {
        CustomerRequest request = new CustomerRequest("John Doe", "12345678900", "john@test.com", "999999999", true);

        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setName("John Doe");
        savedCustomer.setDocument("12345678900");
        savedCustomer.setEmail("john@test.com");
        savedCustomer.setPhone("999999999");
        savedCustomer.setActive(true);

        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        CustomerResponse response = customerService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("John Doe");
        assertThat(response.active()).isTrue();
    }

    @Test
    @DisplayName("Should find existing customer by id")
    void shouldFindCustomerById() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("John Doe");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should throw NOT_FOUND when finding non-existent customer")
    void shouldThrowWhenFindingNonExistentCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cliente não encontrado");
    }

    @Test
    @DisplayName("Should update existing customer")
    void shouldUpdateCustomer() {
        Customer existingCustomer = new Customer();
        existingCustomer.setId(1L);
        existingCustomer.setName("Old Name");
        existingCustomer.setActive(true);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existingCustomer));

        Customer updatedCustomer = new Customer();
        updatedCustomer.setId(1L);
        updatedCustomer.setName("New Name");
        updatedCustomer.setActive(false);

        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);

        CustomerRequest request = new CustomerRequest("New Name", null, null, null, false);
        CustomerResponse response = customerService.update(1L, request);

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.active()).isFalse();
    }
}
