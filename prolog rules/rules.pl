%client: X, Y, X_dest, Y_dest, hours, persons, luggage, language
%taxi: X, Y, id, available, capacity, languages, rating, long_distance, type
%node: node_id, index, line_id, X, Y
%line: Id, Highway, Oneway, Lit, Lanes, Maxspeed, Railway, Boundary, Access, Natural, Barrier, Tunnel, Bridge, Incline, Waterway, Busway, Toll

%θεωρουμε οτι μια γραμμη ειναι μη διαθεσιμη αν εχουμε πληροφορια για ενα απο τα Railway, Access, Natural, Barrier, Waterway, Busway 
%ή αν το highway δεν ανηκει σε μια απο τις κατηγοριες που θεωρουμε ως δρομους, αν το highway ειναι unclassified και δεν εχουμε πληροφορια για τη κατευθυνση του θεωρειται ακυρος
unavailable(Line):-
	line(Line, Highway, Oneway, _, _, _, Railway, _, Access, Natural, Barrier, _, _, _, Waterway, Busway, _),
	(Railway \= unknown;
	Access \= unknown;
	Natural \= unknown;
	Barrier \= unknown;
	Waterway \= unknown;
	Busway \= unknown ;
	\+ member(Highway, [tertiary, tertiary_link, secondary, secondary_link, highway, primary, primary_link, trunk, trunk_link, motorway, motorway_link, unclassified])),
	(Highway = unclassified -> Oneway = unknown; true).

%X is OldNodeId, Y is NewNodeId
%χρησιμοποιουμε το index που εχουμε δωσει στα nodes ωστε να παρουμε το προηγουμενο ή το επομενο node και αναλογα με τη κατευθυνση του δρομου επιστρεφουμε αν ειναι δυνατη η μεταβαση απο το ενα στο αλλο
next(X, Y) :-
	node(X, Index, LineId, _, _),
	line(LineId, _, Dir, _, _, _, _, _, _, _, _, _, _, _, _, _, _),
	(Dir = yes ->
		NewIndex is (Index + 1) ;
	 Dir = -1 ->
		NewIndex is (Index - 1) ;
	 (Dir = no ; Dir = unknown) ->
		(NewIndex is (Index + 1) ; NewIndex is (Index - 1))
	),
	node(Y, NewIndex, LineId, _, _).

%ελεγχουμε αν ενα node ανηκει σε ενα δρομο μεσω των id τους	
belongsTo(X, L) :-
	node(X, _, L, _, _).	

%με χρηση του δρομου και του χρονου του πελατη (ωρες) φτιαχνουμε ενα βαρος αναλογα με τις 3 καταστασεις του traffic και το επιστρεφουμε	
trafficPriority(L, T, P) :-
	traffic(L, StartH, EndH, State),
	(T >= StartH, T =< EndH ->
		(State = high -> P is 2.0 ;
		 State = medium -> P is 1.0 ;
		 State = low -> P is 0.8
		)
	).
	
