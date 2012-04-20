// TODO: deal with user input errors
// TODO: task 4
// TODO: deal with sanitation (last)

import java.sql.*;
import java.util.Scanner;

class Driver {
    public static void main(String args[]) throws SQLException {
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

        String username = args[0];
        String password = args[1];

        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:hr/hr@oracle1.cise.ufl.edu:1521:orcl",
                                                      username, password);

        Statement stmt = conn.createStatement();
        
        try {
            dropCandidatesTable(conn, stmt);
        } catch (SQLException ex) {
            // continue
        }
        try {
            dropLargeSetTable(conn, stmt);
        } catch (SQLException ex) {
            // continue
        }
        try {
            dropTempTable(conn, stmt);
        } catch (SQLException ex) {
            // continue
        }
        
        try {
            createCandidatesTable(conn, stmt);
        } catch (SQLException ex) {
            // continue
        }
        try {
            createLargeSetTable(conn, stmt);
        } catch (SQLException ex) {
            // continue
        }
        try {
            createTempTable(conn, stmt);
        } catch (SQLException ex) {
            // continue
        }

        int selection;
        do {
            selection = promptUser();
            switch (selection) {
                case 1: task_one(conn, stmt);
                        break;
                case 2: task_two(conn, stmt);
                        break;
                case 3: task_three(conn, stmt);
                        break;
                case 4: task_four(conn, stmt);
                        break;
                case 5: break;
            }
        } while (selection != 0);

