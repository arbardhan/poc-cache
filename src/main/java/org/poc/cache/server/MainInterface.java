package org.poc.cache.server;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.context.annotation.ImportResource;

@ComponentScan
@Configuration
@ImportResource("classpath:jmx.xml")
@EnableMBeanExport

public class MainInterface {
}
