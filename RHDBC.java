package mysql;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class RHDBC {
    public void get_users(Connection connection) {
        try {
            //Execute a query
            Statement stmt = connection.createStatement();
            String sql = "SELECT * FROM Users;";
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("--------------------------\nCURRENT USERS ON MYBNB: \n--------------------------");

            //STEP 5: Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                int SIN = rs.getInt("SIN");
                String role = rs.getString("role");
                String first_name = rs.getString("first_name");
                String last_name = rs.getString("last_name");
                String address = rs.getString("address");
                String DOB = rs.getString("DOB");
                String occupation = rs.getString("occupation");
                String credit_card_info = rs.getString("credit_card_info");

                //Display values
                System.out.print("SIN: " + SIN);
                System.out.print(", Role: " + role);
                System.out.print(", First Name: " + first_name);
                System.out.print(", Last Name: " + last_name);
                System.out.print(", Address: " + address);
                System.out.print(", Date of Birth: " + DOB);
                System.out.print(", Occupation: " + occupation);
                System.out.println(", Credit Card Info: " + credit_card_info);

            }
        } catch(SQLException e){
            System.err.println("Error when trying to get users.");
        }
        System.out.print("\n\n\n");
    }
    public void handle_create_user(Connection connection, Scanner scanner) {
        String input;

        //ATTRIBUTES
        int SIN;
        String role;
        String first_name;
        String last_name;
        String address;
        String DOB;
        String occupation;
        String credit_card_info = null;

        System.out.println("How old are you? [Just enter the number]");
        try{
            int age_check = Integer.parseInt(scanner.nextLine());
            if(age_check < 18){
                System.out.println("Only users 18 and above can create an account for MyBNB.\n");
                return;
            }
        } catch(NumberFormatException e){
            System.err.println("\n[ERROR] NOT VALID NUMBER. ABORTING...\n");
            return;
        }
        System.out.println("Are you a renter OR a host? Enter:\n[1]: renter\n[2]: host");
        input = scanner.nextLine();
        if(input.equals("1")){
            role = "renter";
            System.out.println("Please enter the Credit Card Information to be saved for payments with this account.");
            credit_card_info = scanner.nextLine();
        }
        else if(input.equals("2")){
            role = "host";
        }
        else{
            System.out.println("Please only enter 1 or 2. Try again.\n");
            return;
        }

        System.out.println("What is your SIN (Social Insurance Number) [REQUIRED]?");
        try {
            SIN = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid integer [0-9 digits AND no letters].");
            return;
        }

        System.out.println("What is your first name?");
        first_name = scanner.nextLine();

        System.out.println("What is your last name?");
        last_name = scanner.nextLine();

        System.out.println("What is your address?");
        address = scanner.nextLine();

        System.out.println("What is your date of birth? [Enter in the format: MM/DD/YYYY]");
        DOB = scanner.nextLine();

        System.out.println("What is your occupation?");
        occupation = scanner.nextLine();

        try {
            String sql = "INSERT INTO Users VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, SIN);
            pstmt.setString(2, role);
            pstmt.setString(3, first_name);
            pstmt.setString(4, last_name);
            pstmt.setString(5, address);
            pstmt.setString(6, DOB);
            pstmt.setString(7, occupation);
            pstmt.setString(8, credit_card_info);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("New user created successfully.\n");
            } else {
                System.out.println("Failed to create a new user.");
            }
        } catch (SQLException e) {
            System.err.println("Error when trying to create a new user: " + e.getMessage());
        }

    }
    public void handle_delete_user(Connection connection, Scanner scanner){
        String input;

        String role;
        int SIN;

        System.out.println("Is the user a renter or a host? Enter:\n[1]: renter\n[2]: host");
        input = scanner.nextLine();
        if(input.equals("1")){
            role = "renter";
        }
        else if(input.equals("2")){
            role = "host";
        }
        else{
            System.out.println("Please only enter 1 or 2. Try again.\n");
            return;
        }

        System.out.println("What is the user's SIN?");
        SIN = Integer.parseInt(scanner.nextLine());

        try {
            String sql = "DELETE FROM Ratings WHERE SIN=? AND role=?;";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, SIN);
            pstmt.setString(2, role);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected <= 0) {
                System.out.println("Failed to user ratings.");
            }

            sql = "DELETE FROM Records WHERE SIN=? AND role=?;";
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, SIN);
            pstmt.setString(2, role);

            rowsAffected = pstmt.executeUpdate();
            if (rowsAffected <= 0) {
                System.out.println("Failed to user records.");
            }

            sql = "DELETE FROM Users WHERE SIN=? AND role=?;";
            pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, SIN);
            pstmt.setString(2, role);

            rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User successfully deleted.");
            } else {
                System.out.println("Failed to delete user.");
            }

        } catch (SQLException e) {
            System.err.println("Error when trying to delete user: " + e.getMessage());
        }
    }
    public User handle_sign_in_user(Connection connection, Scanner scanner){
        String input;
        String role;
        int SIN;
        User current_user;

        System.out.println("Are you a renter or a host? Enter:\n[1]: renter\n[2]: host");
        input = scanner.nextLine();
        if(input.equals("1")){
            role = "renter";
        }
        else if(input.equals("2")){
            role = "host";
        }
        else{
            System.out.println("Please only enter 1 or 2. Try again.\n");
            return null;
        }
        try {
            System.out.println("What is your SIN associated to this account?");
            SIN = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e){
            System.out.println("\n\nERROR IN ENTERING SIN: Please try again.\n\n");
            return null;
        }

        try {
            String sql = "SELECT * FROM Users WHERE SIN=? AND role=?;";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, SIN);
            pstmt.setString(2, role);
            ResultSet rs = pstmt.executeQuery();

            if(!rs.next()){
                System.out.println("\n\n|********************************** USER NOT FOUND *********************************|");
                System.out.println("|************** Check if you have entered your role or SIN correctly. **************|");
                System.out.println("|******************** Or consider creating a new user profile. *********************|\n\n");
                return null;
            }

            System.out.println("\n********** SUCCESSFUL **********\n");

            current_user = new User(SIN, role);
            return current_user;

        } catch (SQLException e) {
            System.err.println("Error when trying to get user: " + e.getMessage());
            return null;
        }
    }
    public void handle_renter_listings(Connection connection, Scanner scanner, User current_user){
        String input;
        while(true){
            System.out.println("As a renter, you can:\n[1]: Make a booking\n[2]: Cancel a booking\n[3]: View bookings\n[4]: Submit a review\n[5]: Reply to a comment\n[6]: Sign out\nWhat would you like to do? Enter the appropriate number.");
            input = scanner.nextLine();
            if(input.equals("1")){
                handle_book_listing(connection, scanner, current_user);
            }
            else if(input.equals("2")){
                handle_cancel_booking(connection, scanner, current_user);
            }
            else if(input.equals("3")){
                view_bookings(connection, current_user);
            }
            else if(input.equals("4")){
                handle_submit_review(connection, scanner, current_user);
            }
            else if(input.equals("5")){
                handle_reply_comment(connection, scanner, current_user);
            }
            else if(input.equals("6")){
                System.out.println("-***************************************************************-");
                System.out.println("-------------------------- SIGNING OUT --------------------------");
                System.out.println("-***************************************************************-\n");
                break;
            }
            else {
                System.out.println("[INVALID OPTION] Try Again.\n");
            }
        }
    }
    public void handle_host_listings(Connection connection, Scanner scanner, User current_user){
        String input;
        while(true){
            System.out.println("As a host, you can:\n[1]: Make a listing\n[2]: Remove a listing\n[3]: Edit Listing\n[4]: View Listing(s)\n[5]: Manage booking(s)\n[6]: Submit a review\n[7]: Reply to a comment\n[8]: Sign out\nWhat would you like to do? Enter the appropriate number.");
            input = scanner.nextLine();
            if(input.equals("1")){
                handle_make_listing(connection, scanner, current_user);
            }
            else if(input.equals("2")){
                handle_remove_listing(connection, scanner, current_user);
            }
            else if(input.equals("3")){
                handle_edit_listing(connection, scanner, current_user);
            }
            else if(input.equals("4")){
                view_listings(connection, scanner, current_user);
            }
            else if(input.equals("5")){
                handle_cancel_booking_host(connection, scanner, current_user);
            }
            else if(input.equals("6")){
                handle_submit_review(connection, scanner, current_user);
            }
            else if(input.equals("7")){
                handle_reply_comment(connection, scanner, current_user);
            }
            else if(input.equals("8")) {
                System.out.println("-***************************************************************-");
                System.out.println("-------------------------- SIGNING OUT --------------------------");
                System.out.println("-***************************************************************-\n");
                break;
            }
            else {
                System.out.println("[INVALID OPTION] Try Again.\n");
            }
        }
    }

    /**
     * RENTER FUNCTIONS/OPTIONS
     */
    public void get_avail_listings(Connection connection){
        try {
            //Execute a query
            Statement stmt = connection.createStatement();
            String sql = "SELECT DISTINCT L.* FROM Listings L NATURAL JOIN Records R WHERE (R.is_booked=false OR (R.is_booked=true AND R.is_cancelled=true)) AND R.is_unavail=false;";
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("\n| ----------------------------- CURRENT AVAILABLE LISTINGS ON MYBNB ----------------------------- |");
            boolean check_if_avail_empty = rs.next();

            if(!check_if_avail_empty){
                System.out.println(" ----- Currently, there are no available listings on MYBNB. Please check at a later time. ----- \n\n");
            }
            while(check_if_avail_empty) {
                //Retrieve by column name
                int LID = rs.getInt("LID");
                String type = rs.getString("type");
                String postal_code = rs.getString("postal_code");
                String city = rs.getString("city");
                String country = rs.getString("country");
                String amenities = rs.getString("amenities");

                //Display values
                System.out.print("LISTING ID: " + LID);
                System.out.print(", TYPE: " + type);
                System.out.print(", LOCATION: " + city + ", " + country + " " + postal_code);
                System.out.println(", AMENITIES: " + amenities);

                check_if_avail_empty = rs.next();
            }
        } catch(SQLException e){
            System.err.println("Error when trying to get available listings: " + e.getMessage());
        }
        System.out.println("\n| ----------------------------------------------------------------------------------------------- |");
    }
    public void view_bookings(Connection connection, User current_user){
        try {
            //Execute a query
            String sql = "SELECT * FROM Records WHERE role='renter' AND SIN=? AND is_booked=true AND is_cancelled=false AND is_unavail=false AND (RID) NOT IN (SELECT RID FROM Records WHERE is_cancelled=true);";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, current_user.getSIN());
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n| -------------------------- YOUR BOOKINGS -------------------------- |");
            boolean check_if_bookings_empty = rs.next();

            if(!check_if_bookings_empty){
                System.out.println("\n  ------------------------ NO BOOKINGS FOUND ------------------------  ");
                System.out.println("\n| ------------------------------------------------------------------- |\n");
            }
            while (check_if_bookings_empty) {
                //Retrieve by column name
                int LID = rs.getInt("LID");
                float price = rs.getFloat("price");
                String date_book = rs.getString("date_avail");

                //Display values
                System.out.println("LISTING ID: " + LID);
                System.out.println("PRICE: " + price);
                System.out.println("DATE: " + date_book);
                System.out.println("* --------------------- *");

                check_if_bookings_empty = rs.next();
            }
        } catch(SQLException e){
            System.err.println("Error when trying to get available listings: " + e.getMessage());
        }
    }
    public void handle_book_listing(Connection connection, Scanner scanner, User current_user){
        get_avail_listings(connection);
        System.out.println("Which listing would you like to rent? Enter the LISTING ID.");
        int LID_to_book = Integer.parseInt(scanner.nextLine());

        System.out.println("\n| -------------- DATES LISTED FOR LISTING ID: " + LID_to_book +  " -------------- |");

        try{
            String sql = "Select DISTINCT date_avail, price FROM Records R WHERE LID=? AND (is_booked=false OR (is_booked=true AND is_cancelled=true))AND is_unavail=false;";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, LID_to_book);
            ResultSet rs = pstmt.executeQuery();

            boolean dates_found = rs.next();
            if(!dates_found){
                System.out.println("\n   ********************* NO DATES FOUND *********************\n");
                return;
            } else {
                while(dates_found){
                    String date_avail = rs.getString("date_avail");
                    float price = rs.getFloat("price");
                    System.out.println("DATE: " + date_avail);
                    System.out.println("PRICE: " + price);
                    System.out.println("* --------------------- *");

                    dates_found = rs.next();
                }
            }


        } catch(SQLException e){
            System.out.println("Error when trying to find dates for listing ID: " + LID_to_book + "\nError: " + e.getMessage());
        }
        while(true){
            System.out.println("Which date would you like to book? Press q to quit. \n[NOTE] Accepted Date Format: MM/DD/YYYY");
            String date_to_book = scanner.nextLine();
            if(date_to_book.equals("q")){
                break;
            }
            try{
                String sql = "Select * FROM Records WHERE LID=? AND role='host' AND date_avail=?";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, LID_to_book);
                pstmt.setString(2, date_to_book);

                ResultSet rs = pstmt.executeQuery();
                if(!rs.next()){
                    System.out.println("Invalid date. Try again.");
                    break;
                }
                float price = rs.getFloat("price");
                int host_SIN = rs.getInt("SIN");

                //CREATE NEW RECORD FOR RENTER
                sql = "INSERT INTO Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) VALUES (?, ?, false, true, false, ?, ?, 'renter');";
                pstmt = connection.prepareStatement(sql);
                pstmt.setFloat(1, price);
                pstmt.setString(2, date_to_book);
                pstmt.setInt(3, current_user.getSIN());
                pstmt.setInt(4, LID_to_book);

                int rowsAffected = pstmt.executeUpdate();

                if(rowsAffected <= 0){
                    System.out.println("****** BOOKING FAILED ******");
                    break;
                }

                //UPDATE RECORDS FOR HOST
                sql = "UPDATE Records SET is_booked=true WHERE LID=? AND role='host' AND date_avail=? AND SIN=?;";
                pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, LID_to_book);
                pstmt.setString(2, date_to_book);
                pstmt.setInt(3, host_SIN);

                rowsAffected = pstmt.executeUpdate();

                if(rowsAffected > 0){
                    System.out.println("****** BOOKING SUCCESSFUL ******");
                } else {
                    System.out.println("****** BOOKING FAILED ******");
                    break;
                }
            } catch(SQLException e){
                System.out.println("Error when trying to find date/ book date for listing ID: " + LID_to_book + "\nError: " + e.getMessage());
            }
        }
    }

    /**
     * HOST FUNCTIONS/OPTIONS
     */
    public boolean is_booked(Connection connection, int LID){
        //check to see if this listing is being booked by a renter
        //action: prevent host from deleting listing
        try{
            String sql = "Select * FROM Records WHERE LID=? AND is_cancelled=false AND is_booked=true;";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, LID);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch(SQLException e){
            System.out.println("Error when trying to find listing ID: " + LID + " in Records.\nError: " + e.getMessage());
        }
        return false;
    }
    public boolean find_listing(Connection connection, User current_user, int LID){
        //check to see if this listing is found for user
        try{
            String sql = "Select * FROM Records NATURAL JOIN Listings WHERE LID=? AND SIN=? AND role='host';";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, LID);
            pstmt.setInt(2, current_user.getSIN());
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch(SQLException e){
            System.out.println("Error when trying to find listing ID: " + LID + "for User: " + current_user.getSIN() + "\nError: " + e.getMessage());
        }
        return false;
    }
    public void view_listings(Connection connection, Scanner scanner, User current_user){
        //show listing for user
        System.out.println("\n| -------------------------- YOUR LISTINGS  -------------------------- |");
        try{
            String sql = "SELECT DISTINCT L.* FROM Listings L NATURAL JOIN Records R WHERE R.role='host' AND R.SIN=?;";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, current_user.getSIN());
            ResultSet rs = pstmt.executeQuery();

            boolean check = rs.next();

            if(!check){
                System.out.println("\n  ------------------------- NO LISTINGS FOUND ------------------------  \n");
                System.out.println("| -------------------------------------------------------------------- |\n");
                return;
            }
            while(check){
                int LID = rs.getInt("LID");
                String type = rs.getString("type");
                String street = rs.getString("street");
                String city = rs.getString("city");
                String country = rs.getString("country");
                String postal_code = rs.getString("postal_code");
                String amenities = rs.getString("amenities");

                //Display values
                System.out.print("LISTING ID: " + LID);
                System.out.print(", TYPE: " + type);
                System.out.print(", ADDRESS: " + street + ", " + city + ", " + country + " " + postal_code);
                System.out.println("\nAMENITIES: " + amenities);

                System.out.println("* -------------------------------------------------------------------- *");

                check = rs.next();
            }

        } catch(SQLException e){
            System.out.println("Error in trying to get listings for user: " + e.getMessage());
            return;
        }

        System.out.println("| -------------------------------------------------------------------- |\n");
    }
    public void view_dates(Connection connection, int LID){
        System.out.println("\n| -------------- DATES LISTED FOR LISTING ID: " + LID +  " -------------- |");

        try{
            String sql = "Select DISTINCT R.* FROM Records R WHERE LID=? AND role='host';";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, LID);
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()){
                String date_avail = rs.getString("date_avail");
                boolean is_unavail = rs.getBoolean("is_unavail");
                boolean is_booked = rs.getBoolean("is_booked");
                boolean is_cancelled = rs.getBoolean("is_cancelled");
                float price = rs.getFloat("price");
                System.out.println("DATE: " + date_avail);
                System.out.println("PRICE: " + price);
                System.out.println("MADE UNAVAILABLE: " + is_unavail);
                System.out.println("RENTER HAS BOOKED (FALSE = NOT BOOKED): " + is_booked);
                System.out.println("RENTER HAS CANCELLED BOOKING (FALSE = NOT CANCELLED): " + is_cancelled);
                System.out.println("* ---------------------------------------------------------------------- *");
            }

        } catch(SQLException e){
            System.out.println("Error when trying to find dates for listing ID: " + LID + "\nError: " + e.getMessage());
        }
    }
    public ArrayList<Float> get_prices(Connection connection, String one_month, String two_month){
        ArrayList<Float> prices = new ArrayList<>();
        try {
            String query = "SELECT DISTINCT price FROM Records WHERE SUBSTRING(date_avail, 1, 2)=? OR SUBSTRING(date_avail, 1, 2)=?;";
            PreparedStatement psmt = connection.prepareStatement(query);
            psmt.setString(1, one_month);
            psmt.setString(2, two_month);
            ResultSet rs = psmt.executeQuery();
            while(rs.next()){
                float price = rs.getFloat("price");
                prices.add(price);
            }
        } catch (SQLException e){
            System.out.println("[ERROR IN GETTING PRICES]: " + e.getMessage());
        }
        return prices;
    }
    public void check_date(Connection connection, String date_avail){
        if(date_avail.substring(0,2).equals("12") || date_avail.substring(0,2).equals("01")) {
            System.out.println("\n[HOST TOOLTIP]: The date you've entered is during a popular season for vacation/stay-cations!\n       To remain comepetitive among other hosts, MyBnB suggests you set your price LOWER than competitors.");
            ArrayList<Float> prices = get_prices(connection, "12", "01");
            System.out.println("\n[FINDING COMPETITVE PRICE ...]\n");
            if(prices.isEmpty()){
                System.out.println("| CURRENTLY, THERE ARE NO HOSTS THAT HAVE LISTINGS IN DECEMBER AND JANUARY MONTHS!\nWe suggest pricing at 100.00 or below for per night. :)\n");
            } else {
                float min_price = Float.MAX_VALUE;
                for (Float check_price : prices) {
                    float curr_price = check_price;
                    if (curr_price < min_price) {
                        min_price = curr_price;
                    }
                }
                System.out.println(">>>> CURRENT CHEAPEST LISTING FOR DECEMBER AND JANUARY MONTHS: " + min_price + " <<<<\n");
            }
        } else if (date_avail.substring(0,2).equals("07") || date_avail.substring(0,2).equals("08")) {
            System.out.println("\n[HOST TOOLTIP]: The date you've entered is during a popular season for vacation/stay-cations!. To remain comepetitive among other hosts, MyBnB suggests you set your price LOWER than competitors.");
            ArrayList<Float> prices = get_prices(connection, "07", "08");
            System.out.println("\n[FINDING COMPETITVE PRICE ...]\n");
            if(prices.isEmpty()){
                System.out.println("| CURRENTLY, THERE ARE NO HOSTS THAT HAVE LISTINGS IN JULY AND AUGUST MONTHS!\nWe suggest pricing at 100.00 or below for per night. :)");
            } else {
                float min_price = Float.MAX_VALUE;
                for (Float check_price : prices){
                    float curr_price = check_price;
                    if(curr_price < min_price){
                        min_price = curr_price;
                    }
                }
                System.out.println(">>>> CURRENT CHEAPEST LISTING FOR JULY AND AUGUST MONTHS: " + min_price + " <<<<\n");
            }
        }
    }
    public void add_dates(Connection connection, Scanner scanner, User current_user, int LID){
        while(true){
            String date_avail;
            float price;

            System.out.println("Please enter the date that your listing will be available for rent. You can only enter them one at a time.\nIf you have no more dates to enter, enter q to quit. \n[NOTE] Accepted Date Format: MM/DD/YYYY");
            date_avail = scanner.nextLine();
            if(date_avail.equals("q") || date_avail.equals("")){
                break;
            }

            System.out.println("Please enter the price associated for this date.\n[NOTE] Accepted Price Format: XXXXX.XX");
            price = Float.parseFloat(scanner.nextLine());

            try {
                String sql = "INSERT INTO Records (price, date_avail, is_unavail, is_booked, is_cancelled, SIN, LID, role) VALUES (?, ?, false, false, false, ?, ?, ?);";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setFloat(1, price);
                pstmt.setString(2, date_avail);
                pstmt.setInt(3, current_user.getSIN());
                pstmt.setInt(4, LID);
                pstmt.setString(5, current_user.getRole());

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("----------------------------------------");
                    System.out.println("\nDate added successfully for listing ID: " + LID + "\n");
                    System.out.println("----------------------------------------");
                } else {
                    System.out.println("\nFailed to add date.\n");
                }
            } catch (SQLException e) {
                System.err.println("Error when trying to add date: " + e.getMessage());
            }
        }
    }
    public void check_amenities(String amenities){
        String filePath = "amenities_to_include.txt";
        String inputString = "This is a test string";

        ArrayList<String> amenities_file = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                amenities_file.add(line.trim());
            }
        } catch (IOException e) {
            System.out.println("[ERROR WHEN READING AMENITIES TXT FILE]: " + e.getMessage());
        }
        int limit = 0;
        for(String amenity : amenities_file){
            if(!amenities.contains(amenity)){
                limit++;
                System.out.println("[HOST TOOLTIP]: We've noticed that you have included >> " + amenity.toUpperCase() + " << as an amenity to this listing.\n        Consider adding this amenity if your listing supplies it! :)\n");
            }
            if(limit > 3){
                break;
            }
        }
    }
    public void handle_make_listing(Connection connection, Scanner scanner, User current_user){
        String input;

        int LID = 0;
        String type;
        float latitude;
        float longitude;
        String street;
        String postal_code;
        String city;
        String country;
        String amenities;


        System.out.println("\n\n| ---------------- TYPE OF LISTING ---------------- |");
        System.out.println("For this new listing, what is the type:\n[1]: Full House\n[2]: Apartment\n[3]: Room\nPlease enter the appropriate number.");
        input = scanner.nextLine();
        if(input.equals("1")){
            type = "Full House";
        } else if(input.equals("2")){
            type = "Apartment";
        } else if(input.equals("3")){
            type = "Room";
        } else {
            System.out.println("\n\nOPTION DOES NOT EXIST. Enter the appropriate number.\n\n");
            return;
        }

        //GET BLACKLIST
        QDBC qdb = new QDBC();
        ArrayList blacklist = qdb.get_blacklist(connection);

        System.out.println("\n| ---------------- LOCATION ---------------- |");
        System.out.println("What is the street name of your listing?");
        street = scanner.nextLine();
        System.out.println("What city is this listing located in?");
        city = scanner.nextLine();
        System.out.println("What country is this listing located in?");
        country = scanner.nextLine();

        //CHECK BLACKLIST
        for(int i = 0; i < blacklist.size(); i++){
            if(blacklist.get(i).equals(current_user.getSIN())){
                if(i+2 < blacklist.size()){
                    if(blacklist.get(i + 1).equals(city) && blacklist.get(i + 2).equals(country)){
                        System.err.println("[SYSTEM PROHIBIT]: YOU CANNOT CREATE ANOTHER LISTING IN THIS CITY. YOU HAVE MORE THAN 10% OF LISTINGS IN " + city.toUpperCase() + ", " + country.toUpperCase());
                        return;
                    }
                }
                if(i+1 < blacklist.size()){
                    if(blacklist.get(i + 1).equals(country)){
                        System.err.println("[SYSTEM PROHIBIT]: YOU CANNOT CREATE ANOTHER LISTING IN THIS COUNTRY. YOU HAVE MORE THAN 10% OF LISTINGS IN " + country.toUpperCase());
                        return;
                    }
                }
            }
        }

        try {
            System.out.println("What is the postal code for this listing?");
            postal_code = scanner.nextLine();
            System.out.println("Please enter the latitude of this listing.");
            latitude = Float.parseFloat(scanner.nextLine());
            System.out.println("Please enter the longitude of this listing.");
            longitude = Float.parseFloat(scanner.nextLine());
        } catch(NumberFormatException e){
            System.err.println("[INVALID INPUT]: " + e.getMessage());
            return;
        }

        System.out.println("\n| ---------------- AMENITIES ---------------- |");
        System.out.println("What are amenities offered for this listing? Please enter them in a list separated by commas. (e.g. WI-FI, patio, outdoor seating, etc.)");
        amenities = scanner.nextLine();

        check_amenities(amenities);
        System.out.println("Would you like to include more amenities? Please enter them in a list separated by commas. (e.g. heated floors, sauna, etc.)");
        amenities += ", ";
        amenities += scanner.nextLine();

        System.out.println("\nCREATING NEW LISTING...               \n");

        try {
            String sql = "INSERT INTO Listings (type, latitude, longitude, street, postal_code, city, country, amenities) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, type);
            pstmt.setFloat(2, latitude);
            pstmt.setFloat(3, longitude);
            pstmt.setString(4, street);
            pstmt.setString(5, postal_code);
            pstmt.setString(6, city);
            pstmt.setString(7, country);
            pstmt.setString(8, amenities);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("\nNew listing created successfully!\n");
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if(generatedKeys.next()){
                    LID = generatedKeys.getInt(1);
                } else if(LID == 0){
                    System.out.println("Cannot find LID for newly created listing.");
                    return;
                }
            } else {
                System.out.println("\nFailed to create a new listing.\n");
            }
        } catch (SQLException e) {
            System.out.println("[ERROR TRYING TO MAKE NEW LISTING]: " + e.getMessage());
            if(e.getMessage().contains("Duplicate entry")){
                System.out.println("There already exists a listing with this address.");
            }
            return;
        }

        // adding dates to records for LID
        System.out.println("\n| ---------------- DATES ---------------- |");

        add_dates(connection, scanner, current_user, LID);

        System.out.println("\n*--------------------- ALL SET! ---------------------*\n");
    }
    public void handle_remove_listing(Connection connection, Scanner scanner, User current_user){
        System.out.println("Which listing would you like to remove? Enter the Listing ID.");
        String input = scanner.nextLine();
        if(input.equals("")){
            return;
        }
        int LID_to_delete = Integer.parseInt(input);

        if(is_booked(connection, LID_to_delete)){
            System.out.println("This listing is currently booked by someone. You cannot delete this listing.");
            return;
        }

        //if listing is not currently being booked by a renter, then delete listing in records and in listings
        //remove trace of listing in records
        try{
            String sql = "DELETE FROM Records WHERE LID=? and role='host';";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, LID_to_delete);
            pstmt.executeUpdate();

        } catch(SQLException e){
            System.out.println("Error in trying to find listing ID: " + LID_to_delete + " in Records. \nError: " + e.getMessage());
            return;
        }

        //remove listing from Listings
        try{
            String sql = "DELETE FROM Listings WHERE LID=?;";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, LID_to_delete);
            int rowsAffected = pstmt.executeUpdate();
            if(rowsAffected > 0){
                System.out.println("\n*---------- LISTING ID: " + LID_to_delete + " SUCCESSFULLY DELETED ----------*\n");
            } else {
                System.out.println("\n*---------- LISTING ID: " + LID_to_delete + " DOES NOT EXIST ----------*\n");
            }

        } catch(SQLException e){
            System.out.println("Error in trying to delete listing ID: " + LID_to_delete + " in Listings. \nError: " + e.getMessage());
            return;
        }
    }
    public void handle_edit_listing(Connection connection, Scanner scanner, User current_user){
        String input;
        int LID_to_edit;
        System.out.println("What listing would you like to edit? Enter the Listing ID.");
        LID_to_edit = Integer.parseInt(scanner.nextLine());

        if(!find_listing(connection, current_user, LID_to_edit)){
            System.out.println("LISTING ID: " + LID_to_edit + " NOT FOUND FOR USER.");
            return;
        }

        view_dates(connection, LID_to_edit);

        System.out.println("Would you like to do? Enter the appropriate number.\n[1]: Edit Availability of Listing.\n[2]: Edit Price of Listing.\n");
        input = scanner.nextLine();

        if(input.equals("1")){
            handle_listing_avail(connection, scanner, current_user, LID_to_edit);
        }
        else if(input.equals("2")){
            handle_listing_price(connection, scanner, LID_to_edit);
        }
        else {
            System.out.println("Choice not valid. Going back.");
        }
    }
    public void handle_listing_avail(Connection connection, Scanner scanner, User current_user, int LID_to_edit){
        String input;
        String date_to_change;

        System.out.println("Options:\n[1]: Add date(s)\n[2]: Make a date unavailable\nEnter the appropriate number.");
        input = scanner.nextLine();
        //add date(s)
        if(input.equals("1")){
            add_dates(connection, scanner, current_user, LID_to_edit);
            return;
        }
        //make date unavail
        if(input.equals("2")){
            System.out.println("Which date would you like to make unavailable?\n[NOTE] Accepted Date Format: MM/DD/YYYY");
            date_to_change = scanner.nextLine();
            try {
                String sql = "UPDATE Records SET is_unavail=true WHERE LID=? AND date_avail=? AND is_booked=false;";
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, LID_to_edit);
                pstmt.setString(2, date_to_change);

                int rowsAffected = pstmt.executeUpdate();

                if(rowsAffected > 0){
                    System.out.println("****** SUCCESSFUL ******");
                } else {
                    System.out.println("[DATE NOT FOUND or DATE IS BOOKED] Try Again.");
                }

            } catch (SQLException e) {
                System.err.println("Error when trying to make date unavailable: " + e.getMessage());
            }
        }
        else {
            System.out.println("Choice not valid. Try again.");
            return;
        }
    }
    public void handle_listing_price(Connection connection, Scanner scanner, int LID_to_edit){
        String date_to_change;
        float new_price;

        System.out.println("Which date would you like to change the price?\n[NOTE] Accepted Date Format: MM/DD/YYYY\n[NOTE] You cannot change the price of a listing for a date where it is booked.");
        date_to_change = scanner.nextLine();
        System.out.println("Please enter the new price.\n[NOTE] Accepted Price Format: XXXX.XX");
        new_price = Float.parseFloat(scanner.nextLine());

        try {
            String sql = "UPDATE Records SET price=? WHERE LID=? and date_avail=? and is_booked=false;";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setFloat(1, new_price);
            pstmt.setInt(2, LID_to_edit);
            pstmt.setString(3, date_to_change);

            int rowsAffected = pstmt.executeUpdate();

            if(rowsAffected > 0){
                System.out.println("\n****** SUCCESSFUL ******\n");
            } else {
                System.out.println("[DATE NOT FOUND] Try Again.");
            }

        } catch (SQLException e) {
            System.err.println("Error when trying to make change price: " + e.getMessage());
        }
    }
    public void handle_cancel_booking_host(Connection connection, Scanner scanner, User current_user){
        int option;
        int LID_to_edit;
        System.out.println("Options:\n[1]: View All Dates for Listing\n[2]: Cancel a Booking");
        try{
            option = Integer.parseInt(scanner.nextLine());
            if(option == 1) {
                System.out.println("For what listing, would you like to see the dates available for? Enter the Listing ID.");
                LID_to_edit = Integer.parseInt(scanner.nextLine());
                view_dates(connection, LID_to_edit);
            } else if (option == 2) {
                handle_cancel_booking(connection, scanner, current_user);
            } else {
                System.out.println("[INVALID OPTION] Try again.");
            }
        } catch(NumberFormatException e){
            System.out.println("[INVALID TYPE] Please only enter a number.\n");
        }
    }

    /**
     * RENTER/HOST FUNCTIONS
     */
    public void handle_cancel_booking(Connection connection, Scanner scanner, User current_user){
        String date_to_cancel;
        int cancel_LID;

        System.out.println("For which listing, would you like to cancel your booking? Enter the LISTING ID.");
        cancel_LID = Integer.parseInt(scanner.nextLine());

        System.out.println("Which date would you like to cancel?\n[NOTE] Accepted Date Format: MM/DD/YYYY");
        date_to_cancel = scanner.nextLine();
        //set is_cancelled to true for both host and renter of that listing for that date
        try {
            String sql = "UPDATE Records SET is_cancelled=true WHERE LID=? AND SIN=? AND role=? AND date_avail=? AND is_booked=true;";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, cancel_LID);
            pstmt.setInt(2, current_user.getSIN());
            pstmt.setString(3, current_user.getRole());
            pstmt.setString(4, date_to_cancel);

            int rowsAffected = pstmt.executeUpdate();

            if(rowsAffected > 0){
                System.out.println("****** CANCELLATION SUCCESSFUL ******");
            } else {
                System.out.println("[INVALID LISTING ID OR DATE] Try Again.");
            }

        } catch (SQLException e) {
            System.err.println("Error when trying to make cancel booking for date given: " + e.getMessage());
        }
    }
    public void handle_submit_review(Connection connection, Scanner scanner, User current_user){
        int SIN_receiver = 0;
        String receiver_role;
        String comments;
        int listings_scale = 0;
        int host_scale = 0;
        int renter_scale = 0;
        int LID;
        try {
            if(current_user.getRole().equals("host")){
                if(!view_renters(connection, current_user)){return;}
                receiver_role = "renter";
                System.out.println("Enter the USER ID of the renter that you would like to rate and comment.");
                SIN_receiver = Integer.parseInt(scanner.nextLine());
                System.out.println("Enter the LISTING ID of the listing that was rented.");
                LID = Integer.parseInt(scanner.nextLine());
                System.out.println("For this renter, what would you rate them based on their cleanliness, communication, and value as a renter.\n[NOTE] Accepted Rating: 1, 2, 3, 4, 5");
                renter_scale = Integer.parseInt(scanner.nextLine());
                System.out.println("For this renter, please detail any comments about your experience with them.");
                comments = scanner.nextLine();
            } else {
                if(!view_hosts(connection, current_user)){return;}
                receiver_role = "host";
                System.out.println("Enter the USER ID of the host that you would like to rate and comment.");
                SIN_receiver = Integer.parseInt(scanner.nextLine());
                System.out.println("Enter the LISTING ID of the listing that you stayed at.");
                LID = Integer.parseInt(scanner.nextLine());
                System.out.println("For this host, what would you rate them based on their cleanliness, communication, and value as a host.\n[NOTE] Accepted Rating: 1, 2, 3, 4, 5");
                host_scale = Integer.parseInt(scanner.nextLine());
                System.out.println("For this host, what would you rate your stay at their listing(s).\n[NOTE] Accepted Rating: 1, 2, 3, 4, 5");
                listings_scale = Integer.parseInt(scanner.nextLine());
                System.out.println("For this host, please detail any comments about your experience with them.");
                comments = scanner.nextLine();
            }
        } catch(NumberFormatException e){
            System.err.println("[INVALID INPUT]: " + e.getMessage());
            return;
        }

        System.out.println("\nSUBMITTING REVIEW ...               \n");
        try {
            //Execute a query
            String sql = "INSERT INTO Ratings VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, false)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, current_user.getSIN());
            pstmt.setString(2, current_user.getRole());
            pstmt.setInt(3, SIN_receiver);
            pstmt.setString(4, receiver_role);
            pstmt.setInt(5, LID);
            pstmt.setString(6, comments);
            pstmt.setInt(7, listings_scale);
            pstmt.setInt(8, host_scale);
            pstmt.setInt(9, renter_scale);
            int rowsAffected = pstmt.executeUpdate();
            if(rowsAffected > 0){
                System.out.println("\nReview Submitted!\n");
            } else {
                System.out.println("\n[INVALID USER ID OR RATING] Review failed to submit.\n");
            }

        } catch(SQLException e){
            System.out.println("\n[ERROR IN SUBMITTING REVIEW]: ");
            if(e.getMessage().contains("Duplicate entry")){
                System.out.println("You have already rated USER ID: " + SIN_receiver + ". You cannot rate them again.\n");
                return;
            }
            System.out.println(e.getMessage() + "\n");
        }
    }
    public boolean view_renters(Connection connection, User current_user){
        System.out.println("\n| -------------------------- YOUR RENTERS  -------------------------- |");

        try {
            String sql = "SELECT DISTINCT LID FROM Records WHERE role='host' AND SIN=? AND is_booked=true;";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, current_user.getSIN());

            ResultSet rs = pstmt.executeQuery();

            boolean renters_found = rs.next();

            if(!renters_found){
                System.out.println("\n  ------------------------ NO RENTERS FOUND -------------------------  ");
                System.out.println("\n| ------------------------------------------------------------------- |\n");
                return false;
            }

            while(renters_found){
                int LID = rs.getInt("LID");
                System.out.println("LISTING ID: " + LID);
                sql = "SELECT DISTINCT U.* FROM Records R NATURAL JOIN Users U WHERE R.role='renter' AND R.LID=? AND R.is_booked=true;";
                pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, LID);

                ResultSet rs_renters = pstmt.executeQuery();
                while(rs_renters.next()){
                    int SIN = rs_renters.getInt("SIN");
                    String first_name = rs_renters.getString("first_name");
                    String last_name = rs_renters.getString("last_name");

                    System.out.println("USER ID: " + SIN);
                    System.out.println("FIRST NAME: " + first_name);
                    System.out.println("LAST NAME: " + last_name);
                    System.out.println("* --------------------- *");
                }
                renters_found = rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Error when trying to find renters for listing: " + e.getMessage());
        }
        return true;
    }
    public boolean view_hosts(Connection connection, User current_user){
        System.out.println("\n| -------------------------- YOUR HOSTS  -------------------------- |");
        try {
            String sql = "SELECT DISTINCT LID FROM Records WHERE role='renter' AND SIN=? AND is_booked=true;";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, current_user.getSIN());

            ResultSet rs = pstmt.executeQuery();

            boolean hosts_found = rs.next();

            if(!hosts_found){
                System.out.println("\n  ------------------------ NO HOSTS FOUND ---------------------------  ");
                System.out.println("\n| ----------------------------------------------------------------- |\n");
                return false;
            }

            while(hosts_found){
                int LID = rs.getInt("LID");
                sql = "SELECT DISTINCT U.* FROM Records R NATURAL JOIN Users U WHERE R.role='host' AND R.LID=? AND R.is_booked=true;";
                pstmt = connection.prepareStatement(sql);
                pstmt.setInt(1, LID);

                ResultSet rs_renters = pstmt.executeQuery();
                System.out.println("LISTING ID: " + LID);
                while(rs_renters.next()){
                    int SIN = rs_renters.getInt("SIN");
                    String first_name = rs_renters.getString("first_name");
                    String last_name = rs_renters.getString("last_name");

                    System.out.println("USER ID: " + SIN);
                    System.out.println("FIRST NAME: " + first_name);
                    System.out.println("LAST NAME: " + last_name);
                    System.out.println("* --------------------- *");
                }
                hosts_found = rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error when trying to find hosts for listing: " + e.getMessage());
        }
        return true;
    }
    public boolean view_commenters(Connection connection, User current_user){
        System.out.println("\n| -------------------------- COMMENT SECTION -------------------------- |");
        try {
            String sql = "SELECT DISTINCT R.LID, R.comments, U_sender.first_name AS sender_first_name, U_sender.last_name AS sender_last_name, U_sender.SIN AS sender_SIN, U_sender.role AS sender_role\n" +
                    "FROM Ratings R\n" +
                    "JOIN Users U_sender ON R.SIN_writer = U_sender.SIN\n" +
                    "JOIN Users U_receiver ON R.SIN_receiver = U_receiver.SIN\n" +
                    "WHERE U_receiver.role = ? AND R.is_reply = false;\n";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, current_user.getRole());

            ResultSet rs = pstmt.executeQuery();

            boolean comments_found = rs.next();

            if(!comments_found){
                System.out.println("\n  -------------------------- NO COMMENTS FOUND ------------------------  ");
                System.out.println("\n| --------------------------------------------------------------------- |\n");
                return false;
            }

            while(comments_found){
                int LID = rs.getInt("LID");
                int SIN = rs.getInt("sender_SIN");
                String role = rs.getString("sender_role");
                String first_name = rs.getString("sender_first_name");
                String last_name = rs.getString("sender_last_name");
                String comments = rs.getString("R.comments");

                System.out.println("LISTING ID: "+ LID);
                System.out.println("USER ID: " + SIN);
                System.out.println("ROLE: " + role);
                System.out.println("FIRST NAME: " + first_name);
                System.out.println("LAST NAME: " + last_name);
                System.out.println("COMMENTS: " + comments);
                System.out.println("* --------------------------------------------------------- *");

                comments_found = rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error when trying to find commenters for user: " + e.getMessage());
        }
        System.out.println("\n");
        return true;
    }
    public void handle_reply_comment(Connection connection, Scanner scanner, User current_user){
        int SIN_reply;
        String role_reply;
        String reply;
        int LID;

        if(!view_commenters(connection, current_user)){return;}
        try{
            System.out.println("Enter the USER ID of the user that you would like to reply back to.");
            SIN_reply = Integer.parseInt(scanner.nextLine());
            System.out.println("Is the user a renter or a host?");
            role_reply = scanner.nextLine();
            System.out.println("What is the LISTING ID that is associated to the user?");
            LID = Integer.parseInt(scanner.nextLine());
            if(!role_reply.equals("renter") && !role_reply.equals("host")){
                System.out.println("Please only type renter or host.");
                return;
            }
            System.out.println("Reply Back With:");
            reply = scanner.nextLine();
        } catch(NumberFormatException e){
            System.err.println("[INVALID INPUT]: " + e.getMessage());
            return;
        }

        System.out.println("\nREPLYING BACK ...               \n");
        try {
            //Execute a query
            String sql = "INSERT INTO Ratings VALUES (?, ?, ?, ?, ?, ?, 0, 0, 0, true)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, current_user.getSIN());
            pstmt.setString(2, current_user.getRole());
            pstmt.setInt(3, SIN_reply);
            pstmt.setString(4, role_reply);
            pstmt.setInt(5, LID);
            pstmt.setString(6, reply);
            int rowsAffected = pstmt.executeUpdate();
            if(rowsAffected > 0){
                System.out.println("\nReview Submitted!\n");
            } else {
                System.out.println("\n[INVALID USER ID OR COMMENT] Failed to submit reply.\n");
            }
        } catch(SQLException e){
            System.out.println("[ERROR IN SUBMITTING REVIEW]: " + e.getMessage());
            if(e.getMessage().contains("Duplicate entry")){
                System.out.println("You have already replied to USER ID: " + SIN_reply + ". You cannot reply to them again.\n");
            }
        }
    }
}

