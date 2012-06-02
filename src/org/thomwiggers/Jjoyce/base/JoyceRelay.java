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


/**
 * Relay between the channel and the hub
 * 
 * @author Thom Wiggers
 *
 */
public class JoyceRelay {

    protected JoyceHub hub;


    public JoyceRelay(JoyceHub hub) {
	this.hub = hub;
    }
    
    /**
     * Not implemented in py-Joyce
     * No idea what it is supposed to do
     * 
     * @param token
     * @param stream
     * @param blocking
     */
    public void sendStream(String token, Stream stream, boolean blocking) {
	throw new NotImplementedException();
	
    }

    /**
     * Not implemented in py-Joyce
     * No idea what it is supposed to do
     * 
     * @param message Message
     */
    public void sendMessage(Message message) {
	throw new NotImplementedException();
    }
    

    /**
     * Closes the channel by removing it from the hub
     * 
     * @param token
     */
    public void closeChannel(String token) {
	this.hub.removeChannel(token);
    }
    
    /**
     * Forwards the stream to the JoyceHub
     * 
     * @param token
     * @param stream
     * @throws HijackedChannelException 
     */
    public void handleStream(String token, Stream stream) throws HijackedChannelException {
	this.hub.handleStream(token, stream, this);
    }
    
    /**
     * Forwards the message to the JoyceHub
     * 
     * @param token
     * @param message
     * @throws HijackedChannelException 
     */
    public void handleMessage(String token, Message message) throws HijackedChannelException {
	this.hub.handleMessage(token, message, this);
    }
    
    
    
    

}
