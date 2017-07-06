package com.taboola.cronyx;

import java.io.Serializable;

public class NameAndGroup implements Serializable {

    private String group;

    private String name;

    public NameAndGroup() {
    }

    public NameAndGroup(String name, String group) {
        this.name = name;
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return (group != null ? group + "." : "") + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NameAndGroup that = (NameAndGroup) o;

        if (group != null ? !group.equals(that.group) : that.group != null) return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = group != null ? group.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
