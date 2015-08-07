/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IPFetcher;

import Downloader.StartDownload;

/**
 *
 * @author Varun
 */
public class InitializeDownload implements Runnable
{
    Thread t;
    DNSFetch fetch;
    
    InitializeDownload(DNSFetch fetch)
    {
        this.fetch = fetch;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() 
    {
        StartDownload sd = new StartDownload();
        sd.download();
    }
    
}
