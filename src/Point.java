package code;

import java.util.ArrayList;

public class Point
{
	private int x;
	private int y;
	private long id;
	private int lineID;
	private ArrayList<Point> neighbours;
	
	Point(int x, int y, long id, int lineID)
	{
		this.x = x;
		this.y = y;
		this.id = id;
		this.lineID = lineID;
		neighbours = new ArrayList<>();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		Point p = (Point) obj;
		
		return this.x == p.x && this.y == p.y;
	}
	
	public int getLineID()
	{
		return lineID;
	}
	
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
	
	public long getId()
	{
		return id;
	}
	
	public void addNeighbour(Point neighbour)
	{
		neighbours.add(neighbour);
	}
	
	public ArrayList<Point> getNeighbours()
	{
		return neighbours;
	}
	
	public Coordinates getCoordinates()
	{
		return new Coordinates(x, y);
	}
}
