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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.thomwiggers.Jjoyce.base.JoyceHub;
import org.thomwiggers.Jjoyce.base.JoyceRelay;
import org.thomwiggers.Jjoyce.base.Message;
import org.thomwiggers.Jjoyce.base.Stream;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.entity.mime.MultipartEntity;

import com.google.gson.stream.JsonReader;


/**
 * @author Thom Wiggers
 *
 */
public class CometJoyceClientRelay extends JoyceRelay {

    private HttpClient httpClient = new DefaultHttpClient();
    
    
    final Lock lock = new ReentrantLock();
    
    private Condition conditionMessageIn;
    private Condition conditionOut;
    
    private boolean running = false;
    
    /**
     * Channel token 
     */
    private String token = null;
    
    private Queue<Message> queueMessageIn = new LinkedList<Message>();
    
    private Queue<Message> queueOut = new LinkedList<Message>();
    
    private int nPending = 0;
    
    
    /**
     * @param hub
     */
    public CometJoyceClientRelay(JoyceHub hub) {
	super(hub);
    }
    
    /**
     * @param hub
     */
    public CometJoyceClientRelay(JoyceHub hub, String token) {
	this(hub);
	
	this.token = token;
    }

    public void sendStream(String token, Stream stream, boolean blocking)
    {
	if(!this.token.equals(token))
	    throw new IllegalArgumentException("Wrong token!");
	
	this.conditionMessageIn = lock.newCondition();
	this.conditionOut = lock.newCondition();
	
	MultipartEntity multipartStream = new MultipartEntity();
	multipartStream.addPart("stream", stream);
	
	HttpPost post = new HttpPost(String.format("http://%s:%s%s?m=%s",
		hub.getHost(), hub.getPort(), hub.getPath()));
	
	post.setEntity(multipartStream);
	
	try {
	    HttpResponse response = httpClient.execute(post);
	    InputStream in = response.getEntity().getContent();
	    InputStreamReader ir = new InputStreamReader(in);
	    JsonReader jr = new JsonReader(ir);
	    
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} finally {
	    
	}
    }
    
    public void sendStream(String token, Stream stream)
    {
	this.sendStream(token, stream, true);
    }
    
}
