package juuxel.unprotect.gradle;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class UnprotectExtension {
    private final Project project;
    private final SourceSetContainer sourceSets;

    @Inject
    public UnprotectExtension(Project project, SourceSetContainer sourceSets) {
        this.project = project;
        this.sourceSets = sourceSets;
    }

    public void registerMrjSourceSet(Action<? super MrjSpec> action) {
        MrjSpec spec = project.getObjects().newInstance(MrjSpec.class);
        spec.getClassFiles().convention(List.of("module-info.class"));
        action.execute(spec);

        int version = spec.getJavaVersion().get();
        List<String> classFiles = spec.getClassFiles().get();
        SourceSet sourceSet = sourceSets.create("java" + version);
        sourceSet.getJava().srcDirs(sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME).getJava().getSrcDirs());

        TaskContainer tasks = project.getTasks();

        tasks.named(sourceSet.getCompileJavaTaskName(), JavaCompile.class, task -> {
            task.getOptions().getRelease().set(version);
            task.getOptions().getJavaModuleVersion().set(project.provider(() -> project.getVersion().toString()));
        });

        tasks.named("jar", Jar.class, task -> {
            task.getManifest().attributes(Map.of("Multi-Release", "true"));

            task.from(sourceSet.getOutput(), copy -> {
                copy.include(classFiles);
                copy.into("META-INF/versions/" + version);
            });
        });

        tasks.named("sourcesJar", Jar.class, task -> {
            task.from(project.fileTree("src/" + sourceSet.getName() + "/java"), copy -> {
                copy.into("META-INF/versions/" + version);
            });
        });
    }

    public interface MrjSpec {
        Property<Integer> getJavaVersion();
        ListProperty<String> getClassFiles();
    }
}
