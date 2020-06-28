package xyz.navyd.mvc.response;

import java.nio.channels.ReadableByteChannel;
import java.util.Optional;

public class ChannelResponse implements Response<ReadableByteChannel> {

    @Override
    public Optional<ReadableByteChannel> getBody() {
        ReadableByteChannel channel;
        
        return null;
    }
    
}