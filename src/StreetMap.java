package code;

import com.ugos.jiprolog.engine.JIPQuery;
import com.ugos.jiprolog.engine.JIPTerm;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

public class StreetMap
{
	private Map<Coordinates, Point> map;
	
	private StateComparator stateComparator;
	
	StreetMap()
	{
		stateComparator = new StateComparator();
		
		map = new HashMap<>();
	}
	
	void readClientFile()
	{
		Scanner input = null;
		
		try
		{
			InputStream stream = StreetMap.class.getResourceAsStream("/resources/client.csv");
			input = new Scanner(stream);
			
			input.nextLine();
			
			String[] line = input.nextLine().split(",");
			input.close();
			
			int x, y, x_dest, y_dest, hours, persons, luggage;
			String language;
			
			//πολλαπλασιαζουμε τις συντεταγμένες με 10^7 ωστε να γίνουν ακεραιοι
			x = (int)(Double.parseDouble(line[0]) * Math.pow(10, 7));
			y = (int)(Double.parseDouble(line[1]) * Math.pow(10, 7));
			
			x_dest = (int)(Double.parseDouble(line[2]) * Math.pow(10, 7));
			y_dest = (int)(Double.parseDouble(line[3]) * Math.pow(10, 7));
			
			String[] time = line[4].split(":");
			hours = Integer.parseInt(time[0]);
			
			persons = Integer.parseInt(line[5]);
			
			luggage = Integer.parseInt(line[7]);
			
			language = line[6];
			
			Main.client = new Client();
			Main.client.setCoordinates(x, y, x_dest, y_dest);
			
			String output = String.format("client(%d, %d, %d, %d, %d, %d, %d, %s).", x, y, x_dest, y_dest, hours, persons, luggage, language);
			
			PrintWriter writer = new PrintWriter(Main.clientData, "UTF-8");
			writer.println(output);
			writer.close();
		}
		catch(Exception e)
		{
			if(input != null) input.close();
			
			e.printStackTrace();
		}
	}
	
	void readTaxisFile()
	{
		Scanner input = null;
		
		try
		{
			InputStream stream = StreetMap.class.getResourceAsStream("/resources/taxis.csv");
			input = new Scanner(stream);
			
			input.nextLine();
			
			PrintWriter writer = new PrintWriter(Main.taxisData, "UTF-8");
			
			int x, y, id, capacity;
			double rating;
			String available, languages, long_distance, type;
			String[] line;
			int counter = 0;
			List<Taxi> taxisList = new ArrayList<>();
			while(input.hasNextLine())
			{
				line = input.nextLine().split(",");
				
				x = (int)(Double.parseDouble(line[0]) * Math.pow(10, 7));
				y = (int)(Double.parseDouble(line[1]) * Math.pow(10, 7));
				
				id = Integer.parseInt(line[2]);
				
				available = line[3];
				
				String[] cap = line[4].split("-");
				capacity = Integer.parseInt(cap[1]);
				
				languages = "[" + line[5].replaceAll("\\|", ", ") + "]";
				
				rating = Double.parseDouble(line[6]);
				
				long_distance = line[7];
				
				type = line[8];
				
				String output = String.format("taxi(%d, %d, %d, %s, %d, %s, %.1f, %s, %s).", x, y, id, available, capacity, languages, rating, long_distance, type);
				
				taxisList.add(new Taxi(x, y, id));
				
				counter++;
				
				writer.println(output);
			}
			
			Main.numberOfTaxis = counter;
			
			Main.taxis = taxisList.toArray(new Taxi[0]);
			
			writer.close();
			input.close();
		}
		catch(Exception e)
		{
			if(input != null) input.close();
			
			e.printStackTrace();
		}
	}
	
