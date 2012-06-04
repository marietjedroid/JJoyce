/**
 * @licence GNU General Public licence http://www.gnu.org/copyleft/gpl.html
 * @Copyright (C) 2012 Thom Wiggers
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.thomwiggers.Jjoyce.comet;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.thomwiggers.Jjoyce.base.JoyceChannel;
import org.thomwiggers.Jjoyce.base.JoyceClient;
import org.thomwiggers.Jjoyce.base.JoyceException;
import org.thomwiggers.Jjoyce.base.JoyceHub;

/**
 * @author Thom Wiggers
 *
 */
public class CometJoyceClient extends JoyceClient {

    private boolean running;

    private Lock lock = new ReentrantLock();
    
    private ArrayList<CometJoyceClientRelay> relays = new ArrayList<CometJoyceClientRelay>();
    
    public CometJoyceClient(){
	this.running = true;
    }
    
    public JoyceChannel createChannel(String token) {
	JoyceChannel c = null;
	
	
	lock.lock();
	    if(!running) return null;
	    if(token == null) token = this.generateToken();
	    JoyceHub jh = new JoyceHub();
	    final CometJoyceClientRelay relay = new CometJoyceClientRelay(jh, token);
	    relays.add(relay);
	    c = createChannel(token, relay);
	lock.unlock();
	
	Thread t = new Thread(new Runnable() {

	    @Override
	    public void run() {
		try {
		    relay.run();
		} catch (JoyceException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		
	    }
	    
	});
	
	t.run();
	return c;
    }
    
    public void run() {
	return;
    }
    
    public void stop() {
	this.lock.lock();
	this.running = false;
	this.lock.unlock();
	for(CometJoyceClientRelay r : relays) 
	    r.stop();
    }
    
}
