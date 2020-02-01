package code;

public class Taxi
{
	private int x;
	private int y;
	private int id;
	
	private Point closestPoint;
	private double distanceFromClosestPoint;
	
	Taxi(int x, int y, int id)
	{
		this.x = x;
		this.y = y;
		this.id = id;
		
		distanceFromClosestPoint = Double.MAX_VALUE;
	}
	
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
	
	public int getId()
	{
		return id;
	}
	
	public double getDistanceFromClosestPoint()
	{
		return distanceFromClosestPoint;
	}
	
	public void setDistanceFromClosestPoint(double distanceFromClosestPoint)
	{
		this.distanceFromClosestPoint = distanceFromClosestPoint;
	}
	
	public Point getClosestPoint()
	{
		return closestPoint;
	}
	
	public void setClosestPoint(Point closestPoint)
	{
		this.closestPoint = closestPoint;
	}
	
	public String toString()
	{
		return new StringBuilder().append(x * 1.0 / Math.pow(10, 7)).append(",").append(y * 1.0 / Math.pow(10, 7)).append(",0").toString();
	}
}
