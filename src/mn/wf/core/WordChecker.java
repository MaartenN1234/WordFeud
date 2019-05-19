package mn.wf.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class WordChecker {


	private static String composeHTTPmsg(String word){
		return
				"POST http://www.taaltik.nl/Wordfeud/zoeken.php HTTP/1.1\n"+
				"Accept: image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*\n"+
				"Referer: ttp://www.taaltik.nl/\n"+
				"Accept-Language: nl\n"+
				"User-Agent: Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET4.0C; .NET CLR 1.1.4322; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)\n"+
				"Content-Type: application/x-www-form-urlencoded\n"+
				"Host: www.taaltik.nl\n"+
				"Content-Length: "+(5+word.length())+"\n"+
				"Connection: Keep-Alive\n"+
				"Cache-Control: no-cache\n"+
				"Cookie: taaltik=f2c32d9e9f4e60ccbd80610a9d4a05dc\n"+
				"\n"+
				"word="+word+"\n";
	}

	static String serverMsg ="" +
			"HTTP/1.1 200 OK\n"+
			"Date: Tue, 05 Jun 2012 22:28:44 GMT\n"+
			"Set-Cookie: taaltik=84ed34de8c24ccde46ec4a4584a58fac; expires=Fri, 06-Jul-2012 22:28:44 GMT; path=/\n"+
			"Vary: Accept-Encoding\n"+
			"Keep-Alive: timeout=15, max=100\n"+
			"Connection: Keep-Alive\n"+
			"Content-Type: text/html\n"+
			"\n"+
			"<html><head></head><body><P><small></P><form method=\"post\">\n"+
			"Wat is er aan de hand met het woord:<BR><input name=\"word\" value=\"\"><input type=\"submit\" size=\"15\" value=\"?\">\n"+
			"</form></BODY></HTML>\n"+
			"\n\n";
	public static void serverTest(){
		try {
			ServerSocket ss = new ServerSocket(83);
			Socket s = ss.accept();


			OutputStream os = s.getOutputStream();
			os.write(serverMsg.getBytes());
			os.flush();

			InputStream is = s.getInputStream();
			Thread.sleep(1000);
			if(is.available()>0){
				byte[]b = new byte[is.available()];
				is.read(b);
				System.out.println(new String(b));
			}


			s.close();
			ss.close();
		} catch (Exception e) {
			e.printStackTrace();
		}


	}
	public static void test(Matcher m){
		for (int q=9; q<15; q++){
			int k=0,x=0;

			System.out.println(q+" letter words");
			for (char c: "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()){
				if(true){
					try{
						BufferedWriter bw = null;
						ArrayList<String>    words = m.match("ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ?",c+"????????????".substring(0,q-1));
						System.out.print("Checking ("+c+") "+((words == null) ? 0:words.size())+" words, example '"+((words == null || words.size()==0) ? null:words.get(0))+"', faillures:");
						k=0;
						x=0;
						for (String w : words){
							if (!(WordChecker.checkWord(w))){
								if (bw==null)
									bw = new BufferedWriter (new OutputStreamWriter( new FileOutputStream("d:\\wordlists\\NL\\blacklist_"+q+c+".txt")));

								bw.write(w +"  ");
								k++;
								x++;
								if (k>=40){bw.newLine(); k=0;}
							}
						}
						System.out.println (x);
						if (bw!=null){
							bw.flush();
							bw.close();
						}
					}	catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}
	}
	public static boolean checkWord(String word){
		String httpMsg = composeHTTPmsg(word);
		boolean out = true;
		boolean done = false;
		try {
			Socket s = new Socket("www.taaltik.nl",80);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			OutputStream os = s.getOutputStream();
			os.write(httpMsg.getBytes());
			s.getOutputStream().flush();
			String inp = br.readLine();
			while(inp!=null){
				if (//Good
						inp.indexOf("Dit woord uit de lijst van OpenTaal is correct en voldoet aan de regels. ")>0 ||
						inp.indexOf("Dit woord wordt in de regels genoemd als goed. Daarom wordt het woord ook toegestaan")>0 ||
						inp.indexOf("Is na beoordeling door de gezamenlijke gebruikers en TaalTik toegevoegd aan de lijst")>0 ||
						inp.indexOf("Deze ongebruikelijke woordvorm (werkwoord zonder n), de aanvoegende wijs (bijv. dat zij leve), is volgens de spelregels toegestaan.")>0 ||
						inp.indexOf("Dit woord voldoet aan de regels en wordt geaccepteerd door de spellingcontrole van OpenTaal. Het staat dus in de lijst.")>0 ||
						inp.indexOf("Dit woord is correct bevonden en wordt dus binnen een paar weken aan de lijst toegevoegd.")>0 ||
						// Doubt
						inp.indexOf("Dit woord komt regelmatig voor in teksten, maar is nog niet helemaal beoordeeld.")>0 ||
						inp.indexOf("We weten nog niet hoe vaak dit woord wordt gebruikt")>0 ||
						inp.indexOf("Dit woord is in de praktijk erg zeldzaam")>0 ||
						false){
					out = true;
					done = true;
				} else if (
						// Wrong
						inp.indexOf("Dit woord was nog niet bekend")>0 ||
						inp.indexOf("Woorden die met een hoofdletter geschreven worden doen niet mee volgens de regels")>0 ||
						inp.indexOf("Dit woord is deel van een vaste woordgroep, geen zelfstandig woord en is daarom niet toegestaan.")>0 ||
						inp.indexOf("Incorrect bevonden door TaalTik")>0 ||
						inp.indexOf("Dit is geen Nederlands. Het woord staat dus niet in de lijst.")>0 ||
						inp.indexOf("Dit is een woord dat (deels) letter voor letter wordt uitgesproken en dus niet is toegestaan.")>0 ||
						inp.indexOf("Dit is een afkorting, en dus niet toegestaan")>0 ||
						inp.indexOf("De reden dat het woord niet in de lijst staat is:")>0||
						inp.indexOf("Incorrect Nederlands. Staat dus niet in de lijst.")>0||
						inp.indexOf("Alle 2- en 3-letterwoorden zijn bepaald door de Scrabblebond. Deze staat daar niet in, en is dus fout.")>0||
						false){
					out = false;
					done = true;
				} else if (
						inp.indexOf("</BODY>")>0) {
					System.out.println("word:"+word+" unexpected message ");
					done=true;
				}

				if(!done){
					inp = br.readLine();
				} else {
					inp=null;
				}
			}
			bw.close();
			br.close();
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}
}
