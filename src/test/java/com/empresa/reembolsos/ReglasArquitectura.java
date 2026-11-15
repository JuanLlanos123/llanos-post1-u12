package com.empresa.reembolsos;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.empresa.reembolsos.procesamiento.calculo.CalculadoraReembolso;
import com.empresa.reembolsos.procesamiento.notificacion.ObservadorCambioEstado;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.empresa.reembolsos")
public class ReglasArquitectura {

    // Regla 1: procesamiento no depende de infraestructura, adaptadores ni JPA
    @ArchTest
    static final ArchRule procesamientoSinPersistenciaNiWeb = noClasses()
        .that().resideInAPackage("..procesamiento..")
        .should().dependOnClassesThat()
        .resideInAnyPackage(
            "..infraestructura..", "..adaptadores..",
            "org.springframework.data..", "jakarta.persistence..");

    // Regla 2: el controlador solo accede al servicio de aplicacion, dominio y su propio paquete REST
    @ArchTest
    static final ArchRule controladorSoloUsaServicio = classes()
        .that().resideInAPackage("..adaptadores.rest..")
        .should().onlyAccessClassesThat()
        .resideInAnyPackage(
            "..aplicacion..", "..dominio..", "..adaptadores.rest..",
            "org.springframework.web..", "org.springframework.http..", "java..");

    // Regla 3: las calculadoras concretas implementan CalculadoraReembolso
    @ArchTest
    static final ArchRule calculadorasImplementanInterfaz = classes()
        .that().resideInAPackage("..procesamiento.calculo..")
        .and().areNotInterfaces()
        .and().haveSimpleNameStartingWith("Calculadora")
        .and().haveSimpleNameNotEndingWith("Test")
        .should().implement(CalculadoraReembolso.class);

    // Regla 4: los notificadores implementan ObservadorCambioEstado
    @ArchTest
    static final ArchRule notificadoresImplementanInterfaz = classes()
        .that().resideInAPackage("..procesamiento.notificacion..")
        .and().areNotInterfaces()
        .and().haveSimpleNameNotEndingWith("Test")
        .should().implement(ObservadorCambioEstado.class);
}
