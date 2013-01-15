/**
 * 
 */

import java.nio.channels.Channel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;
import org.jboss.netty.handler.codec.replay.VoidEnum;

import frames.NSQFrame;


/**
 * @author Dustin Norlander
 * @created Jan 14, 2013
 * 
 */
public class NSQDecoder extends ReplayingDecoder<NSQDecoder.MyDecoderState>{

	protected static Log log = LogFactory.getLog(NSQDecoder.class);
	
	public static enum MyDecoderState {
		READ_SIZE,
		READ_FRAME_ID,
		READ_DATA;
	};
	
	private int size;
	private NSQFrame frame;
	
	 public NSQDecoder() {
	    // Set the initial state.
	    super(MyDecoderState.READ_SIZE);
	 }

	/* (non-Javadoc)
	 * @see org.jboss.netty.handler.codec.replay.ReplayingDecoder#decode(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.Channel, org.jboss.netty.buffer.ChannelBuffer, java.lang.Enum)
	 */
	@Override
	protected Object decode(ChannelHandlerContext ctx,
			org.jboss.netty.channel.Channel channel, ChannelBuffer buf,
			MyDecoderState state) throws Exception {
		switch (state) {
	     case READ_SIZE:
	       size = buf.readInt();
	       System.out.println("Got size: " + size);
	       checkpoint(MyDecoderState.READ_FRAME_ID);
	       break;
	     case READ_FRAME_ID:
	    	 int id = buf.readInt();
	    	 System.out.println("GOT ID: " + id);
	    	 this.frame = NSQFrame.instance(id);
	    	 if (this.frame == null) {
	    		 //uhh, bad response from server..  what should we do?
	    		 throw new Exception("Bad frame id from server (" + id + ").  disconnect!");
	    	 }
	    	 this.frame.setSize(size);
	    	 checkpoint(MyDecoderState.READ_DATA);
	    	 break;
	     case READ_DATA:
	    	 ChannelBuffer bytes = buf.readBytes(frame.getSize()-4); //subtract 4 because the frame id is included
	    	 this.frame.setData(bytes.array());
	    	 checkpoint(MyDecoderState.READ_SIZE);
	    	 return this.frame;
	     default:
	       throw new Error("Shouldn't reach here.");
	     }
		return null;
	}
	 
//	@Override
//	protected Object decode(ChannelHandlerContext ctx,
//	                           Channel channel,
//	                           ChannelBuffer buf,
//	                           NSQDecoder.MyDecoderState state) throws Exception {
//	     
//	   }
	 
}
