/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IPFetcher;

import Downloader.StartDownload;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Varun
 */
public class DNSFetch extends QueueClass{
    
    static QueueClass dnsQueue;

    static ArrayList<String> hosts;
    static ArrayList<String> fetched_ip;

    static QueueClass fileName;
    boolean valSet = false;
    int count=0;
    public static UpdateQueue uQ;
    FetchIP fI;
    InitializeDownload iD;

    static Connection c;
    static Statement s;
    static ResultSet r;
    
    public static Boolean queueSet = true;
    public static Boolean canEditip_with_host = true;
    
    public static Boolean isFetchNeeded = true;
    
    DNSFetch() throws IOException
    {
        try 
        {
            Class.forName("com.mysql.jdbc.Driver");
            
            c = DriverManager.getConnection("JDBC:mysql://localhost:3306/web_crawler","root","root");
            s = c.createStatement();
            
            hosts = new ArrayList<>();
            fetched_ip = new ArrayList<>();
            
            fileName = new QueueClass();
            dnsQueue = new QueueClass();
            
            iD = new InitializeDownload(this);
            uQ = new UpdateQueue(this);
            fI = new FetchIP(this);
        } 
        catch (ClassNotFoundException ex) {
            Logger.getLogger(DNSFetch.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DNSFetch.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
    synchronized final void startFetch() throws IOException
    {
        String gotHost="";
        String gotFile="";
        //append IP Address to IP addresses.txt and add hosts to CoveredHosts with ip/filename
        while(!valSet)
            try 
            {
                System.out.print("DNS "+StartDownload.countPage+" ");
                System.out.println("Wating for queue");
                wait();
            }
            catch (InterruptedException ex) {
                Logger.getLogger(DNSFetch.class.getName()).log(Level.SEVERE, null, ex);
            }
        try 
        {
            InetAddress addressL = InetAddress.getLocalHost();
            
            String addressStr="";
            InetAddress address;

            int length = dnsQueue.size();
            
            System.out.println("DNS "+"Is editing IP with host");
            while(StartDownload.isEdittingip_with_host){
//                System.out.println("Waiting in DNSFetch stop by startDown");
            }
            System.out.println("DNS "+"Completed");
            
            canEditip_with_host=false;
            for(int i=0; i<length; i++)
            {
                try
                {
                    gotHost = dnsQueue.poll();
                    gotFile = fileName.poll();
                    
                    address = InetAddress.getByName(gotHost);
                    addressStr = address.toString().split("/")[1];
                    
                    hosts.remove(gotHost+gotFile);
                    fetched_ip.add(gotHost+gotFile);
                    
                    s.executeUpdate("Insert into ip_with_host (Host,Ip)values('"+gotHost+"','"+(addressStr+gotFile)+"')");
                    s.executeUpdate("Insert into fetched_ip (Page)values('"+(gotHost+gotFile)+"')");
                }
                catch(UnknownHostException e)
                {
//                    e.printStackTrace();
//                    System.out.println(i+"->UnknownHost........."+gotHost+gotFile);
                    hosts.remove(gotHost+gotFile);
                    try 
                    {
                        String del = gotHost+gotFile;
                        s.executeUpdate("Delete from new_pages where Page='"+del+"'");
                    }
                    catch (SQLException ex) {
                        Logger.getLogger(DNSFetch.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                catch (SQLException ex)
                {
                    Logger.getLogger(DNSFetch.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            StartDownload.updateFetchedIP();
            canEditip_with_host = true;
        }
        catch (IOException ex)
        {
            Logger.getLogger(DNSFetch.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
        }

        System.out.println("DNS "+"Can edit new_page "+StartDownload.canEditNew_Page);
        while(!StartDownload.canEditNew_Page){}
        System.out.println("DNS "+"Completed");
        if(StartDownload.canEditNew_Page)    
            for(String host: fetched_ip) 
            {
                try 
                {
                    s.executeUpdate("Delete from new_pages where Page='"+host+"'");
                }
                catch (SQLException ex) {
                    Logger.getLogger(DNSFetch.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        
        count++;
        System.out.println("DNS "+count);
        valSet = false;

//        if(count>100)
//        {
//            uQ.t.stop();
//            fI.t.stop();
//        }

//        try 
//        {
//            r = s.executeQuery("Select * from ip_with_host");
//            while(r.next())
//            {
//                StartDownload.countPage++;
//            }
//        }
//        catch (SQLException ex) {
//            Logger.getLogger(DNSFetch.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
        notify();
    }
    
    synchronized final void getHostname()
    {
        while(valSet)
            try 
            {
                System.out.println("DNS "+"Waiting for fetch");
                wait();
            }
            catch (InterruptedException ex) {
                Logger.getLogger(DNSFetch.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        System.out.println("DNS "+"Fetch Needed");
        while(!isFetchNeeded)
        {
//            System.out.println("In while");
        }
        System.out.println("DNS "+"Completed");
        
        queueSet = false;
//        if(StartDownload.canEditNew_Page)
            addHosts();
        queueSet = true;
        
        String arr[] = new String[2];
        for(String host: hosts) {
            
            arr = formatHostName(host);
            dnsQueue.add(arr[0]);
            fileName.add(arr[1]);
        }
        valSet = true;
        isFetchNeeded = false;
        notify();
    }
    
    static boolean addHosts()
    {
        String temp;

        try      
        {
            r = s.executeQuery("Select * from new_pages");
            while(r.next()){
                temp = r.getString("Page");
                hosts.add(temp);
            }
            return true;
        }
        catch(SQLException ex) {
            Logger.getLogger(DNSFetch.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        finally{
        }
    }        
    
    static String[] formatHostName(String name)
    {
        String path = name;
        String[] arr = new String[2];

        Scanner fmt = new Scanner(name);
        fmt.useDelimiter("/");
        
        name = fmt.next();
        
        fmt.useDelimiter("");
        
        path = path.replaceFirst(name, "");
        
        arr[0] = name;
        arr[1] = path;
        
//        System.out.println("Host+file "+arr[0]+" "+arr[1]);
        
        return arr;
    }
    
}
