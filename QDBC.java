package mysql;

import javax.xml.transform.Result;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;

public class QDBC {
    public void handle_query_options(Connection connection, Scanner scanner){
        String choice;
        while(true){
            System.out.println("What would you like to see queries on? Enter the appropriate number.\n[1]: LISTINGS\n[2]: BOOKINGS\n[3]: HOSTS & RENTERS\n[4]: REVIEWS\n[q]: quit");
            choice = scanner.nextLine();

            if(choice.equals("1")){
                handle_listing_queries(connection, scanner);
            }
            else if(choice.equals("2")){
                handle_total_booking_queries(connection, scanner);
            }
            else if(choice.equals("3")){
                handle_host_and_renter_queries(connection, scanner);
            }
            else if(choice.equals("4")){
                handle_review_queries(connection);
            }
            else if(choice.equals("q")){
                System.out.println("\n\n[EXITING QUERIES ...]\n\n");
                break;
            }
            else {
                System.out.println("[INVALID OPTION] Try Again.\n");
            }
        }
    }
    public void print_no_results(){
        System.out.println("\n| -------------------------------------------------------------- |");
        System.out.println("\n  ************************* NO RESULTS *************************  ");
        System.out.println("\n| -------------------------------------------------------------- |\n");
    }
    public DateWithQuery parse_date_range(Scanner scanner, String query, String date_range){
        String[] dateRangeArray = null;
        List<Date> dates = new ArrayList<>();

        if(!date_range.isEmpty()){
            dateRangeArray = date_range.split("-");
            if(dateRangeArray.length == 1){
                query += " AND date_avail=?";
            } else {
                DateFormat date_format = new SimpleDateFormat("MM/dd/yyyy");
                try {
                    Date start_date = date_format.parse(dateRangeArray[0].trim());
                    Date end_date = date_format.parse(dateRangeArray[1].trim());

                    long interval = 24 * 1000 * 60 * 60;
                    long endTime = end_date.getTime();
                    long currentTime = start_date.getTime();
                    while(currentTime <= endTime){
                        dates.add(new Date(currentTime));
                        currentTime += interval;
                    }
                } catch (ParseException e){
                    System.err.println("\n[INVALID DATE FORMAT]: " + e.getMessage() + "\n\n");
                }
                if(!dates.isEmpty()){
                    StringBuilder date_placeholders = new StringBuilder();
                    for(int i = 0; i < dates.size(); i++){
                        if(i > 0){
                            date_placeholders.append(", ");
                        }
                        date_placeholders.append("?");
                    }
                    query += " AND date_avail IN (" + date_placeholders +")";
                }
            }
        }
        DateWithQuery dq = new DateWithQuery(dates, query);

        return dq;
    }

    /**
     * FUNCTIONS FOR LISTING QUERIES
     */
    public void handle_listing_queries(Connection connection, Scanner scanner){
        String choice;
        System.out.println("\n| -------------------------- LISTING QUERIES  -------------------------- |");
        System.out.println("Would you like to search based on:\n[1]: CLOSENESS (distance-wise)\n[2]: FILTERS (postal code, address, date range, price range, amenities provided)\n[3]: TOTAL LISTINGS\n[4]: EXACT SEARCH\n[q]: quit");
        choice = scanner.nextLine();
        if(choice.equals("1")){
            handle_closeness(connection, scanner);
        }
        else if(choice.equals("2")){
            handle_filters(connection, scanner);
        }
        else if(choice.equals("3")){
            filter_by_total_listings(connection, scanner);
        }
        else if(choice.equals("4")){
            exact_search(connection, scanner);
        }
        else if(choice.equals("q")){
            System.out.println("\n\n[QUITING LISTING QUERIES ...]\n\n");
        }
        else {
            System.out.println("[INVALID OPTION] Try Again.");
        }
    }

