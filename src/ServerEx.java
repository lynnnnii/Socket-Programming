import java.io.*;
import java.net.*;

public class ServerEx {
    private static String processRequest(String request){
        try {
            String[] tokens = request.trim().split(" "); //공백 기준으로 나눔.

            //인자 갯수 검사, "MIN 5 2 1" 같은 형식 거름
            if (tokens.length != 3) {
                return "401 TOO_MANY_ARGS";
            }

            String command = tokens[0].toUpperCase(); // 명령어(ADD)
            int NUM1 = Integer.parseInt(tokens[1]); // 첫 번째 숫자
            int NUM2 = Integer.parseInt(tokens[2]); // 두 번째 숫자

            //사칙연산
            switch (command) {
                case "ADD":
                    return "200 " + (NUM1 + NUM2); // 200 코드와 결과 반환
                case "SUB":
                    return "200 " + (NUM1 - NUM2);
                case "MUL":
                    return "200 " + (NUM1 * NUM2);
                case "DIV":
                    // 0으로 나누기 예외 처리 (400 코드)
                    if (NUM2 == 0) {
                        return "400 Bad Request (Divide by Zero)";
                    } else {
                        return "200 " + (NUM1 / NUM2); // 정수 나눗셈
                    }
                default:
                    // "MIN 5 2" 처럼 알려지지 않은 명령어 (401 코드)
                    return "401 Bad Request (Invalid Format)";
            }
        } catch(NumberFormatException e){
            return "401 Bad Request (Invalid Format)";
        } catch (Exception e) {
            //기타 오류들 처리
            System.out.println("Processing Error: " + e.getMessage());
            return "500 Internal Server Error";
        }
    }


    public static void main(String[] args){
        ServerSocket listener = null; //클라이언트의 접속을 기다리는 서버 소켓
        Socket socket = null; //클라이언트와 1:1 통신
        BufferedReader in = null; //클라이언트로부터 데이터를 읽어올 스트림
        BufferedWriter out = null; //클라이언트에게 데이터를 보낼 스트림
        int nPort=1234;

        try {
            //포트번호 1234로 서버 소켓을 생성하고,이 포트로 들어오는 연결 요청을 받을 준비를 함.
            listener = new ServerSocket(nPort);
            System.out.println("연결을 기다리고 있습니다.....");
            //.accept가 호출 -> 클라이언트가 접속할 때까지 대기 -> 클라이언트 접속 -> 통신용 socket반환 및 진행
            socket=listener.accept();
            System.out.println("연결되었습니다.");

            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())); //클라이언트의 데이터를 읽음
            
            out = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())); //클라이언트에게 데이터를 보냄

            while(true){
                String inputMessage = in.readLine(); //클라이언트가 한 줄의 메시지를 보낼 때까지 대기
                
                // 클라이언트가 갑자기 연결을 끊었을 때 null이 반환됨.
                if (inputMessage == null) {
                    System.out.println("클라이언트 연결이 끊어졌습니다.");
                    break;
                }

                //클라이언트가 "bye" (대소문자 무관)를 보냈는지 확인
                if (inputMessage.equalsIgnoreCase("bye")) {
                    System.out.println("클라이언트에서 연결을 종료하였음");
                    break; // "bye"를 받으면 루프 종료
                }

                System.out.println(inputMessage); //받은 메시지를 화면에 출력

                String res = processRequest(inputMessage); // 받은 메시지를 processRequest로 보냄, 프로토콜 형식으로 변환

                out.write(res + "\n");
                out.flush(); // BufferedWriter는 flush()가 필수
                System.out.println("클라이언트로 보냄: " + res);
            }
        } catch (IOException e) {
            System.out.println("서버 오류: " + e.getMessage()); //예외처리
        } finally {
    
            System.out.println("서버를 종료합니다."); //예외가 발생하든,정상 종료되든 항상 실행
            
            try{ 
                //연 순서의 역순으로 닫는다.
                if (out != null) out.close(); // 출력 스트림 닫기
                if (in != null) in.close(); // 입력 스트림 닫기
                if (socket != null) socket.close(); // 클라이언트 통신용 소켓 닫기
                if (listener != null) listener.close(); // 서버 대기용 소켓 닫기
            } catch (IOException e) {
                System.out.println("자원 정리 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}