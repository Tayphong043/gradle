/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.tasks.diagnostics;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Incubating;
import org.gradle.api.Project;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.diagnostics.artifacttransforms.ArtifactTransformReports;
import org.gradle.api.tasks.diagnostics.internal.artifacttransforms.ArtifactTransformReportsImpl;
import org.gradle.api.tasks.diagnostics.internal.artifacttransforms.model.ArtifactTransformReportModel;
import org.gradle.api.tasks.diagnostics.internal.artifacttransforms.model.ArtifactTransformReportModelFactory;
import org.gradle.api.tasks.diagnostics.internal.artifacttransforms.renderer.ConsoleArtifactTransformReportRenderer;
import org.gradle.api.tasks.diagnostics.internal.artifacttransforms.spec.ArtifactTransformReportSpec;
import org.gradle.api.tasks.options.Option;
import org.gradle.internal.logging.text.StyledTextOutput;
import org.gradle.internal.logging.text.StyledTextOutputFactory;
import org.gradle.internal.serialization.Cached;
import org.gradle.util.internal.ClosureBackedAction;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;

/**
 * A task which reports information about the Artifact Transforms (implemented by {@link TransformAction}) used by a project.
 *
 * This is useful for investigating ambiguous transformation scenarios.  The output can help predict which ATs will need
 * to be modified to remove ambiguity.  This class implements {@link Reporting} to make configuring additional file output formats simple.
 *
 * @since 8.12
 */
@Incubating
@DisableCachingByDefault(because = "Produces only non-cacheable console output")
public abstract class ArtifactTransformsReportTask extends DefaultTask implements Reporting<ArtifactTransformReports> {
    private final ArtifactTransformReports reports;
    private final Cached<ArtifactTransformReportModel> reportModel = Cached.of(() -> buildReportModel(getProject()));

    @Inject protected abstract ObjectFactory getObjectFactory();
    @Inject protected abstract StyledTextOutputFactory getTextOutputFactory();

    /**
     * Creates a new instance.
     * @since 8.12
     */
    public ArtifactTransformsReportTask() {
        reports = getObjectFactory().newInstance(ArtifactTransformReportsImpl.class);
    }

    /**
     * Limits the report to reporting on transforms using a type with this (simple) classname.
     *
     * @return property holding name of the type of transform to report
     * @since 8.12
     */
    @Input
    @Optional
    @Option(option = "type", description = "The type of Transform to report")
    public abstract Property<String> getTransformType();

    /**
     * The reports to be generated by this task.
     */
    @Override
    @Nested
    public final ArtifactTransformReports getReports() {
        return reports;
    }

    /**
     * Configures the reports to be generated by this task.
     *
     * The contained reports can be configured by task name and closures.
     *
     * @param closure The configuration block for the reports
     * @return The reports container
     */
    @Override
    @SuppressWarnings("rawtypes")
    public ArtifactTransformReports reports(@DelegatesTo(value = ArtifactTransformReports.class, strategy = Closure.DELEGATE_FIRST) Closure closure) {
        return reports(new ClosureBackedAction<>(closure));
    }

    /**
     * Configures the reports to be generated by this task.
     *
     * The contained reports can be configured by task name and closures.
     *
     * @param configureAction The configuration block for the reports
     * @return The reports container
     */
    @Override
    public ArtifactTransformReports reports(Action<? super ArtifactTransformReports> configureAction) {
        configureAction.execute(reports);
        return reports;
    }

    /**
     * Generates the report.
     * @since 8.12
     */
    @TaskAction
    public final void report() {
        final ArtifactTransformReportSpec reportSpec = buildReportSpec();
        final ArtifactTransformReportModel model = reportModel.get();

        reportToConsole(reportSpec, model);
    }

    private void reportToConsole(ArtifactTransformReportSpec reportSpec, ArtifactTransformReportModel reportModel) {
        final ConsoleArtifactTransformReportRenderer renderer = new ConsoleArtifactTransformReportRenderer(reportSpec);
        final StyledTextOutput output = getTextOutputFactory().create(getClass());
        renderer.render(reportModel, output);
    }

    private ArtifactTransformReportModel buildReportModel(Project project) {
        final ArtifactTransformReportModelFactory factory = getObjectFactory().newInstance(ArtifactTransformReportModelFactory.class);
        return factory.buildForProject(project);
    }

    private ArtifactTransformReportSpec buildReportSpec() {
        return new ArtifactTransformReportSpec(getTransformType().getOrNull());
    }
}