    public void handle_closeness(Connection connection, Scanner scanner){
        float latitude;
        float longitude;
        float distance;
        String query;
        String choice;
        try{
            System.out.println("[ENTER THE EXACT COORDINATES]\nLATITUDE:");
            latitude = Float.parseFloat(scanner.nextLine());
            System.out.println("LONGITUDE:");
            longitude = Float.parseFloat(scanner.nextLine());
            System.out.println("ENTER A DISTANCE (in km) FOR HOW FAR THE LISTINGS SHOULD BE FROM COORDINATES:\n[NOTE]: Default is 10km.\n[NOTE]: Press Enter to use default.");
            String test = scanner.nextLine();
            if(test.isEmpty()){
                distance = 10;
            } else {
                distance = Float.parseFloat(test);
            }
        } catch(NumberFormatException e){
            System.err.println("[INVALID INPUT]: " + e.getMessage());
            return;
        }
        query = "SELECT type, latitude, longitude, street, postal_code, city, country,";

        System.out.println("RANK RESULTS BY: \n[1]: PRICE\n[2]: DISTANCE");
        choice = scanner.nextLine();
        if(choice.equals("1")){
            query += " price, date_avail," +
                    "2 * 6371 * ASIN(SQRT(" +
                    "POWER(SIN((? - ABS(latitude)) * PI() / 180 / 2), 2) + " +
                    "COS(?) * COS(ABS(latitude) * PI() / 180) * " +
                    "POWER(SIN((? - longitude) * PI() / 360), 2))) AS distance " +
                    "FROM Listings NATURAL JOIN Records WHERE role='host' AND is_booked=false or (is_booked=true AND is_cancelled=true) HAVING distance <= ? ORDER BY price;";
        } else if (choice.equals("2")){
            query += " 2 * 6371 * ASIN(SQRT(" +
                    "POWER(SIN((? - ABS(latitude)) * PI() / 180 / 2), 2) + " +
                    "COS(?) * COS(ABS(latitude) * PI() / 180) * " +
                    "POWER(SIN((? - longitude) * PI() / 360), 2))) AS distance " +
                    "FROM Listings HAVING distance <= ? ORDER BY distance;";
        } else {
            System.out.println("[INVALID OPTION] Try Again.");
            return;
        }


        try(PreparedStatement psmt = connection.prepareStatement(query)){
            psmt.setFloat(1, latitude);
            psmt.setFloat(2, (float)(latitude * Math.PI / 180));
            psmt.setFloat(3, longitude);
            psmt.setFloat(4, distance);

            ResultSet rs = psmt.executeQuery();
            boolean nearby_found = rs.next();
            if(!nearby_found){
                print_no_results();
            } else {
                System.out.println("\n| ------------------------------ RESULTS ------------------------------ |");
                while(nearby_found){
                    String type = rs.getString("type");
                    float rs_latitude = rs.getFloat("latitude");
                    float rs_longitude = rs.getFloat("longitude");
                    float rs_distance = rs.getFloat("distance");
                    String rs_street = rs.getString("street");
                    String rs_city = rs.getString("city");
                    String rs_country = rs.getString("country");
                    String rs_postal_code = rs.getString("postal_code");

                    System.out.println("ADDRESS: " + rs_street + ", " + rs_city + ", " + rs_country + " " + rs_postal_code);
                    System.out.println("TYPE: "+ type);
                    System.out.println("EXACT COORDINATES: (" + rs_latitude + ", " + rs_longitude + ")");
                    System.out.println("DISTANCE: " + rs_distance + "km");

                    if(choice.equals("1")){
                        float price = rs.getFloat("price");
                        String date_avail = rs.getString("date_avail");
                        System.out.println("DATE AVAILABLE: " + date_avail);
                        System.out.println("PRICE: " + price);
                    }

                    System.out.println("* -------------------------------------------------------------- *");
                    nearby_found = rs.next();
                }
            }

        } catch (SQLException e ){
            System.out.println("[ERORR IN SEARCHING NEARBY LISTINGS]: "+ e.getMessage());
        }

    }
    public void handle_filters(Connection connection, Scanner scanner){
        String city;
        String country;
        String postal_code;
        String adj_postal_code_start = "";
        String date_range;
        String price_range;
        String amenities;

        String query;

        try{
            query = "SELECT * FROM Listings NATURAL JOIN Records WHERE role='host'";

            System.out.println("*********** [NOTE]: If you would like skip a filter, ENTER to skip when prompted to enter said filter. ***********");

            System.out.println("Enter the city:");
            city = scanner.nextLine();
            if(!city.isEmpty()){
                query += " AND city=?";
            }

            System.out.println("Enter the country:");
            country = scanner.nextLine();
            if(!country.isEmpty()){
                query += " AND country=?";
            }

            System.out.println("Enter the postal code:");
            postal_code = scanner.nextLine();
            if(!postal_code.isEmpty()){
                adj_postal_code_start = postal_code.substring(0, 3);
                query += " AND (postal_code=? OR postal_code LIKE ?)";
            }

            System.out.println("Enter a date range of availability:\n[NOTE] Accepted Date Range Format: MM/DD/YYYY-MM/DD/YYYY\n[NOTE] if just one date, enter MM/DD/YYYY");
            date_range = scanner.nextLine();
            DateWithQuery dq = parse_date_range(scanner, query, date_range);
            List<Date> dates = dq.getDates();
            query = dq.getQuery();

            System.out.println("Enter a price range:\n[NOTE] Accepted Price Range Format: MIN(X.XX)-MAX(XXXX.XX)\n[NOTE] if just one price, enter XXXXX.XX");
            price_range = scanner.nextLine();
            String[] price_values = null;
            float min_price = 0;
            float max_price = 0;
            if(!price_range.isEmpty()){
                price_values = price_range.split("-");
                if(price_values.length == 1){
                    query += " AND price=?";
                } else {
                    min_price = Float.parseFloat(price_values[0].trim());
                    max_price = Float.parseFloat(price_values[1].trim());
                    query += " AND price BETWEEN ? AND ?";
                }
            }

            System.out.println("Enter amenities:\n[NOTE] List amenities as such: [amenity #1], [amenity #2], [amenity #3], etc.");
            amenities = scanner.nextLine();
            String[] amenities_values = null;
            if(!amenities.isEmpty()){
                amenities_values = amenities.split(",");
                if(amenities_values.length == 1){
                    query += " AND amenities COLLATE utf8mb4_general_ci LIKE ?";
                    System.out.println(amenities);
                } else {
                    query += " AND (";
                    for(int i = 0; i < amenities_values.length; i++){
                        //cover case where amenity might be with comma
                        if(i > 0){
                            query += " OR amenities COLLATE utf8mb4_general_ci LIKE ?";
                        } else {
                            query += "amenities COLLATE utf8mb4_general_ci LIKE ?";
                        }
                    }
                    query += ")";
                }
            }

            //end query
            query += ";";

            //System.out.println(query);
            //System.out.println(country);

            PreparedStatement pstmt = connection.prepareStatement(query);
            int param_index = 1;
            if(!city.isEmpty()){
                pstmt.setString(param_index++, city.trim());
            }
            if(!country.isEmpty()){
                pstmt.setString(param_index++, country.trim());
            }
            if(!postal_code.isEmpty()){
                pstmt.setString(param_index++, postal_code.trim());
                pstmt.setString(param_index++, adj_postal_code_start.trim() + "%");
            }
            if(!date_range.isEmpty()){
                if(date_range.indexOf('-') == -1){
                    pstmt.setString(param_index++, date_range.trim());
                } else {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                    for(Date date : dates){
                        String date_string = dateFormat.format(date);
                        pstmt.setString(param_index++, date_string.trim());
                    }
                }
            }
            if(!price_range.isEmpty()){
                if(price_values.length == 1){
                    pstmt.setFloat(param_index++, Float.parseFloat(price_range));
                } else {
                    pstmt.setFloat(param_index++, min_price);
                    pstmt.setFloat(param_index++, max_price);
                }
            }
            if(!amenities.isEmpty()){
                if(amenities_values.length == 1){
                    pstmt.setString(param_index++, "%" + amenities_values[0].trim() + "%");
                } else {
                    for(int i = 0; i < amenities_values.length; i++){
                        pstmt.setString(param_index++, "%" + amenities_values[i].trim() + "%");
                    }
                }
            }

            if(param_index == 1){
                System.out.println("[NO FILTERS APPLIED] Try Again.");
                return;
            }

            System.out.println("[RUNNING QUERY ...]\n");

            ResultSet rs = pstmt.executeQuery();
            boolean results_filter = rs.next();
            if(!results_filter){
                print_no_results();
                return;
            }
            System.out.println("\n|  -------------------------- RESULTS ------------------------  |");
            while(results_filter){
                String rs_street = rs.getString("street");
                String rs_city = rs.getString("city");
                String rs_country = rs.getString("country");
                String rs_postal_code = rs.getString("postal_code");
                String rs_date = rs.getString("date_avail");
                String rs_amenities = rs.getString("amenities");
                float rs_price = rs.getFloat("price");

                System.out.println("ADDRESS: " + rs_street + ", " + rs_city + ", " + rs_country + " " + rs_postal_code);
                System.out.println("DATE: " + rs_date);
                System.out.println("AMENITIES: " + rs_amenities);
                System.out.println("PRICE: " + rs_price);
                System.out.println("* -------------------------------------------------------------- *");

                results_filter = rs.next();
            }
            
        } catch(SQLException e){
            System.out.println("\n[ERROR IN APPLYING FILTERS] " + e.getMessage() + "\n");
        }
        System.out.println("\n\n");
    }
    public void filter_by_total_listings(Connection connection, Scanner scanner){
        String country;
        String city;
        String postal_code;

        String choice;
        String query;

        query = "SELECT ";

        System.out.println("GROUP TOTAL LISTINGS (enter the number): \n[1]: per country\n[2]: per country & city\n[3]: per country & city & postal code");
        choice = scanner.nextLine();
        if(choice.equals("1")){
            query += "country, COUNT(*) AS total_listings";
        } else if (choice.equals("2")){
            query += "country, city, COUNT(*) AS total_listings";
        } else if (choice.equals("3")){
            query += "country, city, postal_code, COUNT(*) AS total_listings";
        } else {
            System.out.println("[INVALID CHOICE] Try Again.");
            return;
        }

        try {
            query += " FROM Listings GROUP BY";

            if(choice.equals("1")){
                query += " country";
            } else if (choice.equals("2")){
                query += " country, city";
            } else if (choice.equals("3")){
                query += " country, city, postal_code";
            }
            query += ";";


            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            boolean totals_found = rs.next();
            int total_listings;

            if(!totals_found){
                print_no_results();
            } else {
                System.out.println("| ------------------------------------- RESULTS ------------------------------------- |");
                while(totals_found){
                    total_listings = rs.getInt("total_listings");
                    country = rs.getString("country");

                    if(choice.equals("1")){
                        System.out.println("  | COUNTRY: " + country );
                        System.out.println("  | TOTAL LISTINGS IN " + country.toUpperCase() + ": " + total_listings );
                    } else if (choice.equals("2")){
                        city = rs.getString("city");
                        System.out.println("  | CITY, COUNTRY: " + city.toUpperCase() + ", " + country.toUpperCase() );
                        System.out.println("  | TOTAL LISTINGS IN (" + city.toUpperCase() + ", " + country.toUpperCase() + "): " + total_listings);
                    } else if (choice.equals("3")){
                        city = rs.getString("city");
                        postal_code = rs.getString("postal_code");
                        System.out.println("  | CITY, COUNTRY POSTAL CODE: " + city.toUpperCase() + ", " + country.toUpperCase() + " " + postal_code );
                        System.out.println("  | TOTAL LISTINGS IN (" + city.toUpperCase() + ", " + country.toUpperCase() + " " + postal_code + "): " + total_listings);
                    }
                    System.out.println("* ------------------------------------------------------------------------------------ *");
                    totals_found = rs.next();
                }
            }
        } catch (SQLException e){
            System.out.println("[ERROR IN COUNTING TOTAL LISTINGS]: " + e.getMessage());
        }
    }
    public void exact_search(Connection connection, Scanner scanner){
        String exact_address;
        String street;
        String city;
        String country;
        String postal_code;

        System.out.println("Enter an exact address:\n[NOTE] Accepted Address Form: [Street Address], [City], [Country] [POSTAL CODE]");
        exact_address = scanner.nextLine();

        // Split the input string by comma and space
        String[] addressComponents = exact_address.split(", ");
        String country_and_post = null;

        if (addressComponents.length == 3) {
            // Extract the components
            street = addressComponents[0];
            city = addressComponents[1];

            int check_index;
            check_index = addressComponents[2].indexOf(' ');
            if(check_index == -1){
                System.out.println("\n\n[INVALID ADDRESS FORMAT] Please enter the address in the correct format.\n");
            } else {
                country = addressComponents[2].substring(0, check_index);
                postal_code = addressComponents[2].substring(check_index + 1);
                String query = "SELECT * FROM Listings WHERE street=? AND city=? AND country=? AND postal_code=?";
                try{
                    PreparedStatement ps = connection.prepareStatement(query);
                    ps.setString(1, street);
                    ps.setString(2, city);
                    ps.setString(3, country);
                    ps.setString(4, postal_code);

                    ResultSet rs = ps.executeQuery();
                    boolean results_exact = rs.next();
                    if(!results_exact){
                        print_no_results();
                    } else {
                        System.out.println("\n|  -------------------------- RESULTS ------------------------  |");
                        while(results_exact){
                            String rs_street = rs.getString("street");
                            String rs_city = rs.getString("city");
                            String rs_country = rs.getString("country");
                            String rs_postal_code = rs.getString("postal_code");
                            String rs_amenities = rs.getString("amenities");

                            System.out.println("ADDRESS: " + rs_street + ", " + rs_city + ", " + rs_country + " " + rs_postal_code);
                            System.out.println("AMENITIES: " + rs_amenities);
                            System.out.println("* -------------------------------------------------------------- *");
                            results_exact = rs.next();
                        }
                    }
                } catch (SQLException e){
                    System.out.println("[ERROR IN FINDING LISTING]: " + e.getMessage());
                }
            }
        } else {
            System.out.println("\n\n[INVALID ADDRESS FORMAT] Please enter the address in the correct format.\n");
        }
    }

