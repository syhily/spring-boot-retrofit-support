package com.oneapm.touch.retrofit.boot;

import com.oneapm.touch.retrofit.boot.annotation.RetrofitService;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Configures Retrofit auto-configuration to scan interfaces annotated with
 * {@link RetrofitService} from the given packages.
 * <p>
 * One of {@link #basePackageClasses()}, {@link #basePackages()} or its alias
 * {@link #value()} may be specified to define specific packages to scan. If specific
 * packages are not defined scanning will occur from the package of the class with this
 * annotation.
 * <p>
 * Note that without this annotation, the services will not be scanned.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RetrofitServiceFactoryBeanRegistrar.class)
public @interface RetrofitServiceScan {

    /**
     * Alias for the {@link #basePackages()} attribute. Allows for more concise annotation
     * declarations e.g.: {@code @RetrofitServiceScan("org.my.pkg")} instead of
     * {@code @RetrofitServiceScan(basePackages="org.my.pkg")}.
     *
     * @return the base packages to scan
     */
    String[] value() default {};

    /**
     * Base packages to scan for annotated entities. {@link #value()} is an alias for (and
     * mutually exclusive with) this attribute.
     * <p>
     * Use {@link #basePackageClasses()} for a type-safe alternative to String-based
     * package names.
     *
     * @return the base packages to scan
     */
    String[] basePackages() default {};

    /**
     * Type-safe alternative to {@link #basePackages()} for specifying the packages to
     * scan for annotated entities. The package of each class specified will be scanned.
     * <p>
     * Consider creating a special no-op marker class or interface in each package that
     * serves no purpose other than being referenced by this attribute.
     *
     * @return classes from the base packages to scan
     */
    Class<?>[] basePackageClasses() default {};
}