	void readLinesFile()
	{
		Scanner input = null;
		
		try
		{
			InputStream stream = StreetMap.class.getResourceAsStream("/resources/lines.csv");
			input = new Scanner(stream);
			
			PrintWriter writer = new PrintWriter(Main.linesData, "UTF-8");
			
			input.nextLine();
			
			String[] line;
			while(input.hasNextLine())
			{
				line = input.nextLine().replaceAll("'", "").replaceAll("%", "").split(",", -1);
				
				if(line.length != 18)
				{
					line[2] = "_";
					line[3] = "_";
					line[4] = "_";
				}
				else
				{
					line[2] = "_";
				}
				
				StringBuilder sb = new StringBuilder();
				sb.append("line(");
				for(String s: line)
				{
					if(!s.equals("_"))
					{
						if(s.isEmpty())
							sb.append("unknown,");
						else
							sb.append(s).append(",");
					}
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append(").");
				
				String output = sb.toString();
				
				writer.println(output);
			}
			writer.close();
			input.close();
		}
		catch(Exception e)
		{
			if(input != null) input.close();
			
			e.printStackTrace();
		}
	}
	
	void readNodesFile()
	{
		Scanner input = null;
		
		try
		{
			InputStream stream = StreetMap.class.getResourceAsStream("/resources/nodes.csv");
			input = new Scanner(stream);
			
			PrintWriter writer = new PrintWriter(Main.nodesData, "UTF-8");
			
			input.nextLine();
			
			JIPQuery jipQuery;
			
			int x, y, line_id, index = 0;
			long node_id;
			String[] line;
			while(input.hasNextLine())
			{
				line = input.nextLine().split(",");
				
				x = (int)(Double.parseDouble(line[0]) * Math.pow(10, 7));
				y = (int)(Double.parseDouble(line[1]) * Math.pow(10, 7));
				
				line_id = Integer.parseInt(line[2]);
				node_id = Long.parseLong(line[3]);
				
				//δινουμε στην prolog το id του δρομου στον οποιο ανηκει το τρεχον node και μας επιστρεφει κανοντας ελεγχους αν ο δρομος ειναι εγκυρος, αν οχι δε το προσθετουμε στα γεγονοτα
				jipQuery = Main.jip.openSynchronousQuery(Main.jip.getTermParser().parseTerm("unavailable(" + line_id + ")."));
				if(jipQuery.nextSolution() == null)
				{
					String output = String.format("node(%d, %d, %d, %d, %d).", node_id, index, line_id, x, y);
					writer.println(output);
					
					map.put(new Coordinates(x, y), new Point(x, y, node_id, line_id));
					
					index++;
				}
				jipQuery.close();
			}
			writer.close();
			input.close();
		}
		catch(Exception e)
		{
			if(input != null) input.close();
			
			e.printStackTrace();
		}
	}
	
	void readTrafficFile()
	{
		Scanner input = null;
		
		try
		{
			InputStream stream = StreetMap.class.getResourceAsStream("/resources/traffic.csv");
			input = new Scanner(stream);
			
			PrintWriter writer = new PrintWriter(Main.trafficData, "UTF-8");
			
			input.nextLine();
			
			String[] line;
			while(input.hasNextLine())
			{
				line = input.nextLine().replaceAll("'", "").split(",");
				
				int lineID;
				String traffic;
				String[] trafficArray;
				if(line.length >= 3)
				{
					try
					{
						lineID = Integer.parseInt(line[0]);
						
						traffic = line[line.length - 1];
						
						trafficArray = traffic.split("\\|");
						
						int startH, endH;
						String trafficState;
						String[] temp, temp2;
						for(String trafficValue : trafficArray)
						{
							temp = trafficValue.split("=");
							trafficState = temp[1];
							
							temp = temp[0].split("-");
							temp2 = temp[1].split(":");
							temp = temp[0].split(":");
							
							startH = Integer.parseInt(temp[0]);
							endH = Integer.parseInt(temp2[0]);
							
							String output = String.format("traffic(%d, %d, %d, %s).", lineID, startH, endH, trafficState);
							
							writer.println(output);
						}
					}
					catch(Exception e)
					{
					
					}
				}
			}
			writer.close();
			input.close();
		}
		catch(Exception e)
		{
			if(input != null) input.close();
			
			e.printStackTrace();
		}
	}
	
	private boolean isNext(Point current, Point next)
	{
		if(current != null && next != null)
		{
			//δινουμε στην Prolog 2 σημεια και μας επιστρεφει με true ή false αν το ενα ειναι επομενο του αλλου
			JIPQuery jipQuery = Main.jip.openSynchronousQuery(
					Main.jip.getTermParser().parseTerm(String.format("next(%d, %d).", current.getId(), next.getId())));
			boolean isN = jipQuery.nextSolution() != null;
			jipQuery.close();
			
			return isN;
		}
		return false;
	}
	
	void findNeighbours()
	{
		Scanner input;
		
		try
		{
			input = new Scanner(Main.nodesData);
			
			Point prev = null, current = null, next;
			Coordinates currentCoordinates;
			
			if(input.hasNextLine()) current = getPointFromLine(input.nextLine().replace("node(", "").replace(").", "").split(", "));
			
			while(current != null)
			{
				if(input.hasNextLine()) next = getPointFromLine(input.nextLine().replace("node(", "").replace(").", "").split(", "));
				else next = null;
				
				currentCoordinates = new Coordinates(current.getX(), current.getY());
				
				if(map.containsKey(currentCoordinates))
				{
					/*
						Αν το σημείο υπάρχει ήδη στο hash map δηλαδή είναι σημείο διασταύρωσης, τοτε
						βρες το μέσα στο hash map και πρόσθεσε του για γείτονες το prev και το next εφόσον
						υπάρχουν και ανήκουν σε δρόμο που περνάει από τη διασταύρωση
					*/
					Point mapPoint = map.get(currentCoordinates);
					if(prev != null && isNext(current, prev)) mapPoint.addNeighbour(prev);
					if(next != null && isNext(current, next)) mapPoint.addNeighbour(next);
				}
				else
				{
					/*
						Αν το σημείο δεν υπάρχει στο hash map δηλαδή είναι απλό σημείο κάποιου δρόμου, τοτε
						πρόσθεσε του για γείτονες το prev και το next εφόσον υπάρχουν και ανήκουν στο δρόμο αυτόν
						και πρόσθεσε το στο hash map
					*/
					if(prev != null && isNext(current, prev)) current.addNeighbour(prev);
					if(next != null && isNext(current, next)) current.addNeighbour(next);
					
					map.put(currentCoordinates, current);
				}
				
				prev = current;
				current = next;
			}
			input.close();
		}
		catch(Exception e)
		{
		
		}
	}
	
	private Point getPointFromLine(String[] line)
	{
		long id = Long.parseLong(line[0]);
		
		int lineId = Integer.parseInt(line[2]);
		
		int x = Integer.parseInt(line[3]);
		
		int y = Integer.parseInt(line[4]);
		
		return new Point(x, y, id, lineId);
	}
	
	Point getClosestPointToClient(boolean start)
	{
		//ψαχνουμε για κάθε σημείο του hash map ποιο είναι το κοντινοτερο
		//στον πελάτη με βαση την ευκλείδεια αποσταση
		
		JIPQuery jipQuery;
		JIPTerm term;
		int x1, x2, y1, y2, minX2 = 0, minY2 = 0;
		long minNodeId = 0;
		double minDistance = Double.MAX_VALUE, dis;
		
		String s;
		if(start) s = "client(X, Y, _, _, _, _, _, _).";
		else s = "client(_, _, X, Y, _, _, _, _).";
		
		jipQuery = Main.jip.openSynchronousQuery(Main.jip.getTermParser().parseTerm(s));
		term = jipQuery.nextSolution();
		x1 = Integer.parseInt(term.getVariablesTable().get("X").toString());
		y1 = Integer.parseInt(term.getVariablesTable().get("Y").toString());
		
		jipQuery = Main.jip.openSynchronousQuery(Main.jip.getTermParser().parseTerm("getCoordinates(NodeID, X2, Y2)."));
		term = jipQuery.nextSolution();
		while (term != null)
		{
			x2 = Integer.parseInt(term.getVariablesTable().get("X2").toString());
			y2 = Integer.parseInt(term.getVariablesTable().get("Y2").toString());
			
			dis = getDistanceBetweenPoints(x1, y1, x2, y2);
			
			if(dis < minDistance)
			{
				minDistance = dis;
				minNodeId = Double.valueOf(term.getVariablesTable().get("NodeID").toString()).longValue();
				minX2 = x2;
				minY2 = y2;
			}
			
			term = jipQuery.nextSolution();
		}
		
		jipQuery.close();
		
		return new Point(minX2, minY2, minNodeId, -1);
	}
	
	void findClosestPointsToTaxis()
	{
		//ψαχνουμε για κάθε σημείο του hash map ποιο είναι το κοντινοτερο
		//σημείο για κάθε ταξί με βαση την ευκλείδεια αποσταση
		
		JIPQuery jipQuery1, jipQuery2;
		JIPTerm term;
		int x1, x2, y1, y2, minX2 = 0, minY2 = 0, id;
		long minNodeId;
		double minDistance, dis;
		
		jipQuery1 = Main.jip.openSynchronousQuery(Main.jip.getTermParser().parseTerm("taxi(X1, Y1, ID, _, _, _, _, _, _)."));
		term = jipQuery1.nextSolution();
		int counter = 0;
		while (term != null)
		{
			x1 = Integer.parseInt(term.getVariablesTable().get("X1").toString());
			y1 = Integer.parseInt(term.getVariablesTable().get("Y1").toString());
			id = Integer.parseInt(term.getVariablesTable().get("ID").toString());
			
			minDistance = Double.MAX_VALUE;
			minNodeId = 0;
			
			jipQuery2 = Main.jip.openSynchronousQuery(Main.jip.getTermParser().parseTerm("getCoordinates(NodeID, X2, Y2)."));
			term = jipQuery2.nextSolution();
			while(term != null)
			{
				x2 = Integer.parseInt(term.getVariablesTable().get("X2").toString());
				y2 = Integer.parseInt(term.getVariablesTable().get("Y2").toString());
				
				dis = getDistanceBetweenPoints(x1, y1, x2, y2);
				
				if(dis < minDistance)
				{
					minDistance = dis;
					minNodeId = Double.valueOf(term.getVariablesTable().get("NodeID").toString()).longValue();
					minX2 = x2;
					minY2 = y2;
				}
				
				term = jipQuery2.nextSolution();
			}
			
			Main.taxis[counter].setClosestPoint(new Point(minX2, minY2, minNodeId, -1));
			Main.taxis[counter].setDistanceFromClosestPoint(minDistance);
			
			jipQuery2.close();
			
			counter++;
			
			term = jipQuery1.nextSolution();
		}
		
		jipQuery1.close();
	}
	
	double getDistanceBetweenPoints(int x1, int y1, int x2, int y2)
	{
		//ευκλείδεια αποσταση
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}
	
	public class StateComparator implements Comparator
	{
		//δίνεται στο MinMaxPriorityQueue ωστε να ξερει πως να συγκρινει τα states Που το δινουμε
		//τα συγκρινουμε με βαση το σκορ τους
		@Override
		public int compare(Object o1, Object o2)
		{
			return (int)(((State)o1).getScore() - ((State)o2).getScore());
		}
	}
	
	public StateComparator getStateComparator()
	{
		return stateComparator;
	}
	
	public Map<Coordinates, Point> getMap()
	{
		return map;
	}
}
