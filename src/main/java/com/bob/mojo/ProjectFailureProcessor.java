package com.bob.mojo;

import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

@Component(role = EventSpy.class)
public class ProjectFailureProcessor extends AbstractEventSpy {

    @Parameter(required = true)
    private String sourceDirectory;

    @Override
    public void onEvent(Object event) throws Exception {
        if (event instanceof ExecutionEvent) {
            ExecutionEvent executionEvent = (ExecutionEvent) event;
            if (executionEvent.getType() == ExecutionEvent.Type.ProjectFailed) {
                try {
                    FileUtils.deleteDirectory(new File(sourceDirectory));
                    FileUtils.copyDirectoryStructure(new File("backup_compiler_storage"), new File(sourceDirectory));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
