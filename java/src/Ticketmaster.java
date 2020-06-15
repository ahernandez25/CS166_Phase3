/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.time.Duration;
import java.time.LocalTime; 

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class Ticketmaster {
	// reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	public Ticketmaster(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try {
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println("Connection URL: " + url + "\n");

			// obtain a physical connection
			this._connection = DriverManager.getConnection(url, user, passwd);
			System.out.println("Done");
		} catch (Exception e) {
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
			System.out.println("Make sure you started postgres on this machine");
			System.exit(-1);
		}
	}

	/**
	 * Method to execute an update SQL statement. Update SQL instructions includes
	 * CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 */
	public void executeUpdate(String sql) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement();

		// issues the update instruction
		stmt.executeUpdate(sql);

		// close the instruction
		stmt.close();
	}// end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT). This method
	 * issues the query to the DBMS and outputs the results to standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult(String query) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement();

		// issues the query instruction
		ResultSet rs = stmt.executeQuery(query);

		/*
		 * obtains the metadata object for the returned result set. The metadata
		 * contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData();
		int numCol = rsmd.getColumnCount();
		int rowCount = 0;

		// iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()) {
			if (outputHeader) {
				for (int i = 1; i <= numCol; i++) {
					System.out.print(rsmd.getColumnName(i) + "\t");
				}
				System.out.println();
				outputHeader = false;
			}
			for (int i = 1; i <= numCol; ++i)
				System.out.print(rs.getString(i) + "\t");
			System.out.println();
			++rowCount;
		} // end while
		stmt.close();
		return rowCount;
	}

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT). This method
	 * issues the query to the DBMS and returns the results as a list of records.
	 * Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult(String query) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement();

		// issues the query instruction
		ResultSet rs = stmt.executeQuery(query);

		/*
		 * obtains the metadata object for the returned result set. The metadata
		 * contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData();
		int numCol = rsmd.getColumnCount();
		int rowCount = 0;

		// iterates through the result set and saves the data returned by the query.
		boolean outputHeader = false;
		List<List<String>> result = new ArrayList<List<String>>();
		while (rs.next()) {
			List<String> record = new ArrayList<String>();
			for (int i = 1; i <= numCol; ++i)
				record.add(rs.getString(i));
			result.add(record);
		} // end while
		stmt.close();
		return result;
	}// end executeQueryAndReturnResult

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT). This method
	 * issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery(String query) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement();

		// issues the query instruction
		ResultSet rs = stmt.executeQuery(query);

		int rowCount = 0;

		// iterates through the result set and count nuber of results.
		if (rs.next()) {
			rowCount++;
		} // end while
		stmt.close();
		return rowCount;
	}

	/**
	 * Method to fetch the last value from sequence. This method issues the query to
	 * the DBMS and returns the current value of sequence used for autogenerated
	 * keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */

	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement();

		ResultSet rs = stmt.executeQuery(String.format("Select currval('%s')", sequence));
		if (rs.next())
			return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup() {
		try {
			if (this._connection != null) {
				this._connection.close();
			} // end if
		} catch (SQLException e) {
			// ignored.
		} // end try
	}// end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login
	 *             file>
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: " + "java [-classpath <classpath>] " + Ticketmaster.class.getName()
					+ " <dbname> <port> <user>");
			return;
		} // end if

		Ticketmaster esql = null;

		try {
			System.out.println("(1)");

			try {
				Class.forName("org.postgresql.Driver");
			} catch (Exception e) {

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}

			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];

			esql = new Ticketmaster(dbname, dbport, user, "");

			boolean keepon = true;
			while (keepon) {
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add User");
				System.out.println("2. Add Booking");
				System.out.println("3. Add Movie Showing for an Existing Theater");
				System.out.println("4. Cancel Pending Bookings");
				System.out.println("5. Change Seats Reserved for a Booking");
				System.out.println("6. Remove a Payment");
				System.out.println("7. Clear Cancelled Bookings");
				System.out.println("8. Remove Shows on a Given Date");
				System.out.println("9. List all Theaters in a Cinema Playing a Given Show");
				System.out.println("10. List all Shows that Start at a Given Time and Date");
				System.out.println("11. List Movie Titles Containing \"love\" Released After 2010");
				System.out.println("12. List the First Name, Last Name, and Email of Users with a Pending Booking");
				System.out.println(
						"13. List the Title, Duration, Date, and Time of Shows Playing a Given Movie at a Given Cinema During a Date Range");
				System.out.println(
						"14. List the Movie Title, Show Date & Start Time, Theater Name, and Cinema Seat Number for all Bookings of a Given User");
				System.out.println("15. EXIT");

				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()) {
					case 1:
						AddUser(esql);
						break;
					case 2:
						AddBooking(esql);
						break;
					case 3:
						AddMovieShowingToTheater(esql);
						break;
					case 4:
						CancelPendingBookings(esql);
						break;
					case 5:
						ChangeSeatsForBooking(esql);
						break;
					case 6:
						RemovePayment(esql);
						break;
					case 7:
						ClearCancelledBookings(esql);
						break;
					case 8:
						RemoveShowsOnDate(esql);
						break;
					case 9:
						ListTheatersPlayingShow(esql);
						break;
					case 10:
						ListShowsStartingOnTimeAndDate(esql);
						break;
					case 11:
						ListMovieTitlesContainingLoveReleasedAfter2010(esql);
						break;
					case 12:
						ListUsersWithPendingBooking(esql);
						break;
					case 13:
						ListMovieAndShowInfoAtCinemaInDateRange(esql);
						break;
					case 14:
						ListBookingInfoForUser(esql);
						break;
					case 15:
						keepon = false;
						break;
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		} finally {
			try {
				if (esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup();
					System.out.println("Done\n\nBye !");
				} // end if
			} catch (Exception e) {
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			} catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			} // end try
		} while (true);
		return input;
	}// end readChoice

	public static void AddUser(Ticketmaster esql) throws IOException, SQLException {// 1
		
		
		System.out.println("Enter first name of new user: ");
                String fname = in.readLine();
                System.out.println("Enter last name of new user: ");
                String lname = in.readLine();
                System.out.println("Enter email address of new user: ");
                String email = in.readLine();
                System.out.println("Enter phone number of new user: ");
                String phone = in.readLine();
		System.out.println("Enter a password for your account: ");
		String password = in.readLine();                

                //enter user into DB
                esql.executeQuery("insert into users values('" + email + "', '" + lname + "', '" + fname  +"', '" + phone + "', '" + password + "');");
                
	}

	public static void AddBooking(Ticketmaster esql) throws IOException, SQLException {// 2
		String email, title, cinema, mvid;

		System.out.println("Please enter email of account you would like to make the booking with: ");
		email = in.readLine();
		
		//validate email exists in db
		List<List<String>> user = esql.executeQueryAndReturnResult("select email from users where email = '" + email + "';");
		if(user != null && user.isEmpty()) {
			//given email is invalid
			System.out.println("Invalid email address! Try again.");
			return;
		}
		
		System.out.println("Enter movie you would like to see: ");
		title = in.readLine();
		//validate movie exists
		List<List<String>> movieid = esql.executeQueryAndReturnResult("select mvid from movies where title = '" + title + "';");
		if(movieid != null && !movieid.isEmpty()) {
			mvid = movieid.get(0).get(0);
		} else {
			//given movie is invalid
			System.out.println("Not a valid movie title! Try again.");
			return;
		}
		
		System.out.println("Which cinema would you like to go to? ");
		cinema = in.readLine();
		System.out.println("We found these showings at that cinema:");

		//pull list of possible showings
		//esql.executeQueryAndPrintResult("select sid, sdate, sttime from shows where mvid = '" + mvid + "' and sid in (select sid from plays where tid in (select tid from theaters where cid in (select cid from cinemas where cname = '" + cinema + "')));");
		esql.executeQueryAndPrintResult("select sid, sdate, sttime from shows where mvid = '2' and sid in (select sid from plays where tid in (select tid from theaters where tname = '" + cinema + " Theaters 1'));");
		System.out.println("Enter sid of showing you would like to attend. If there are no showings listed, enter \"no showing\": ");
		String sid = in.readLine();
		
		//check that a showing was able to be found!!
		if(sid.equals("no showing")) {
			return;
		}
		
		//get fate and time in proper format
		List<List<String>> showingdatetime = esql.executeQueryAndReturnResult("select sdate, sttime from shows where sid = '" + sid + "';");
		String datetime = "";
		if(showingdatetime != null && !showingdatetime.isEmpty()) {
			datetime = showingdatetime.get(0).get(0) + " " +  showingdatetime.get(0).get(1);
		}
		
		//bid and payment status
		List<List<String>> maxbid = esql.executeQueryAndReturnResult("select max(bid) from bookings;");
		String bid = Integer.toString(Integer.parseInt(maxbid.get(0).get(0)) + 1);
		String status = "Paid";
		
		//book seating
		System.out.println("How many seats would you like to reserve? ");
		String seats = in.readLine();
		int numSeats = Integer.parseInt(seats);
		
		List<List<String>> theaterid = esql.executeQueryAndReturnResult("select tid from plays where sid = '" + sid + "';");
		String tid = theaterid.get(0).get(0);
		
		//enter booking info into table
		String q1 = "insert into bookings values ('" + bid + "', '" + status + "', '" + datetime + "', '" + seats + "', '" + sid + "', '" + email + "');";
                esql.executeUpdate(q1);

		//reserve seat selections
		for(int i = 0; i < numSeats; i++) {
			System.out.println("These are the available seats in the theater. Enter seat number you want to reserve. If there are no empty seats, enter \"no seats\": ");
			esql.executeQueryAndPrintResult("select sno from cinemaseats where tid = '" + tid + "' and csid not in (select csid from showseats);");
			String sno = in.readLine();
		
			if(sno.equals("no seats")) {
				return;
			}
			
			String csid = esql.executeQueryAndReturnResult("select csid from cinemaseats where tid = '" + tid + "' and sno = '" + sno + "';").get(0).get(0);
			String price = "8";
			
			List<List<String>> maxssid = esql.executeQueryAndReturnResult("select max(ssid) from showseats;");
                	String ssid = Integer.toString(Integer.parseInt(maxssid.get(0).get(0)) + 1);
			
			String q = "insert into showseats values('" + ssid + "', '" + sid + "', '" + csid + "', '" + bid + "', '" + price + "');";
			esql.executeUpdate(q);
		}	

		System.out.println("Your booking was sucessfully processed!");
	}


	public static void AddMovieShowingToTheater(Ticketmaster esql) throws IOException, SQLException {// 3
		/*insert into movies (mvid, title, rdate, country, description, duration, lang, genre) values
		 (54, 'School of Rock', '10/03/2003', 'United States', 'Good Movie', 6180, 'en', 'Comedy');

		 insert into shows (sid, mvid, sdate, sttime, edtime) values (201, 54, '06/09/2020', '12:00', '1:43');

		 insert into plays (sid, tid) values (201, 444);

	*/
	String title, releaseDate, country, description, lang, genre, sdate, sttime, edtime, q1, q2, q3;
	int mvid, sid, tid, duration;



	List<List<String>> mvidMax = esql.executeQueryAndReturnResult("select max(mvid) from movies;");
	mvid = Integer.parseInt(mvidMax.get(0).get(0)) + 1;

	List<List<String>> sidMax = esql.executeQueryAndReturnResult("select max(sid) from shows;");
	sid = Integer.parseInt(sidMax.get(0).get(0)) + 1;

	System.out.println("**** Movie Information ****");
	System.out.print("Movie Title: ");
	title = in.readLine();
	System.out.println("");

	System.out.print("Release Date(MM/DD/YYYY): ");
	releaseDate = in.readLine();
	System.out.println("");

	System.out.print("Country: ");
	country = in.readLine();
	System.out.println("");

	System.out.print("Description: ");
	description = in.readLine();
	System.out.println("");	

	do {
		System.out.print("Duration(Seconds): ");
		try { // read the integer, parse it and break.
			duration = Integer.parseInt(in.readLine());
			break;
		} catch (Exception e) {
			System.out.println("Your input is invalid!");
			continue;
		} // end try
	} while (true);
	System.out.println("");

	System.out.print("Language(2 Letter Abreviation): ");
	lang = in.readLine();
	System.out.println("");	

	System.out.print("Genre: ");
	genre = in.readLine();
	System.out.println("");	

	System.out.println("**** Show Information ****");
	System.out.print("Show Date(MM/DD/YYYY): ");
	sdate = in.readLine();
	System.out.println("");
	
	System.out.print("Start Time(HH:MM): ");
	sttime = in.readLine();
	System.out.println("");

	System.out.print("End Time(HH:MM): ");
	edtime = in.readLine();
	System.out.println("");


	List<List<String>> tidMin = esql.executeQueryAndReturnResult("select min(tid) from theaters;");
	int tidMIN = Integer.parseInt(tidMin.get(0).get(0));

	List<List<String>> tidMax = esql.executeQueryAndReturnResult("select max(tid) from theaters;");
	int tidMAX = Integer.parseInt(tidMax.get(0).get(0));

	System.out.print("Theater ID (Between " + tidMIN + " and " + tidMAX + ") : ");
	tid = Integer.parseInt(in.readLine());
	System.out.println("");

	q1 = "INSERT INTO Movies (mvid, title, rdate, country, description, duration, lang, genre) values (" 
	+ mvid + ", '" + title + "', '" + releaseDate + "', '" + country + "', '" + description + "', " + 
	duration + ", '" + lang + "', '" + genre + "' );";
	esql.executeUpdate(q1);

	q2 = "INSERT INTO Shows (sid, mvid, sdate, sttime, edtime) values (" + sid + ", " + mvid + ", '" + 
	sdate + "', '" + sttime + "', '" + edtime + "');";
	esql.executeUpdate(q2);

	q3 = "INSERT INTO Plays (sid, tid) values (" + sid + ", " + tid + ");";
	esql.executeUpdate(q3);


	}//end option 3


	public static void CancelPendingBookings(Ticketmaster esql) {// 4
		String query;
		query = "UPDATE Bookings SET status = 'cancelled' WHERE status = 'pending';";
		try {
			esql.executeUpdate(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("\n Pending Bookings cancelled\n");		

	}

	
 	public static void ChangeSeatsForBooking(Ticketmaster esql) throws IOException, SQLException {// 5
                System.out.println("What is the email that the booking was made with? ");
                String email = in.readLine();

                List<List<String>> confirmEmail = esql.executeQueryAndReturnResult("select email from bookings where email = '" + email + "';");

                if(confirmEmail != null && confirmEmail.isEmpty()) {
                        System.out.println("We coudn't find any bookings with that email! Try again.");
                        return;
                }

                System.out.println("Here are the bookings on your account: ");
                esql.executeQueryAndPrintResult("select * from bookings where email = '" + email + "';");


                System.out.println("What is the booking id you would like to change? ");
                String bid = in.readLine();

                List<List<String>> bookingid = esql.executeQueryAndReturnResult("select bid from bookings where bid = '" + bid + "';");
                if(bookingid != null && bookingid.isEmpty()) {
                        System.out.println("Invalid booking id! Try again.");
                        return;
                }

                String sid = esql.executeQueryAndReturnResult("select sid from bookings where bid = '" + bid + "';").get(0).get(0);
                String tid = esql.executeQueryAndReturnResult("select tid from plays where sid in (select sid from bookings where sid = '" + sid + "');").get(0).get(0);

                //how many seats are on the reservation
                int numseats = Integer.parseInt(esql.executeQueryAndReturnResult("select seats from bookings where bid = '" + bid + "';").get(0).get(0));
                String maxseat = esql.executeQueryAndReturnResult("select max(sno) from cinemaseats where tid = '" + tid + "';").get(0).get(0);
                List<List<String>> seats = esql.executeQueryAndReturnResult("select tid, sno, stype, csid from cinemaseats where csid in (select csid from showseats where bid = " + bid + ");");

                //run through all booked seats and change
                for(int i = 0; i < numseats; i++) {
                        String seat = seats.get(i).get(0) + ", " + seats.get(i).get(1) + ": " + seats.get(i).get(2);
                        String csid = seats.get(i).get(3);
                        System.out.println("Replace seat [" + seat + "]. These seats are currently free: ");
                        esql.executeQueryAndPrintResult("select sno, stype from cinemaseats where tid = '" + tid + "' and csid not in (select csid from showseats);");


                        System.out.println("Which seat would you like to reserve?");
                        String replace = in.readLine();

                        //checking that seat selection is in range
                        if(Integer.parseInt(replace) > Integer.parseInt(maxseat)) {
                                System.out.println("There is no seat with that number in the theater.");
                                i--;
                                break;
                        }

 			String newcsid = esql.executeQueryAndReturnResult("select csid from cinemaseats where tid = '" + tid + "' and sno = '" + replace + "';").get(0).get(0);
                        String oldtype = esql.executeQueryAndReturnResult("select stype from cinemaseats where csid = '" + csid + "';").get(0).get(0);
                        String newtype = esql.executeQueryAndReturnResult("select stype from cinemaseats where csid = '" + newcsid + "';").get(0).get(0);

                        //checking that seat is exchangable
                        if(!oldtype.equals(newtype)) {
                                System.out.println("You can only exchange seats that are the same price as the original.");
                                i--;
                                break;
                        }

                        String q = "update showseats set csid = '" + newcsid + "' where csid = '" + csid + "';";
                        esql.executeUpdate(q);
                }

                System.out.println("Seat reservations sucessfully updated!");

	}



	public static void RemovePayment(Ticketmaster esql) throws IOException, SQLException {// 6
                System.out.println("What is the email that the booking was made with? ");
                String email = in.readLine();

                List<List<String>> confirmEmail = esql.executeQueryAndReturnResult("select email from bookings where email = '" + email + "';");

                if(confirmEmail != null && confirmEmail.isEmpty()) {
                        System.out.println("We coudn't find any bookings with that email! Try again.");
                        return;
                }

                System.out.println("Here are the bookings on your account: ");
                esql.executeQueryAndPrintResult("select * from bookings where email = '" + email + "';");


                System.out.println("Which bid would you like to cancel? ");
                String bid = in.readLine();

                List<List<String>> bookingid = esql.executeQueryAndReturnResult("select bid from bookings where bid = '" + bid + "';");
                if(bookingid != null && bookingid.isEmpty()) {
                        System.out.println("Invalid booking id! Try again.");
                        return;
                }

                String status = esql.executeQueryAndReturnResult("select status from bookings where bid = '" + bid + "';").get(0).get(0);

                if(status.equals("Paid")) {
                        //remove payment from db
                        String q = "delete from payments where bid = '" + bid + "';";
                        esql.executeUpdate(q);
                }

                if(!status.equals("Cancelled")) {
                        //change status to cancelled
                        String q = "update bookings set status = 'Cancelled' where bid = '" + bid + "';";
                        esql.executeUpdate(q);
                }


                System.out.println("Your payment has been sucessfully been deleted.");
        }
	
	public static void ClearCancelledBookings(Ticketmaster esql) {// 7
		String query;
		query = "DELETE FROM Bookings WHERE status = 'cancelled';";
		try {
			esql.executeUpdate(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			
		}

		System.out.println("\n Cancelled Bookings Removed \n");
	}

	public static void RemoveShowsOnDate(Ticketmaster esql) throws IOException, SQLException {// 8

		//delete from shows where sid in (select sid from plays where tid in (select tid from theaters where cid in
		// (select cid from cinemas where cname =  'AMC')));

		// update bookings set status = 'cancelled' where sid in (select sid from shows where sdate = '2/22/2019');


		//select * from bookings where sid in (select sid from shows where sdate = '2/22/2019') and 
		//sid in ((select sid from plays where tid in (select tid from theaters where cid in (select cid from cinemas where cname =  'AMC')))


		/* delete from ShowSeats  where sid in (select sid from shows where sdate = '1/01/2019') and 
 			sid in ((select sid from plays where tid in (select tid from theaters where cid in 
 			(select cid from cinemas where cname =  'AMC'))));
 
 
		 delete from plays  where sid in (select sid from shows where sdate = '1/01/2019') and
		 sid in ((select sid from plays where tid in (select tid from theaters where cid in (select cid from cinemas where cname =  'AMC')))
		);

		delete from shows  where sid in (select sid from shows where sdate = '1/01/2019') and sid in 
		((select sid from plays where tid in (select tid from theaters where cid in (select cid from cinemas where cname =  'AMC')))
		); */

		String date, cinema, q1, q2, q3;

		System.out.print("Enter Date(MM/DD/YYYY): ");
		date = in.readLine();

		System.out.print("Enter Cinema: ");
		cinema = in.readLine();

		q1 = "delete from ShowSeats where sid in (select sid from shows where sdate = '" + date + "') and " + 
		"sid in ((select sid from plays where tid in (select tid from theaters where cid in	" + 
		"(select cid from cinemas where cname =  '" + cinema + "'))));";

		q2 = "delete from plays where sid in (select sid from shows where sdate = '" + date + "') and " +
		"sid in ((select sid from plays where tid in (select tid from theaters where cid in " + 
		"(select cid from cinemas where cname =  '" + cinema + "'))));";

		q3 = "delete from shows  where sid in (select sid from shows where sdate = '" + date + "') and sid in " +
		"((select sid from plays where tid in (select tid from theaters where cid in " + 
		"(select cid from cinemas where cname =  '" + cinema + "'))));";

	
		esql.executeUpdate(q1);
		esql.executeUpdate(q2);
		esql.executeUpdate(q3);	

		String getBid = "select bid from bookings where sid in (select sid from shows where sdate = '" + date + "') and sid in " +
		"((select sid from plays where tid in (select tid from theaters where cid in " +
		"(select cid from cinemas where cname =  '" + cinema + "'))));";

		List<List<String>> bidList = esql.executeQueryAndReturnResult(getBid);
		for(int i = 0; i < bidList.size(); i++){
			String status = esql.executeQueryAndReturnResult("select status from bookings where bid = '" + 
							bidList.get(0).get(i) + "';").get(0).get(0);

			if(status.equals("Paid")) {
			//remove payment from db
			    String q = "delete from payments where bid = '" + bidList.get(0).get(i) + "';";
			    esql.executeUpdate(q);
			}
			
			if(!status.equals("Cancelled")) {
			//change status to cancelled
			    String q = "update bookings set status = 'Cancelled' where bid = '" + bidList.get(0).get(i) + "';";
			    esql.executeUpdate(q);
			}			
		}
		System.out.println("Shows on given date deleted ....\n");

	}

	public static void ListTheatersPlayingShow(Ticketmaster esql) throws IOException, SQLException  {// 9
                System.out.println("Which cinema would you like to see the listings for?");
                String cinema = in.readLine();

                System.out.println("Which movie would you like to look for?" );
                String title = in.readLine();

                esql.executeQueryAndPrintResult("select * from shows where mvid in (select mvid from movies where title = '" + title + "') and sid in (select sid from theaters where tname = '" + cinema + " Theaters 1');");

                System.out.println("Enter sid to track: ");
                String sid = in.readLine();

                esql.executeQueryAndPrintResult("select * from theaters where tid in (select tid from plays where sid = '" + sid + "');");
        }

	public static void ListShowsStartingOnTimeAndDate(Ticketmaster esql) throws IOException, SQLException {// 10
		// still need to figure out how to conver string to date in sql


		String startTime;
		String date;

		System.out.println("Date(MM/DD/YYYY): ");
		date = in.readLine();

		System.out.println("Start time (Hour:Minute): ");
		startTime = in.readLine();
		

		String q = "SELECT title FROM movies WHERE mvid IN  (SELECT mvid FROM shows WHERE sdate = '" + date + "' and sttime = '" 
		+ startTime + "')";

		System.out.println("");
		int result = esql.executeQueryAndPrintResult(q);
		System.out.println("\n");
	
	}

	public static void ListMovieTitlesContainingLoveReleasedAfter2010(Ticketmaster esql) throws SQLException {// 11
		//select extract(year from rdate) from movies where mvid = 1;

		int result = esql.executeQueryAndPrintResult("select title from movies where title like '%Love%' and extract(year from rdate) > 2010  ;");
		

		
	}

	public static void ListUsersWithPendingBooking(Ticketmaster esql) throws SQLException {//12
                esql.executeQueryAndPrintResult("select fname, lname, email from users where email in (select email from bookings where status = 'Pending');");
        }

	public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql) throws IOException, SQLException {// 13
		/*select m1.title, m1.duration, s1.sdate, s1.sttime from movies m1, shows s1 where m1.title = 
		'Avatar' and sdate in (select s2.sdate from shows s2 where s2.sdate between '01/01/2019' and 
		'12/31/2019' and s2.mvid = (select m2.mvid from movies m2 where m2.title = 'Avatar')) and 
		sid in (select p1.sid from plays p1 where p1.tid in (select t1.tid from theaters t1 where 
		t1.cid in (select c1.cid from cinemas c1 where c1.cname = 'AMC')));*/

		String movieTitle, theaterName, date1, date2;

		System.out.println("\n\nMovie Title : ");
		movieTitle = in.readLine();

		System.out.println("Theater Name : ");
		theaterName = in.readLine();

		System.out.println("Begin Date(MM/DD/YYYY) : ");
		date1 = in.readLine();

		System.out.println("End Date(MM/DD/YYYY): ");
		date2 = in.readLine();

		String q = "select m1.title, m1.duration, s1.sdate, s1.sttime from movies m1, shows s1 where m1.title = '"
		+ movieTitle + "' and sdate in (select s2.sdate from shows s2 where s2.sdate between '" + date1 + "' and'" 
		+ date2 + "' and s2.mvid = (select m2.mvid from movies m2 where m2.title = '" + movieTitle + "')) and" 
		+ " sid in (select p1.sid from plays p1 where p1.tid in (select t1.tid from theaters t1 where " + 
		"t1.cid in (select c1.cid from cinemas c1 where c1.cname = '" + theaterName + "')));";

		System.out.println("");
		int result = esql.executeQueryAndPrintResult(q);
		System.out.println("\n");


	}

	public static void ListBookingInfoForUser(Ticketmaster esql) throws IOException, SQLException {//14
                System.out.println("Enter user email: ");
                String email = in.readLine();

                List<List<String>> bookings = esql.executeQueryAndReturnResult("select bid, bdatetime from bookings where email = '" + email + "';");
                //System.out.println("get bookings info");
                List<List<String>> titles = esql.executeQueryAndReturnResult("select title from movies where mvid in (select mvid from shows where sid in (select sid from bookings where email = '" + email + "'));");
                //System.out.println("get titles info");
                List<List<String>> theatername = esql.executeQueryAndReturnResult("select tname from theaters where tid in (select tid from plays where sid in (select sid from bookings where email = '" + email + "'));");
                //System.out.println("get theater info");
                List<List<String>> csids = esql.executeQueryAndReturnResult("select sno from cinemaseats where csid in (select csid from showseats where bid > 0 and bid in (select bid from bookings where email = '" + email + "'));");
                //System.out.println("get csids");

                int numbookings = bookings.size();

                System.out.println("Title\tDate and Time\t\t\tTheater Name\t\t\tCinema Seat Number");
                for(int i = 0; i < numbookings; i++) {
                        String bid = bookings.get(i).get(0);
                        String title = titles.get(i).get(0);
                        String datetime = bookings.get(i).get(1);
                        String theater = theatername.get(i).get(0);
                        String csid = csids.get(i).get(0);

                        System.out.println(title + "\t" + datetime + "\t\t" + theater + "\t\t" + csid);
                }
	}
	
}
