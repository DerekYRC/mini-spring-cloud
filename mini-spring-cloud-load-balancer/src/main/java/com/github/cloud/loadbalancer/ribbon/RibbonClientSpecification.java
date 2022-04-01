package com.github.cloud.loadbalancer.ribbon;

import org.springframework.cloud.context.named.NamedContextFactory;

import java.util.Arrays;
import java.util.Objects;

/**
 * ribbon客户端配置
 *
 * @author derek(易仁川)
 * @date 2022/3/22
 */
public class RibbonClientSpecification implements NamedContextFactory.Specification {

    private String name;

    private Class<?>[] configuration;

    public RibbonClientSpecification() {
    }

    public RibbonClientSpecification(String name, Class<?>[] configuration) {
        this.name = name;
        this.configuration = configuration;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?>[] getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Class<?>[] configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RibbonClientSpecification that = (RibbonClientSpecification) o;
        return Arrays.equals(configuration, that.configuration)
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuration, name);
    }

    @Override
    public String toString() {
        return new StringBuilder("RibbonClientSpecification{").append("name='")
                .append(name).append("', ").append("configuration=")
                .append(Arrays.toString(configuration)).append("}").toString();
    }

}