%με χρηση του δρομου και του χρονου του πελατη (ωρες) φτιαχνουμε ενα βαρος που δινεται στη java και πολλαπλασιαζεται με την ευριστικη συναρτηση του Α*
%το βαρος προκυπτει πολλαπλασιαζοντασ τα βαρη των παρακατω κατηγοριων. μεγαλυτερο βαρος σημαινει οτι ειναι λιγοτερο πιθανο να προτιμηθει αυτος ο δρομος
priority(L, T, P) :-
	line(L, Highway, _, Lit, Lanes, Maxspeed, _, _, _, _, _, _, _, Incline, _, _, Toll),
	%χρησιμοποιουμε το trafficPriority ωστε να παρουμε ενα βαρος αναλογα με τις 3 καταστασεις του traffic
	(trafficPriority(L, T, TrafficP) -> true ; \+ trafficPriority(L, T, TrafficP) -> TrafficP is 1.0),
	%αν η ωρα του πελατη ειναι μεταξυ 6 το απογευμα και 8 το πρωι και ο δρομος δεν ειναι φωτισμενος εχει μεγαλυτερο βαρος
	((T >= 18 ; T =< 8), Lit = no -> LitP is 1.4 ; LitP is 1.0),
	%περισσοτερες λωριδες γυρνανε μικροτερο βαρος
	(Lanes = 1 -> LanesP is 1.1 ; (Lanes = 2 ; Lanes = 3) -> LanesP is 1.0 ; (Lanes = 4 ; Lanes = 5 ; Lanes = 6) -> LanesP is 0.9 ; LanesP is 1.0),
	%μεγαλυτερη μεγιστη ταχυτητα δινει μικροτερο βαρος
	(member(Maxspeed, [10, 20, 30, 40]) -> MaxspeedP is 1.4 ; member(Maxspeed, [50, 60]) -> MaxspeedP is 1.0 ; member(Maxspeed, [70, 80, 90]) -> MaxspeedP is 0.8 ; MaxspeedP is 1.0),
	%αν το incline εχει τιμη 10 ή 15 δινουμε μεγαλυτερο βαρος, οι υπολοιπες τιμες δε το επηρεαζουν 
	((Incline = 10 ; Incline = 15) -> InclineP is 1.2 ; InclineP is 1.0),
	%διαδρομη με διοδια δινει μεγαλυτερο βαρος
	(Toll = yes -> TollP is 1.2 ; TollP is 1.0),
	%δρομοι ταχυτερης κυκλοφοριας δινουν μικροτερο βαρος
	((Highway = residential ; Highway = unclassified) -> HighwayP is 1.0 ; 
	 member(Highway, [tertiary, tertiary_link]) -> HighwayP is 0.95 ; 
	 member(Highway, [secondary, secondary_link, highway]) -> HighwayP is 0.9 ;
	 member(Highway, [primary, primary_link]) -> HighwayP is 0.85 ; 
	 member(Highway, [trunk, trunk_link, motorway, motorway_link]) -> HighwayP is 0.8),
	 P is TrafficP * LitP * LanesP * MaxspeedP * InclineP * TollP * HighwayP.
	
%παιρνουμε τις συντεταγμενες ενος Node δινοντας το nodeID
getCoordinates(NodeID, X, Y) :-
	node(NodeID, _, _, X, Y).
	
%ευκλειδια αποσταση μεταξυ 2 nodes	
getDistance(X1, Y1, X2, Y2, D) :-
	D is sqrt((X1 - X2) ** 2 + (Y1 - Y2) ** 2).
	
%αποφασιζει αν ενα ταξι ειναι καταλληλο για τη μεταφορα του πελατη	
isTaxiSuitable(TaxiID) :-
	client(X1, Y1, X2, Y2, _, PersonsC, LuggageC, LanguageC),
	taxi(_, _, TaxiID, Available, PersonsT, LanguagesT, _, LongDistance, Type),
	getDistance(X1, Y1, X2, Y2, D),
	Available = yes, %πρεπει το ταξι να ειναι διαθεσιμο
	PersonsC =< PersonsT, %πρεπει να μπορει να μεταφερει τον αριθμο των ατομων που ζηταει ο πελατης
	(D > 4500000 -> LongDistance = yes ; true), %πρεπει να υποστηριζει το ταξι μεγαλες διαδρομες αν η αποσταση που θελει ο πελατης ειναι μεγαλυτερη ενος οριου που θετουμε (αποσταση μεταξυ των δυο ακρων της Αθηνας)
	(Type = subcompact -> LuggageC =< 1 ; Type = compact -> LuggageC =< 3 ; Type = large -> LuggageC =< 6), %ορισαμε αυθαιρετα ποσες αποσκευες χωραει καθε τυπος ταξι και αναλογα αποφασιζουμε αν το ταξι ειναι καταλληλο
	member(LanguageC, LanguagesT). %πρεπει ο ταξιτζης να μιλαει τη γλωσσα που μιλαει ο πελατης
	
%δινουμε taxiID και μας επιστρεφει το rating του ταξι	
getTaxiRating(TaxiID, Rating) :-
	taxi(_, _, TaxiID, _, _, _, Rating, _, _).
		