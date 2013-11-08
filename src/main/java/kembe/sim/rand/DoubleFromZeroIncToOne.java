package kembe.sim.rand;

public class DoubleFromZeroIncToOne extends Bound {
    public final double value;

    public static final DoubleFromZeroIncToOne one = new DoubleFromZeroIncToOne( 1d );
    public static final DoubleFromZeroIncToOne zero = new DoubleFromZeroIncToOne( 0d );
    public static final DoubleFromZeroIncToOne half = new DoubleFromZeroIncToOne( 0.5d );

    public DoubleFromZeroIncToOne(double value) {
        if (value < 0 || value > 1)
            throw new IllegalArgumentException( "The value must be in the range [0,1]" );
        this.value = value;
    }

    @Override public String toString() {
        return "DoubleFromZeroIncToOne(" +
                "value=" + value +
                ')';
    }
}
