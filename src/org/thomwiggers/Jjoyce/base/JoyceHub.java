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
package org.thomwiggers.Jjoyce.base;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Java implementation of py-Joyce's JoyceHub
 * 
 * @author Thom Wiggers
 * 
 */
public class JoyceHub extends MirteModule {

    /**
     * Channel list
     */
    private HashMap<String, JoyceChannel> channels;

    /**
     * Because we cannot be sure if we have /dev/urandom
     */
    private SecureRandom random = new SecureRandom();

    /**
     * List of relays to channel tokens
     */
    private HashMap<String, JoyceRelay> channelToRelay;

    /**
     * Event for new channel creations
     */
    private ChannelEvent newChannelEvent = new ChannelEvent();

    private HashMap<JoyceRelay, ArrayList<String>> relayToChannels;

    public JoyceHub() {
	super();
	this.channels = new HashMap<String, JoyceChannel>();
	this.channelToRelay = new HashMap<String, JoyceRelay>();
	this.relayToChannels = new HashMap<JoyceRelay, ArrayList<String>>();
    }

    /**
     * Gets the channel for a relay
     * 
     * @param token
     * @param relay
     * @return channel
     * @throws HijackedChannelException
     */
    private JoyceChannel getChannelForRelay(String token, JoyceRelay relay)
	    throws HijackedChannelException {
	boolean newChannel = false;
	JoyceChannel c;

	synchronized (this) {
	    if (this.channels.get(token) == null) {
		c = this.createChannel(token, relay);
		newChannel = true;
	    } else {
		if (relay != channelToRelay.get(token)) {
		    throw new HijackedChannelException();
		}
		c = this.channels.get(token);
	    }
	}

	if (newChannel)
	    this.newChannelEvent.call(c);

	return c;

    }

    /**
     * Handles a stream by passing it on to the channel
     * 
     * @param token
     * @param stream
     * @param joyceRelay
     * @throws HijackedChannelException
     */
    public void handleStream(String token, Stream stream, JoyceRelay relay)
	    throws HijackedChannelException {
	JoyceChannel c = this.getChannelForRelay(token, relay);
	c.handleStream(stream);
    }

    /**
     * Handles a message by passing it on to the channel
     * 
     * @param token
     * @param message
     * @param joyceRelay
     * @throws HijackedChannelException
     */
    public void handleMessage(String token, Message message, JoyceRelay relay)
	    throws HijackedChannelException {
	JoyceChannel c = this.getChannelForRelay(token, relay);
	c.handleMessage(message);

    }

    /**
     * Broadcasts a message to all channels
     * 
     * @param message
     * @throws NotImplementedException
     */
    public void broadcastMessage(Message message)
	    throws NotImplementedException {
	JoyceChannel[] chans;
	synchronized (this.channels) {
	    chans = this.channels.values().toArray(new JoyceChannel[0]);
	}
	for (JoyceChannel c : chans) {
	    if (c == null)
		continue;
	    c.sendMessage(message);
	}
    }

    /**
     * Creates a new channel and adds it to the hashmaps
     * 
     * @param token
     * @param relay
     * @return the channel created
     */
    private JoyceChannel createChannel(String token, JoyceRelay relay) {
	if (token == null) {
	    token = generateToken();
	}

	JoyceChannel channel = new JoyceChannel(relay, token);
	this.channelToRelay.put(token, relay);
	if (!this.relayToChannels.containsKey(relay)) {
	    this.relayToChannels.put(relay, new ArrayList<String>());
	}
	this.relayToChannels.get(relay).add(token);
	this.channels.put(token, channel);

	return channel;
    }

    /**
     * Generates a new token
     * 
     * @return a new token
     */
    private String generateToken() {

	while (true) {
	    String attempt = new BigInteger(130, random).toString();
	    attempt = org.iharder.Base64.encodeBytes(attempt.getBytes())
		    .substring(0, 6);
	    if (!this.channels.containsKey(attempt)) {
		this.channels.put(attempt, null);
		return attempt;
	    }
	}
    }

    /**
     * Deletes a channel
     * 
     * @param token
     */
    public void removeChannel(String token) {
	JoyceChannel c;
	synchronized (this) {
	    c = this.channels.get(token);
	    this.channels.remove(token);
	    JoyceRelay relay = this.channelToRelay.get(token);
	    this.relayToChannels.get(relay).remove(token);
	    this.channelToRelay.remove(token);
	}
	c.afterClose();
    }
    
    /**
     * Removes a relay and the associated channels
     * 
     * @param relay
     */
    public synchronized void removeRelay (JoyceRelay relay) {
	String[] channels = this.relayToChannels.get(relay).toArray(new String[0]);
	for (String token : channels) {
	    this.channels.remove(token);
	    assert(this.channelToRelay.get(token) == relay);
	    this.channelToRelay.remove(relay);
	}
	this.channelToRelay.remove(relay);
    }

    /**
     * @return path
     */
    public String getPath() {
	return this.getSetting("path");
	
    }

    /**
     * @return port
     */
    public String getPort() {
	return this.getSetting("port");
    }

    /**
     * @return host
     */
    public String getHost() {
	return this.getSetting("host");
    }

}