    /**
     * FUNCTIONS FOR BOOKING QUERIES
     */
    public void handle_total_booking_queries(Connection connection, Scanner scanner){
        String choice;
        String date_range;
        String query;

        System.out.println("\n| -------------------------- BOOKING QUERIES  -------------------------- |");
        System.out.println("Run reports on the TOTAL BOOKINGS in a specified date range:\n[1]: BY CITY\n[2]: BY POSTAL CODE (within a city)\n[q]: quit");
        choice = scanner.nextLine();

        query = "SELECT ";

        if(choice.equals("1")){
            query += "L.city, COUNT(*) AS total_bookings";
        }
        else if(choice.equals("2")){
            query += "L.city, L.postal_code, COUNT(*) AS total_bookings";
        }
        else if(choice.equals("q")){
            System.out.println("\n\n[QUITING BOOKING QUERIES ...]\n\n");
            return;
        }
        else {
            System.out.println("[INVALID OPTION] Try Again.");
            return;
        }

        query += " FROM Records R NATURAL JOIN Listings L WHERE R.role='renter' AND is_booked=true";

        System.out.println("Enter a date range of availability:\n[NOTE] Accepted Date Range Format: MM/DD/YYYY-MM/DD/YYYY\n[NOTE] if just one date, enter MM/DD/YYYY");
        date_range = scanner.nextLine();
        DateWithQuery dq = parse_date_range(scanner, query, date_range);
        List<Date> dates = dq.getDates();
        query = dq.getQuery();

        if(choice.equals("1")){
            query += " GROUP BY L.city;";
        } else if(choice.equals("2")){
            query += " GROUP BY L.city, L.postal_code;";
        }

        //System.out.println(query);

        try{
            PreparedStatement pstmt = connection.prepareStatement(query);
            int param_index = 1;

            if(date_range.indexOf('-') == -1){
                pstmt.setString(param_index++, date_range.trim());
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                for(Date date : dates){
                    String date_string = dateFormat.format(date);
                    pstmt.setString(param_index++, date_string.trim());
                }
            }
            ResultSet rs = pstmt.executeQuery();

            boolean totals_found = rs.next();
            int total_bookings;
            String city;
            String postal_code;

            if(!totals_found){
                print_no_results();
            } else {
                System.out.println("| ------------------------------------- RESULTS ------------------------------------- |");
                while(totals_found){
                    total_bookings = rs.getInt("total_bookings");
                    city = rs.getString("city");
                    if(choice.equals("1")){
                        System.out.println("  | CITY: " + city);
                        System.out.println("  | TOTAL BOOKINGS IN " + city.toUpperCase() + ": " + total_bookings );
                    } else if(choice.equals("2")){
                        postal_code = rs.getString("postal_code");
                        System.out.println("  | CITY, POSTAL CODE: " + city.toUpperCase() + ", " + postal_code.toUpperCase() );
                        System.out.println("  | TOTAL BOOKINGS IN (" + city.toUpperCase() + ", " + postal_code.toUpperCase() + "): " + total_bookings);
                    }
                    System.out.println("* ------------------------------------------------------------------------------------ *");
                    totals_found = rs.next();
                }
            }
        } catch(SQLException e){
            System.out.println("\n[ERROR IN RUNNING TOTAL BOOKINGS BY CITY]: " + e.getMessage() + "\n");
        }
        System.out.println("\n");
    }

