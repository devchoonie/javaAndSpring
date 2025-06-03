package kr.choonie.test.working;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


/*
 기존 인증번호를 받기위해 인증번호를 발급하고 이메일이나 폰으로 발송하는 로직을 하나의 트랜잭션에 묶음.
 이렇게 db를 사용하지 않으면서 외부와 통신하는 로직을 트랜잭션에 묶으면 외부와 통신하는 동안 커넥션을 점유하고 있게되기 때문에
 잘못된 설계.
 이를 위한 해결으로 트랜잭션메서드와 일반 메서드를 분리해서 호출하되 이 과정에서 예외 발생시 후처리하는 코드 작성.
 */
@Service
@RequiredArgsConstructor
public class AuthCodeProcessServiceImpl implements AuthCodeProcessService {

    private final AuthCodeService authCodeService;
    private final SmsSender smsSender;
    private final EmailSender emailSender;

    @Override
    public AuthCodeDto saveAuthCodeSendEmailOrPhone(MemberDto memberDto, String method, String updateTarget) {
        //단순한 일반 인증번호 생성 일반메서드
        String code = authCodeService.getNumberCode();

        AuthCodeDto authCodeDto = null;

        try {
            //@Transactional(rollbackFor = Exception.class) 메서드
            authCodeDto = authCodeService.saveAuthCode(memberDto, method, code, updateTarget);

            //분기에 따라 실행되는 메일보내기 일반 메서드
            if ("phone".equals(method)) {
                smsSender.sendOne(code, memberDto.getPhone()); //사용자에게 문자보내기
            } else if ("email".equals(method)) {
                emailSender.sendEmail(code, memberDto.getEmail());
            }

            return authCodeDto;

        } catch (Exception e) {
            //여기에 authcode삭제하는 로직 추가
            if(authCodeDto != null && authCodeDto.getAuthCodeId() != null){
                //@Transactional(propagation = Propagation.REQUIRES_NEW) 메서드
                authCodeService.deleteAuthCode(authCodeDto.getAuthCodeId());
            }
            e.printStackTrace();
            //예외가 발생해서 예외를 던진다.
            throw new RuntimeException();
        }
    }
}
