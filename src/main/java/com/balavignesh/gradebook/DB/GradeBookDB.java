/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.balavignesh.gradebook.DB;

import com.balavignesh.gradebook.connection.SendRequest;
import com.balavignesh.gradebook.domain.GradeBook;
import com.balavignesh.gradebook.domain.GradeBookList;
import com.balavignesh.gradebook.domain.Server;
import com.balavignesh.gradebook.domain.ServerList;
import com.balavignesh.gradebook.domain.Student;
import com.balavignesh.gradebook.domain.StudentList;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author BalaVignesh
 */
public class GradeBookDB {
    
    public GradeBookList gradeBookList = new GradeBookList();
    private ServerList serverList = new ServerList();
    private Map<Long, StudentList> gradeWithStudent= new HashMap<>();
    public static AtomicInteger idCounter = new AtomicInteger();
    
    public void addDefaults(){
        //createServer("bala","35.224.65.85","8080","GradeBook");
        //createServer("ashok","34.68.29.81","8080","GradeBook");
        //createServer("manasa","104.197.5.217","8080","GradeBook");
        createServer("neeraja","34.72.167.47","8080","GradeBook");
        createServer("localhost","192.168.0.105","8080","GradeBook");
              
    }
    
    public void clearall(){
        gradeBookList = new GradeBookList();
        serverList = new ServerList();
        gradeWithStudent= new HashMap<>();
        idCounter = new AtomicInteger();
        addDefaults();
    }
    
    public long getAtomicInteger(){
        return idCounter.longValue();
    }
    
    public long createGradebook(String title) throws IOException{
        GradeBook gradeBook = new GradeBook();
        idCounter.incrementAndGet();
        gradeBook.setGradeId(idCounter.longValue());
        gradeBook.setGradeTitle(title);
        
        Server server = filterServerByIp(getMyIP());
        gradeBook.setServer(server);
        gradeBook.setServerList(new ServerList());
       gradeBookList.getGradebook().add(gradeBook);
       return idCounter.longValue();
    }
    
    public void createGradebook(GradeBook gradeBook) throws IOException{
        gradeBookList.getGradebook().add(gradeBook);
        idCounter.set((int)gradeBook.getGradeId());
    }
     
    public void pushToAllServers(GradeBook gradeBook) throws IOException{
        if(gradeBook!=null && serverList.getServer().size()>1){
            List<Server> servers = filterServerByNotIp(serverList.getServer(),getMyIP());
            servers.stream().forEach(server->{
                sentMessage(server,"/resources/gradebook","POST",jaxbObjectToXML(gradeBook));
            });
        }
    }
     
    public GradeBook filterGradeBookByName(String name) {
        if(name == null || name.trim().length()==0 || gradeBookList.getGradebook()==null || gradeBookList.getGradebook().isEmpty()){
            return null;
        }
        return gradeBookList.getGradebook().stream().filter(student->name.equalsIgnoreCase(student.getGradeTitle()))
                .findFirst().orElse(null);
    } 
    
    public GradeBook filterGradeBookById(long id) {
        if(id == 0 || gradeBookList.getGradebook()==null || gradeBookList.getGradebook().isEmpty()){
            return null;
        }
        return gradeBookList.getGradebook().stream().filter(student->id == student.getGradeId())
                .findFirst().orElse(null);
    } 
    
    public Student filterStudent(StudentList studentList, String name) {
        if(name == null || name.trim().length()==0 || studentList==null || studentList.getStudent()==null || studentList.getStudent().isEmpty()){
            return null;
        }
        return studentList.getStudent().stream().filter(student->name.equalsIgnoreCase(student.getName()))
                .findFirst().orElse(null);
    }
    
     public Server filterServerByName(String name) {
        if(name == null || name.trim().length()==0 || serverList.getServer()==null || serverList.getServer().isEmpty()){
            return null;
        }
        return serverList.getServer().stream().filter(student->name.equalsIgnoreCase(student.getName()))
                .findFirst().orElse(null);
    } 
     
    public Server filterServerByIp(String ip) {
        if(ip == null || ip.trim().length()==0 || serverList.getServer()==null || serverList.getServer().isEmpty()){
            return null;
        }
        return serverList.getServer().stream().filter(student->
                ip.equalsIgnoreCase(student.getIp()))
                .findFirst().orElse(null);
    } 
    
     public Server filterServerByIp(List<Server> servers, String ip) {
        if(ip == null || ip.trim().length()==0 || servers==null || servers.isEmpty()){
            return null;
        }
        return servers.stream().filter(student->
                ip.equalsIgnoreCase(student.getIp()))
                .findFirst().orElse(null);
    } 
     