    /**
     * FUNCTIONS FOR RENTER & HOST QUERIES
     */
    public void handle_host_and_renter_queries(Connection connection, Scanner scanner){
        String choice;

        System.out.println("\n| -------------------------- RENTER & HOST QUERIES -------------------------- |");
        System.out.println("OPTIONS:\n[1]: RANK HOSTS BY TOTAL NUMBER OF LISTINGS\n[2]: FLAG HOSTS BASED ON % OF LISTINGS\n[3]: RANK RENTERS BY TOTAL NUMBER OF BOOKINGS\n[4]: LARGEST NUMBER OF CANCELLATIONS\n[q]: quit");
        choice = scanner.nextLine();
        if(choice.equals("1")){
            handle_rank_hosts(connection, scanner);
        } else if(choice.equals("2")){
            handle_flag_hosts(connection);
        } else if(choice.equals("3")){
            handle_rank_renters(connection, scanner);
        } else if(choice.equals("4")) {
            handle_cancellations(connection);
        } else if(choice.equals("q")){
            System.out.println("\n\n[QUITING RENTER & HOST QUERIES ...]\n\n");
        } else {
            System.out.println("[INVALID OPTION] Try Again.");
        }
    }
    public void handle_rank_hosts(Connection connection, Scanner scanner){
        String choice;
        String query;

        System.out.println("\n| -------------------------- RANK HOSTS  -------------------------- |");
        System.out.println("Rank MyBnB hosts by total number of listings:\n[1]: PER COUNTRY\n[2]: BY CITY (within a country)\n[q]: quit");
        choice = scanner.nextLine();

        query = "SELECT DISTINCT ";

        if(choice.equals("1")){
            query += "country, SIN, COUNT(DISTINCT LID) AS total_listings";
        }
        else if(choice.equals("2")){
            query += "country, city, SIN, COUNT(DISTINCT LID) AS total_listings";
        }
        else if(choice.equals("q")){
            System.out.println("\n\n[QUITING RANK HOSTS QUERIES ...]\n\n");
            return;
        }
        else {
            System.out.println("[INVALID OPTION] Try Again.");
            return;
        }

        query += " FROM Listings NATURAL JOIN Records WHERE role='host' AND is_unavail=false GROUP BY";

        try {

            if (choice.equals("1")) {
                query += " country, SIN ORDER BY total_listings DESC;";
            } else if (choice.equals("2")) {
                query += " country, city, SIN ORDER BY total_listings DESC;";
            }

            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            boolean totals_found = rs.next();

            if(!totals_found){
                print_no_results();
            } else {
                while(totals_found){
                    System.out.println("| ------------------------------------- RANKINGS ------------------------------------- |");
                    while(totals_found) {
                        int total_listings = rs.getInt("total_listings");
                        String country = rs.getString("country");
                        int SIN = rs.getInt("SIN");

                        System.out.println("  | USER SIN: " + SIN);

                        if (choice.equals("1")) {
                            System.out.println("  | COUNTRY: " + country);
                            System.out.println("  | TOTAL LISTINGS IN " + country.toUpperCase() + ": " + total_listings);
                        } else if (choice.equals("2")) {
                            String city = rs.getString("city");
                            System.out.println("  | CITY, COUNTRY: " + city.toUpperCase() + ", " + country.toUpperCase());
                            System.out.println("  | TOTAL LISTINGS IN (" + city.toUpperCase() + ", " + country.toUpperCase() + "): " + total_listings);
                        }
                        System.out.println("* ------------------------------------------------------------------------------------ *");
                        totals_found = rs.next();
                    }
                }
            }
        } catch (SQLException e){
            System.out.println("\n[ERROR WHEN RANKING HOSTS]: " + e.getMessage());
        }
        System.out.println("\n");
    }
    public String flag_city_query(){
        return "SELECT first_name, last_name, SIN, city, country, COUNT(DISTINCT LID, SIN, city, country) as flag_num_listings, total_city_listings" +
                " FROM Records" +
                " NATURAL JOIN" +
                " Listings" +
                " NATURAL JOIN" +
                " Users" +
                " NATURAL JOIN" +
                " (" +
                "   SELECT city, country, COUNT(*) AS total_city_listings " +
                "   FROM Listings L " +
                "   GROUP BY city, country" +
                " ) as city_listings " +
                " WHERE role='host' and is_unavail=false " +
                " GROUP BY SIN, city, country" +
                " HAVING flag_num_listings > 0.1 * city_listings.total_city_listings;";
    }
    public String flag_country_query(){
        return "SELECT first_name, last_name, SIN, country, COUNT(DISTINCT LID, SIN, country) as flag_num_listings, total_country_listings" +
                " FROM Records" +
                " NATURAL JOIN" +
                " Listings" +
                " NATURAL JOIN" +
                " Users" +
                " NATURAL JOIN" +
                " (" +
                "   SELECT country, COUNT(*) AS total_country_listings " +
                "   FROM Listings" +
                "   GROUP BY country" +
                " ) as country_listings " +
                " WHERE role='host' and is_unavail=false " +
                " GROUP BY SIN, country" +
                " HAVING flag_num_listings > 0.1 * country_listings.total_country_listings;";
    }

