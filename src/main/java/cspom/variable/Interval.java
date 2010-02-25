package cspom.variable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a domain given as an interval (lower bound & upper
 * bound).
 * 
 * @param <T>
 *            Type of bounds
 * 
 * @author vion
 */
public final class Interval<T extends Number> implements Domain<T> {

    /**
     * Lower bound.
     */
    private final T lb;
    /**
     * Upper bound.
     */
    private final T ub;

    /**
     * Constructs a new interval domain according to given lower and upper
     * bounds. Both bound must be of the same type, and lower bound must be <=
     * to upper bound.
     * 
     * @throws IllegalArgumentException
     *             if given bounds are not legal.
     * 
     * @param lb
     *            lower bound
     * @param ub
     *            upper bound
     */
    public Interval(final T lb, final T ub) {
        if (!lb.getClass().equals(ub.getClass())) {
            throw new IllegalArgumentException(
                    "Both bounds must be of the same type");
        }
        if (!(lb instanceof Comparable<?>)) {
            throw new IllegalArgumentException(
                    "Bounds must implement Comparable");
        }
        if (((Comparable<T>) lb).compareTo(ub) > 0) {
            throw new IllegalArgumentException(
                    "Lower bound must be inferior or equal to upper bound");
        }
        this.lb = lb;
        this.ub = ub;

    }

    public static Interval<Number> valueOf(String interval) {
        final String[] fromto = interval.trim().split("\\.\\.");
        final Number lb = numValueOf(fromto[0]);
        final Number ub = numValueOf(fromto[1]);
        return new Interval<Number>(lb, ub);
    }

    public static Number numValueOf(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            //
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            //
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            //
        }

        return Double.parseDouble(value);
    }

    /**
     * @return the lower bound
     */
    public T getLb() {
        return lb;
    }

    /**
     * @return the upper bound
     */
    public T getUb() {
        return ub;
    }

    @Override
    public String toString() {
        return "[" + lb + ".." + ub + "]";
    }

    @Override
    public int hashCode() {
        return 961 + 31 * lb.hashCode() + ub.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Interval<?>)) {
            return false;
        }
        final Interval<?> itv = (Interval<?>) obj;
        return lb.equals(itv.getLb()) && ub.equals(itv.getUb());
    }

    @Override
    public List<T> getValues() {
        if (Integer.class.isInstance(lb)) {
            final List<T> list = new ArrayList<T>();
            final int intUb = this.ub.intValue();
            for (Integer i = lb.intValue(); i <= intUb; i++) {
                list.add((T) i);
            }

            return list;
        }
        if (Long.class.isInstance(lb)) {
            final List<T> list = new ArrayList<T>();
            final long longUb = this.ub.longValue();
            for (Long i = lb.longValue(); i <= longUb; i++) {
                list.add((T) i);
            }

            return list;
        }
        throw new IllegalArgumentException(
                "Cannot obtain list of values from an interval of "
                        + lb.getClass());
    }
}
