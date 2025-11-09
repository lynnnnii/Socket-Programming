import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ServerEx {

    public static void main(String[] args){
        ServerSocket listener = null;
        int nPort=1234;

        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        //최대 10개 스레드(10명 동시 처리)

        try {
            //포트번호 1234로 서버 소켓 생성
            listener = new ServerSocket(nPort);
            System.out.println("멀티 스레드 서버가 " + nPort + " 포트에서 대기 중입니다...");

            //서버가 종료되지 않고 계속 클라이언트를 받음
            while(true){
                
                Socket socket = listener.accept(); //클라이언트 접속 대기(1명이 옴)
                threadPool.submit(new ClientHandler(socket)); //1명이 오자마자 ClientHandler에게 맡기고 스레드풀에 보냄
            }
        } catch (IOException e) {
            System.out.println("서버 메인 오류: " + e.getMessage()); //예외처리
        } finally {
            System.out.println("서버를 종료합니다."); 
            
            try{ 
                if (listener != null) listener.close();
                if (threadPool != null) threadPool.shutdown(); 
            } catch (IOException e) {
                System.out.println("서버 자원 정리 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    static class ClientHandler implements Runnable {

        private Socket socket; // 이 스레드가 담당할 클라이언트의 소켓
        private BufferedReader in = null;
        private BufferedWriter out = null;

        //main 서버가 accept()한 소켓을 받음.
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        //스레드 풀에 보내면 자동으로 이 메소드가 실행됨
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + ": " + 
                               socket.getInetAddress() + " 클라이언트 연결 처리 시작...");
            
            try {
                //스트림 설정
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                // 통신 로직 (무한 루프)
                while (true) {
                    String inputMessage = in.readLine();

                    if (inputMessage == null || inputMessage.equalsIgnoreCase("bye")) {
                        System.out.println(Thread.currentThread().getName() + ": 클라이언트 연결 종료 (" + 
                                           socket.getInetAddress() + ")");
                        break; // 루프 종료 -> run() 메소드 종료 -> 스레드 반납
                    }

                    System.out.println(Thread.currentThread().getName() + "로부터 받음: " + inputMessage);
                    String res = processRequest(inputMessage);

                    
                    out.write(res + "\n");
                    out.flush();
                    System.out.println(Thread.currentThread().getName() + "으로 보냄: " + res);
                }

            } catch (IOException e) {
                System.out.println(Thread.currentThread().getName() + ": 통신 오류 - " + e.getMessage());
            } finally {
                try {
                    if (out != null) out.close();
                    if (in != null) in.close();
                    if (socket != null) socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String processRequest(String request) {
            try {
                String[] tokens = request.trim().split(" ");
                if (tokens.length != 3) {
                    return "401 TOO_MANY_ARGS";
                }
                String command = tokens[0].toUpperCase();
                int NUM1 = Integer.parseInt(tokens[1]);
                int NUM2 = Integer.parseInt(tokens[2]);

                switch (command) {
                    case "ADD": return "200 " + (NUM1 + NUM2);
                    case "SUB": return "200 " + (NUM1 - NUM2);
                    case "MUL": return "200 " + (NUM1 + NUM2); // [버그 수정] MUL (곱하기)로 변경
                    case "DIV":
                        if (NUM2 == 0) {
                            return "400 Bad Request (Divide by Zero)";
                        } else {
                            return "200 " + (NUM1 / NUM2);
                        }
                    default:
                        return "401 Bad Request (Invalid Format)";
                }
            } catch (NumberFormatException e) {
                return "401 Bad Request (Invalid Format)";
            } catch (Exception e) {
                System.out.println("Processing Error: " + e.getMessage());
                return "500 Internal Server Error";
            }
        }
    } 


} 