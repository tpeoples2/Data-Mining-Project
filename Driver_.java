// TODO: deal with user input errors
// TODO: task 2
// TODO: task 3
// TODO: task 4
// TODO: deal with sanitation (last)

import java.sql.*;
import java.util.Scanner;

class Driver {
    public static void main(String args[]) throws SQLException {
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:hr/hr@oracle1.<domain>:<port>:orcl",
                                                      "<username>", "<password>");

        Statement stmt = conn.createStatement();

        createFISetsTable(conn, stmt);

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
        int support = getFrequentISSupportLevel();   

        String query = "SELECT ITEMNAME, PERCENT\n" +  
                       "FROM ITEMS, (SELECT ITEMID, 100 * COUNT(*) / (SELECT COUNT(DISTINCT TRANSID) FROM TRANS) AS PERCENT\n" +
                                     "FROM TRANS\n" + 
                                     "GROUP BY ITEMID) PERCENTS\n" + 
                       "WHERE ITEMS.ITEMID = PERCENTS.ITEMID AND PERCENT >= " + support + "\n" +
                       "ORDER BY PERCENT DESC";
        ResultSet items_rset = stmt.executeQuery(query);

        System.out.println("\n    ----------------------------------------------------------------");
        System.out.println("\tThe following items appear in at least " + support + "% of \n\tthe database transactions:\n");
        if (!items_rset.next()) {
            System.out.println("\tNo items found.");
        }
        else {
            printItemString(items_rset.getString("ITEMNAME"), items_rset.getDouble("PERCENT"));
            while (items_rset.next()) {
                printItemString(items_rset.getString("ITEMNAME"), items_rset.getDouble("PERCENT"));
            }
        }
        System.out.println("    -----------------------------------------------------------------\n");
    }

    static void task_two(Connection conn, Statement stmt) throws SQLException {
        int support = getFrequentISSupportLevel();   
               
        emptyFISetsTable(); 

    }

    static void task_three(Connection conn, Statement stmt) throws SQLException {
    
    }

    static void task_four(Connection conn, Statement stmt) throws SQLException {
    
    }
    
    static void createFISetsTable(Connection conn, Statement stmt) throws SQLException {
        String createFISets = "CREATE TABLE FISets\n" +
                              "(SETID INT,\n" + 
                              "ITEMID INT,\n" +
                              "PRIMARY KEY(SETID, ITEMID))";

        stmt.executeUpdate(createFISets);
    }
    
    static void emptyFISetsTable(Connection conn, Statement stmt) throws SQLException {
        String deletionQuery = "DELETE FROM FISets";
        stmt.executeUpdate(deletionQuery);
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

    static int getFrequentISSupportLevel() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("At what support do you want to mine frequent itemsets? ");
        int support = scanner.nextInt();
        return support;
    }
    static void printItemString(String itemname, double percent) {
        if (itemname.length() > 15) {
            System.out.println("\t\t" + itemname + "\t\t" + percent + "%");
        }
        else if (itemname.length() < 8) {
            System.out.println("\t\t" + itemname + "\t\t\t\t" + percent + "%");
        }
        else {
            System.out.println("\t\t" + itemname + "\t\t\t" + percent + "%");
        } 
    
    }

    
}
