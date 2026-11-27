package org.delcom.ifs25025springbootcashflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.component.dependency.StyleSheet;

@SpringBootApplication
@Push
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet("/styles.css")
public class Ifs25025P6Pbo2026SpringbootCashflowApplication
        implements AppShellConfigurator {

    public static void main(String[] args) {
        DotEnv.load();
        SpringApplication.run(Ifs25025P6Pbo2026SpringbootCashflowApplication.class, args);
    }
}