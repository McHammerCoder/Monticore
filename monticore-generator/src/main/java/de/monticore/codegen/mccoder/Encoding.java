/*
 * Copyright (c) 2016 RWTH Aachen. All rights reserved.
 *
 * http://www.se-rwth.de/ 
 */
package de.monticore.codegen.mccoder;

import java.util.Map;

/**
 * TODO: Write me!
 *
 * @author  (last commit) $Author$
 * @version $Revision$, $Date$
 * @since   TODO: add version number
 *
 */
public class Encoding implements java.io.Serializable{

		private int type;
		private Map<String,String> map;
		private String startEncoding;
			

		public Encoding(int type, Map<String,String> map, String startEncoding){
			this.map = map;
			this.type = type;
			this.startEncoding = startEncoding;
			}

		public int getType(){
			return type;
		}
		
		public Map<String,String> getMap(){
			return map;
		}
		public String getStartEncoding(){
			return startEncoding;
		}

	}
