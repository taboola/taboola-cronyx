package com.taboola.cronyx;

public class NameAndGroupOrderedPair {
    private NameAndGroup previous;
    private NameAndGroup after;

    public NameAndGroupOrderedPair(NameAndGroup previous, NameAndGroup after) {
        this.previous = previous;
        this.after = after;
    }

    public NameAndGroup getPrevious() {
        return previous;
    }

    public NameAndGroup getAfter() {
        return after;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NameAndGroupOrderedPair that = (NameAndGroupOrderedPair) o;

        if (previous != null ? !previous.equals(that.previous) : that.previous != null) return false;
        return after != null ? after.equals(that.after) : that.after == null;
    }

    @Override
    public int hashCode() {
        int result = previous != null ? previous.hashCode() : 0;
        result = 31 * result + (after != null ? after.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NameAndGroupOrderedPair{" +
                "previous=" + previous +
                ", after=" + after +
                '}';
    }
}
