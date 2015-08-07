/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IPFetcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Varun
 */
public class StartFetch 
{
    static ArrayList<String> hosts;

    public static void main(String args[])
    {
        try 
        {
            DNSFetch fetch1 = new DNSFetch();
        }
        catch (IOException ex) 
        {
            Logger.getLogger(StartFetch.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
