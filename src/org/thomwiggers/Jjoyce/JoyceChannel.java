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
package org.thomwiggers.Jjoyce;

/**
 * This class handles the Joyce channel streams
 * 
 * @author Thom Wiggers
 *
 */
public class JoyceChannel {
	
	/**
	 * TODO unsure wat het type is
	 */
	private JoyceRelay relay;
	
	/**
	 * Token for the connection 
	 */
	private String token;

	/**
	 * Event to register message events to
	 */
	private Event onMessage;

	/**
	 * Event to register stream events to
	 */
	private Event onStream; 
	
	public JoyceChannel(JoyceRelay relay, String token) {
	    this.relay = relay;
	    this.token = token;
	    this.onMessage = new Event();
	    this.onStream  = new Event();
	}
	
	public void sendStream(Stream stream) throws NotImplementedException {
	    this.relay.sendStream(token, stream, true);
	}
	
	public void sendStream(Stream stream, boolean blocking) throws NotImplementedException {
	    this.relay.sendStream(this.token, stream, blocking);
	}
	
	public void sendMessage(Message message) throws NotImplementedException {
	    this.relay.sendMessage(message);
	}
	
	public void handleMessage(Message message) {
	    this.onMessage.call(message);
	}
	
	public void handleStream(Stream stream) {
	    this.onStream.call(stream);
	}
	
	public void close() {
	    this.relay.closeChannel(this.token);
	}
	
	public void afterClose() {
	    
	}
}
