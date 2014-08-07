/*
 * Copyright 2014 William Seemann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wseemann.media.jplaylistparser.parser.pls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Set;

import org.xml.sax.SAXException;

import wseemann.media.jplaylistparser.exception.JPlaylistParserException;
import wseemann.media.jplaylistparser.mime.MediaType;
import wseemann.media.jplaylistparser.parser.AbstractParser;
import wseemann.media.jplaylistparser.playlist.Playlist;
import wseemann.media.jplaylistparser.playlist.PlaylistEntry;

public class PLSPlaylistParser extends AbstractParser {
	public final static String EXTENSION = ".pls";
    
	private static int mNumberOfFiles = 0;
    private boolean processingEntry = false;
    
    private static final Set<MediaType> SUPPORTED_TYPES =
    		Collections.singleton(MediaType.audio("x-scpls"));

    public Set<MediaType> getSupportedTypes() {
    	return SUPPORTED_TYPES;
    }
    
	/**
	 * Retrieves the files listed in a .pls file
	 * @throws IOException 
	 */
    private void parsePlaylist(InputStream stream, Playlist playlist) throws IOException {
        String line = null;
        BufferedReader reader = null;
        PlaylistEntry playlistEntry = null;
        
		reader = new BufferedReader(new InputStreamReader(stream));
		    
		playlistEntry = new PlaylistEntry();
		processingEntry = false;
		    
		while ((line = reader.readLine()) != null) {
			if (line.trim().equals("")) {
		    	if (processingEntry) {
		    		savePlaylistFile(playlistEntry, playlist);
		    	}
		    		
		    	playlistEntry = new PlaylistEntry();
		    	processingEntry = false;
		    } else {
			    int index = line.indexOf('=');
			    String [] parsedLine = new String[0];
			    
			    if (index != -1) {
			    	parsedLine = new String[2];
			    	parsedLine[0] = line.substring(0, index);
			    	parsedLine[1] = line.substring(index + 1);
			    }
			    
			    if (parsedLine.length == 2) {
			    	if (parsedLine[0].trim().matches("[Ff][Ii][Ll][Ee].*")) {
                        processingEntry = true;
                        playlistEntry.set(PlaylistEntry.URI, parsedLine[1].trim());
                    } else if (parsedLine[0].trim().contains("Title")) {
    		        	playlistEntry.set(PlaylistEntry.PLAYLIST_METADATA, parsedLine[1].trim());
                    } else if (parsedLine[0].trim().contains("Length")) {
        		    	if (processingEntry) {
        		    		savePlaylistFile(playlistEntry, playlist);
        		    	}
        		    		
        		    	playlistEntry = new PlaylistEntry();
        		    	processingEntry = false;
                       }
			    }
		    }
        }
		    
		// added in case the file doesn't follow the standard pls
		// structure:
		// FileX:
		// TitleX:
		// LengthX:
        if (processingEntry) {
            savePlaylistFile(playlistEntry, playlist);
        }
    }
    
    private void savePlaylistFile(PlaylistEntry playlistEntry, Playlist playlist) {
    	mNumberOfFiles = mNumberOfFiles + 1;
    	playlistEntry.set(PlaylistEntry.TRACK, String.valueOf(mNumberOfFiles));
    	parseEntry(playlistEntry, playlist);
    	processingEntry = false;
    }

	@Override
	public void parse(String uri, InputStream stream, Playlist playlist)
			throws IOException, SAXException, JPlaylistParserException {
		parsePlaylist(stream, playlist);
	}
}

