/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.balavignesh.gradebook.services;

import com.balavignesh.gradebook.DB.GradeBookDB;
import com.balavignesh.gradebook.connection.SendRequest;
import com.balavignesh.gradebook.domain.GradeBook;
import com.balavignesh.gradebook.domain.GradeBookList;
import com.balavignesh.gradebook.domain.Server;
import com.balavignesh.gradebook.domain.ServerList;
import com.balavignesh.gradebook.domain.Student;
import com.balavignesh.gradebook.domain.StudentList;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.InternalServerErrorException;


/**
 *
 * @author BalaVignesh
 */
@Path("/")
public class StudentResource {
    
    private GradeBookDB gradeBookDb = new GradeBookDB();
    
    @GET
    @Path("/show")
    public String showStudent() throws Exception
    {
        try{
         SendRequest s = new SendRequest();
         String str = s.SendRequest("http://35.224.65.85:8080/GradeBook/resources/serverdetails", "GET",null);
         System.out.println(str);
         return str;
        }
        
        catch(Exception e){
            return e.getMessage();
        }
        
       
        
    } 
    
    @GET
    @Path("/serverdetails")
    @Produces(MediaType.TEXT_PLAIN+";charset=utf-8")
    public String getServerDetails() throws UnknownHostException, SocketException, IOException{
        gradeBookDb.addDefaults();
        StringBuffer buffer = new StringBuffer();
         buffer.append(" \n getMyHostName:"+gradeBookDb.getMyHostName());
         buffer.append(" \n my external ip:" + gradeBookDb.getMyIP());
        return buffer.toString();
    }
    
    @GET
    @Path("/clearall")
    @Produces(MediaType.TEXT_PLAIN+";charset=utf-8")
    public String clearall() throws UnknownHostException, SocketException, IOException{
        gradeBookDb.clearall();
        StringBuilder buffer = new StringBuilder();
         buffer.append(" Cleared all");
        return buffer.toString();
    }
    
    @GET
    @Path("/getInt")
    @Produces(MediaType.TEXT_PLAIN+";charset=utf-8")
    public String getInt() throws UnknownHostException, SocketException, IOException{
        
        StringBuilder buffer = new StringBuilder();
         buffer.append(gradeBookDb.getAtomicInteger());
        return buffer.toString();
    }
    
    @GET
    @Path("/injectprimary/{name}/{id}")
   @Produces(MediaType.TEXT_XML+";charset=utf-8")
    public GradeBook injectPrimary(@PathParam("name") String name,@PathParam("id") long id) throws UnknownHostException, SocketException, IOException{
        
      GradeBook gradeBook = gradeBookDb.filterGradeBookById(id);
      Server server = gradeBookDb.filterServerByName(name);
      gradeBook.setServer(server);
      return gradeBook;
    }
    
    @GET
    @Path("/injectsecondary/{name}/{id}")
    @Produces(MediaType.TEXT_XML+";charset=utf-8")
    public GradeBook injectsecondary(@PathParam("name") String name,@PathParam("id") long id) throws UnknownHostException, SocketException, IOException{
       GradeBook gradeBook = gradeBookDb.filterGradeBookById(id);
       if(!gradeBookDb.isSecondary(gradeBook)){
           Server server = gradeBookDb.filterServerByName(name);
           gradeBook.getServerList().getServer().add(server);
       }
       return gradeBook;
    }

    @GET
    @Path("/gradebook")
    @Produces(MediaType.TEXT_XML+";charset=utf-8")
    public Response getGradeBookList() throws IOException{
       List<GradeBook> gradeBookLists =gradeBookDb.getGradeBookListOnlyVisible();
       boolean gbsize = gradeBookLists.isEmpty();
        if (gbsize){
        System.out.println("gradebook list is empty");
        throw new NotFoundException(Response.status(Response.Status.NOT_FOUND).entity("<xml>GradeBook List is Empty.Create new Gradebooks!</xml>").build());
                }else {
        System.out.println("Getting all gradebooks");    
        GradeBookList bookList = new GradeBookList();
        bookList.getGradebook().addAll(gradeBookLists);
        return Response.status(javax.ws.rs.core.Response.Status.OK).entity(bookList).build();
    }   
}
       
    @POST
    @Path("/gradebook/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response createGradeBooks(@PathParam("name") String name) throws IOException{
        return createOrModifyGradeBooks(name);    
    }
    
