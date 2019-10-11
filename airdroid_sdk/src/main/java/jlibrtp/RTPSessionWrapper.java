package jlibrtp;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class RTPSessionWrapper extends RTPSession {

    public RTPSessionWrapper(DatagramSocket rtpSocket, DatagramSocket rtcpSocket) {
        super(rtpSocket, rtcpSocket);
    }

    public RTPSessionWrapper(MulticastSocket rtpSock, MulticastSocket rtcpSock, InetAddress multicastGroup) throws Exception {
        super(rtpSock, rtcpSock, multicastGroup);
    }

    public long[][] sendDataByMutiPkt(byte[] bytes) {
        if(null == bytes || bytes.length == 0){
            return null;
        }
        int dataLength = (bytes.length - 1) / 1480 + 1;
        final byte[][] data = new byte[dataLength][];
        final boolean[] marks = new boolean[dataLength];
        marks[marks.length - 1] = true;
        int x = 0;
        int y = 0;
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            if (y == 0) {
                data[x] = new byte[length - i > 1480 ? 1480 : length - i];
            }
            data[x][y] = bytes[i];
            y++;
            if (y == data[x].length) {
                y = 0;
                x++;
            }
        }
        return sendData(data, null, marks, -1l, null);
    }

    public void initSequenceNum(){
        super.seqNum = 0;
    }

}
