package code;

public class State
{
	private Coordinates coordinates;
	private double distanceTravelled;
	private double score;
	
	State(Coordinates coordinates, double distanceTravelled, double score)
	{
		this.coordinates = coordinates;
		this.distanceTravelled = distanceTravelled;
		this.score = score;
	}
	
	public Coordinates getCoordinates()
	{
		return coordinates;
	}
	
	public double getScore()
	{
		return score;
	}
	
	public double getDistanceTravelled()
	{
		return distanceTravelled;
	}
	
	public void setDistanceTravelled(double distanceTravelled)
	{
		this.distanceTravelled = distanceTravelled;
	}
}
