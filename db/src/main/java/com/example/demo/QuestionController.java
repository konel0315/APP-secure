package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/question") // 기본 URL
public class QuestionController {

	private final Encoder encoder;
    private final QuestionRepository questionRepository;

    @Autowired
    public QuestionController(QuestionRepository questionRepository,Encoder encoder) {
        this.questionRepository = questionRepository;
        this.encoder = encoder;//'
    }

    // 질문 추가 (POST)
    @PostMapping("/sign")
    public ResponseEntity<Question> createQuestion(@RequestBody Question question) {
        // 요청 받은 데이터 로그 출력
        System.out.println("Received Question: " + question.getUsername() + ", " + question.getPassword());
         Question user=encoder.registerUser(question.getUsername(), question.getPassword());
        
        Question savedQuestion = questionRepository.save(user); // DB에 저장
        return new ResponseEntity<>(savedQuestion, HttpStatus.CREATED); // 저장된 데이터 반환
    }
    @PostMapping("/login")  // "/hello" URL로 접근 시 이 메소드가 실행됩니다.
    public int sayHello(@RequestBody Question question) {
    	boolean k=encoder.authenticate(question.getUsername(), question.getPassword());
    	int a=-1;
    	if(k==true) {a=0;}
    	else if(k==false) {a=1;}
        return a;  // resources/templates/hello.html 파일을 찾아서 반환
    }
}