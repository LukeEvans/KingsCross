package com.reactor.nlp.utilities;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class IPTools {

	@SuppressWarnings("rawtypes")
	public static String getPrivateIp() {

		try {
			Enumeration e=NetworkInterface.getNetworkInterfaces();

			while(e.hasMoreElements()) {
				NetworkInterface n=(NetworkInterface) e.nextElement();
				Enumeration ee = n.getInetAddresses();
				while(ee.hasMoreElements()) {
					InetAddress i= (InetAddress) ee.nextElement();
					
					String ip = i.getHostAddress();
					
					if (ip.startsWith("10")) {
						return ip;
					}
				}
			}

			return "127.0.0.1";
			
		} catch (Exception e) {
			return "127.0.0.1";
		}
	}
}