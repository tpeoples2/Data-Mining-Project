import java.sql.*;
import java.util.Scanner;

class Driver {
    public static void main(String args[]) throws SQLException {
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:hr/hr@oracle1.<domain>:<port>:orcl",
                                                      "<username>", "<password>");

        Statement stmt = conn.createStatement();

        task_one(conn, stmt);
        task_two(conn, stmt);

        conn.close();
    }

    public static void task_one(Connection conn, Statement stmt) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("At what support do you want to mine frequent itemsets? ");
        int support = scanner.nextInt();    // 20 of the items are not in the transactions, should they be outputted when the support is 0.00%?

        String query = "SELECT ITEMNAME FROM ITEMS WHERE ITEMID IN (SELECT ITEMID FROM TRANS GROUP BY ITEMID " + 
                       "HAVING COUNT(*) >= ((SELECT COUNT(DISTINCT TRANSID) FROM TRANS) / 100) * " + support + ")";
        ResultSet items_rset = stmt.executeQuery(query);

        System.out.println("\n    ------------------------------------------------------------");
        System.out.println("\tThe following items appear in at least " + support + "% of \n\tthe database transactions:\n");
        if (!items_rset.next()) {
            System.out.println("\tNo items found.");
        }
        else {
            System.out.println("\t\t" + items_rset.getString("ITEMNAME"));
            while (items_rset.next()) {
                System.out.println("\t\t" + items_rset.getString("ITEMNAME"));
            }
        }
        System.out.println("    -------------------------------------------------------------\n");
    }

    // TODO
    public static void task_two(Connection conn, Statement stmt) throws SQLException {
        
    }
}
