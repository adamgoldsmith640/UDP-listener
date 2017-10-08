package meh.UDPListener;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    ArrayAdapter<String> adapter;
    private static final int PORT = 8888;
    ListenerThread listener = new ListenerThread();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<String> listItems = new ArrayList<>();
        ListView listview = (ListView) findViewById(R.id.lvMain);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listItems);
        listview.setAdapter(adapter);

        start();
    }

    public void start() {
        Thread t = new Thread(listener);
        t.start();
    }

    public synchronized void unpause(View view) {
        listener.onResume();
    }

    public synchronized void pause(View view) {
        listener.onPause();
    }

    protected void display(final String message) {
        runOnUiThread(new Thread(new Runnable() {
            public void run() {
                adapter.add(message);
                adapter.notifyDataSetChanged();
            }
        }));
    }



    private class ListenerThread implements Runnable {
        byte[] buf = new byte[1500];
        private final Object listenLock = new Object();
        boolean listen = true;
        boolean finished = false;


        @Override
        public void run() {
            try {
                DatagramSocket socket = new DatagramSocket(PORT);
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                String packetContents;

                while(!finished) {
                    socket.receive(packet);
                    //display(new String(packet.getData(), 0, packet.getLength()), true);
                    packetContents = new String(packet.getData(), 0, packet.getLength());
                    display(convertToHex(packetContents));
                    Thread.sleep(2000);

                    synchronized (listenLock) {
                        while(!listen) {
                            try {
                                listenLock.wait();
                            } catch (InterruptedException e) {
                                display("Interrupted Exception in run(): " + e.toString());
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            catch(IOException e) {
                display("IO Exception in run(): " + e.toString());
                e.printStackTrace();
            }
            catch(Exception e) {
                display("GENERAL EXCEPTION in run(): " + e.toString());
                e.printStackTrace();
            }
        }



        private String convertToHex(String input) {
            StringBuilder output = new StringBuilder();
            DateFormat format = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date();
            String time = format.format(date);
            char[] hexBytes = String.format("%040X", new BigInteger(1, input.getBytes())).toCharArray();

            for (int i = 0; i < hexBytes.length - 1; i += 2) {
                output.append(hexBytes[i]);
                output.append(hexBytes[i + 1]);
                output.append(" ");
            }
            return "Time: " + time + "\n" + output;
        }

        private void onPause() {
            synchronized (listenLock) {
                if(listen) {
                    listen = false;
                    display("\nUDP listener paused\n");
                }
            }
        }

        private void onResume() {
            synchronized (listenLock) {
                if(!listen) {
                    display("\nUDP listener unpaused\n");
                    listen = true;
                    listenLock.notifyAll();
                }
            }
        }
    }
}
