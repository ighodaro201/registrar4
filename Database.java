//----------------------------------------------------------------------
// Database.java
// Author: Osita Ighodaro Ben Musoke-Lubega
//----------------------------------------------------------------------

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;

public class Database
{
   private static final String DATABASE_NAME = "reg.sqlite";
   private Connection connection;

   // Creates new Database object
   public Database() {}

   // Connects to database
   public void connect() throws Exception
   {
      File databaseFile = new File(DATABASE_NAME);
      if (! databaseFile.isFile())
         throw new Exception("Database connection failed");
      connection =
         DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME);
   }

   //Disconnects from database
   public void disconnect() throws Exception
   {
      connection.close();
   }

   // Returns an ArrayList of all courses that match query info
   public ArrayList<CourseBasic> searchBasic(String[] inputs) throws Exception
   {
        ArrayList<CourseBasic> results = new ArrayList<CourseBasic>();

        // Identifies search queries
        String whereString = new String();
       
        // Read in queries
        String value = "";

        // Deals with wildcard literals and null entries in queries
        for (int i = 0; i < inputs.length; i++)
        {
            inputs[i] = inputs[i].replace("%", "\\%");
            inputs[i] = inputs[i].replace("_", "\\_");
            if (inputs[i] == null) inputs[i] = "";
        }

        value = "\"%" + inputs[0].toUpperCase() + "%\"";
        whereString += " AND dept LIKE " + value + " ESCAPE \"\\\"";
    
        value = "\"%" + inputs[1].toUpperCase() + "%\"";
        whereString += " AND coursenum LIKE " + value + " ESCAPE \"\\\"";
    
        value = "\"%" + inputs[2].toUpperCase() + "%\"";
        whereString += " AND area LIKE " + value + " ESCAPE \"\\\"";
    
        value = "\"%" + inputs[3].toUpperCase() + "%\"";
        whereString += " AND title LIKE " + value + " ESCAPE \"\\\"";
       
        try
        {
            String stmtStr = "SELECT classid, dept, coursenum, area, title  " + 
            "FROM courses, classes, crosslistings " +
            "WHERE courses.courseid = crosslistings.courseid " +
            "AND classes.courseid = courses.courseid " +
            "AND classes.courseid = crosslistings.courseid " +
            whereString.toString() +
            " ORDER BY dept, coursenum, classid;";

            PreparedStatement statement = connection.prepareStatement(stmtStr);

            ResultSet resultSet = statement.executeQuery();
            

            while (resultSet.next())
            {
                CourseBasic course;
                String classid = resultSet.getString("classid");
                String dept = resultSet.getString("dept");
                String coursenum = resultSet.getString("coursenum");
                String area = resultSet.getString("area");
                String title = resultSet.getString("title");

                course = new CourseBasic(dept, coursenum, area, title, classid);
                results.add(course);
            }

            return results;
        }
        catch (Exception e) 
        { 
            System.err.println(e);
        }
   
        return results;
   }

   // Returns CourseInfo on a specific class
   public CourseInfo searchDetails(String classid) throws Exception
   {
        try
        {
            CourseInfo output;
            ArrayList<String> deptList = new ArrayList<String>();
            ArrayList<String> courseNumList = new ArrayList<String>();
            ArrayList<String> profNameList = new ArrayList<String>();

            if (!Pattern.matches("[0-9]*", classid))
            {
                throw new Exception("regdetails: classid is not an integer");
            }

            if (classid.length() == 0) throw new Exception("regdetails: missing classid");

            String stmtStr = "SELECT courses.courseid, days, starttime, endtime, bldg, roomnum, area, title, " +
                "descrip, prereqs " + 
                "FROM courses, classes " +
                "WHERE classes.courseid = courses.courseid " +
                "AND classid = ?";

            String stmtStr2 = "SELECT dept, coursenum FROM crosslistings, classes " + 
                "WHERE crosslistings.courseid = classes.courseid " +
                "AND classid = ? ORDER BY dept, coursenum";

            String stmtStr3 = "SELECT profname FROM profs, coursesprofs, classes " + 
                "WHERE coursesprofs.courseid = classes.courseid " +
                "AND coursesprofs.profid = profs.profid AND classid = ? " + 
                "ORDER BY profname";

            // Create a prepared statement and substitute values.
            PreparedStatement statement = 
                connection.prepareStatement(stmtStr);
            statement.setString(1, classid);

            PreparedStatement statement2 = 
                connection.prepareStatement(stmtStr2);
            statement2.setString(1, classid);

            PreparedStatement statement3 = 
                connection.prepareStatement(stmtStr3);
            statement3.setString(1, classid);

            ResultSet resultSet = statement.executeQuery();
            ResultSet resultSet2 = statement2.executeQuery();
            ResultSet resultSet3 = statement3.executeQuery();

            if (resultSet.isClosed())
            {
                throw new Exception("regdetails: classid does not exist");
            }

            String courseID = resultSet.getString("courseid");
            String days = resultSet.getString("days");
            String startTime = resultSet.getString("starttime");
            String endTime = resultSet.getString("endtime");
            String bldg = resultSet.getString("bldg");
            String roomNum = resultSet.getString("roomnum");
            String area = resultSet.getString("area");
            String title = resultSet.getString("title");
            String descrip = resultSet.getString("descrip");
            String prereqs = resultSet.getString("prereqs");
            int count = 0;
            String[] dept;
            String[] courseNum;
            String[] profName;
            

            
            while(resultSet2.next())
            {
                deptList.add(resultSet2.getString("dept"));
                courseNumList.add(resultSet2.getString("coursenum"));
            }
            
            while(resultSet3.next())
            {
                profNameList.add(resultSet3.getString("profname"));
            }

            dept = new String[deptList.size()];
            
            for (String s: deptList)
            {
                dept[count] = s;
                count++;
            }

            count = 0;

            courseNum = new String[courseNumList.size()];

            for (String s: courseNumList)
            {
                courseNum[count] = s;
                count++;
            }

            count = 0;

            profName = new String[profNameList.size()];

            for (String s: profNameList)
            {
                profName[count] = s;
                count++;
            }

            count = 0;

            output = new CourseInfo(classid, courseID, days, startTime, endTime,
            bldg, roomNum, area, title, descrip, prereqs, dept, courseNum, profName);

            return output;
        }
        catch (Exception e) 
        {
            System.err.println(e);
        }
        return null;
   }

   // For testing:
   public static void main(String[] args) throws Exception
   {
        String[] inputs = {"EAS", "2", "", ""};
        Database database = new Database();
        database.connect();
        CourseInfo test = database.searchDetails("9032");
        ArrayList<CourseBasic> test2 = database.searchBasic(inputs);
        
        for (CourseBasic course: test2)
        {
            System.out.println(course);
        }

        System.out.println(test);
        database.disconnect();
   }
}