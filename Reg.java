//----------------------------------------------------------------------
// Reg.java
// Author: Osita Ighodaro Ben Musoke-Lubega
//----------------------------------------------------------------------

import spark.Spark;
import spark.Request;
import spark.Response;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import spark.template.velocity.VelocityTemplateEngine;
import spark.ModelAndView;

public class Reg
{  
    private static String index(Request req, Response res)
    {             
        String dept = req.queryParams("dept");
        String coursenum = req.queryParams("coursenum");
        String area = req.queryParams("area");
        String title = req.queryParams("title");

        String[] fields = {dept, coursenum, area, title};
        for (int i = 0; i < fields.length; i++)
            if (fields[i] == null) fields[i] = "";
        
        res.cookie("dept", fields[0]);
        res.cookie("coursenum", fields[1]);
        res.cookie("area", fields[2]);
        res.cookie("title", fields[3]);

        ArrayList<CourseBasic> courses = null;
        try
        {
            Database database = new Database();
            database.connect();
            courses = database.searchBasic(fields);
            database.disconnect();
        }
        catch (Exception e)
        {
            // have to do error stuff again
            return e.toString();
        }

        ArrayList<String> rows = new ArrayList<String>();
        for (CourseBasic c: courses)
        {
            rows.add("<td> <a href=\"regdetails?classid=" + c.getClassID() + "\">" + c.getClassID() + "</td> <td>" + c.getDept() + 
            "</td> <td>" + c.getCourseNum() + "</td> <td>" + c.getArea() + "</td> <td>" + c.getTitle() + "</td>");
        }

        Map<String, Object> model = new HashMap<>();
        model.put("rows", rows);
		ModelAndView mv = new ModelAndView(model, "index.vtl");
        return new VelocityTemplateEngine().render(mv); 
   }

   private static String courseDetails(Request req, Response res) throws UnsupportedEncodingException
   {
        String classId = req.queryParams("classid");
        String dept = req.cookie("dept");
        String coursenum = req.cookie("coursenum");
        String area = req.cookie("area");
        String title = req.cookie("title");


        String html = ""; 
        html += "<!DOCTYPE html>";
        html += "<html>";
        html += "<head>";
        html += "<title>Registrar's Office Class Search</title>";
        html += "</head>";
        html += "<body>";
        html += "<h1>Registrar's Office</h1>";

        CourseInfo info = null;
        try
        {
            Database database = new Database();
            database.connect();
            info = database.searchDetails(classId);
            database.disconnect();
        }
        catch (Exception e)
        {
            return e.toString();
        }

        if (info == null)
        {
            html += "<p>";
            if (classId == null || classId == "") html += "Missing class id";
            else if (Pattern.matches("[0-9]*", classId)) html += "Class id " + classId + " does not exist.";
            else html += "Class id is not numeric";
            html += "</p>";
            html += "<hr>";
            html += "<foot>";
            html += "Created by Osita Ighodaro and Ben Musoke-Lubega";
            html += "<hr>";
            html += "</foot>";
            html += "</html>";

            return html;
        }

        html += "<hr>";
        html += "<h2>";
        html += "Class Details (class id " + classId + ")";
        html += "</h2>";

        html += "<b>Course Id: </b>" + info.getCourseID();
        html += "<br>";

        html += "<b>Days: </b>" + info.getDays();
        html += "<br>";

        html += "<b>Start time: </b>" + info.getStartTime();
        html += "<br>";

        html += "<b>End time: </b>" + info.getEndTime();
        html += "<br>";

        html += "<b>Building: </b>" + info.getBldg();
        html += "<br>";

        html += "<b>Room: </b>" + info.getRoomNum();
        html += "<br>";
        html += "<hr>";

        html += "<h2>";
        html += "Course Details (course id " + info.getCourseID() + ")";
        html += "</h2>";

        for (int i = 0; i < info.getDept().length; i++)
        {
            html += "<b>Dept and Number: </b>";
            html += info.getDept()[i] + " " + info.getCourseNum()[i] + "<br>";
        }
        
        html += "<b>Area: </b>" + info.getArea();
        html += "<br>";
        html += "<b>Title: </b>" + info.getTitle();
        html += "<br>";
        html += "<b>Description: </b>" + info.getDescrip();
        html += "<br>";
        html += "<b>Prerequisites: </b>" + info.getPrereqs();
        html += "<br>";
        
        html += "<b>Professor(s): </b>";
        for (int i = 0; i < info.getProfNames().length; i++)
        {
            if (i == 0) {}
            else if (i < info.getProfNames().length - 1) html += ", ";
            else html += " and ";
            html += info.getProfNames()[i];
        }

        html += "<hr>";
        html += "<p>";
        html += "Click here to do ";
        html += "<a href=\"index?dept=" + dept + "&coursenum=" + coursenum + "&area=" + area + "&title=" + title + "\">another class search </a>.";
        html += "</p>";
        html += "<hr>";      
        html += "</body>";

        html += "<foot>";
        html += "Created by Osita Ighodaro and Ben Musoke-Lubega";
        html += "<hr>";
        html += "</foot>";

        html += "</html>";

        return html;

   }
   public static void main(String[] args) 
   {
      if (args.length != 1)
      {
         System.err.println("Usage: java Reg port");
         System.exit(1);
      } 

      Spark.port(Integer.parseInt(args[0]));
      
      Spark.get("/", 
         (req, res) -> index(req, res)
      );
      
      Spark.get("/index",
         (req, res) -> index(req, res)
      );

      Spark.get("/regdetails",
         (req, res) -> courseDetails(req, res)
      );
   }
}