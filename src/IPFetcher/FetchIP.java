/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IPFetcher;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Varun
 */
public class FetchIP implements Runnable 
{
    Thread t;
    DNSFetch fetch;
    FetchIP(DNSFetch fetch)
    {
        this.fetch = fetch;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        try 
        {
            while(true)
            {
                fetch.startFetch();            
            }
        }
        catch (IOException ex) {
            Logger.getLogger(FetchIP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
