package com.green.project_quadruaple.expense;

import com.green.project_quadruaple.common.config.enumdata.ResponseCode;
import com.green.project_quadruaple.common.config.security.AuthenticationFacade;
import com.green.project_quadruaple.common.model.ResponseWrapper;
import com.green.project_quadruaple.expense.model.dto.DeDto;
import com.green.project_quadruaple.expense.model.dto.DutchPaidUserDto;
import com.green.project_quadruaple.expense.model.req.DutchReq;
import com.green.project_quadruaple.expense.model.req.ExpenseInsReq;
import com.green.project_quadruaple.expense.model.res.DutchRes;
import com.green.project_quadruaple.expense.model.res.ExpensesRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseMapper expenseMapper;
    private final AuthenticationFacade authenticationFacade;

    //추가하기
    public ResponseEntity<ResponseWrapper<Long>> insSamePrice(ExpenseInsReq p){
        if(!expenseMapper.IsUserInTrip(p.getTripId(),authenticationFacade.getSignedUserId())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseWrapper<>(ResponseCode.Forbidden.getCode(), null));
        }
        DeDto d=new DeDto(null, p.getPaidFor());
        expenseMapper.insDe(d);
        Long deId=d.getDeId();
        if(deId==null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseWrapper<>(ResponseCode.NOT_FOUND.getCode(), null));}
        p.setDeId(deId);
        log.info("service>p:{}",p);
        int result=expenseMapper.insPaid(p);
        if(result==0){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ResponseWrapper<>(ResponseCode.SERVER_ERROR.getCode(), null));
        }
        return ResponseEntity.ok(new ResponseWrapper<>(ResponseCode.OK.getCode(),deId));
    }

    //정산하기
    public ResponseEntity<ResponseWrapper<DutchRes>> dutchExpenses(DutchReq p){
        if(!expenseMapper.IsUserInTrip(p.getTripId(),authenticationFacade.getSignedUserId())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseWrapper<>(ResponseCode.Forbidden.getCode(), null));
        }
        int totalPrice=p.getTotalPrice();
        List<DutchPaidUserDto> dutchPaidUserDtos=expenseMapper.selDutchUsers(p);
        int price=totalPrice/dutchPaidUserDtos.size();
        for(DutchPaidUserDto dto:dutchPaidUserDtos){
            dto.setPrice(price);
        }
        if(price*dutchPaidUserDtos.size() != totalPrice){
            Random r=new Random();
            int morePrice=totalPrice-price*dutchPaidUserDtos.size()+price;
            dutchPaidUserDtos.get(r.nextInt(dutchPaidUserDtos.size())).setPrice(morePrice);
        }
        DutchRes res=new DutchRes(p.getTotalPrice(),dutchPaidUserDtos);
        return ResponseEntity.ok(new ResponseWrapper<>(ResponseCode.OK.getCode(),res));
    }

    //가계부 보기
    public ResponseEntity<ResponseWrapper<ExpensesRes>> getExpenses(long tripId){
        long userId=authenticationFacade.getSignedUserId();
        if(!expenseMapper.IsUserInTrip(tripId,userId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseWrapper<>(ResponseCode.Forbidden.getCode(), null));
        }
        ExpensesRes result= expenseMapper.getExpenses(tripId,userId);
        if(result==null){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ResponseWrapper<>(ResponseCode.SERVER_ERROR.getCode(), null));
        }
        return ResponseEntity.ok(new ResponseWrapper<>(ResponseCode.OK.getCode(),result));
    }
}
