package jlibrtp;

import java.util.Arrays;

public abstract class RTPAppIntfWrapper implements RTPAppIntf {

    public RTPAppIntfWrapper() {
    }

    byte[] buf;

    @Override
    public void receiveData(DataFrame frame, Participant participant) {
        if (buf == null) {
            buf = frame.getConcatenatedData();
        } else {
            buf = merge(buf, frame.getConcatenatedData());
        }
        if (frame.marked()) {
            offerDecoder(buf, buf.length);
            buf = null;
        }
    }

    protected  abstract void offerDecoder(byte[] buf, int length);

    @Override
    public int frameSize(int payloadType) {
        return 0;
    }

    public byte[] merge(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }


}
