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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.http.entity.mime.content.StringBody;

/**
 * @author Thom Wiggers
 *
 */
public class Stream extends StringBody{

    /**
     * @param text
     * @param mimeType
     * @param charset
     * @throws UnsupportedEncodingException
     */
    public Stream(String text, String mimeType, Charset charset)
	    throws UnsupportedEncodingException {
	super(text, mimeType, charset);
	// TODO Auto-generated constructor stub
    }

    
    /**
     * @param text
     * @param charset
     * @throws UnsupportedEncodingException
     */
    public Stream(final String text, final Charset charset) throws UnsupportedEncodingException {
        this(text, "text/plain", charset);
    }

    /**
     * Create a StringBody from the specified text.
     * The mime type is set to "text/plain".
     * The hosts default charset is used.
     *
     * @param text to be used for the body, not {@code null}
     * @throws UnsupportedEncodingException
     * @throws IllegalArgumentException if the {@code text} parameter is null
     */
    public Stream(final String text) throws UnsupportedEncodingException {
        this(text, "text/plain", null);
    }

}
