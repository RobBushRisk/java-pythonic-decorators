package com.bob;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Mojo(name = "decorator-compiler-postprocessor", defaultPhase = LifecyclePhase.COMPILE)
public class CompilerMojoPostProcessor extends AbstractMojo {

    @Parameter(required = true)
    private String sourceDirectory;

    public void execute() {

        try {
            FileUtils.deleteDirectory(new File(sourceDirectory));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            FileUtils.copyDirectoryStructure(new File("temp_compiler_storage"), new File(sourceDirectory));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            FileUtils.deleteDirectory(new File("temp_compiler_storage"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
