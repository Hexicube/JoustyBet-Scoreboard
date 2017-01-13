package org.cubecorp.hexicube.joustybet.scoreboard;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

public class WebSocket extends Socket
{
	private static MessageDigest md;
	private static Random rand;
	
	public WebSocket(String host, int port, String url, String extension) throws IOException
	{
		super(host, port);
		if(md == null)
		{
			try
			{
				md = MessageDigest.getInstance("SHA-1");
			}
			catch(NoSuchAlgorithmException e)
			{
				close();
				throw new IOException("Handshake failure: Unable to create SHA-1 digest");
			}
		}
		if(rand == null) rand = new Random();
		
		final InputStream superIn = super.getInputStream();
		final OutputStream superOut = super.getOutputStream();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(superIn));
		PrintWriter out = new PrintWriter(superOut, false);
		
		out.println("GET "+url+" HTTP/1.1");
		
		int keyLen = 16;
		byte[] randBytes = new byte[keyLen];
		rand.nextBytes(randBytes);
		String encoded = Base64.getEncoder().encodeToString(randBytes);
		String expected = Base64.getEncoder().encodeToString(md.digest((encoded + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes()));
		
		out.println("Host: " + host);
		out.println("Connection: Upgrade");
		out.println("Upgrade: websocket");
		out.println("Sec-WebSocket-Version: 13");
		out.println("Sec-WebSocket-Extensions: " + extension);
		out.println("Sec-WebSocket-Key: " + encoded);
		out.println();
		out.flush();
		
		boolean validated = false;
		while(!in.ready()) try{Thread.sleep(1);}catch(InterruptedException e){}
		while(in.ready())
		{
			String inLine = in.readLine();
			if(inLine.equals("")) break;
			else if(inLine.startsWith("Sec-WebSocket-Accept: "))
			{
				String response = inLine.substring(22);
				if(!response.equalsIgnoreCase(expected))
				{
					close();
					throw new IOException("Handshake failure: WebSocket response key invalid");
				}
				validated = true;
				break;
			}
		}
		if(!validated)
		{
			close();
			throw new IOException("Handshake failure: WebSocket response key missing");
		}
		
		dataIn = new InputStream(){
			private ByteArrayOutputStream storage = new ByteArrayOutputStream();
			private byte[] currentMessage = new byte[0];
			private int pos = 0;
			
			private int readByte() throws IOException
			{
				int val = superIn.read();
				if(val == -1) throw new IOException("End of stream");
				return val;
			}
			
			private void parseHeader() throws IOException
			{
				storage.reset();
				
				boolean expecting = true;
				boolean first = true;
				while(expecting)
				{
					int val = readByte();
					boolean finalFragment = (val & 0x80) != 0;
					//Bits 2/3/4 are typically not used, so we're ignoring them.
					val = val & 0xF;
					
					//TODO: Close connection if first is continuation or if non-first is not continuation/control.
					if(val == 0) //continuation
					{
						if(finalFragment) expecting = false;
					}
					else if(val == 1) //text
					{
						if(finalFragment) expecting = false;
					}
					else if(val == 2) //binary
					{
						if(finalFragment) expecting = false;
					}
					else if(val > 2 && val < 8) //other non-control
					{
						if(finalFragment) expecting = false;
					}
					else if(val == 8) //close
					{
						WebSocket.this.close();
						return;
					}
					else if(val == 9) //ping
					{
						if(finalFragment && first) expecting = false;
						
						superOut.write(0b10001010);
						superOut.write(0b00000000);
					}
					else if(val == 10) //pong
					{
						if(finalFragment && first) expecting = false;
					}
					else //other control
					{
						if(finalFragment && first) expecting = false;
					}
					
					int len = 0;
					val = readByte();
					boolean masked = (val & 0x80) != 0;
					val = val & 0x7F;
					if(val == 126) len = readByte() * 256 + readByte();
					else if(val == 127) len = readByte() * 256 * 256 * 256 + readByte() * 256 * 256 + readByte() * 256 + readByte();
					else len = val;
					
					byte[] currentKey = null;
					if(masked) currentKey = new byte[]{(byte)readByte(), (byte)readByte(), (byte)readByte(), (byte)readByte()};
					
					for(int a = 0; a < len; a++)
					{
						if(currentKey == null) storage.write(readByte());
						else storage.write(readByte() ^ currentKey[a & 3]);
					}
					
					first = false;
				}
				currentMessage = storage.toByteArray();
				pos = 0;
			}
			
			@Override
			public int available() throws IOException
			{
				if(pos >= currentMessage.length)
				{
					if(superIn.available() == 0) return 0;
					parseHeader();
				}
				return currentMessage.length - pos;
			}
			
			@Override
			public int read() throws IOException
			{
				if(pos >= currentMessage.length) parseHeader();
				if(pos >= currentMessage.length) return -1;
				return currentMessage[pos++];
			}
		};
		
		dataOut = new OutputStream(){
			private ByteArrayOutputStream storage = new ByteArrayOutputStream();
			
			@Override
			public void write(int b) throws IOException
			{
				storage.write(b);
			}
			
			@Override
			public void flush() throws IOException
			{
				synchronized(superOut)
				{
					byte[] data = storage.toByteArray();
					superOut.write(0b10000010);
					if(data.length < 126)
					{
						superOut.write(128 | (byte)data.length);
					}
					else if(data.length <= 65536)
					{
						superOut.write(254);
						superOut.write((byte)(data.length >> 4));
						superOut.write((byte)data.length);
					}
					else
					{
						superOut.write(255);
						superOut.write((byte)(data.length >> 12));
						superOut.write((byte)(data.length >> 8));
						superOut.write((byte)(data.length >> 4));
						superOut.write((byte)data.length);
					}
					
					byte[] mask = new byte[]{(byte)rand.nextInt(), (byte)rand.nextInt(), (byte)rand.nextInt(), (byte)rand.nextInt()};
					superOut.write(mask);
					for(int a = 0; a < data.length; a++)
					{
						superOut.write((byte)data[a] ^ mask[a & 3]);
					}
					superOut.flush();
					
					storage.reset();
				}
			}
		};
		
		new Thread(new Runnable(){
			@Override
			public void run()
			{
				while(true)
				{
					try{Thread.sleep(5000);}catch(InterruptedException e){}
					
					if(WebSocket.this.isClosed()) return;
					
					try
					{
						synchronized(superOut)
						{
							superOut.write(0b10001001);
							superOut.write(0b00000000);
							superOut.flush();
						}
					}
					catch(IOException e)
					{
						e.printStackTrace();
						return;
					}
				}
			}
		}).start();
	}
	
	private InputStream dataIn;
	@Override
	public InputStream getInputStream()
	{
		return dataIn;
	}
	
	private OutputStream dataOut;
	@Override
	public OutputStream getOutputStream()
	{
		return dataOut;
	}
}