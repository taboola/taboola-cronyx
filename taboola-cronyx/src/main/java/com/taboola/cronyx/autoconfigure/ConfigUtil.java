package com.taboola.cronyx.autoconfigure;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.type.filter.AssignableTypeFilter;

import com.taboola.cronyx.exceptions.CronyxException;

public class ConfigUtil {

    public static final String PACKAGE = "com.taboola.cronyx.impl.converter";

    public static <T> List<Pair<Class, T>> cronyxQuartzConverterPairs(Class<T> tClass) {

        List<Pair<Class, T>> results = new ArrayList<>();

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(tClass));

        Set<BeanDefinition> components = provider.findCandidateComponents(PACKAGE);
        for (BeanDefinition component : components) {
            try {
                Class cls = Class.forName(component.getBeanClassName());
                Class<?> typeArgument = GenericTypeResolver.resolveTypeArgument(cls, tClass);
                results.add(new ImmutablePair<>(typeArgument, (T) cls.newInstance()));

            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new CronyxException("Could not instantiate cronyxToQuartzConverters", e);
            }
        }
        return results;
    }
}
