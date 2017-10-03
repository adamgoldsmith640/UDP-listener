package meh.yetanother;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayAdapter<String> adapter;
    private static final int PORT = 8888;
    boolean listen = true;
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

    public void stop(View view) {
        listen = false;
        try {
            listener.wait();
        }
        catch(InterruptedException e) {
            output("Interrupted Exception: " + e.toString() );
            e.printStackTrace();
        }
        catch(Exception e) {
            output("Exception: " + e.toString() );
            e.printStackTrace();
        }
    }

    public void start(View view) {
        listen = true;
        listener.start();
    }

    public void start() {
        listener.start();
    }

    protected void output(final String message) {
        runOnUiThread(new Thread(new Runnable() {
            public void run() {
                char[] hexBytes = String.format( "%040X", new BigInteger(1, message.getBytes()) ).toCharArray();
                StringBuilder output = new StringBuilder();

                for(int i=0; i<hexBytes.length-1; i+=2) {
                    output.append(hexBytes[i]);
                    output.append(hexBytes[i+1]);
                    output.append(" ");
                }
                adapter.add("input: " + message);
                adapter.add("output: " + output);
                adapter.notifyDataSetChanged();
            }
        }));
    }



    private class ListenerThread extends Thread {
        @Override
        public void run() {
            byte[] buf = new byte[1500];

            try {
                DatagramSocket socket = new DatagramSocket(PORT);
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                while(listen) {
                    socket.receive(packet);
                    output(new String(packet.getData(), 0, packet.getLength()));

                    try {
                        Thread.sleep(2000);
                    }
                    catch(InterruptedException e) {
                        output("Interrupted Exception: " + e.toString() );
                        e.printStackTrace();
                    }
                }
            }
            catch(IOException e) {
                output("IO Exception: " + e.toString() );
                e.printStackTrace();
            }
            catch(Exception e) {
                output("Exception: " + e.toString() );
                e.printStackTrace();
            }
        }
    }
}
