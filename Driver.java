// TODO: deal with user input errors
// TODO: deal with sanitation (last)

import java.sql.*;
import java.util.Scanner;

class Driver {
    static Connection conn;
    static Statement stmt;

    public static void main(String args[]) throws SQLException {
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

        String username = args[0];
        String password = args[1];

        conn = DriverManager.getConnection("jdbc:oracle:thin:hr/hr@oracle1.cise.ufl.edu:1521:orcl",
                                                      username, password);

        stmt = conn.createStatement();
       
        initializeTables();
        
        int selection;
        do {
            selection = promptUserWithMenu();
            switch (selection) {
                case 1: task_one();
                        break;
                case 2: task_two();
                        break;
                case 3: task_three();
                        break;
                case 4: task_four();
                        break;
                case 5: break;
            }
        } while (selection != 0);

        conn.close();
    }

    static void task_one() throws SQLException {
        double support = getFrequentISSupportLevel();   
        
        createFrequentItems(support, 1);
        printFrequentItems(support);
    }

    static void task_two() throws SQLException {
        double support = getFrequentISSupportLevel();   

        createFrequentItems(support, 2);
        printFrequentItems(support);
    }

    static void task_three() throws SQLException {
        double support = getFrequentISSupportLevel();   
        int max_size = getMaxSize();
       
        createFrequentItems(support, max_size);
        printFrequentItems(support);
    }
    
    static void task_four() throws SQLException {
        double support = getFrequentISSupportLevel();
        int max_size = getMaxSize();
        double confidence = getConfidenceLevel();
        
        createFrequentItems(support, max_size);
        createAssociationRules(support, confidence);
        printAssociationRules(support, confidence);
    }

    static void createFrequentItems(double support, int max_size) throws SQLException {
        String clearCandidateSet = "DELETE FROM CANDIDATES";
        stmt.executeUpdate(clearCandidateSet);

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
    }

    static void createAssociationRules(double support, double confidence) throws SQLException {
        CallableStatement createAR = conn.prepareCall("{call CreateAssociationRuleSet(?, ?)}");
        createAR.setDouble(1, support);
        createAR.setDouble(2, confidence);
        createAR.executeUpdate();
    }

    static void printAssociationRules(double support, double confidence) throws SQLException {
        String num_of_ar_query = "SELECT COUNT(*) AS COUNT FROM ASSOCIATIONRULES";
        ResultSet count_rset = stmt.executeQuery(num_of_ar_query);
        count_rset.next();
        int num_of_associationrules = count_rset.getInt("COUNT");

        System.out.println("\n    ----------------------------------------------------------------");
        System.out.println("\tThe following sets appear in at least " + support + "% of \n\tthe database transactions:\n");
        System.out.println("\tThe following association rules are valid for support level " + support + "% and confidence level " + confidence + "%:\n");

        String ar_query = "";
        ResultSet rules_rset;

        String largeset_query = "";
        ResultSet largeset_rset;
        
        if (num_of_associationrules == 0) {
            System.out.println("\tNo rules found.");
        }
        
        for (int i = 1; i <= num_of_associationrules; i++) {
            ar_query = "SELECT SETID, ITEMNAME, SUPPORT, CONFIDENCE FROM ASSOCIATIONRULES, ITEMS WHERE ITEMS.ITEMID = ASSOCIATIONRULES.ITEMID";
            rules_rset = stmt.executeQuery(ar_query); 

            rules_rset.next();
            int setid = rules_rset.getInt("SETID");
            String itemname = rules_rset.getString("ITEMNAME");
            double actual_support = rules_rset.getDouble("SUPPORT");
            double actual_confidence = rules_rset.getDouble("CONFIDENCE");

            largeset_query = "SELECT ITEMNAME FROM LARGESET, ITEMS WHERE LARGESET.ITEMID = ITEMS.ITEMID AND SETID = " + setid;
            largeset_rset = stmt.executeQuery(largeset_query);

            largeset_rset.next();
            System.out.print("\t\t{ " + largeset_rset.getString("ITEMNAME"));
            while (largeset_rset.next()) {
                System.out.print(", " + largeset_rset.getString("ITEMNAME"));
            }
            System.out.print(" }");

            System.out.print(" -> { " + itemname + " } ... support = " + actual_support + "%, confidence = " + actual_confidence + "%\n");
        }

        System.out.println("    -----------------------------------------------------------------\n");
    }

