import sun.security.util.Password;

import java.sql.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        //Initial Settings
        Scanner sc = new Scanner(System.in);
        Connection con = null;
        Statement stmt = null;
        PreparedStatement pstmt = null;

        //Strings for DB Connection
        String DBName = "dbsys";
        String UserName = "root";
        String Password = "1234";

        try {
            //DB Connection
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + DBName + "?useUnicode=true&"
                    + "useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", UserName, Password);
            System.out.println("Connected Successfully");

            //Current Account Print
            stmt = con.createStatement();
            ResultSet rset = stmt.executeQuery(
                    "select * from dbsys.account");

            System.out.println("Current Account Table");
            while(rset.next()) {
                System.out.println(rset.getInt("acct_no") + " / " + rset.getInt("balance"));
            }

            //Get input for transfer
            System.out.println("Your Account Number?: ");
            int sendAccount = sc.nextInt();
            System.out.println("Account Number to send?: ");
            int receiveAccount = sc.nextInt();
            System.out.println("How much to send?: ");
            int transfer = sc.nextInt();

            if(transfer <= 0) {
                System.out.println("Input error! transfer > 0");
                System.exit(-1);
            }

            //Transaction Start
            con.setAutoCommit(false);

            //Get balance for calculating to send transfer
            pstmt = con.prepareStatement(
                    "select balance " +
                            "from dbsys.account " +
                            "where acct_no = (?)"
            );
            pstmt.setInt(1, sendAccount);
            rset = pstmt.executeQuery();

            int sendBalance = 0;
            while(rset.next()) {
                 sendBalance = rset.getInt("balance" );
            }

            //Actual calculation for sending
            sendBalance = sendBalance - transfer;

            if(sendBalance < 0) {
                System.out.println("There is not enough balance!");
                System.exit(-2);
            }

            //Update DB
            pstmt = con.prepareStatement(
                    "update dbsys.account " +
                            "set balance = (?) " +
                            "where acct_no = (?)"
            );
            pstmt.setInt(1, sendBalance);
            pstmt.setInt(2, sendAccount);
            pstmt.executeUpdate();

            //Get balance for calculating to receive transfer
            pstmt = con.prepareStatement(
                    "select balance " +
                            "from dbsys.account " +
                            "where acct_no = (?)"
            );
            pstmt.setInt(1, receiveAccount);
            rset = pstmt.executeQuery();

            int receiveBalance = 0;
            while(rset.next()) {
                receiveBalance = rset.getInt("balance" );
            }

            //Actual calculation for receiving
            receiveBalance = receiveBalance + transfer;

            //Update DB
            pstmt = con.prepareStatement(
                    "update dbsys.account " +
                            "set balance = (?) " +
                            "where acct_no = (?)"
            );
            pstmt.setInt(1, receiveBalance);
            pstmt.setInt(2, receiveAccount);
            pstmt.executeUpdate();

            //Transaction Finish
            con.commit();

            //Current Account Print
            stmt = con.createStatement();
            rset = stmt.executeQuery(
                    "select * from dbsys.account");

            System.out.println("Current Account Table");
            while(rset.next()) {
                System.out.println(rset.getInt("acct_no") + " / " + rset.getInt("balance"));
            }

        } catch (SQLException sqle) {
            System.out.println("Transfer error!: " + sqle);
            System.out.println("Execute rollback!");
            if(con!=null) {
                try {
                    con.rollback();
                } catch(SQLException transaction) {
                    System.out.println("Rollback error!: " + transaction);
                }
            }
        }

    }
}