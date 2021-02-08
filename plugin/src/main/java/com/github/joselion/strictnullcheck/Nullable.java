package com.github.joselion.strictnullcheck;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static javax.annotation.meta.When.MAYBE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierNickname;

@Nonnull(when = MAYBE)
@Documented
@Target({FIELD, METHOD, PARAMETER})
@Retention(RUNTIME)
@TypeQualifierNickname
public @interface Nullable {

}
