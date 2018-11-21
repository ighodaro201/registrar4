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
            rows.add("<td> <a  target=\"_blank\" href=\"regdetails?classid=" + c.getClassID() + "\">" + c.getClassID() + "</td> <td>" + c.getDept() + 
            "</td> <td>" + c.getCourseNum() + "</td> <td>" + c.getArea() + "</td> <td>" + c.getTitle() + "</td>");
        }

        Map<String, Object> model = new HashMap<>();
        model.put("rows", rows);
		ModelAndView mv = new ModelAndView(model, "index.vtl");
        return new VelocityTemplateEngine().render(mv); 
   }

   private static String search(Request req, Response res)
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
            rows.add("<td> <a  target=\"_blank\" href=\"regdetails?classid=" + c.getClassID() + "\">" + c.getClassID() + "</td> <td>" + c.getDept() + 
            "</td> <td>" + c.getCourseNum() + "</td> <td>" + c.getArea() + "</td> <td>" + c.getTitle() + "</td>");
        }
        
        Map<String, Object> model = new HashMap<>();
        model.put("rows", rows);
		ModelAndView mv = new ModelAndView(model, "search.vtl");
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

        ArrayList<String> classes = new ArrayList<String>();
        for (int i = 0; i < info.getDept().length; i++)
        {
            String s = " <b>Dept and Number: </b>";
            s += info.getDept()[i] + " " + info.getCourseNum()[i] + "<br>";
            classes.add(s);
        }
        
        String profs = "<b>Professor(s): </b>";
        for (int i = 0; i < info.getProfNames().length; i++)
        {
            if (i == 0) {}
            else if (i < info.getProfNames().length - 1) profs += ", ";
            else profs += " and ";
            profs += info.getProfNames()[i];
        }
        ArrayList<String> infoArray = new ArrayList<String>();
        infoArray.add(info.getClassID());
        infoArray.add(info.getCourseID());
        infoArray.add(info.getDays());
        infoArray.add(info.getStartTime());
        infoArray.add(info.getEndTime());
        infoArray.add(info.getBldg());
        infoArray.add(info.getRoomNum());
        infoArray.add(info.getArea());
        infoArray.add(info.getTitle());
        infoArray.add(info.getPrereqs());
        infoArray.add(info.getDescrip());

        Map<String, Object> model = new HashMap<>();
        model.put("infoArray", infoArray);
        model.put("profs", profs);
        model.put("classes", classes);
		ModelAndView mv = new ModelAndView(model, "coursedetails.vtl");
        return new VelocityTemplateEngine().render(mv); 
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

      Spark.get("/search",
         (req, res) -> search(req, res)
      );
   }
}