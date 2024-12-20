package com.bob.mojo;

import com.bob.Parser;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.Objects;

@Mojo(name = "decorator-parser", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class DecoratorParser extends AbstractMojo {

    @Parameter(required = true)
    private String sourceDirectory;
    @Parameter(required = true)
    private String outputDirectory;

    public void execute() {
        assert !Objects.equals(sourceDirectory, outputDirectory);
        Parser.parse(new File(sourceDirectory), new File(outputDirectory));
    }

}
