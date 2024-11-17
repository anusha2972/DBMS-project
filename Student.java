import java.io.*;
import java.sql.*;
import java.util.*;
import oracle.jdbc.driver.*;
import org.apache.ibatis.jdbc.ScriptRunner;

public class Student{
    static Connection con;
    static Statement stmt;

    public static void main(String[] args) {
        connectToDatabase();
        while (true) {
            getMenuOptions();
        }
    }

    public static void getMenuOptions() {
        Scanner sc = new Scanner(System.in);
        String[] menuOptions = {
                "View table contents",
                "Search by PUBLICATIONID",
                "Search by one or more attributes",
                "Exit"
        };

        for (int i = 0; i < menuOptions.length; i++) {
            System.out.println((i + 1) + ". " + menuOptions[i]);
        }
        System.out.print("Enter menu option: ");
        int option;
        option = sc.nextInt();
        switch(option)
        {
            case 1:
                viewTableContents();
                break;
            case 2:
                searchByID();
                break;
            case 3:
                searchByAttributes();
                break;
            case 4:
                quit();
                break;
            default:
                System.out.println("Invalid input. Please enter a number between 1 and 4.");
        }

    }

    public static void viewTableContents() {
        Scanner sc = new Scanner(System.in);
        System.out.print("PUBLICATIONS (Yes/ No): ");
        String pubChoice = sc.nextLine().toLowerCase();
        boolean isPublicationsTable = Objects.equals(pubChoice, "yes");
        System.out.print("AUTHORS (Yes/ No): ");
        String authChoice = sc.nextLine().toLowerCase();
        boolean isAuthorsTable = Objects.equals(authChoice, "yes");

        try {
            if(isPublicationsTable) {
                String selectQueryPub = "SELECT * FROM PUBLICATIONS";
                Statement statement = con.createStatement();

                ResultSet resultSet = statement.executeQuery(selectQueryPub);
                while (resultSet.next()) {
                    int id = resultSet.getInt("PUBLICATIONID");
                    String title = resultSet.getString("TITLE");
                    int year = resultSet.getInt("YEAR");
                    String type = resultSet.getString("TYPE");
                    String summary = resultSet.getString("SUMMARY");

                    System.out.println("ID: " + id + " Title: " + title + " Year: " + year + " Type: " + type + " Summary: " + summary);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            if(isAuthorsTable) {
                String selectQueryAuth = "SELECT * FROM AUTHORS";
                Statement statement = con.createStatement();

                ResultSet resultSet = statement.executeQuery(selectQueryAuth);
                while (resultSet.next()) {
                    int id = resultSet.getInt("PUBLICATIONID");
                    String author = resultSet.getString("AUTHOR");

                    System.out.println("ID: " + id + " Author: " + author);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void searchByID() {
        Scanner sc = new Scanner(System.in);
        System.out.print("PUBLICATIONID: ");
        int searchId = sc.nextInt();

        try {
            String selectQueryPub = "SELECT P.PUBLICATIONID AS PUBLICATIONID, P.YEAR AS YEAR, P.TYPE AS TYPE, P.TITLE AS TITLE, P.SUMMARY AS SUMMARY, COUNT(A.AUTHOR) AS AUTHOR_COUNT FROM PUBLICATIONS P JOIN AUTHORS A ON P.PUBLICATIONID = A.PUBLICATIONID WHERE P.PUBLICATIONID = ? GROUP BY P.PUBLICATIONID, P.YEAR, P.TYPE, P.TITLE, P.SUMMARY";
            PreparedStatement pstmt = con.prepareStatement(selectQueryPub);
            pstmt.setInt(1, searchId);
            ResultSet resultSet = pstmt.executeQuery();
            System.out.println(resultSet);
            while (resultSet.next()) {
                int id = resultSet.getInt("PUBLICATIONID");
                String title = resultSet.getString("TITLE");
                int year = resultSet.getInt("YEAR");
                String type = resultSet.getString("TYPE");
                String summary = resultSet.getString("SUMMARY");
                int authorcount = resultSet.getInt("AUTHOR_COUNT");

                System.out.println("ID: " + id + " Title: " + title + " Year: " + year + " Type: " + type + " Author Count: " + authorcount + " Summary: " + summary);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void searchByAttributes() {
        Scanner sc = new Scanner(System.in);
        List<String> conditionsInput = new ArrayList<>();
        List<String> conditionsOutput = new ArrayList<>();
        Map<Integer, String> parameters = new HashMap<>();
        int paramIndex = 1;
        System.out.println("Input fields:");
        System.out.print("AUTHOR: ");
        String author = sc.nextLine();
        if (!author.isEmpty()) {
            conditionsInput.add("A.AUTHOR LIKE ?");
            parameters.put(paramIndex++, "%" + author + "%");
        }
        System.out.print("TITLE: ");
        String title = sc.nextLine();
        if (!title.isEmpty()) {
            conditionsInput.add("P.TITLE LIKE ?");
            parameters.put(paramIndex++, "%" + title + "%");
        }
        System.out.print("YEAR: ");
        String year = sc.nextLine();
        if (!year.isEmpty()) {
            conditionsInput.add("P.YEAR = ?");
            parameters.put(paramIndex++, year);
        }
        System.out.print("TYPE: ");
        String type = sc.nextLine();
        if (!type.isEmpty()) {
            conditionsInput.add("P.TYPE LIKE ?");
            parameters.put(paramIndex++, "%" + type + "%");
        }

        System.out.println("Output fields:");
        System.out.print("PUBLICATIONID (Yes/No):");
        String displayPubIDCol = sc.nextLine().toLowerCase();
        if(Objects.equals(displayPubIDCol, "yes"))
        {
            conditionsOutput.add("P.PUBLICATIONID AS PUBLICATIONID,");
        }
        System.out.print("AUTHOR (Yes/No):");
        String displayAuthCol = sc.nextLine().toLowerCase();
        if(Objects.equals(displayAuthCol, "yes"))
        {
            conditionsOutput.add("A.AUTHOR AS AUTHOR,");
        }
        System.out.print("TITLE (Yes/No):");
        String displayTitleCol = sc.nextLine().toLowerCase();
        if(Objects.equals(displayTitleCol, "yes"))
        {
            conditionsOutput.add("P.TITLE AS TITLE,");
        }
        System.out.print("YEAR (Yes/No):");
        String displayYearCol = sc.nextLine().toLowerCase();
        if(Objects.equals(displayYearCol, "yes"))
        {
            conditionsOutput.add("P.YEAR AS YEAR,");
        }
        System.out.print("TYPE (Yes/No):");
        String displayTypeCol = sc.nextLine().toLowerCase();
        if(Objects.equals(displayTypeCol, "yes"))
        {
            conditionsOutput.add("P.TYPE AS TYPE,");
        }
        System.out.print("SUMMARY (Yes/No):");
        String displaySummaryCol = sc.nextLine().toLowerCase();
        if(Objects.equals(displaySummaryCol, "yes"))
        {
            conditionsOutput.add("P.SUMMARY AS SUMMARY,");
        }
        System.out.println("\nSorted by:");
        String sortByCol = sc.nextLine();
        String query = "";
        if(!conditionsOutput.isEmpty()) {
            query = "SELECT " + String.join(" ", conditionsOutput);
            query = query.substring(0, query.length() - 1);
            query += " FROM PUBLICATIONS P JOIN AUTHORS A ON P.PUBLICATIONID = A.PUBLICATIONID";
        }

        if (!conditionsInput.isEmpty()) {
            query += " WHERE " + String.join(" AND ", conditionsInput);
        }

        if (!sortByCol.isEmpty()) {
            query += " ORDER BY " + (sortByCol.equalsIgnoreCase("AUTHOR") ? "A." : "P.") + sortByCol;
        }


        try {
            PreparedStatement pstmt = con.prepareStatement(query);
            for (Map.Entry<Integer, String> entry : parameters.entrySet()) {
                pstmt.setString(entry.getKey(), entry.getValue());
            }
            System.out.println(query);
            ResultSet resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                for(int i = 0; i < conditionsOutput.size(); i++)
                {
                    String alias = conditionsOutput.get(i).split(" AS ")[1].trim();
                    alias = alias.substring(0, alias.length() - 1);
                    if(alias.equals("PUBLICATIONID") || alias.equals("YEAR"))
                    {
                        System.out.print(alias + ": " + resultSet.getInt(alias) + " ");
                    }
                    else
                    {
                        System.out.print(alias + ": " + resultSet.getString(alias) + " ");
                    }
                }
                System.out.println();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }



    public static void quit() {
        System.out.println("Exiting...");
        System.exit(0);
    }


    public static void connectToDatabase() {
        String driverPrefixURL="jdbc:oracle:thin:@";
	    String jdbc_url="artemis.vsnet.gmu.edu:1521/vse18c.vsnet.gmu.edu";
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();
	
        try{
	    //Register Oracle driver
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        } catch (Exception e) {
            System.out.println("Failed to load JDBC/ODBC driver.");
            return;
        }

       try{
            System.out.println(driverPrefixURL+jdbc_url);
            con=DriverManager.getConnection(driverPrefixURL+jdbc_url, username, password);
            DatabaseMetaData dbmd=con.getMetaData();
            stmt=con.createStatement();

            System.out.println("Connected.");

            if(dbmd==null){
                System.out.println("No database meta data");
            }
            else {
                System.out.println("Database Product Name: "+dbmd.getDatabaseProductName());
                System.out.println("Database Product Version: "+dbmd.getDatabaseProductVersion());
                System.out.println("Database Driver Name: "+dbmd.getDriverName());
                System.out.println("Database Driver Version: "+dbmd.getDriverVersion());
            }
        }catch( Exception e) {
           e.printStackTrace();
           System.exit(0);
       }

//        ScriptRunner sr = new ScriptRunner(con);
//        System.out.print("Enter file location: ");
//        String filename = sc.nextLine();
//        try  {
//            Reader reader = new BufferedReader(new FileReader(filename));
//            sr.runScript(reader);
//            System.out.println("Data loaded successfully");
//        }
//        catch (Exception e) {
//            System.out.println("Failed to load");
//            return;
//        }

    }// End of connectToDatabase()
}// End of class

