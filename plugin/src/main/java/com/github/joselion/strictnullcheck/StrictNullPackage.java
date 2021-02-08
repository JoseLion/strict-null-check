package com.github.joselion.strictnullcheck;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;

@Nonnull
@Documented
@Target(PACKAGE)
@Retention(RUNTIME)
@TypeQualifierDefault({FIELD, METHOD, PARAMETER})
public @interface StrictNullPackage {

}
