package br.com.devpasso.order_management.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

    @RestController
@RequestMapping("/products")
public class controllerorder {

    @GetMapping
    public List<String> getProducts() {
        return List.of("Notebook", "Mouse", "Teclado");
    }
}

