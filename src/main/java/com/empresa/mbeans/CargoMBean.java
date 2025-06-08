package com.empresa.mbeans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class CargoMBean {
    private String[] cargos = {
            "Gerente",
            "Desenvolvedor",
            "Analista",
            "Designer"
    };

    public String[] getCargos() {
        return cargos;
    }
}