    static void printFrequentItems(double support) throws SQLException {
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
        
        for (int i = 1; i <= num_of_sets; i++) {
            query = "SELECT ITEMNAME, SUPPORT FROM LARGESET, ITEMS WHERE LARGESET.ITEMID = ITEMS.ITEMID AND SETID = " + i; 
            items_rset = stmt.executeQuery(query);
            
            boolean had_atleast_one_result = false;
            double actual_support = 0.0;

            if (items_rset.next()) {
                System.out.print("\t\t{ " + items_rset.getString("ITEMNAME"));
                had_atleast_one_result = true;
                actual_support = items_rset.getDouble("SUPPORT");
            }
            while (items_rset.next()) {
                System.out.print(", " + items_rset.getString("ITEMNAME"));
            }
            if (had_atleast_one_result) {
                System.out.print(" } ... " + actual_support + "%\n");
            }
        }
            
        System.out.println("    -----------------------------------------------------------------\n");
    }

    static void initializeTables() throws SQLException {
        String dropCandidatesTable = "DROP TABLE CANDIDATES";
        stmt.executeUpdate(dropCandidatesTable);

        String dropLargeSetTable = "DROP TABLE LARGESET";
        stmt.executeUpdate(dropLargeSetTable);
        
        String dropTempTable = "DROP TABLE TEMP";
        stmt.executeUpdate(dropTempTable);

        String dropTemp2Table = "DROP TABLE TEMP_CANDIDATES";
        stmt.executeUpdate(dropTemp2Table);

        String dropTemp3Table = "DROP TABLE TEMP_TRANS";
        stmt.executeUpdate(dropTemp3Table);

        String dropAssociationRulesTable = "DROP TABLE ASSOCIATIONRULES";
        stmt.executeUpdate(dropAssociationRulesTable);

        String createCandidatesTable = "CREATE TABLE CANDIDATES (\n" + 
                                            "SETID NUMBER,\n" + 
                                            "ITEMID NUMBER," +
                                            "PRIMARY KEY(SETID, ITEMID))";
        stmt.executeUpdate(createCandidatesTable);

        String createLargeSetTable = "CREATE TABLE LARGESET (\n" + 
                                            "SETID NUMBER,\n" + 
                                            "ITEMID NUMBER,\n" +
                                            "SUPPORT NUMBER,\n" +
                                            "PRIMARY KEY(SETID, ITEMID))";
        stmt.executeUpdate(createLargeSetTable);

        String createTempTable = "CREATE TABLE TEMP (\n" + 
                                            "ITEMID NUMBER,\n" + 
                                            "PRIMARY KEY(ITEMID))";
        stmt.executeUpdate(createTempTable);

        String createTempCandidates = "CREATE TABLE TEMP_CANDIDATES (\n" + 
                                            "ITEMID NUMBER,\n" + 
                                            "PRIMARY KEY(ITEMID))";
        stmt.executeUpdate(createTempCandidates);

        String createTempTrans = "CREATE TABLE TEMP_TRANS (\n" + 
                                            "TRANSID NUMBER,\n" + 
                                            "PRIMARY KEY(TRANSID))";
        stmt.executeUpdate(createTempTrans);

        String createAssociationRules = "CREATE TABLE ASSOCIATIONRULES (\n" +
                                            "SETID NUMBER,\n" +
                                            "ITEMID NUMBER,\n" +
                                            "SUPPORT NUMBER,\n" +
                                            "CONFIDENCE NUMBER,\n" +
                                            "PRIMARY KEY(SETID, ITEMID))";
        stmt.executeUpdate(createAssociationRules);

    }

    static int promptUserWithMenu() {
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

    static double getConfidenceLevel() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("At what confidence do you want to mine association rules? ");
        double confidence = scanner.nextDouble();
        return confidence;
    }
}
