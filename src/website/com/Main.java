package website.com;


import javax.swing.*;
import java.awt.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends JFrame {

    private JTextArea textArea;
    private JTextField textField;

    private final String FRM_TITLE= "Chat";
    private final int FRM_LOC_X=100;
    private final int FRM_LOC_Y=100;
    private final int FRM_WIDTH=400;
    private final int FRM_HEIGHT=400;

    private final int PORT = 9876;
    private final String IP_BROADCAST="192.168.0.255";


    private class thdReceiver extends  Thread{
        @Override
        public void start(){
            super.start();
            try {
                customize();
            }catch (Exception ex){
                ex.printStackTrace();
            }

        }
        private void customize() throws Exception{
            DatagramSocket receiveSocket = new DatagramSocket(PORT);
            Pattern regex = Pattern.compile("[\u0020-\uFFFF]");

            while(true){
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket= new DatagramPacket(receiveData,receiveData.length);
                receiveSocket.receive(receivePacket);
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();

                String sentence = new String(receivePacket.getData());
                Matcher m = regex.matcher(sentence);

                textArea.append(IPAddress.toString()+":"+port+": ");
                while (m.find()){
                    textArea.append(sentence.substring(m.start(),m.end()));
                }
                textArea.append("\r\n");
                textArea.setCaretPosition(textArea.getText().length());
            }
        }
    };


    private void btnSend_Handler() throws Exception{
        DatagramSocket sendSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(IP_BROADCAST);
        byte[] sendData;
        String sentence = textField.getText();
        textField.setText("");
        sendData=sentence.getBytes(StandardCharsets.UTF_8);
        DatagramPacket sendPacket = new DatagramPacket(sendData,sendData.length,IPAddress,PORT);
        sendSocket.send(sendPacket);
    }

    private void frameDraw(JFrame frame){
        textArea = new JTextArea(FRM_HEIGHT/19,50);
        textField = new JTextField();
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setLocation(0,0);
        textArea.setLineWrap(true);
        textArea.setEditable(false);

        JButton btnSend = new JButton();
        btnSend.setText("Send");
        btnSend.setToolTipText("Broadcast a message");
        btnSend.addActionListener(e->{

            try{
                btnSend_Handler();
            }catch (Exception ex){
                ex.printStackTrace();
            }
        });

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle(FRM_TITLE);
        frame.setLocation(FRM_LOC_X,FRM_LOC_Y);
        frame.setSize(FRM_WIDTH,FRM_HEIGHT);
        frame.setResizable(false);
        frame.getContentPane().add(BorderLayout.NORTH, scrollPane);
        frame.getContentPane().add(BorderLayout.CENTER,textField);
        frame.getContentPane().add(BorderLayout.EAST,btnSend);
        frame.setVisible(true);
    }

    private void antistatic(){
        frameDraw(new Main());
        new thdReceiver().start();
    }

    public static  void main(String[] args){

       new Main().antistatic();

    }
}
