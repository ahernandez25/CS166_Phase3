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

	public static boolean validInput(Ticketmaster esql, String table, String column, String query) throws IOException, SQLException {
		if(query.isEmpty()) {
			return false;
		}
		
		//return True;
		//search table to see if entry exists
		List<List<String>> output = esql.executeQueryAndReturnResult("select " + column + " from " + table + "T where  T." + column + " = " + query);
		if(output.isEmpty()) {
			return false;
		}
		return true;
	}

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
                esql.executeQuery("insert into user(" + email + " unique, first_name " + fname + ", phone " + phone  +", last_name " + lname + ", password " + password + ");");
	}

	public static void AddBooking(Ticketmaster esql) throws IOException, SQLException {// 2
		String email = "", cinema = "", theater = "", movie, date, time;
		while(!validInput(esql, "user", "email", email)) {
			System.out.println("Enter email to book under: ");
			email = in.readLine();
		}
		
		while(!validInput(esql, "cinema", "name", cinema)) {
			System.out.println("Enter cinema: ");
			cinema = in.readLine();
		}

		while(!validInput(esql, "cinema theater", "theater name", theater)) {
			System.out.println("Enter theater: ");
			theater = in.readLine();
		}

		System.out.println("Enter movie: ");
		movie = in.readLine();
		System.out.println("Enter date of show: ");
		date = in.readLine();
		System.out.println("Enter time of show: ");
		time = in.readLine();
		System.out.println("How many seats do you want to book? ");
		String numSeats = in.readLine();
		int seats = Integer.parseInt(numSeats);
		for(int i = 0; i < seats; i++) {
			System.out.println("Enter seat number to book: ");
			String seat = in.readLine();
			//query to book seat
			esql.executeQuery("");
		}
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
		String bookingid = "";
		
		while(!validInput(esql, "booking", "bookingID", bookingid)) {
			System.out.println("Enter booking ID: ");
			bookingid = in.readLine();
		}

		List<List<String>> seats = esql.executeQueryAndReturnResult("Select numSeats from booking there bookingid = " + bookingid);
		
		int numSeats = Integer.parseInt(seats.get(0).get(4)); 
		//? I'll need to chack if that's the right place to look
		
		for(int i = 0; i < numSeats; i++) {
			System.out.println("Which seat would like to change to? ");
			String seat = in.readLine();
			//umm.. I'll need to double check the database & fix this
			//esql.executeQuery("Update seatBooked
		}

		System.out.println("Your booked seats have been updated");
	}

	public static void RemovePayment(Ticketmaster esql) throws IOException, SQLException {// 6
		String bookingid = "";
		
		while(!validInput(esql, "booking", "bookingid", bookingid)) {
			System.out.println("Enter booking ID: ");
			bookingid = in.readLine();
		}
		
		//update payment so that payment is not connected to booking
		//esql.executeQuery("update 

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

	public static void RemoveShowsOnDate(Ticketmaster esql) {// 8

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

	}

	public static void ListTheatersPlayingShow(Ticketmaster esql) throws IOException, SQLException  {// 9
		String cinema, show;

		System.out.println("Enter cinema ID: ");
		cinema = in.readLine();
		
		System.out.println("Enter show ID: ");
		show = in.readLine();
		
		//see how palyed in is being handled
		//List<List<String>> theaters = esql.executeQueryAndReturnResult("select cinema_theater_id from cinema_theater where 
		
		System.out.println("These theaters are playing show with ID " + show);
		//print out theater ID's and theeater names
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

	public static void ListUsersWithPendingBooking(Ticketmaster esql){//12
		//
		
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

	public static void ListBookingInfoForUser(Ticketmaster esql){//14
		//
		
	}
	
}
