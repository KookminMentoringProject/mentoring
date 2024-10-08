package kookmin.kookmin.service.client;

import kookmin.kookmin.Utility.CryptoUtil;
import kookmin.kookmin.Utility.MailUtil;
import kookmin.kookmin.config.handler.SignatureNomatchException;
import kookmin.kookmin.dto.client.ReservationDto;
import kookmin.kookmin.dto.client.ReservationfullDto;
import kookmin.kookmin.mapper.client.MentoMapper;
import kookmin.kookmin.mapper.client.ReservationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    @Autowired
    private ReservationMapper reservationMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private PlanService planService;
    @Autowired
    private MentoMapper mentoMapper;

    @Autowired
    private JavaMailSender emailSender;

    @Value("${crypto.kookmin.key}")
    private String kookminKey;

    @Value("${spring.mail.username}")
    private String email;

    //private final String testEmail = "greylife5451@gmail.com";

    public boolean newReservationMailSend(ReservationDto reservationDto) {
        SimpleDateFormat sdfAddTime = new SimpleDateFormat("yyyy-MM-dd a hh:mm");
        SimpleDateFormat sdfDay = new SimpleDateFormat("yyyy-MM-dd");
        ReservationfullDto reservationfullDto = replaceFullDto(reservationDto);
        String title = "COFFWEE : ["+reservationfullDto.getUser().getNickname()+"]님의 예약 신청의 건";
        String asktype = switch (reservationfullDto.getAskType()){
            case 1 -> "전과";
            case 2 -> "복수전공";
            case 3 -> "부전공";
            case 4 -> "그 외";
            default -> "잘모르겠음";
        };
        String status = switch (reservationfullDto.getReservationStatus()){
            case 1 -> "확정 대기(입금 전)";
            case 2 -> "예약 확정(입금 후)";
            case 3 -> "멘토링 완료(희망날짜가 모두 지남)";
            default -> "환불 신청 중";
        };
        String content = new StringBuilder("[예약 내용]\n")
            .append("질문타입 : ").append(asktype).append("\n")
            .append("질문내용 : ").append(reservationfullDto.getAskContent()).append("\n")
            .append("예약한날짜 : ").append(sdfDay.format(reservationfullDto.getReservationDate())).append("\n")
            .append("희망날짜1 : ").append(sdfAddTime.format(reservationfullDto.getDesiredDate1())).append("\n")
            .append("희망날짜2 : ").append(sdfAddTime.format(reservationfullDto.getDesiredDate2())).append("\n")
            .append("예약상태 : ").append(status).append("\n")
            .append("위치 : ").append(reservationfullDto.getPosition()).append("\n\n")
            .append("[신청자 정보]\n")
            .append("닉네임 : ").append(reservationfullDto.getUser().getNickname()).append("\n")
            .append("메일주소 : ").append(reservationfullDto.getUser().getEmail()).append("\n")
            .append("연락처 : ").append(reservationfullDto.getUser().getPhone()).append("\n")
            .append("학과 : ").append(reservationfullDto.getUser().getDepartment()).append("\n")
            .append("학번 : ").append(reservationfullDto.getUser().getStudentNumber()).append("\n")
            .append("학년 : ").append(reservationfullDto.getUser().getGrade()).append("학년\n\n")
            .append("[멘토 정보]\n")
            .append("닉네임 : ").append(reservationfullDto.getMento().getNickname()).append("\n")
            .append("메일주소 : ").append(reservationfullDto.getMento().getEmail()).append("\n")
            .append("연락처 : ").append(reservationfullDto.getMento().getPhone()).append("\n")
            .append("학과 : ").append(reservationfullDto.getMento().getDepartment()).append("\n")
            .append("학번 : ").append(reservationfullDto.getMento().getStudentNumber()).append("\n")
            .append("학년 : ").append(reservationfullDto.getMento().getGrade()).append("학년\n").toString();
        boolean ret = MailUtil.sendEmail(emailSender, email, email, title, content);
        return ret;
    }
    public boolean refundMailSend(ReservationDto reservationDto, String titleAddTxt) {
        SimpleDateFormat sdfAddTime = new SimpleDateFormat("yyyy-MM-dd a hh:mm");
        ReservationfullDto reservationfullDto = replaceFullDto(reservationDto);
        String title = "COFFWEE : ["+reservationfullDto.getUser().getNickname()+"]님의 "+ titleAddTxt;
        String content = new StringBuilder("[환불 정보]\n")
                .append("환불 신청 날짜 : ").append(sdfAddTime.format(reservationfullDto.getRefundTime())).append("\n")
                .append("환불 은행 : ").append(reservationfullDto.getRefundBankName()).append("\n")
                .append("환불 계좌 : ").append(reservationfullDto.getRefundBankNum()).append("\n\n")
                .append("[환불 신청자 정보]\n")
                .append("닉네임 : ").append(reservationfullDto.getUser().getNickname()).append("\n")
                .append("메일주소 : ").append(reservationfullDto.getUser().getEmail()).append("\n")
                .append("연락처 : ").append(reservationfullDto.getUser().getPhone()).append("\n\n")
                .append("[멘토 정보]\n")
                .append("닉네임 : ").append(reservationfullDto.getMento().getNickname()).append("\n")
                .append("메일주소 : ").append(reservationfullDto.getMento().getEmail()).append("\n")
                .append("연락처 : ").append(reservationfullDto.getMento().getPhone()).append("\n").toString();
        boolean ret = MailUtil.sendEmail(emailSender, email, email, title, content);
        return ret;
    }

    public List<ReservationDto> findByEmail(String email) {
        return reservationMapper.findByEmail(email);
    }
    public List<ReservationDto> findByMentoId(String mentoId){
        return reservationMapper.findByMentoId(mentoId);
    }

    public ReservationfullDto replaceFullDto(ReservationDto r) {
        ReservationfullDto rf = new ReservationfullDto();
        if(r == null){
            System.out.println("r 이 null임");
            return null;
        }
        rf.setMentoId(r.getMentoId());
        rf.setReservationId(r.getReservationId());
        rf.setAskType(r.getAskType());
        rf.setAskContent(r.getAskContent());
        rf.setDesiredDate1(r.getDesiredDate1());
        rf.setDesiredDate2(r.getDesiredDate2());
        rf.setReservationDate(r.getReservationDate());
        rf.setReservationStatus(r.getReservationStatus());
        rf.setPosition(r.getPosition());
        rf.setUser(userService.findByUserId(r.getUserId()));
        rf.setMento(mentoMapper.findUserByMentoId(r.getMentoId()));
        rf.setPlan(planService.findByPlanTitle(r.getPlanTitle()));
        rf.setReviewScore(r.getReviewScore());
        rf.setReviewContent(r.getReviewContent());
        rf.setReviewDate(r.getReviewDate());
        if(r.getReservationStatus() == 4){
            rf.setRefundTime(r.getRefundTime());
            rf.setRefundBankName(r.getRefundBankName());
            rf.setRefundBankNum(decryptBankNums(r.getRefundBankNum()));
        }
        return rf;
    }

    public Map<Integer, List<ReservationfullDto>> findByEmailSplitStatus(String email){
        Map<Integer, List<ReservationfullDto>> map = findByEmail(email).stream()
                .map(r -> replaceFullDto(r)).collect(Collectors.groupingBy(r -> r.getReservationStatus()));

        return map;
    }

    public HashMap<String, Integer> myInfoNums(String email){
        HashMap<String, Integer> map = new HashMap<>();
        String []keyset = {"yetMoneyNum", "moneyGet","reviewNo","refundStay"};
        for(int i = 0; i < keyset.length; i++){
            List<ReservationfullDto> list =findByEmailSplitStatus(email).get(i+1);
            if(list != null){
                if(keyset[i].equals("reviewNo")){
                    list = list.stream().filter(r-> r.getReviewDate() == null).toList();
                }
                map.put(keyset[i], list.size());
            }else{
                map.put(keyset[i], 0);
            }
        }
        return map;
    }

    public Boolean signatureChk(ReservationDto reservationDto){
        String originSign = reservationMapper.findSignatureById(reservationDto.getReservationId());
        ReservationDto oldR = findById(reservationDto.getReservationId());
        String newSign = null;
        try{
            String num = CryptoUtil.decryptDataByAES(oldR.getRefundBankNum(),kookminKey);
            System.out.println(num);
            oldR.setRefundBankNum(num);
            newSign = CryptoUtil.hashingSha512(oldR.getRefundBankNum()+"_"+oldR.getRefundBankName()+"_"+oldR.getReservationId());
        }catch(Exception e){
            System.out.println(e);
            e.printStackTrace();

        }
        return originSign.equals(newSign);
    }
    public void updateReservationSignature(ReservationDto reservationDto){
        String signature = null;
        try{
            signature = CryptoUtil.hashingSha512(reservationDto.getRefundBankNum()+"_"+reservationDto.getRefundBankName()+"_"+reservationDto.getReservationId());
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        reservationMapper.updateSign(signature, reservationDto.getReservationId());
    }
    public void replaceBankNum(ReservationDto reservationDto){
        String replaceRefundBankNum = null;
        try{
            replaceRefundBankNum = CryptoUtil.encryptDataByAES(reservationDto.getRefundBankNum(), kookminKey);
        }catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
        reservationMapper.replaceBankNum(replaceRefundBankNum, reservationDto.getReservationId());
    }
    public void refundEdit(ReservationDto r){
        //서명검증
        Boolean flag = signatureChk(r);
        System.out.println("서명검증 결과값 :"+flag);
        System.out.println("서명검증 완료 해서 다음 메소드로 넘어옴");
        System.out.println("업데이트 할 값 : "+ r);

        try{
            if(flag){
                //서명검증 완료되면 DB 값 변경 및 변경된 값으로 r 값 변경
                reservationMapper.refundEdit(r);

                //r 값으로 새 서명검증 업데이트
                updateReservationSignature(r);

                //계좌 번호 암호화
                replaceBankNum(r);
            }else{
                //서명검증이 안되면 떤지기ㅠㅠ
                throw new SignatureNomatchException("서명검증 값이 알맞지 않은듯?");
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
    public void refund(ReservationDto reservationDto){
        reservationMapper.refund(reservationDto);
        updateReservationSignature(reservationDto);
        replaceBankNum(reservationDto);
    }

    public String decryptBankNums(String encryptBankNums){
        String decryptRefundBankNum = null;
        try{
            decryptRefundBankNum = CryptoUtil.decryptDataByAES(encryptBankNums, kookminKey);
        }catch(Exception e){
            System.out.println(e);
            e.printStackTrace();
        }
        return decryptRefundBankNum;
    }

    public ReservationDto findById(String reservationId){
        return reservationMapper.findById(reservationId);
    }

    public void review(ReservationDto reservationDto){
        reservationMapper.review(reservationDto);
    }

    public Date replaceDate(LocalDate ld, LocalTime lt){
        LocalDateTime ldt = LocalDateTime.of(ld, lt);
        Date d = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        return d;
    }

    public ReservationDto stepSetReservation01(String mentoId, String userId, LocalDate desiredDateDay1, LocalTime desiredDateTime1, LocalDate desiredDateDay2, LocalTime desiredDateTime2, ReservationDto InputReservation){
        ReservationDto reservationDto = new ReservationDto();
        Date d1 = replaceDate(desiredDateDay1, desiredDateTime1);
        Date d2 = replaceDate(desiredDateDay2, desiredDateTime2);
        reservationDto.setUserId(userId);
        if(mentoId != null && !mentoId.equals("")){
            reservationDto.setMentoId(mentoId);
        }
        reservationDto.setAskType(InputReservation.getAskType());
        reservationDto.setAskContent(InputReservation.getAskContent());
        reservationDto.setPosition(InputReservation.getPosition());
        reservationDto.setDesiredDate1(d1);
        reservationDto.setDesiredDate2(d2);
        reservationDto.setReservationDate(new Date());
        reservationDto.setReservationStatus(1);
        return reservationDto;
    }

    public void newReservation(ReservationDto reservationDto){
        reservationMapper.newReservation(reservationDto);
    }

    //입금 전(1)의 상태일 때 사용자가 취소를 눌렀을 경우
    public void replaceStatusCancel(String reservationId){
        reservationMapper.replaceStatusCancel(reservationId);
    }

    // 매일 자정에 입금 전(1) 의 상태일 때, 예약일이 모두 다 지나면 0으로 변하고, 메일이 가도록 변경
    @Scheduled(cron = "0 1 0  * * ?")
    private void autoCancelNomoney(){
        reservationMapper.autoReplaceStatusCancel();
    }
}
