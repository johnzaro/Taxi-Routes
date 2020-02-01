package code;

public class Coordinates
{
	private int x;
	private int y;
	private int hash;
	
	Coordinates(int x, int y)
	{
		this.x = x;
		this.y = y;
		
		//taken from https://stackoverflow.com/questions/22826326/good-hashcode-function-for-2d-coordinates
		hash = 0;
		hash = (hash * 397) ^ x;
		hash = (hash * 397) ^ y;
	}
	
	@Override
	public int hashCode()
	{
		return hash;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		Coordinates c = (Coordinates) obj;
		
		return this.x == c.x && this.y == c.y;
	}
	
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
	
	public String toString()
	{
		return new StringBuilder().append(x / Math.pow(10, 7)).append(",").append(y / Math.pow(10, 7)).append(",0").toString();
	}
}
