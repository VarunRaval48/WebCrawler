/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IPFetcher;

/**
 *
 * @author Varun
 */
public class UpdateQueue implements Runnable
{
    DNSFetch addQueue;
    Thread t;
    public UpdateQueue(DNSFetch addQueue) 
    {
        this.addQueue = addQueue;
        t = new Thread(this);
        t.start();
    }
    
    @Override
    public void run()
    {
        while(true)
        {
            addQueue.getHostname();        
        }
    }
    
}