     public boolean isPrimary(GradeBook gradeBook) throws IOException {
        if(gradeBook==null || gradeBook.getServer()==null ||gradeBook.getServer().getIp()==null){
            return false;
        }
        return getMyIP().equalsIgnoreCase(gradeBook.getServer().getIp());
    }
     
     public boolean isSecondary(GradeBook gradeBook) throws IOException {
        if(gradeBook==null || gradeBook.getServer()==null ||gradeBook.getServer().getIp()==null){
            return false;
        }
        return filterServerByIp(gradeBook.getServerList().getServer(),getMyIP())!=null ? true:false;
    }
    
    public List<Server> filterServerByNotIp(List<Server> list ,String ip) {
        if(ip == null || ip.trim().length()==0 || list==null || list.isEmpty()){
            return null;
        }
        return list.stream().filter(student-> !ip.equalsIgnoreCase(student.getIp()))
                .collect(Collectors.toList()); 
    } 

    public GradeBookList getGradeBookList() {
        return gradeBookList;
    }
    public List<GradeBook> getGradeBookListOnlyVisible() throws IOException {
       String myIp = getMyIP();
       List<GradeBook> gradeBookLists= gradeBookList.getGradebook().stream().map(gradeBook -> { 
           if(gradeBook.getServer()!=null && myIp.equalsIgnoreCase(gradeBook.getServer().getIp())){
           gradeBook.setVisible(true);
            }else if(filterServerByIp(gradeBook.getServerList().getServer(),myIp)!=null){
                gradeBook.setVisible(true);
            }else{
                gradeBook.setVisible(false);
            }
        return gradeBook;
       }).filter(gradebook -> gradebook.isVisible()).collect(Collectors.toList()); 
       return gradeBookLists;
    }
    
    public GradeBook getGradeBookOnlyVisible(long id) throws IOException {
        List<GradeBook> gradeBookLists = getGradeBookListOnlyVisible();
      if(id == 0 || gradeBookLists==null || gradeBookLists.isEmpty()){
            return null;
        }
        return gradeBookLists.stream().filter(student->id == student.getGradeId())
                .findFirst().orElse(null);
    }
    
     public ServerList getServerList() {
        return serverList;
    }
     
    public void createServer(String name,String ip,String port,String contextroot){
        
        Server serverPresent = filterServerByName(name);
        if(serverPresent == null ){
            Server server = new Server();
            server.setName(name);
            server.setIp(ip);
            server.setPort(port);
            server.setContextRoot(contextroot);    
            serverList.getServer().add(server);
        }
        
    }

    public void createStudent(long id, String name, String grade) throws IOException {
        Student student = new Student();
        student.setName(name);
        student.setGrade(grade);
        
        if(gradeWithStudent.containsKey(id)){
           Student studentSaved = filterStudent(gradeWithStudent.get(id),name);
        
        if(studentSaved == null ){     
            gradeWithStudent.get(id).getStudent().add(student);
        }else{
            studentSaved.setGrade(grade);
        }
        }else{
            StudentList studentList = new StudentList();
            studentList.getStudent().add(student);
            gradeWithStudent.put(id, studentList);
        }
        pushToAllSecondaries(id,student);
    }
    
    public void copyStudent(long id, Student student) {
        
       if(gradeWithStudent.containsKey(id) && gradeWithStudent.get(id)!=null && gradeWithStudent.get(id).getStudent().size()>0){
           Student studentSaved = filterStudent(gradeWithStudent.get(id),student.getName());
        
        if(studentSaved == null ){
           
            gradeWithStudent.get(id).getStudent().add(student);
        }else{
            studentSaved.setGrade(student.getGrade());
        }
        }else{
            StudentList studentList = new StudentList();
            studentList.getStudent().add(student);
            gradeWithStudent.put(id, studentList);
        }
    }

    public StudentList getAllStudents(long id) {
        return gradeWithStudent.containsKey(id) ? gradeWithStudent.get(id):null;
    }
    
    public Student getStudent(long id, String name) {
        return gradeWithStudent.containsKey(id) ? filterStudent(gradeWithStudent.get(id),name):null;
    }
    
    public boolean validGrade(String grade) {
        ArrayList<String> grades = new ArrayList<String>(
                Arrays.asList("A+","A-","B+","B-","C+","C-","D+","D-","A","B","C","D","E","F","I","W","Z"));
        return grades.stream().filter(gr->gr.equalsIgnoreCase(grade)).count()==1;
    }
    
    public String getMyHostName() throws UnknownHostException, SocketException, IOException{
        return InetAddress.getLocalHost().getHostName();
    }
    
    public String getMyIP() throws UnknownHostException, SocketException, IOException{
        String ip = execReadToString("curl https://checkip.amazonaws.com");
        ip = ip.replace("\n", "");
        return ip;
    }
    
