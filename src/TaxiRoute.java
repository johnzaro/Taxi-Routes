package code;

import java.util.Comparator;

public class TaxiRoute
{
	private int taxiID;
	private double distance;
	private double rating;
	
	private String route;
	
	private RouteComparator routeComparator;
	private RatingComparator ratingComparator;
	
	public TaxiRoute(int taxiID, double distance, double rating, String route)
	{
		routeComparator = new RouteComparator();
		ratingComparator = new RatingComparator();
		
		this.taxiID = taxiID;
		this.distance = distance;
		this.rating = rating;
		this.route = route;
	}
	
	public void setTaxiID(int taxiID)
	{
		this.taxiID = taxiID;
	}
	
	public int getTaxiID()
	{
		return taxiID;
	}
	
	public double getDistance()
	{
		return distance;
	}
	
	public double getRating()
	{
		return rating;
	}
	
	public void setRating(double rating)
	{
		this.rating = rating;
	}
	
	public String getRoute()
	{
		return route;
	}
	
	public class RouteComparator implements Comparator
	{
		@Override
		public int compare(Object o1, Object o2)
		{
			double cmp = ((TaxiRoute)o1).getDistance() - ((TaxiRoute)o2).getDistance();
			if(cmp < 0) return -1;
			else if(cmp > 0) return 1;
			else return 0;
		}
	}
	
	public class RatingComparator implements Comparator
	{
		@Override
		public int compare(Object o1, Object o2)
		{
			double cmp = ((TaxiRoute)o2).getRating() - ((TaxiRoute)o1).getRating();
			if(cmp < 0) return -1;
			else if(cmp > 0) return 1;
			else return 0;
		}
	}
	
	public RouteComparator getRouteComparator()
	{
		return routeComparator;
	}
	
	public RatingComparator getRatingComparator()
	{
		return ratingComparator;
	}
}
