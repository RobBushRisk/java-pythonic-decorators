package com.bob.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@Mojo(name = "decorator-compiler-postprocessor", defaultPhase = LifecyclePhase.COMPILE)
public class CompilerPostProcessor extends AbstractMojo {

    @Parameter(required = true)
    private String sourceDirectory;

    public void execute() {

        try {
            FileUtils.deleteDirectory(new File(sourceDirectory));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            FileUtils.copyDirectory(new File("temp_compiler_storage"), new File(sourceDirectory));
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
