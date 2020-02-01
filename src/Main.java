package code;

import com.google.common.collect.MinMaxPriorityQueue;
import com.ugos.jiprolog.engine.JIPEngine;
import com.ugos.jiprolog.engine.JIPQuery;
import com.ugos.jiprolog.engine.JIPTerm;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Main
{
	private static StreetMap streetMap;
	private static Map<Coordinates, Point> map;
	private static int queueSize;
	
	public static File clientData, taxisData, nodesData, linesData, trafficData;
	
	public static JIPEngine jip;
	
	static Client client;
	static Taxi[] taxis;
	static int numberOfTaxis;
	
	static List<TaxiRoute> taxiRoutes;
	private static TaxiRoute clientRoute;
	
	private static int clientHour;
	
	private static int winningTaxiID;
	
	public static void main(String[] args)
	{
		clientData = new File("clientData.pl");
		taxisData = new File("taxisData.pl");
		linesData = new File("linesData.pl");
		nodesData = new File("nodesData.pl");
		trafficData = new File("trafficData.pl");
		
		taxiRoutes = new ArrayList<>();
		
//		Initialization
		streetMap = new StreetMap();
		map = streetMap.getMap();
		
		try
		{
			System.out.println("Reading input files and creating the corresponding prolog data files (enjoy)...");
			
			jip = new JIPEngine();
			
			System.out.println("-Learning the rules...");
			jip.consultFile("src/resources/rules.pl");
			System.out.println("-I feel so much smarter now...");
			
			System.out.println("-Asking the client...");
			streetMap.readClientFile();
			jip.consultFile(clientData.getAbsolutePath());
			System.out.println("-Had a very interesting conversation with the client...");
			
			System.out.println("-Let's find out which taxis are around...");
			streetMap.readTaxisFile();
			jip.consultFile(taxisData.getAbsolutePath());
			System.out.println("-So many good taxi choices...");
			
			System.out.println("-Let me walk every single road of the map...");
			streetMap.readLinesFile();
			jip.consultFile(linesData.getAbsolutePath());
			System.out.println("-My legs are in pain with so much walking...");
			
			System.out.println("-Marking every point...");
			streetMap.readNodesFile();
			jip.consultFile(nodesData.getAbsolutePath());
			System.out.println("-I lost count with so many points...");
			
			System.out.println("-I can't stand the traffic, please get me out...");
			streetMap.readTrafficFile();
			jip.consultFile(trafficData.getAbsolutePath());
			System.out.println("-Done with the honking...");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		JIPQuery jipQuery3 = jip.openSynchronousQuery(jip.getTermParser().parseTerm("client(_, _, _, _, Hour, _, _, _)."));
		JIPTerm term1 = jipQuery3.nextSolution();
		jipQuery3.close();
		clientHour = Integer.parseInt(term1.getVariablesTable().get("Hour").toString());
		
		System.out.println("-Stalking the neighbours...");
		streetMap.findNeighbours();
		
		client.setClosestPointToStart(streetMap.getClosestPointToClient(true));
		client.setClosestPointToEnd(streetMap.getClosestPointToClient(false));
		client.setDistanceToClosestPointToStart(streetMap.getDistanceBetweenPoints(client.getStartX(), client.getStartY(), client.getClosestPointToStart().getX(), client.getClosestPointToStart().getY()));
		client.setDistanceToClosestPointToEnd(streetMap.getDistanceBetweenPoints(client.getEndX(), client.getEndY(), client.getClosestPointToEnd().getX(), client.getClosestPointToEnd().getY()));
		
		streetMap.findClosestPointsToTaxis();

		queueSize = Integer.parseInt(args[0]);
		
		System.out.println("-Getting you a taxi...");
		for(int i = 0; i < numberOfTaxis; i++)
		{
			JIPQuery jipQuery = jip.openSynchronousQuery(jip.getTermParser().parseTerm(String.format("isTaxiSuitable(%d).", taxis[i].getId())));
			if(jipQuery.nextSolution() != null)
			{
				
				taxiRoutes.add(
						findTaxiRoute(taxis[i].getClosestPoint(), client.getClosestPointToStart(),
								taxis[i].getDistanceFromClosestPoint(), client.getDistanceToClosestPointToStart(), taxis[i].toString(), client.getStartString()));
				
				JIPQuery jipQuery1 = jip.openSynchronousQuery(
						jip.getTermParser().parseTerm(String.format("getTaxiRating(%d, Rating).", taxis[i].getId())));
				JIPTerm term = jipQuery1.nextSolution();
				jipQuery1.close();
				double rating = Double.parseDouble(term.getVariablesTable().get("Rating").toString());
				
				taxiRoutes.get(i).setTaxiID(taxis[i].getId());
				taxiRoutes.get(i).setRating(rating);
			}
			else
			{
				taxiRoutes.add(new TaxiRoute(taxis[i].getId(), Double.MAX_VALUE, 0, ""));
			}
			jipQuery.close();
		}
		
		System.out.println("-Lets find out how to arrive to your destination...");
		clientRoute = findTaxiRoute(client.getClosestPointToStart(), client.getClosestPointToEnd(),
				client.getDistanceToClosestPointToStart(), client.getDistanceToClosestPointToEnd(), client.getStartString(), client.getEndString());
		
		if(!taxiRoutes.isEmpty())
		{
			taxiRoutes.sort(taxiRoutes.get(0).getRouteComparator());
			
			System.out.println("Available taxis sorted by distance to client");
			for(int i = 0, index = 1; i < taxiRoutes.size(); i++)
			{
				if(taxiRoutes.get(i).getDistance() < Double.MAX_VALUE)
				{
					System.out.println(String.format("%d --- Taxi id=%d", index, taxiRoutes.get(i).getTaxiID()));
					index++;
				}
			}
			
			taxiRoutes.sort(taxiRoutes.get(0).getRatingComparator());
			
			System.out.println("Available taxis sorted by rating to client");
			for(int i = 0, index = 1; i < taxiRoutes.size(); i++)
			{
				if(taxiRoutes.get(i).getDistance() < Double.MAX_VALUE)
				{
					System.out.println(String.format("%d --- Taxi id=%d, rating=%.1f", index, taxiRoutes.get(i).getTaxiID(), taxiRoutes.get(i).getRating()));
					index++;
				}
			}
		}
		
		System.out.print("Type which taxi you prefer by giving its ID and pressing <Enter>:");
		Scanner input = new Scanner(System.in);
		winningTaxiID = 0;
		while(winningTaxiID == 0)
		{
			try
			{
				winningTaxiID = input.nextInt();
				System.out.println("");
				
				boolean correct = false;
				for(int i = 0; i < taxiRoutes.size(); i++)
				{
					if(taxiRoutes.get(i).getDistance() < Double.MAX_VALUE && taxiRoutes.get(i).getTaxiID() == winningTaxiID)
					{
						correct = true;
						break;
					}
					
				}
				if(!correct)
				{
					System.out.println("Wrong input, try again...");
					winningTaxiID = 0;
				}
			}
			catch(Exception e)
			{
				System.out.println("Wrong input, try again...");
			}
		}
		
		System.out.println("That's a very good choice");
		
		System.out.println("KML file will be created...");
		
		createKML();
		
		System.out.println("It was a pleasure to calculate stuff for you, bye bye :)");
	}
	
	static TaxiRoute findTaxiRoute(Point startPoint, Point endPoint, double distanceFromStart, double distanceFromEnd, String realStart, String realEnd)
	{
		TaxiRoute taxiRoute = null;
		
		MinMaxPriorityQueue<State> pq = MinMaxPriorityQueue.orderedBy(streetMap.getStateComparator()).maximumSize(queueSize).create();
		Map<Coordinates, State> prev = new HashMap<>();
		
		//προσθετουμε ως πρωτη κατασταση στο queue αυτη που οριζεται απο το κοντινοτερο στο ταξι και αντιστοιχα στον πελατη, σημειο
		State firstState = new State(startPoint.getCoordinates(), distanceFromStart,
				distanceFromStart + streetMap.getDistanceBetweenPoints(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY()));
		
		prev.put(firstState.getCoordinates(), firstState); //when backtracking first state is found if prev.coordinates == state.getCoordinates
		pq.add(firstState);
		
		while(!pq.isEmpty())
		{
			//αφαιρουμε την καλυτερη κατασταση απο το queue
			State currentState = pq.poll();
			
			//ελεγχουμε αν φτασαμε στο τελικο σημειο δηλαδη στο κοντινοτερο σημειο στον πελατη
			if(currentState.getCoordinates().getX() == endPoint.getX() && currentState.getCoordinates().getY() == endPoint.getY())
			{
				//add distance between final node and real final point to "distanceTraveled"
				currentState.setDistanceTravelled(currentState.getDistanceTravelled() + distanceFromEnd);
				
				Coordinates currentCoordinates = endPoint.getCoordinates();
				
				StringBuilder sb = new StringBuilder();
				sb.append(realEnd).append("\n");
				sb.append(currentCoordinates.toString()).append("\n");
				
				while(true)
				{
					currentCoordinates = prev.get(currentCoordinates).getCoordinates();
					sb.append(currentCoordinates.toString()).append("\n");
					
					if(currentCoordinates == prev.get(currentCoordinates).getCoordinates()) break;
				}
				
				sb.append(realStart).append("\n");
				
				taxiRoute = new TaxiRoute(0, currentState.getDistanceTravelled(), 0, sb.toString());
				
				break;
			}
			else
			{
				//για την κατασταση που αφαιρεσαμε απο το queue παιρνουμε το Point μεσω του hash map των σημειων
				Point currentPoint = map.get(currentState.getCoordinates());
				
				//για καθε γειτονα του τρεχοντος σημειου τον τοποθετουμε στο queue
				//και εφοσον δεν υπαρχει ηδη στο Prev ή αν τωρα το εχουμε με καλυτερο σκορ, ενημερωνουμε και το prev
				currentPoint.getNeighbours().forEach(neighbour ->
				{
					//με βαση τον δρομο του επομενου κομβου και την ωρα του πελάτη υπολογιζουμε μεσω της prolog την
					//προτεραιοτητα η οποια ειναι ενα βαρος με το οποιο πολλαπλασιαζουμε την ευθεια αποσταση απο τον νεο κομβο στο τελικο στοχο
					JIPQuery jipQuery1 = jip.openSynchronousQuery(
							jip.getTermParser().parseTerm(String.format("priority(%d, %d, P).", neighbour.getLineID(), clientHour)));
					JIPTerm term = jipQuery1.nextSolution();
					double priority = Double.parseDouble(term.getVariablesTable().get("P").toString());
					jipQuery1.close();
					
					double newDistanceTravelled = currentState.getDistanceTravelled() +
							streetMap.getDistanceBetweenPoints(currentPoint.getX(), currentPoint.getY(),
									neighbour.getX(), neighbour.getY());
					double newScore = newDistanceTravelled +
							streetMap.getDistanceBetweenPoints(neighbour.getX(), neighbour.getY(),
									client.getClosestPointToStart().getX(), client.getClosestPointToStart().getY()) * priority;
					
					State newState = new State(neighbour.getCoordinates(), newDistanceTravelled, newScore);
					
					if(!prev.containsKey(newState.getCoordinates()) || newState.getDistanceTravelled() < prev.get(newState.getCoordinates()).getDistanceTravelled())
					{
						State tempState = new State(currentState.getCoordinates(), currentState.getDistanceTravelled(), newDistanceTravelled);
						
						prev.put(newState.getCoordinates(), tempState);
						pq.add(newState);
					}
				});
			}
		}
		
		return taxiRoute;
	}
	
	private static void createKML()
	{
		try
		{
			SAXBuilder builder = new SAXBuilder();
			InputStream kmlStream = Main.class.getResourceAsStream("/resources/sample.kml");

			Document doc = builder.build(kmlStream);

			Element rootNode = doc.getRootElement();
			Namespace namespace = rootNode.getNamespace();

			Element documentElement = rootNode.getChildren().get(0);
			documentElement.setNamespace(namespace);

			//client start point
			Element clientStartElement = new Element("Placemark", namespace);
			clientStartElement.addContent(new Element("name", namespace).setText("Client Start Point"));
			clientStartElement.addContent(
					new Element("Point", namespace).addContent(
							new Element("coordinates", namespace).setText(client.getStartString())));
			documentElement.addContent(clientStartElement);
			
			//client end point
			Element clientEndElement = new Element("Placemark", namespace);
			clientEndElement.addContent(new Element("name", namespace).setText("Client End Point"));
			clientEndElement.addContent(
					new Element("Point", namespace).addContent(
							new Element("coordinates", namespace).setText(client.getEndString())));
			documentElement.addContent(clientEndElement);

			//all taxi positions
			for(Taxi taxi : taxis)
			{
				Element taxiElement = new Element("Placemark", namespace);
				taxiElement.addContent(new Element("name", namespace).setText("Taxi " + taxi.getId()));
				taxiElement.addContent(
						new Element("Point", namespace).addContent(
								new Element("coordinates", namespace).setText(taxi.toString())));

				documentElement.addContent(taxiElement);
			}

			//all available taxi routes
			for(int i = 0; i < taxiRoutes.size(); i++)
			{
				if(!taxiRoutes.get(i).getRoute().isEmpty())
				{
					Element taxiRouteElement = new Element("Placemark", namespace);
					taxiRouteElement.addContent(new Element("name", namespace).setText("Taxi " + taxiRoutes.get(i).getTaxiID()));
					Element styleURL = new Element("styleUrl", namespace);
					if(winningTaxiID == taxiRoutes.get(i).getTaxiID()) styleURL.setText("#green");
					else styleURL.setText("#red");
					taxiRouteElement.addContent(styleURL);
					Element lineString = new Element("LineString", namespace);
					lineString.addContent(new Element("altitudeMode", namespace).setText("relative"));
					lineString.addContent(new Element("coordinates", namespace).setText(taxiRoutes.get(i).getRoute()));
					taxiRouteElement.addContent(lineString);
					documentElement.addContent(taxiRouteElement);
				}
			}
			
			//client route
			Element clientRouteElement = new Element("Placemark", namespace);
			clientRouteElement.addContent(new Element("name", namespace).setText("Client Route"));
			Element styleURL = new Element("styleUrl", namespace);
			styleURL.setText("#brown");
			clientRouteElement.addContent(styleURL);
			Element lineString = new Element("LineString", namespace);
			lineString.addContent(new Element("altitudeMode", namespace).setText("relative"));
			lineString.addContent(new Element("coordinates", namespace).setText(clientRoute.getRoute()));
			clientRouteElement.addContent(lineString);
			documentElement.addContent(clientRouteElement);

			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat().setIndent("\t"));
			xmlOutput.output(doc, new FileWriter("taxiRoutes_" + queueSize + ".kml"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
