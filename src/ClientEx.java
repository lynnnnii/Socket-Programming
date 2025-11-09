import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientEx {

    public static void main(String[] args){
        String serverIp = "localhost"; // 기본 IP
        int port = 1234; // 기본 포트
        String configFileName = "server_info.dat";

        try (BufferedReader fileReader = new BufferedReader(new FileReader(configFileName))) {
            //파일 읽기 시도
            System.out.println(configFileName + " 파일에서 서버 정보를 읽습니다.");
            serverIp = fileReader.readLine(); // 첫 번째 줄을 IP로 읽음
            port = Integer.parseInt(fileReader.readLine()); // 두 번째 줄을 포트로 읽음
            
            if (serverIp == null) serverIp = "localhost"; // 파일은 있으나 내용이 비었을 경우 대비

        } catch (FileNotFoundException e) {
            //파일이 없을 경우 (예외 처리)
            System.out.println(configFileName + " 파일을 찾을 수 없습니다.");
            System.out.println("기본 정보(" + serverIp + ":" + port + ")로 접속을 시도합니다.");
            // IP와 Port는 이미 기본값으로 설정되어 있으므로 아무것도 안 해도 됨
        } catch (IOException e) {
            //파일 읽기 중 오류 발생
            System.out.println(configFileName + " 파일 읽기 중 오류 발생: " + e.getMessage());
            return; // 프로그램 종료
        } catch (NumberFormatException e) {
            //포트 번호가 숫자가 아닐 경우
            System.out.println(configFileName + "에 포트 번호가 잘못 기록되었습니다. 기본 포트(1234)를 사용합니다.");
            port = 1234;
        }

        System.out.println("서버 접속 시도: " + serverIp + ":" + port);


        //서버 접속 및 통신
        BufferedReader in = null;
        BufferedWriter out = null;
        Socket socket = null;
        Scanner scanner = new Scanner(System.in); //키보드 입력을 받기 위한 scanner


        try {
            //서버에 접속 (Socket 생성)
            socket = new Socket(serverIp, port);
            System.out.println("서버에 연결되었습니다.");

        
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            //입력 및 통신 시작 
            while (true) {
                System.out.print("계산식 (예: ADD 10 20) >> ");
                String outputMessage = scanner.nextLine(); // 키보드에서 한 줄 입력 받음

                //서버로 메시지 전송
                out.write(outputMessage + "\n");
                out.flush();

                //사용자가 "bye"를 입력하면 루프 종료
                if (outputMessage.equalsIgnoreCase("bye")) {
                    System.out.println("연결을 종료합니다.");
                    break;
                }

                //서버로부터 응답 받기 (대기)
                String inputMessage = in.readLine();
                if (inputMessage == null) {
                    System.out.println("서버 연결이 끊어졌습니다.");
                    break;
                }

                //받은 응답을 파싱해서 출력 (프로토콜 기반)
                processResponse(inputMessage);
            }

        } catch (UnknownHostException e) {
            System.out.println("서버 IP를 찾을 수 없습니다 (" + serverIp + ")");
        } catch (IOException e) {
            System.out.println("서버와 통신 중 오류 발생: " + e.getMessage());
        } finally {
            //모든 자원 정리
            try {
                scanner.close(); // 스캐너 닫기
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.out.println("자원 정리 중 오류 발생: " + e.getMessage());
            }
        }
    }

    //응답을 사용자에게 보여주는 부분
    private static void processResponse(String response) {
        String[] tokens = response.split(" ", 2); // 응답을 [코드]와 [데이터] 2개로만 나눔
        String statusCode = tokens[0];

        if (statusCode.equals("200")) {
            // 성공
            System.out.println("Answer: " + tokens[1]);
        } else {
            // 실패 (400, 401)
            System.out.println("Error: " + tokens[1]); // 에러 메시지(데이터) 출력
        }

    }

}