        conn.close();
    }

    static void task_one(Connection conn, Statement stmt) throws SQLException {
        double support = getFrequentISSupportLevel();   

        clearTables(conn, stmt);

        String initializeCandidateSet = "INSERT INTO CANDIDATES (SELECT ROWNUM, ITEMID FROM ITEMS)";
        stmt.executeUpdate(initializeCandidateSet);
        
        CallableStatement initLargeSet = conn.prepareCall("{call InitLargeSet(?)}");
        initLargeSet.setDouble(1, support);
        initLargeSet.executeUpdate();

        String query = "SELECT ITEMNAME FROM LARGESET, ITEMS WHERE LARGESET.ITEMID = ITEMS.ITEMID"; 
        ResultSet items_rset = stmt.executeQuery(query);

        System.out.println("\n    ----------------------------------------------------------------");
        System.out.println("\tThe following sets appear in at least " + support + "% of \n\tthe database transactions:\n");
        if (!items_rset.next()) {
            System.out.println("\tNo items found.");
        }
        else {
            System.out.println("\t\t{ " + items_rset.getString("ITEMNAME") + " }");
            while (items_rset.next()) {
                System.out.println("\t\t{ " + items_rset.getString("ITEMNAME") + " }");
            }
        }
        System.out.println("    -----------------------------------------------------------------\n");
    }

    static void task_two(Connection conn, Statement stmt) throws SQLException {
        double support = getFrequentISSupportLevel();   
       
        clearTables(conn, stmt);

        String initializeCandidateSet = "INSERT INTO CANDIDATES (SELECT ROWNUM, ITEMID FROM ITEMS)";
        stmt.executeUpdate(initializeCandidateSet);
        
        CallableStatement initLargeSet = conn.prepareCall("{call InitLargeSet(?)}");
        initLargeSet.setDouble(1, support);
        initLargeSet.executeUpdate();

        CallableStatement createCandidateSet = conn.prepareCall("{call CreateCandidateSet(?)}");
        createCandidateSet.setInt(1, 2);
        createCandidateSet.executeUpdate();

        CallableStatement trimCandidateSet = conn.prepareCall("{call TrimCandidateSet(?)}");
        trimCandidateSet.setInt(1, 2 - 1);
        trimCandidateSet.executeUpdate();

        CallableStatement filterIntoLargeSet = conn.prepareCall("{call FilterIntoLargeSet(?)}");
        filterIntoLargeSet.setDouble(1, support);
        filterIntoLargeSet.executeUpdate();

        String num_of_sets_query = "SELECT COUNT(DISTINCT SETID) AS COUNT FROM LARGESET";
        ResultSet count_rset = stmt.executeQuery(num_of_sets_query);
        count_rset.next();
        int num_of_sets = count_rset.getInt("COUNT");

        System.out.println("\n    ----------------------------------------------------------------");
        System.out.println("\tThe following sets appear in at least " + support + "% of \n\tthe database transactions:\n");

        String query = "";
        ResultSet items_rset;

        if (num_of_sets == 0) {
            System.out.println("\tNo items found.");
        }
        
        for (int i = 1; i < num_of_sets; i++) {
            query = "SELECT ITEMNAME FROM LARGESET, ITEMS WHERE LARGESET.ITEMID = ITEMS.ITEMID AND SETID = " + i; 
            items_rset = stmt.executeQuery(query);
            
            items_rset.next();
            System.out.print("\t\t{ " + items_rset.getString("ITEMNAME"));
            while (items_rset.next()) {
                System.out.print(", " + items_rset.getString("ITEMNAME"));
            }
            System.out.print(" }\n");
        }
            
        System.out.println("    -----------------------------------------------------------------\n");
    }

    static void task_three(Connection conn, Statement stmt) throws SQLException {
        double support = getFrequentISSupportLevel();   
        int max_size = getMaxSize();
       
        clearTables(conn, stmt);

        String initializeCandidateSet = "INSERT INTO CANDIDATES (SELECT ROWNUM, ITEMID FROM ITEMS)";
        stmt.executeUpdate(initializeCandidateSet);
        
        CallableStatement initLargeSet = conn.prepareCall("{call InitLargeSet(?)}");
        initLargeSet.setDouble(1, support);
        initLargeSet.executeUpdate();
        
        CallableStatement createCandidateSet = conn.prepareCall("{call CreateCandidateSet(?)}");
        CallableStatement trimCandidateSet = conn.prepareCall("{call TrimCandidateSet(?)}");
        CallableStatement filterIntoLargeSet = conn.prepareCall("{call FilterIntoLargeSet(?)}");

        for (int i = 2; i <= max_size; i++) {
            createCandidateSet.setInt(1, i);
            createCandidateSet.executeUpdate();

            trimCandidateSet.setInt(1, i - 1);
            trimCandidateSet.executeUpdate();

            filterIntoLargeSet.setDouble(1, support);
            filterIntoLargeSet.executeUpdate();
        }

        String num_of_sets_query = "SELECT COUNT(DISTINCT SETID) AS COUNT FROM LARGESET";
        ResultSet count_rset = stmt.executeQuery(num_of_sets_query);
        count_rset.next();
        int num_of_sets = count_rset.getInt("COUNT");
        System.out.println(num_of_sets);

        System.out.println("\n    ----------------------------------------------------------------");
        System.out.println("\tThe following sets appear in at least " + support + "% of \n\tthe database transactions:\n");

        String query = "";
        ResultSet items_rset;

        if (num_of_sets == 0) {
            System.out.println("\tNo items found.");
        }
        
        for (int i = 1; i <= num_of_sets; i++) {
            query = "SELECT ITEMNAME FROM LARGESET, ITEMS WHERE LARGESET.ITEMID = ITEMS.ITEMID AND SETID = " + i; 
            items_rset = stmt.executeQuery(query);
            
            boolean had_atleast_one_result = false;

            if (items_rset.next()) {
                System.out.print("\t\t{ " + items_rset.getString("ITEMNAME"));
                had_atleast_one_result = true;
            }
            while (items_rset.next()) {
                System.out.print(", " + items_rset.getString("ITEMNAME"));
            }
            if (had_atleast_one_result) {
                System.out.print(" }\n");
            }
        }
            
        System.out.println("    -----------------------------------------------------------------\n");
    }
    
    static void task_four(Connection conn, Statement stmt) throws SQLException {
    
    }

    static void clearTables(Connection conn, Statement stmt) throws SQLException {
        String clearTables = "DELETE FROM CANDIDATES";
        stmt.executeUpdate(clearTables);
        clearTables = "DELETE FROM TEMP";
        stmt.executeUpdate(clearTables);
        clearTables = "DELETE FROM LARGESET";
        stmt.executeUpdate(clearTables);
    }

    static void dropCandidatesTable(Connection conn, Statement stmt) throws SQLException {
        String dropTable = "DROP TABLE CANDIDATES";
        stmt.executeUpdate(dropTable);
    }

    static void dropLargeSetTable(Connection conn, Statement stmt) throws SQLException {
        String dropTable = "DROP TABLE LARGESET";
        stmt.executeUpdate(dropTable);
    }

    static void dropTempTable(Connection conn, Statement stmt) throws SQLException {
        String dropTable = "DROP TABLE TEMP";
        stmt.executeUpdate(dropTable);
    }

    static void createCandidatesTable(Connection conn, Statement stmt) throws SQLException {
        String createCandidatesTable = "CREATE TABLE CANDIDATES (\n" + 
                                            "SETID NUMBER,\n" + 
                                            "ITEMID NUMBER," +
                                            "PRIMARY KEY(SETID, ITEMID))";
        stmt.executeUpdate(createCandidatesTable);
    }

    static void createLargeSetTable(Connection conn, Statement stmt) throws SQLException {
        String createLargeSetTable = "CREATE TABLE LARGESET (\n" + 
                                            "SETID NUMBER,\n" + 
                                            "ITEMID NUMBER," +
                                            "PRIMARY KEY(SETID, ITEMID))";
        stmt.executeUpdate(createLargeSetTable);
    }

    static void createTempTable(Connection conn, Statement stmt) throws SQLException {
        String createTempTable = "CREATE TABLE TEMP (\n" + 
                                            "ITEMID NUMBER,\n" + 
                                            "PRIMARY KEY(ITEMID))";
        stmt.executeUpdate(createTempTable);
    }

    static int promptUser() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n\n\t---------------------------------");
        System.out.println("\t| MENU\t\t\t\t|");
        System.out.println("\t|\t(1) Task 1\t\t|");
        System.out.println("\t|\t(2) Task 2\t\t|");
        System.out.println("\t|\t(3) Task 3\t\t|");
        System.out.println("\t|\t(4) Task 4\t\t|");
        System.out.println("\t|\t(0) Quit\t\t|");
        System.out.println("\t---------------------------------\n");

        System.out.print("Enter your selection: ");
        int selection = scanner.nextInt();
        return selection;
    }

    static int getMaxSize() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("What do you want the max size of a frequent itemset to be? ");
        int max_size = scanner.nextInt();
        return max_size;
    }

    static double getFrequentISSupportLevel() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("At what support do you want to mine frequent itemsets? ");
        double support = scanner.nextDouble();
        return support;
    }
}
