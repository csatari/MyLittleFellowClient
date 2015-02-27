package hu.jex.mylittlefellow.model;

/**
 * Egy koordinátát kezel
 * @author Albert
 *
 */
public class Point {
	/*BigDecimal x;
	BigDecimal y;*/
	
	double x;
	double y;
	public Point() {}
	
	/*public Point(BigDecimal x, BigDecimal y) {
		this.x = x;
		this.y = y;
	}*/
	public Point(double x, double y) {
		/*String xString = (""+x).substring(0, 15);
		String yString = (""+y).substring(0, 15);
		this.x = new BigDecimal(xString);
		this.y = new BigDecimal(yString);*/
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "Point [x=" + x + ", y=" + y + "]";
	}
	
}