    public ArrayList get_blacklist(Connection connection){
        String query;
        ArrayList blacklist = new ArrayList<>();

        try{
            query = flag_city_query();
            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            boolean flag_hosts_found = rs.next();
            while(flag_hosts_found){
                int SIN = rs.getInt("SIN");
                String city = rs.getString("city");
                String country = rs.getString("country");
                blacklist.add(SIN);
                blacklist.add(city);
                blacklist.add(country);
                flag_hosts_found = rs.next();
            }

            query = flag_country_query();
            pstmt = connection.prepareStatement(query);
            rs = pstmt.executeQuery();
            flag_hosts_found = rs.next();
            while(flag_hosts_found){
                int SIN = rs.getInt("SIN");
                String country = rs.getString("country");

                blacklist.add(SIN);
                blacklist.add(country);
                flag_hosts_found = rs.next();
            }
        } catch(SQLException e){
            System.out.println("[ERROR IN GETTING BLACKLIST]: " + e.getMessage());
        }
        return blacklist;
    }
    public void handle_flag_hosts(Connection connection){
        String query;
        //GROUP BY CITY, COUNTRY
        try{
            query = flag_city_query();

            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            boolean flag_hosts_found = rs.next();
            if(!flag_hosts_found){
                print_no_results();
            } else {
                System.out.println("\n| --------------------------- HOSTS THAT HAVE > 10% OF LISTINGS  -------------------------- |");
                System.out.println("| ------------------------------------ IN A GIVEN CITY ------------------------------------ |\n");
                while(flag_hosts_found){
                    String first_name = rs.getString("first_name");
                    String last_name = rs.getString("last_name");
                    int SIN = rs.getInt("SIN");
                    String city = rs.getString("city");
                    String country = rs.getString("country");
                    String flag_num_listings = rs.getString("flag_num_listings");
                    String total_city_listings = rs.getString("total_city_listings");

                    System.out.println("NAME: " + first_name + " " + last_name + " | " + SIN);
                    System.out.println("CITY: " + city);
                    System.out.println("COUNTRY: " + country);
                    System.out.println("HOST'S # OF LISTINGS IN (" + city + ", " + country + "): " + flag_num_listings);
                    System.out.println("TOTAL # OF LISTINGS IN (" + city + ", " + country + "): " + total_city_listings);
                    System.out.println("* ------------------------------------------------------------- *");
                    flag_hosts_found = rs.next();
                }
            }
        } catch(SQLException e){
            System.out.println("\n[ERROR IN FLAGGING HOSTS]: " + e.getMessage());
        }
        System.out.println("\n");

        //GROUPING BY COUNTRY
        try{
            query = flag_country_query();

            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            boolean flag_hosts_found = rs.next();
            if(!flag_hosts_found){
                print_no_results();
            } else {
                System.out.println("\n| --------------------------- HOSTS THAT HAVE > 10% OF LISTINGS  -------------------------- |");
                System.out.println("| ----------------------------------- IN A GIVEN COUNTRY ---------------------------------- |\n");
                while(flag_hosts_found){
                    String first_name = rs.getString("first_name");
                    String last_name = rs.getString("last_name");
                    int SIN = rs.getInt("SIN");
                    String country = rs.getString("country");
                    String flag_num_listings = rs.getString("flag_num_listings");
                    String total_country_listings = rs.getString("total_country_listings");

                    System.out.println("NAME: " + first_name + " " + last_name + " | " + SIN);
                    System.out.println("COUNTRY: " + country);
                    System.out.println("HOST'S # OF LISTINGS IN " + country + ": " + flag_num_listings);
                    System.out.println("TOTAL # OF LISTINGS IN " + country + ": " + total_country_listings);
                    System.out.println("* ------------------------------------------------------------- *");
                    flag_hosts_found = rs.next();
                }
            }
        } catch(SQLException e){
            System.out.println("\n[ERROR IN FLAGGING HOSTS]: " + e.getMessage());
        }
        System.out.println("\n");
    }
    public void handle_rank_renters(Connection connection, Scanner scanner){
        String choice;
        String date_range;
        String query;

        System.out.println("\n| -------------------------- RANK RENTERS -------------------------- |");
        System.out.println("Rank MyBnB renters by total number of bookings:\n[1]: IN A SPECIFIED DATE RANGE\n[2]: IN A SPECIFIED DATE RANGE BY CITY (among renters who have made at least 2 bookings in a year)\n[q]: quit");
        choice = scanner.nextLine();

        query = "SELECT ";

        if(choice.equals("1")){
            query += "SIN, COUNT(*) AS total_bookings";
        }
        else if(choice.equals("2")){
            query += "R.SIN, L.city, COUNT(*) AS total_bookings";
        }
        else if(choice.equals("q")){
            System.out.println("\n\n[QUITING RANK RENTERS QUERIES ...]\n\n");
            return;
        }
        else {
            System.out.println("[INVALID OPTION] Try Again.");
            return;
        }

        query += " FROM Records R NATURAL JOIN Listings L WHERE R.role='renter' AND R.is_booked=true";

        System.out.println("Enter a date range of availability:\n[NOTE] Accepted Date Range Format: MM/DD/YYYY-MM/DD/YYYY\n[NOTE] if just one date, enter MM/DD/YYYY");
        date_range = scanner.nextLine();
        DateWithQuery dq = parse_date_range(scanner, query, date_range);
        List<Date> dates = dq.getDates();
        query = dq.getQuery();

        if(choice.equals("1")){
            query += " GROUP BY SIN";
        } else if(choice.equals("2")){
            query += " AND R.SIN IN (SELECT SIN FROM Records WHERE is_booked=true AND role='renter' AND YEAR(STR_TO_DATE(date_avail, '%m/%d/%Y')) IS NOT NULL" +
                    " GROUP BY SIN, YEAR(STR_TO_DATE(date_avail, '%m/%d/%Y')) HAVING COUNT(*) >= 2) GROUP BY R.SIN, L.city";
        }

        query += " ORDER BY total_bookings DESC;";

        System.out.println(query);

        try{
            PreparedStatement pstmt = connection.prepareStatement(query);
            int param_index = 1;

            if(date_range.indexOf('-') == -1){
                pstmt.setString(param_index++, date_range.trim());
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                for(Date date : dates){
                    String date_string = dateFormat.format(date);
                    pstmt.setString(param_index++, date_string.trim());
                }
            }
            ResultSet rs = pstmt.executeQuery();

            boolean totals_found = rs.next();

            if(!totals_found){
                print_no_results();
            } else {
                System.out.println("| ------------------------------------- RANKINGS ------------------------------------- |");
                while(totals_found){
                    int total_bookings = rs.getInt("total_bookings");
                    int SIN = rs.getInt("SIN");

                    System.out.println("  | USER SIN: " + SIN);

                    if(choice.equals("1")){
                        System.out.println("  | TOTAL BOOKINGS: " + total_bookings );
                    } else if(choice.equals("2")){
                        String city = rs.getString("city");
                        System.out.println("  | CITY: " + city);
                        System.out.println("  | TOTAL BOOKINGS IN " + city.toUpperCase() + ": " + total_bookings);
                    }
                    System.out.println("* ------------------------------------------------------------------------------------ *");
                    totals_found = rs.next();
                }
            }
        } catch(SQLException e){
            System.out.println("\n[ERROR IN RANKING RENTERS]: " + e.getMessage() + "\n");
        }
        System.out.println("\n");
    }
    public void handle_cancellations(Connection connection){
        String query;
        try{
            query = "SELECT role, cancellation_year, MAX(total_cancellations) AS largest_cancellations" +
                    " FROM ( " +
                    " SELECT role, r.SIN, YEAR(STR_TO_DATE(date_avail, '%m/%d/%Y')) AS cancellation_year, COUNT(*) AS total_cancellations" +
                    " FROM Records r" +
                    " WHERE r.is_cancelled=true AND YEAR(STR_TO_DATE(date_avail, '%m/%d/%Y')) IS NOT NULL" +
                    " GROUP BY r.SIN, role, YEAR(STR_TO_DATE(date_avail, '%m/%d/%Y'))) as cancellations" +
                    " NATURAL JOIN Users" +
                    " GROUP BY role, cancellation_year" +
                    " ORDER BY largest_cancellations DESC;";

            PreparedStatement pstmt = connection.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
            boolean cancellations_found = rs.next();
            if(!cancellations_found){
                print_no_results();
            } else {
                System.out.println("\n| --------------------------- CANCELLATIONS FROM RENTERS AND HOSTS -------------------------- |");
                while(cancellations_found){
                    String year = rs.getString("cancellation_year");
                    String role = rs.getString("role");
                    int num_cancellations = rs.getInt("largest_cancellations");

                    //System.out.print("NAME: " + first_name.toUpperCase() + " " + last_name.toUpperCase());
                    //System.out.print(" | SIN: " + SIN);
                    System.out.println(" | ROLE: " + role.toUpperCase());
                    System.out.println(" | LARGEST # OF CANCELLATIONS: " + num_cancellations);
                    System.out.println(" | YEAR: " + year);
                    System.out.println("* -------------------------------------------- * ");

                    cancellations_found = rs.next();
                }
            }
        } catch(SQLException e){
            System.out.println("[ERROR IN REPORTING CANCELLATIONS]: " + e.getMessage());
        }
        System.out.println("\n");
    }
    /**
     * FUNCTIONS FOR REVIEW QUERIES
     */
    public void handle_review_queries(Connection connection){
        try {
            System.out.println("\n[GENERATING RESULTS ....]\n");
            InputStream parser_file = new FileInputStream("en-parser-chunking.bin");
            ParserModel pm = new ParserModel(parser_file);
            Parser prsr = ParserFactory.create(pm);
            String query = "SELECT LID, GROUP_CONCAT(DISTINCT comments SEPARATOR '; ') AS all_comments FROM Ratings NATURAL JOIN Records GROUP BY LID;";
            PreparedStatement psmt = connection.prepareStatement(query);
            ResultSet rs = psmt.executeQuery();
            boolean reviews_found = rs.next();
            if(!reviews_found){
                print_no_results();
            } else {
                System.out.println("\n| --------------------------- RESULTS -------------------------- |");
                while(reviews_found){
                    String comments = rs.getString("all_comments");
                    int rs_LID = rs.getInt("LID");
                    System.out.println(" ******************* LISTING ID: " + rs_LID + " ******************* ");
                    HashMap<String, Integer> noun_phrases = new HashMap<>();
                    String[] phrases = comments.toLowerCase().split("[;,.!?]");
                    for(String phrase : phrases){
                        Parse[] parser = ParserTool.parseLine(phrase, prsr, 1);
                        for(Parse p : parser){
                            get_frequency(p, noun_phrases);
                        }
                    }
                    for(String noun: noun_phrases.keySet()){
                        int freq = noun_phrases.get(noun);
                        System.out.println("NOUN PHRASE: \"" + noun + "\" | " + "FREQUENCY: " + freq);
                    }
                    System.out.println("* -------------------------------------------------------------- *");
                    reviews_found = rs.next();
                }
            }
        } catch(FileNotFoundException e){
            System.err.println("[CANNOT OPEN PARSER FILE]: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[ERROR WITH PARSERMODEL]: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("[ERROR WITH RUNNING REVIEW QUERY]: "+ e.getMessage());
        }
        System.out.println("\n");
    }

    //recursive function to get frequency of noun phrases
    public void get_frequency(Parse p, HashMap<String, Integer> noun_phrases){
        if(p.getType().equals("NP")){
            String np = p.getCoveredText();
            noun_phrases.put(np, noun_phrases.getOrDefault(np, 0) + 1);
        }
        for(Parse child : p.getChildren()){
            get_frequency(child, noun_phrases);
        }
    }

}
