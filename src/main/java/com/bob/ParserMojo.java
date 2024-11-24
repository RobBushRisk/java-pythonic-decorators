package com.bob;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "decorator-parser", defaultPhase = LifecyclePhase.COMPILE)
public class ParserMojo extends AbstractMojo {

    public void execute() {
        //Parser.parse();
    }

}
