package br.com.devpasso.order_management.domain.repository;

import br.com.devpasso.order_management.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}
