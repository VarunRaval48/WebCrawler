/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package OtherClasses;

import java.io.File;
import java.io.IOException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Varun
 */
public class TestDownloadJSoup 
{
    public static void main(String args[]) throws IOException
    {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        Connection con = Jsoup.connect("http://208.80.154.224/wiki/Cricket")
                        .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                        .timeout(1000*20)
                        .followRedirects(true)
                        .header("Host","en.wikipedia.org")
                        .referrer("http://www.google.com");
        
        Connection.Response resp = con.execute();
        Document doc = null;
        System.out.println(resp.statusCode()+" "+resp.statusMessage());
//        if(resp.statusCode() == 200)
        {
            doc = con.get();
            System.out.println(doc.title());
                        
            Element content = doc.body();
            Elements links = content.getElementsByTag("a");
            
            for(Element link : links)
            {
                System.out.println(link.attr("href")+" "+link.text());
            }
        }
    }
}
