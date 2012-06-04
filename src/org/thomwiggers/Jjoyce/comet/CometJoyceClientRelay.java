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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.thomwiggers.Jjoyce.base.HijackedChannelException;
import org.thomwiggers.Jjoyce.base.JoyceException;
import org.thomwiggers.Jjoyce.base.JoyceHub;
import org.thomwiggers.Jjoyce.base.JoyceRelay;
import org.thomwiggers.Jjoyce.base.Message;
import org.thomwiggers.Jjoyce.base.NotImplementedException;
import org.thomwiggers.Jjoyce.base.Stream;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.entity.mime.MultipartEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author Thom Wiggers
 * 
 */
public class CometJoyceClientRelay extends JoyceRelay {

    private HttpClient httpClient = new DefaultHttpClient();

    final Lock lock = new ReentrantLock();

    private Condition conditionMessageIn;
    private Condition conditionOut;

    protected Boolean running;
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

    public void sendStream(String token, Stream stream, boolean blocking) {
	if (!this.token.equals(token))
	    throw new IllegalArgumentException("Wrong token!");

	this.conditionMessageIn = lock.newCondition();
	this.conditionOut = lock.newCondition();

	MultipartEntity multipartStream = new MultipartEntity();
	multipartStream.addPart("stream", stream);

	final HttpPost post = new HttpPost(String.format("http://%s:%s%s?m=%s",
		hub.getHost(), hub.getPort(), hub.getPath()));

	post.setEntity(multipartStream);

	Thread t = new Thread(new Runnable() {
	    public void run() {
		try {
		    httpClient.execute(post);
		} catch (ClientProtocolException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }
	});

	t.start();

	if (blocking) {
	    try {
		t.join();
	    } catch (InterruptedException e) {

	    }
	}

    }

    public void sendStream(String token, Stream stream) {
	this.sendStream(token, stream, true);
    }

    public void sendMessage(String token, Message message) {
	if (token != this.token) {
	    throw new IllegalArgumentException("Not my token");
	}
	try {
	    this.conditionOut.await();
	    this.queueOut.add(message);
	    this.conditionOut.signal();
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	}

    }

    private class MessageDispatcher implements Runnable {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
	    while (running) {
		CometJoyceClientRelay.this.lock.lock();
		try {
		    while (!queueMessageIn.isEmpty()) {
			handleMessage(token, queueMessageIn.poll());
		    }
		    conditionMessageIn.await();
		} catch (InterruptedException e) {
		} catch (HijackedChannelException e) {
		} finally {
		    lock.unlock();
		}

	    }
	}

    }

    private class Requester implements Runnable {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
	    while (running) {
		Message[] data = null;
		lock.lock();
		if ((queueOut.isEmpty() || token == null) && nPending > 0) {
		    try {
			conditionOut.await();
		    } catch (InterruptedException e) {
		    }
		    if (!running)
			return;
		    continue;
		}
		nPending++;
		if (!queueOut.isEmpty()) {
		    data = queueOut.toArray(new Message[0]);
		    queueOut.clear();
		}
		lock.unlock();
		try {
		    doRequest(Arrays.asList(data));
		} catch (JoyceException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		lock.lock();
		nPending--;
		lock.unlock();
	    }

	}

    }

    public void run() throws JoyceException {
	if (this.running)
	    throw new IllegalStateException("Already running");
	this.running = true;
	if (this.token == null) {
	    this.doRequest(null);
	}
	Thread t1 = new Thread(new MessageDispatcher());
	t1.setDaemon(true);
	this.hub.getThreadPool().execute(t1);

	Thread t2 = new Thread(new Requester());
	t2.setDaemon(true);
	this.hub.getThreadPool().execute(t2);

	Requester r = new Requester();
	r.run();

    }

    /**
     * @param data
     * @throws JoyceException 
     * 
     */
    private void doRequest(List<Message> data) throws JoyceException  {
	HttpClient httpClient = new DefaultHttpClient();
	if(this.token == null){
	    throw new IllegalStateException("token is null");
	}
	
	if(data == null) {
	    data = new ArrayList<Message>();
	}
	data.add(new Message(token));
	JSONArray json = new JSONArray();
	for(Message m : data) 
	    json.put(m.toString());
	HttpPost hp = new HttpPost(String.format("http://%s:%s%s?m=%s",this.hub.getHost(),
		this.hub.getPort(), this.hub.getPath(), json.toString()));
	StringBuilder sb = new StringBuilder();
	try {
	    HttpResponse r = httpClient.execute(hp);
	    r.getEntity();
	    InputStreamReader is = new InputStreamReader(r.getEntity().getContent());
	    BufferedReader br = new BufferedReader(is);
	    String line;
	    while((line = br.readLine())!= null){
		sb.append(line);
	    }
	} catch (ClientProtocolException e) {
	} catch (IOException e) {
	}
	JSONArray d = null;
	try {
	    d = new JSONArray(new JSONTokener(sb.toString()));
	    
	} catch (JSONException e) {
	    // TODO Auto-generated catch block
	}
	
	if(d.length() != 3 || d == null)
	    throw new JoyceException("Unexpected length of response list");
	String token = null;
	JSONArray msgs = null;
	try {
	    token = d.getString(0);
	    msgs = d.getJSONArray(1);
	    JSONObject stream = d.getJSONObject(2);
	} catch (JSONException e) {
	    // TODO Auto-generated catch block
	}
	
	this.lock.lock();
	try {
	    this.conditionOut.await();
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	}
	
	String oldToken = this.token;
	this.token = token;
	
	if(oldToken == null) {
	    this.conditionOut.signal();
	}
	
	for(int i = 0; i < msgs.length(); i++){
	    try {
		this.queueMessageIn.add(new Message(msgs.getJSONObject(i).toString()));
	    } catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	
	this.lock.unlock();
	
	//TODO Streams left out.
	
    }
    
    @SuppressWarnings("unused")
    private void retrieveStream(int streamId) {
	throw new NotImplementedException();
    }
    
    public void stop() {
	this.running = false;
	this.conditionMessageIn.notifyAll();
	this.conditionOut.notifyAll();
    }
}
