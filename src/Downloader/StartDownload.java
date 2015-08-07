/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Downloader;

import IPFetcher.DNSFetch;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Varun
 */
public class StartDownload 
{
    static java.sql.Connection c;
    static Statement s;
    static ResultSet r;
    static ResultSet r1;
    static Statement s1;
    static ArrayList<String> fetchedIp;
    
    static org.jsoup.Connection dCon;
    
    public static boolean canEditNew_Page = true;
    public static boolean isEdittingip_with_host = false;
    
    static HashMap<String, Integer> busyServer;
    
    static int id_fetched_ip;
    public static int countPage;
    
    static ArrayList<String> downdIP;
    
    public StartDownload() 
    {
        id_fetched_ip = 1;
        busyServer = new HashMap<>();
        countPage=0;
                
        try 
        {
            Class.forName("com.mysql.jdbc.Driver");
            c = DriverManager.getConnection("JDBC:mysql://localhost:3306/web_crawler","root","root");
            s = c.createStatement();
            s1 = c.createStatement();
            fetchedIp = new ArrayList<>();
            downdIP = new ArrayList<>();
        }
        catch (ClassNotFoundException | SQLException ex)
        {
            Logger.getLogger(StartDownload.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String args[])
    {
        new StartDownload();
        
        download();
    }
    
    public static void download()
    {
        String ip;
        String host;
        int val, business=50, cnt=0;

//        if(countPage == 0)
//        {
//        }
            
        
        System.out.println("SD "+countPage);
        while(true){
        try 
        {
            while(true)
            {
                try
                {
                    r = s.executeQuery("Select * from ip_with_host");
                    cnt = 0;
                    while(r.next())
                    {
                        cnt++;
                    }
                } 
                catch (SQLException ex) {
                    Logger.getLogger(StartDownload.class.getName()).log(Level.SEVERE, null, ex);
                }

                countPage = cnt;
                if(countPage==0)
//                    Thread.sleep(2000);
                    
                r = s.executeQuery("Select * from downloaded_pages");
                if(DNSFetch.canEditip_with_host && countPage>10)
                {
                    System.out.println("SD "+countPage+" .....Deleting....");
                    isEdittingip_with_host = true;
                    while(r.next())
                    {
                        s1.executeUpdate("Delete from ip_with_host where Ip='"+r.getString("Page")+"' AND Host='"+r.getString("Host")+"'");
                    }
                    downdIP = new ArrayList<>();
                    isEdittingip_with_host = false;
                    break;
                }
                if(countPage<10)
                {
                    isEdittingip_with_host = false;
                    break;
                }
            }
                 
            System.out.println("SD "+"After Deleting");
            r = s.executeQuery("Select * from ip_with_host");
            while(r.next())
            {
                host = r.getString("Host");
                ip = r.getString("Ip");
                
                if(downdIP.contains(ip))
                    continue;
                
                if(busyServer.containsKey(host))
                {
                    val = busyServer.get(host);
                    if(val>business)
                    {
                        continue;
                    }
                    else
                    {
                        busyServer.replace(host, val, val+1);
                    }
                }
                else
                {
                    busyServer.put(host, 1);
                }
                
                System.out.println("SD "+r.getInt(1));
                
                downloadPage(host, ip);
                System.out.println("SD "+countPage);
            }

            if(countPage<10)
            {
                System.out.println("SD "+"Warning DNSServer");
                DNSFetch.isFetchNeeded = true;
            }
        }
        catch (SQLException ex)
        {
            Logger.getLogger(StartDownload.class.getName()).log(Level.SEVERE, null, ex);
        }
//        catch (InterruptedException ex) {
//                Logger.getLogger(StartDownload.class.getName()).log(Level.SEVERE, null, ex);
//            }
        }
    }
    
    static String[] formatHostNameWithHTTP(String name)
    {
        String path = name;
        String[] arr = new String[2];
        
        name = name.replaceFirst("http://", "");
        path = name;
        Scanner fmt = new Scanner(name);
        
        fmt.useDelimiter("/");
        name = fmt.next();
        
        fmt.useDelimiter("");
        path = path.replaceFirst(name, "");
        
        arr[0] = name;
        arr[1] = path;
        
        return arr;
    }

    
    static void downloadPage(String host, String ip) throws SQLException
    {
        org.jsoup.Connection.Response resp;
        org.jsoup.nodes.Document doc = null;
        Element content;
        Elements links;
        String title, fileStore, append="", text;
        int val;
        File newDir, newFile = null;
        
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        dCon = Jsoup.connect("http://"+ip)
                .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                .timeout(1000*60)
                .followRedirects(true)
                .header("Host", host)
                .referrer("http://www.google.com");

        try
        {
            resp = dCon.execute();

            doc = null;

            System.out.println("SD "+"."+resp.statusCode()+" "+resp.statusMessage());
            if(resp.statusCode() == 200)
            {
                downdIP.add(ip);
                doc = dCon.get();
                title = doc.title();
                title = title.replace("|", "");
                try 
                {
                    text = dCon.maxBodySize(Integer.MAX_VALUE).get().text();
                    newDir = new File("D:\\Users\\Varun Raval\\Varun Documents\\"
                            + "College\\SECOND YEAR 4th Sem\\Seminar\\WebCrawler\\DownloadedPages\\"+host);
                    
                    append = getPageFromIp(ip);
                    fileStore = "D:\\Users\\Varun Raval\\Varun Documents\\College\\"
                            + "SECOND YEAR 4th Sem\\Seminar\\WebCrawler\\DownloadedPages\\"+host+"\\"+title+"-"+append+".txt";

                    newFile = new File(fileStore);

                    if(!newDir.exists())
                    {
                        newDir.mkdir();
                    }

                    System.out.println("SD "+".."+title+"-"+append);
                    if(!newFile.exists())
                        newFile.createNewFile();
                    FileWriter write = new FileWriter(newFile);
                    write.append(text);
                    write.close();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                    System.out.println("SD "+"IO Exception");
                }
                content = doc.body();
                links = content.getElementsByTag("a");
//
//                r1 = s1.executeQuery("Select Page from fetched_ip where Id>='"+id_fetched_ip+"'");
//                while(r1.next())
//                {
//                    fetchedIp.add(r1.getString(1));
//                    id_fetched_ip++;
//                }

                String temp;
                String arr[] = new String[2];
                for(Element link : links)
                {
                    temp = link.attr("href");
                    if(temp.startsWith("http://"))
                    {
                        canEditNew_Page = true;
                        arr = formatHostNameWithHTTP(temp);
                        while(!DNSFetch.queueSet)
                        {
                        }
                        canEditNew_Page = false;
                        try{
                            if(!fetchedIp.contains(arr[0]+arr[1]))
                                s1.executeUpdate("Insert into new_pages (Page) Values ('"+arr[0]+arr[1]+"')");
                            canEditNew_Page = true;
                        }
                        catch(com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException e)
                        {
                            continue;
                        }
                    }
                    else
                    {
//                                System.out.println("Not>>"+temp);
                        canEditNew_Page = true;
                        while(!DNSFetch.queueSet)
                        {
                        }
                        canEditNew_Page = false;
                        try{
                            if(!fetchedIp.contains(host+temp))
                                s1.executeUpdate("Insert into new_pages (Page) Values ('"+host+temp+"')");
                            canEditNew_Page = true;                        
                        }
                        catch(com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException e)
                        {
                            continue;
                        }
                    }   
                }

                if(host.equalsIgnoreCase("twitter.com") || host.equalsIgnoreCase("www.twitter.com"))
                    getTweets(doc, title, append);
                
                countPage--;
                s1.executeUpdate("Insert into downloaded_pages (Page,Host) Values ('"+ip+"','"+host+"')");

                val = busyServer.get(host);
                if(val==1)
                {
                    busyServer.remove(host);
                }
                else
                {
                    busyServer.replace(host, val, val-1);                    
                }
            }
        }
        catch(org.jsoup.HttpStatusException e)
        {
            System.out.println("SD "+"HTTP status Exception....."+ip);
            s1.executeUpdate("Delete from ip_with_host where Ip='"+ip+"' AND Host='"+host+"'");                    

        }
        catch(SocketTimeoutException e)
        {
            
        }
        catch(IOException e){
            e.printStackTrace();
            s1.executeUpdate("Delete from ip_with_host where Ip='"+ip+"' AND Host='"+host+"'");                    
        }

//        if(countPage<10)
//        {
//            DNSFetch.isFetchNeeded = true;
//        }
    }
    
    static String getPageFromIp(String ip)
    {
        String name="";
        int in;
        Scanner sc = new Scanner(ip);
        sc.useDelimiter("/");
        
        
        while(sc.hasNext())
        {
            name = sc.next();
        }
        
        if(name.endsWith(".html") || name.endsWith(".htm"))
        {
            in = name.indexOf(".htm");
            name = name.substring(0, in);
        }
        return name;
    }
    
    public static void updateFetchedIP()
    {
        try 
        {
            r1 = s1.executeQuery("Select Page from fetched_ip where Id>='"+id_fetched_ip+"'");
            while(r1.next())
            {
                fetchedIp.add(r1.getString(1));
                id_fetched_ip++;
            }
        } 
        catch (SQLException ex)
        {
            Logger.getLogger(StartDownload.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    static void getTweets(org.jsoup.nodes.Document doc, String title, String append) throws IOException
    {
        String tweet;
        File f = new File("D:\\Users\\Varun Raval\\Varun Documents\\"
                + "College\\SECOND YEAR 4th Sem\\Seminar\\WebCrawler\\DownloadedPages\\twitter.com\\"
                + "Tweets\\"+title+"-"+append+".txt");
        if(!f.exists())
            f.createNewFile();
        FileWriter write = new FileWriter(f, true);
        Elements tweets = doc.select("p.TweetTextSize  js-tweet-text tweet-text");
        for(Element t: tweets)
        {
            tweet = t.text();
            write.write(tweet);
            write.write("\n");
        }
    }
}