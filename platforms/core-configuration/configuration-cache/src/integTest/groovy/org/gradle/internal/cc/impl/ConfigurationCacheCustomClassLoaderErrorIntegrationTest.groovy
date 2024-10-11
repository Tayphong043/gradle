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

package org.gradle.internal.cc.impl

class ConfigurationCacheCustomClassLoaderErrorIntegrationTest extends AbstractConfigurationCacheIntegrationTest {

    def 'reports problem for custom classloader'() {
        given:
        jarWithClasses(
            'Foo': 'public class Foo {}',
            file('custom.jar')
        )
        buildFile << '''
            def foo = new URLClassLoader(new URL[]{file('custom.jar').toURI().toURL()})
                .loadClass('Foo')
                .getConstructor()
                .newInstance()

            tasks.register('fail') {
                doLast { println(foo) }
            }
        '''

        when:
        configurationCacheFails 'fail'

        then:
        problems.assertFailureHasProblems(failure) {
            withProblemsWithStackTraceCount 0
            withProblem "Task `:fail` of type `org.gradle.api.DefaultTask`: Class 'Foo' cannot be encoded because class loader 'java.net.URLClassLoader"
        }
    }
}