    @POST
    @Path("/gradebook")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.TEXT_PLAIN)
    public Response createGradeBook(GradeBook gradeBook) throws IOException{
        if(gradeBook==null){
        System.out.println("inside null name loop");
       throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).entity("<xml>Gradebook name cannot be blank!</xml>").build());
       }else{
        return copyGradeBook(gradeBook);
    }
    }
    
    @PUT
    @Path("/gradebook/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response modifyGradeBooks(@PathParam("name") String name) throws IOException{
         return createOrModifyGradeBooks(name);
    }
    
    private Response createOrModifyGradeBooks(String name) throws IOException{
         if(name==null || "".equalsIgnoreCase(name)){
            throw new BadRequestException();
            }
         if(name.matches("^ .*")) {
        //throw new IllegalArgumentException("Gradebook name cannot start with a space.");
        System.out.println("inside whitespace name loop");
        throw new InternalServerErrorException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("<xml>Gradebook name cannot start with a space!</xml>").build());
    }
         
        GradeBook gradePresent = gradeBookDb.filterGradeBookByName(name);
       if(gradePresent == null ){
          long gradeId = gradeBookDb.createGradebook(name);
          
           GradeBook gradecreated = gradeBookDb.filterGradeBookByName(name);
           if(gradecreated != null ){
               gradeBookDb.pushToAllServers(gradecreated);
           }
           
          return Response.status(javax.ws.rs.core.Response.Status.CREATED).entity(gradeId).build();
        }else{
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST).entity("<xml>Gradebook name already present. Use new name</xml>").build());
        }
    }
    
    private Response copyGradeBook(GradeBook gradeBook) throws IOException{ 
        if(gradeBook==null || "".equalsIgnoreCase(gradeBook.getGradeTitle())){
            throw new BadRequestException();
        }
        GradeBook gradePresent = gradeBookDb.filterGradeBookByName(gradeBook.getGradeTitle());
       if(gradePresent != null ){
           gradePresent.setServer(gradeBook.getServer());
           gradePresent.setServerList(gradeBook.getServerList());
           return Response.status(javax.ws.rs.core.Response.Status.CREATED).build();
        }else{
           gradeBookDb.createGradebook(gradeBook);
           return Response.status(javax.ws.rs.core.Response.Status.CREATED).build();
        }
    }
    
    @DELETE
    @Path("/gradebook/{id}")
    public Response deleteGradebookbyId(@PathParam("id") long id) throws IOException{
        GradeBook gradeBook = gradeBookDb.filterGradeBookById(id);
        if(gradeBook ==null || !gradeBookDb.isPrimary(gradeBook)){
          throw new BadRequestException();  
        }
        else{
            gradeBookDb.deleteAllSecondary(gradeBook);
            gradeBookDb.getGradeBookList().getGradebook().remove(gradeBook);
            return Response.ok().build();
        }
        
    }
    
    @DELETE
    @Path("/gradebookcopy/{id}")
    public Response deleteGradebookCopybyId(@PathParam("id") long id) throws IOException{
        GradeBook gradeBook = gradeBookDb.filterGradeBookById(id);
        if(gradeBook ==null){
          throw new BadRequestException();  
        }
        else{
            gradeBookDb.getGradeBookList().getGradebook().remove(gradeBook);
            return Response.ok().build();
        }
        
    }
    
    @PUT
    @Path("/secondary/{id}")
    public Response createSecondary(@PathParam("id") long id) throws IOException{
        return createOrModifySecondary(id);
    }
    
    @POST
    @Path("/secondary/{id}")
    public Response modifySecondary(@PathParam("id") long id) throws IOException{
        return createOrModifySecondary(id);
    }
    
    private Response createOrModifySecondary(long id) throws IOException{
        GradeBook gradeBook = gradeBookDb.filterGradeBookById(id);      
        if(gradeBook ==null){
            throw new BadRequestException("there is no GradeBook with the given id");  
        }else{
            String ip = gradeBookDb.getMyIP();
            if(gradeBookDb.isSecondary(gradeBook)){
                throw new BadRequestException("the server already has a secondary copy of the GradeBook");  
            }else{           
                if(gradeBookDb.isPrimary(gradeBook)){
                    throw new BadRequestException("the server is the primary server for the GradeBook");  
                }else{
                   gradeBook.getServerList().getServer().add(gradeBookDb.filterServerByIp(ip));
                   gradeBookDb.populateStudents(gradeBook);
                    gradeBookDb.pushToAllServers(gradeBook);
                    return Response.ok().build();  
                }           
            }           
        }
    }
    
    @DELETE
    @Path("/secondary/{id}")
    public Response deleteSecondarybyId(@PathParam("id") long id) throws IOException{
        GradeBook gradeBook = gradeBookDb.filterGradeBookById(id);
        if(gradeBook ==null){
          throw new BadRequestException("there is no GradeBook with the given id");  
        }
        else{
            String ip = gradeBookDb.getMyIP();
            if(!gradeBookDb.isSecondary(gradeBook)){
                throw new BadRequestException("the server does not have a secondary copy of the GradeBook");  
            }else{           
                if(gradeBookDb.isPrimary(gradeBook)){
                    throw new BadRequestException("the server is the primary server for the GradeBook");  
                }else{
                    gradeBook.getServerList().getServer().remove(gradeBookDb.filterServerByIp(ip));
                    gradeBookDb.removeStudents(gradeBook);
                    gradeBookDb.pushToAllServers(gradeBook);
                    return Response.ok().build();  
                }           
            }          
        }
        
    }
    
    @POST
    @Path("/gradebook/{id}/student/{name}/grade/{grade}")
    public Response createStudent(@PathParam("id") long id,@PathParam("name") String name,@PathParam("grade") String grade) throws IOException{
          return createOrModifyStudent(id,name,grade);
    }
    
    @PUT
    @Path("/gradebook/{id}/student/{name}/grade/{grade}")
    public Response modifyStudent(@PathParam("id") long id,@PathParam("name") String name,@PathParam("grade") String grade) throws IOException{
       return createOrModifyStudent(id,name,grade);
    }
    
     private Response createOrModifyStudent(long id,String name,String grade) throws IOException{
        if(id == 0 || grade==null || "".equalsIgnoreCase(grade) || !gradeBookDb.validGrade(grade) ){
            throw new BadRequestException();
        }
        GradeBook gradePresent = gradeBookDb.filterGradeBookById(id);
       if(gradePresent != null && gradeBookDb.isPrimary(gradePresent)){
          gradeBookDb.createStudent(id,name,grade);
          return Response.ok().build();
        }else{
            throw new BadRequestException("GradeBook not present or this is not a primary server");
        }
    } 
    
    @POST
    @Path("/gradebook/{id}/student")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.TEXT_PLAIN)
    public Response copyStudent(@PathParam("id") long id,Student student) throws IOException{
         if(student==null || "".equalsIgnoreCase(student.getName())){
            throw new BadRequestException();
        }
        GradeBook gradePresent = gradeBookDb.getGradeBookOnlyVisible(id);
       if(gradePresent != null ){
          gradeBookDb.copyStudent(id, student);
           return Response.status(javax.ws.rs.core.Response.Status.CREATED).build();
        }else{
           throw new BadRequestException("there is no GradeBook with the given id");  
        }
    }
    
    @DELETE
    @Path("/gradebook/{id}/student/{name}")
    public Response deleteStudent(@PathParam("id") long id,@PathParam("name") String name) throws IOException{
        GradeBook gradebook = gradeBookDb.filterGradeBookById(id);
        if(gradebook ==null){
            throw new BadRequestException();  
        }else{
            StudentList studentList = gradeBookDb.getAllStudents(id);
            Student namePresent = gradeBookDb.filterStudent(studentList,name);
            if(namePresent == null){
                throw new BadRequestException();  
            }
            else{
                gradeBookDb.deleteAllSecondaryStudent(gradebook,namePresent);
                studentList.getStudent().remove(namePresent);
                return Response.ok().build();   
            }     
        }   
    }
    
    
    @DELETE
    @Path("/secondary/{id}/student/{name}")
    public Response deleteSecondaryStudent(@PathParam("id") long id,@PathParam("name") String name) throws IOException{
        GradeBook gradebook = gradeBookDb.filterGradeBookById(id);
        if(gradebook ==null || !gradeBookDb.isSecondary(gradebook)){
            throw new BadRequestException();  
        }else{
            StudentList studentList = gradeBookDb.getAllStudents(id);
            Student namePresent = gradeBookDb.filterStudent(studentList,name);
            if(namePresent == null){
                throw new BadRequestException();  
            }
            else{
                studentList.getStudent().remove(namePresent);
                return Response.ok().build();   
            }     
        }   
    }
    
    
     
    
    @GET
    @Path("/gradebook/{id}/student")
    @Produces(MediaType.TEXT_XML+";charset=utf-8")
    public StudentList getAllStudent(@PathParam("id")  long id){
       return gradeBookDb.getAllStudents(id);
    }
    
    @GET
    @Path("/gradebook/{id}/student/{name}")
    @Produces(MediaType.TEXT_XML+";charset=utf-8")
    public Student getAllStudent(@PathParam("id")  long id,@PathParam("name") String name){
       return gradeBookDb.getStudent(id,name);
    }

    @GET
    @Path("/server")
    @Produces(MediaType.TEXT_XML+";charset=utf-8")
    public ServerList getServerList(){
        return gradeBookDb.getServerList();
    }
    
    @POST
    @Path("/server/{name}/ip/{ip}/port/{port}/contextroot/{contextroot}")
    public Response createServer(@PathParam("name") String name,@PathParam("ip") String ip,
            @PathParam("port") String port,@PathParam("contextroot") String contextroot){
         return createOrModifyServer(name,ip,port,contextroot);
    }
    
    @PUT
    @Path("/server/{name}/ip/{ip}/port/{port}/contextroot/{contextroot}")
    public Response modifyServer(@PathParam("name") String name,@PathParam("ip") String ip,
            @PathParam("port") String port,@PathParam("contextroot") String contextroot){
         return createOrModifyServer(name,ip,port,contextroot);
    }
    
    private Response createOrModifyServer(String name,String ip,String port,String contextroot){
         if(name==null || "".equalsIgnoreCase(name)){
            throw new BadRequestException();
        }
        Server server = gradeBookDb.filterServerByName(name);
        if(server == null ){
            Server ipPresent = gradeBookDb.filterServerByIp(ip);
            if(ipPresent!=null){
               throw new BadRequestException();
            }
            gradeBookDb.createServer(name,ip,port,contextroot);
        }else{
            server.setName(name);
            server.setIp(ip);
            server.setPort(port);
            server.setContextRoot(contextroot); 
        }
       return Response.status(javax.ws.rs.core.Response.Status.CREATED).build();
    }
    
    @DELETE
    @Path("/server/{name}")
    public Response deleteServerByName(@PathParam("name") String name){
        Server server = gradeBookDb.filterServerByName(name);
        if(server == null){
          throw new BadRequestException();  
        }
        else{
            gradeBookDb.getServerList().getServer().remove(server);
            return Response.ok().build();
        }  
    }
    
    /*
    
    
    private StudentList studentList = new StudentList();
    
     
    
    
    @POST
    @Path("{name}/grade/{grade}")
    public Response addStudent(@PathParam("name") String name,@PathParam("grade") String grade){
        return addOrReplaceStudent(name,grade);
    }
    
    @PUT
    @Path("{name}/grade/{grade}")
    public Response modifyStudent(@PathParam("name") String name,@PathParam("grade") String grade){
        return addOrReplaceStudent(name,grade);
    }
    
    @POST
    @Path("{name}/grade")
    public Response addEmptyStudent(@PathParam("name") String name){
        return addOrReplaceStudent(name,"");
    }
    
    @PUT
    @Path("{name}/grade")
    public Response modifyEmptyStudent(@PathParam("name") String name){
        return addOrReplaceStudent(name,"");
    }
    
    private Response addOrReplaceStudent(String name,String grade){
        
        if(grade==null || "".equalsIgnoreCase(grade) || !validGrade(grade)){
            throw new BadRequestException();
        }
        
        Student studentSaved = filterStudent(studentList,name);
        
        if(studentSaved == null ){
           Student student = new Student();
           student.setName(name);
            student.setGrade(grade);
            studentList.getStudent().add(student);
        }else{
            studentSaved.setGrade(grade);
        }
        
        return Response.ok().build();
    }
    
    @GET
    @Produces(MediaType.TEXT_XML+";charset=utf-8")
    public StudentList getStudents(){
        return studentList;
    }
    
    @GET
    @Path("{name}")
    @Produces(MediaType.TEXT_XML+";charset=utf-8")
    public Student getStudent(@PathParam("name") String name){
        Student student = filterStudent(studentList,name);
        if(student==null){
            throw new NotFoundException();
        }
        return student;
    }
    
    @DELETE
    @Path("{name}")
    public Response deleteStudent(@PathParam("name") String name){
        Student student = filterStudent(studentList,name);
        if(student==null){
            throw new NotFoundException();
        }else{
            studentList.getStudent().remove(student);
        }
        return Response.ok().build();
    }

    private Student filterStudent(StudentList studentList, String name) {
        if(name == null || name.trim().length()==0 || studentList.getStudent()==null || studentList.getStudent().size()==0){
            return null;
        }
        return studentList.getStudent().stream().filter(student->name.equalsIgnoreCase(student.getName()))
                .findFirst().orElse(null);
    }

    private boolean validGrade(String grade) {
        ArrayList<String> grades = new ArrayList<String>(
                Arrays.asList("A+","A-","B+","B-","C+","C-","D+","D-","A","B","C","D","E","F","I","W","Z"));
        return grades.stream().filter(gr->gr.equalsIgnoreCase(grade)).count()==1;
    }*/
}
