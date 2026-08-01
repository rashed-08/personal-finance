package io.rashed.finance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Spring applies {@code @Transactional} and method security by wrapping the
 * bean in a CGLIB subclass. A final class — or a final method — cannot be
 * subclassed, so the context fails to start with
 * "Could not generate CGLIB subclass".
 *
 * The project's services are otherwise written as {@code final class}, which
 * makes this an easy mistake to repeat. This test catches it without needing
 * a database, unlike the @SpringBootTest suites.
 */
class ProxyableBeanTest {

    private static final String BASE_PACKAGE = "io/rashed/finance";

    /** Annotations whose behaviour is delivered by a CGLIB proxy. */
    private static final List<Class<? extends Annotation>> PROXIED_ANNOTATIONS =
            List.of(Transactional.class, PreAuthorize.class);

    @Test
    @DisplayName("classes needing a CGLIB proxy are not final")
    void proxiedClassesAreNotFinal() throws Exception {

        List<String> violations = new ArrayList<>();

        for (Class<?> type : applicationClasses()) {

            boolean classLevel = PROXIED_ANNOTATIONS.stream()
                    .anyMatch(type::isAnnotationPresent);

            List<Method> proxiedMethods = new ArrayList<>();

            for (Method method : type.getDeclaredMethods()) {
                if (PROXIED_ANNOTATIONS.stream().anyMatch(method::isAnnotationPresent)) {
                    proxiedMethods.add(method);
                }
            }

            if (!classLevel && proxiedMethods.isEmpty()) {
                continue;
            }

            if (Modifier.isFinal(type.getModifiers())) {
                violations.add(type.getName() + " is final");
            }

            // With a class-level annotation every public method is advised,
            // so any final one breaks the proxy too.
            List<Method> mustBeOverridable = classLevel
                    ? List.of(type.getDeclaredMethods())
                    : proxiedMethods;

            for (Method method : mustBeOverridable) {
                if (Modifier.isFinal(method.getModifiers())
                        && Modifier.isPublic(method.getModifiers())) {
                    violations.add(type.getName() + "#" + method.getName() + " is final");
                }
            }
        }

        assertTrue(
                violations.isEmpty(),
                "Spring cannot create a CGLIB proxy for these; drop 'final':\n  "
                        + String.join("\n  ", violations)
        );
    }

    private static List<Class<?>> applicationClasses() throws Exception {

        PathMatchingResourcePatternResolver resolver =
                new PathMatchingResourcePatternResolver();

        MetadataReaderFactory metadataReaderFactory =
                new CachingMetadataReaderFactory(resolver);

        Resource[] resources =
                resolver.getResources("classpath*:" + BASE_PACKAGE + "/**/*.class");

        List<Class<?>> classes = new ArrayList<>();

        for (Resource resource : resources) {

            String className = metadataReaderFactory
                    .getMetadataReader(resource)
                    .getClassMetadata()
                    .getClassName();

            try {
                // Loaded without initialization: reading annotations must not
                // run static initializers.
                classes.add(Class.forName(
                        className,
                        false,
                        ProxyableBeanTest.class.getClassLoader()
                ));
            } catch (Throwable ignored) {
                // Not loadable in the test classpath — nothing to assert on.
            }
        }

        return classes;
    }
}
