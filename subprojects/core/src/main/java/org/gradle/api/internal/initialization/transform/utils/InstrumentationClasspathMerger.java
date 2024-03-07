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

package org.gradle.api.internal.initialization.transform.utils;

import org.gradle.api.artifacts.ArtifactCollection;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.internal.component.local.model.TransformedComponentFileArtifactIdentifier;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

public class InstrumentationClasspathMerger {

    /**
     * Merges external dependencies and project dependencies to one classpath that is sorted based on the original classpath.
     */
    public static List<File> mergeToClasspath(
        ArtifactCollection originalDependencies,
        ArtifactCollection externalDependencies
    ) {
        return externalDependencies.getArtifacts().stream()
            .map(ClassPathTransformedArtifact::ofTransformedArtifact)
            // We sort based on the original classpath to we keep the original order,
            // we also rely on the fact that for ordered streams `sorted()` method has stable sort.
            .map(artifact -> artifact.file)
            .collect(Collectors.toList());
    }

    private static class ClassPathTransformedArtifact {
        private final File file;
        private final OriginalArtifactIdentifier originalIdentifier;

        private ClassPathTransformedArtifact(File file, OriginalArtifactIdentifier originalIdentifier) {
            this.file = file;
            this.originalIdentifier = originalIdentifier;
        }

        public static ClassPathTransformedArtifact ofTransformedArtifact(ResolvedArtifactResult transformedArtifact) {
            checkArgument(transformedArtifact.getId() instanceof TransformedComponentFileArtifactIdentifier);
            return new ClassPathTransformedArtifact(transformedArtifact.getFile(), OriginalArtifactIdentifier.of(transformedArtifact));
        }

        @Override
        public String toString() {
            return "ClassPathTransformedArtifact{" +
                "file=" + file +
                ", originalIdentifier=" + originalIdentifier +
                '}';
        }
    }

    private static class OriginalArtifactIdentifier {
        private final String originalFileName;
        private final ComponentIdentifier componentIdentifier;

        private OriginalArtifactIdentifier(String originalFileName, ComponentIdentifier componentIdentifier) {
            this.originalFileName = originalFileName;
            this.componentIdentifier = componentIdentifier;
        }

        private static OriginalArtifactIdentifier of(ResolvedArtifactResult artifact) {
            if (artifact.getId() instanceof TransformedComponentFileArtifactIdentifier) {
                TransformedComponentFileArtifactIdentifier identifier = (TransformedComponentFileArtifactIdentifier) artifact.getId();
                return new OriginalArtifactIdentifier(identifier.getOriginalFileName(), identifier.getComponentIdentifier());
            } else {
                return new OriginalArtifactIdentifier(artifact.getFile().getName(), artifact.getId().getComponentIdentifier());
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            OriginalArtifactIdentifier that = (OriginalArtifactIdentifier) o;
            return Objects.equals(originalFileName, that.originalFileName) && Objects.equals(componentIdentifier, that.componentIdentifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(originalFileName, componentIdentifier);
        }

        @Override
        public String toString() {
            return "OriginalArtifactIdentifier{" +
                "originalFileName='" + originalFileName + '\'' +
                ", componentIdentifier=" + componentIdentifier +
                '}';
        }
    }
}