    public static String execReadToString(String execCommand) throws IOException {
    try (Scanner s = new Scanner(Runtime.getRuntime().exec(execCommand).getInputStream()).useDelimiter("\\A")) {
        return s.hasNext() ? s.next() : "";
    }
    }
    
    
    public String sentMessage(Server server,String url, String method,String content){
        SendRequest s = new SendRequest();
        
         String str = s.SendRequest("http://"+server.getIp()+":"+server.getPort()+"/"+
                 server.getContextRoot()+url, method,content);
         System.out.println(str);
         return str;
    }
    
    private static String jaxbObjectToXML(GradeBook gradebook) {
    String xmlString = "";
    try {
        JAXBContext context = JAXBContext.newInstance(GradeBook.class);
        Marshaller m = context.createMarshaller();

        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // To format XML

        StringWriter sw = new StringWriter();
        m.marshal(gradebook, sw);
        xmlString = sw.toString();

    } catch (JAXBException e) {
        e.printStackTrace();
    }
    return xmlString;
    }
    private static String jaxbObjectToXML(Student student) {
    String xmlString = "";
    try {
        JAXBContext context = JAXBContext.newInstance(Student.class);
        Marshaller m = context.createMarshaller();

        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // To format XML

        StringWriter sw = new StringWriter();
        m.marshal(student, sw);
        xmlString = sw.toString();

    } catch (JAXBException e) {
        e.printStackTrace();
    }

    return xmlString;
    }
    
    private static StudentList jaxbXMLToobject(String studentList) {
    StudentList studentListRece = null;
    try {
        JAXBContext context = JAXBContext.newInstance(StudentList.class);
        Unmarshaller m = context.createUnmarshaller();
        studentListRece = (StudentList)m.unmarshal(new StringReader(studentList));
        } catch (JAXBException e) {
        e.printStackTrace();
    }
    return studentListRece;
    }

    private void pushToAllSecondaries(long id,Student student) throws SocketException, IOException {
        if(student!=null && serverList.getServer().size()>1){
             GradeBook gradePresent = getGradeBookOnlyVisible(id);
            if(gradePresent != null && isPrimary(gradePresent)){
                List<Server> servers = filterServerByNotIp(gradePresent.getServerList().getServer(),getMyIP());
                if(servers!=null && servers.size()>0) {
                    servers.stream().forEach(server->{
                    sentMessage(server,"/resources/gradebook/"+id+"/student","POST",jaxbObjectToXML(student));
                });
            }}
            }
    }

    public void deleteAllSecondary(GradeBook gradeBook) throws IOException {
        System.out.println("delete all secondary Size:" + gradeBook.getServerList().getServer().size());
        System.out.println("delete all secondary.check for is primary:" + isPrimary(gradeBook));
        if(isPrimary(gradeBook)){
            System.out.println("inside if loop of deleteall secondary");
            List<Server> servers = filterServerByNotIp(serverList.getServer(),getMyIP());
            servers.parallelStream().forEach(server->{
               System.out.println("Server loop inside delete all secondary" + server);
               sentMessage(server,"/resources/gradebookcopy/"+gradeBook.getGradeId(),"DELETE",null); 
            });
        }
    }

    public void deleteAllSecondaryStudent(GradeBook gradebook,Student student) throws IOException {
        if(student!=null && serverList.getServer().size()>1){
             if(gradebook != null && isPrimary(gradebook) && gradebook.getServerList()!=null 
                     && gradebook.getServerList().getServer()!=null && gradebook.getServerList().getServer().size()>0){
                List<Server> servers = filterServerByNotIp(gradebook.getServerList().getServer(),getMyIP());
                if(servers!=null && servers.size()>0) {
                servers.stream().forEach(server->{
                    sentMessage(server,"/resources/secondary/"+gradebook.getGradeId()+"/student/"+student.getName(),"DELETE",null);
                });
                }
            }
            
        }
    }

    public void populateStudents(GradeBook gradeBook) throws IOException {
        String test = null;
        if(serverList.getServer().size()>1){
             if(gradeBook != null && isSecondary(gradeBook)){
                Server server = filterServerByIp(serverList.getServer(),gradeBook.getServer().getIp());
               StudentList studentListRece = jaxbXMLToobject(sentMessage(server,"/resources/gradebook/"+gradeBook.getGradeId()+"/student","GET",null));
               gradeWithStudent.put(gradeBook.getGradeId(), studentListRece);
                
            }
            
        }
    }

    public void removeStudents(GradeBook gradeBook) {
        gradeWithStudent.remove(gradeBook.getGradeId());
    }
    
}
