package code;

public class Client
{
	private int startX;
	private int startY;
	private int endX;
	private int endY;
	
	private Point closestPointToStart;
	private double distanceToClosestPointToStart;
	
	private Point closestPointToEnd;
	private double distanceToClosestPointToEnd;
	
	Client()
	{
		distanceToClosestPointToStart = Double.MAX_VALUE;
		distanceToClosestPointToEnd = Double.MAX_VALUE;
	}
	
	void setCoordinates(int startX, int startY, int endX, int endY)
	{
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
	}
	
	public int getStartX()
	{
		return startX;
	}

	public int getStartY()
	{
		return startY;
	}
	
	public Point getClosestPointToStart()
	{
		return closestPointToStart;
	}
	
	public void setClosestPointToStart(Point closestPointToStart)
	{
		this.closestPointToStart = closestPointToStart;
	}
	
	public double getDistanceToClosestPointToStart()
	{
		return distanceToClosestPointToStart;
	}
	
	public void setDistanceToClosestPointToStart(double distanceToClosestPointToStart)
	{
		this.distanceToClosestPointToStart = distanceToClosestPointToStart;
	}
	
	public String getStartString()
	{
		return new StringBuilder().append(startX * 1.0 / Math.pow(10, 7)).append(",").append(startY * 1.0 / Math.pow(10, 7)).append(",0").toString();
	}
	public String getEndString()
	{
		return new StringBuilder().append(endX * 1.0 / Math.pow(10, 7)).append(",").append(endY * 1.0 / Math.pow(10, 7)).append(",0").toString();
	}
	
	public int getEndX()
	{
		return endX;
	}
	
	public int getEndY()
	{
		return endY;
	}
	
	public Point getClosestPointToEnd()
	{
		return closestPointToEnd;
	}
	
	public void setClosestPointToEnd(Point closestPointToEnd)
	{
		this.closestPointToEnd = closestPointToEnd;
	}
	
	public double getDistanceToClosestPointToEnd()
	{
		return distanceToClosestPointToEnd;
	}
	
	public void setDistanceToClosestPointToEnd(double distanceToClosestPointToEnd)
	{
		this.distanceToClosestPointToEnd = distanceToClosestPointToEnd;
	}
}
