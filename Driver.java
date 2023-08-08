package mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class Driver {
    private static final String dbClassName = "com.mysql.cj.jdbc.Driver";
    private static final String CONNECTION = "jdbc:mysql://127.0.0.1/cscc43_mybnb";

    public static Connection logging_handler(int signal, Connection conn) throws ClassNotFoundException {
        //Register JDBC driver
        Class.forName(dbClassName);

        //Database credentials
        final String USER = "root";
        final String PASS = "Iamagerbie123*";

        try {
            if(signal == 1) {
                //Establish connection
                System.out.println("\n\n[[ Connecting to database... ]]");
                conn = DriverManager.getConnection(CONNECTION, USER, PASS);
                System.out.println("[[ Successfully connected to MySQL! ]]\n");
                return conn;
            }
            if(signal == 0){
                System.out.println("[[ Closing connection... ]]\n\n");
                conn.close();
            }
            if(signal != 0 && signal != 1){
                System.err.println("Wrong signal given! 1 for Sign In and 0 for Sign Out!");
            }

        } catch (SQLException e) {
            System.err.println("Connection error occurred! OR Tried to sign out but NOT logged into database!");
        }
        return null;
    }

    public static void main(String [] args) throws ClassNotFoundException {
        Scanner scanner = new Scanner(System.in);
        String input;
        Connection connection = null;
        RHDBC mybnb_db = new RHDBC();
        QDBC queries_db = new QDBC();


        //connecting to database
        connection = logging_handler(1, connection);
        System.out.println("---------------------------------------------------------");
        System.out.println("******************* WELCOME TO MYBNB *******************");
        System.out.println("---------------------------------------------------------\n");

        while(true){
            System.out.println("What would you like to do? Enter the appropriate number. \n[test]: Show Users.\n[q]: Quit the System\n[1]: Create New User Profile\n[2]: Delete User Profile\n[3]: Log In\n[4]: Run Queries\n");
            input = scanner.nextLine();

            //creating new user profile
            if(input.equals("test")){
                mybnb_db.get_users(connection);
            }
            //quiting the system/signing out
            else if(input.equals("q")){
                connection = logging_handler(0, connection);
                if(connection == null){
                    System.out.println("SHUTTING DOWN MYBNB.\n\nHave a nice day! :)");
                    break;
                }
                else{
                    System.err.println("Connection could not be closed.");
                    break;
                }
            }
            //creating new user profile
            else if(input.equals("1")){
                System.out.println("--------------------------\nCREATE NEW USER PROFILE:\n--------------------------");
                mybnb_db.handle_create_user(connection, scanner);
            }
            //deleting user profile
            else if(input.equals("2")){
                System.out.println("--------------------------\nDELETE USER PROFILE:\n--------------------------");
                mybnb_db.handle_delete_user(connection, scanner);
            }
            //signing in as a renter or host
            else if(input.equals("3")){
                System.out.println("--------------------------\nLOG IN:\n--------------------------");
                User current_user = mybnb_db.handle_sign_in_user(connection, scanner);
                if(current_user != null){
                    if(current_user.getRole().equals("renter")){
                        mybnb_db.handle_renter_listings(connection, scanner, current_user);
                    } else if(current_user.getRole().equals("host")){
                        mybnb_db.handle_host_listings(connection, scanner, current_user);
                    }
                }
                //do nothing if role is null (i.e. user did not successfully sign in)
            }
            //TODO: run queries
            else if(input.equals("4")){
                System.out.println("--------------------------\nQUERY OPTIONS:\n--------------------------");
                queries_db.handle_query_options(connection, scanner);
            }
            else {
                System.out.println("[INVALID OPTION] Try Again.\n");
            }
        }
        scanner.close();
    }
}
