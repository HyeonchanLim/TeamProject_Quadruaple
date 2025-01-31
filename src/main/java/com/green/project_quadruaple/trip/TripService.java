package com.green.project_quadruaple.trip;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.green.project_quadruaple.trip.model.PathInfoVo;
import com.green.project_quadruaple.trip.model.PathType;
import com.green.project_quadruaple.trip.model.PathTypeVo;
import com.green.project_quadruaple.trip.model.PubTransPathVo;
import com.green.project_quadruaple.trip.model.req.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import com.green.project_quadruaple.common.config.constant.OdsayApiConst;
import com.green.project_quadruaple.common.config.enumdata.ResponseCode;
import com.green.project_quadruaple.common.model.ResponseWrapper;
import com.green.project_quadruaple.common.model.ResultResponse;
import com.green.project_quadruaple.trip.model.dto.*;
import com.green.project_quadruaple.trip.model.res.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripService {

    private final TripMapper tripMapper;
    private final OdsayApiConst odsayApiConst;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ResponseWrapper<MyTripListRes> getMyTripList() {
//        long signedUserId = AuthenticationFacade.getSignedUserId();
        long signedUserId = 101L;
        long now = new Date().getTime();
        List<TripDto> TripList = tripMapper.getTripList(signedUserId);

        List<TripDto> beforeTripList = new ArrayList<>();
        List<TripDto> afterTripList = new ArrayList<>();
        for (TripDto trip : TripList) {
            long tripEndTime = getMilliTime(trip.getEndAt());
            if(now > tripEndTime) {
                beforeTripList.add(trip);
            } else {
                afterTripList.add(trip);
            }
        }
        MyTripListRes res = new MyTripListRes();
        res.setBeforeTripList(beforeTripList);
        res.setAfterTripList(afterTripList);
        return new ResponseWrapper<>(ResponseCode.OK.getCode(), res);
    }

    public ResponseWrapper<LocationRes> getLocationList() {
        List<LocationDto> dto = tripMapper.selLocationList();
        LocationRes res = new LocationRes();
        res.setLocationList(dto);
        return new ResponseWrapper<>(ResponseCode.OK.getCode(), res);
    }

    @Transactional
    public ResponseWrapper<PostTripRes> postTrip(PostTripReq req) {
        long signedUserId = 102L;
        req.setManagerId(signedUserId);
        tripMapper.insTrip(req);
        tripMapper.insTripLocation(req.getTripId(), req.getLocationId());
        PostTripRes res = new PostTripRes();
        res.setTripId(req.getTripId());
        return new ResponseWrapper<PostTripRes>(ResponseCode.OK.getCode(), res);
    }

    public ResponseWrapper<TripDetailRes> getTrip(long tripId) {
        ScheCntAndMemoCntDto scamcdDto = tripMapper.selScheduleCntAndMemoCnt(tripId);
        List<TripDetailDto> tripDetailDto = tripMapper.selScheduleDetail(tripId);
        long totalDistance = 0L;
        long totalDuration = 0L;
        for (TripDetailDto detailDto : tripDetailDto) {
            detailDto.setWeather("sunny"); // 날씨 API 받아와야함
            for (ScheduleDto schedule : detailDto.getSchedules()) {
                Long distance = schedule.getDistance();
                Long duration = schedule.getDuration();
                if(distance == null || duration == null) {
                    continue;
                }
                totalDistance += distance;
                totalDuration += duration;
            }
        }
        TripDetailRes res = new TripDetailRes();
        res.setScheduleCnt(scamcdDto.getScheduleCnt());
        res.setMemoCnt(scamcdDto.getMemoCnt());
        res.setDays(tripDetailDto);
        res.setTotalDistance(totalDistance);
        res.setTotalDuration(totalDuration);
        return new ResponseWrapper<>(ResponseCode.OK.getCode(), res);
    }

    @Transactional
    public ResultResponse patchTrip(PatchTripReq req) {
        long tripId = req.getTripId();
        tripMapper.updateTrip(req);

        // 참여 유저 수정
        if(req.getInsUserList() != null && !req.getInsUserList().isEmpty()) {
            tripMapper.insTripUser(tripId, req.getInsUserList());
        }

        if(req.getDelUserList() != null && !req.getDelUserList().isEmpty()) {
            List<Long> scheduleUserIdList = tripMapper.selScheduleUserId(tripId, req.getDelUserList());
            if(scheduleUserIdList != null && !scheduleUserIdList.isEmpty()) {
                tripMapper.delTripMemo(scheduleUserIdList);
                tripMapper.delTripScheMemo(scheduleUserIdList);
            }
            tripMapper.delTripUser(tripId, req.getDelUserList());
        }

        if(req.getInsLocationList() != null && !req.getInsLocationList().isEmpty()) {
            tripMapper.insTripLocation(tripId, req.getInsLocationList());
        }

        if(req.getDelLocationList() != null && !req.getDelLocationList().isEmpty()) {
            tripMapper.delTripLocation(tripId, req.getDelLocationList());
        }

        // 여행 지역 수정
        return ResultResponse.success();
    }

    /*
     * 1. 요청 값인 상품ID 로 DB 에서 상품의 locationId 를 찾기
     * 2. 로그인 유저의 여행 리스트 데이터 DB 에서 가져오기
     * 3. 여행 리스트 중 현재 시간보다 endAt 이 미래라면 미완료 여행으로 파악.
     * 4. 미완료 여행의 총 일차를 계산해서 totalDay 에 담음
     * 5. 미완료 여행 목록 중 상품의 locationId 를 이미 가지고 있는 여행이라면 locateTripList 에 저장, 아니라면 totalTripList 에 저장
     * */
    public ResponseWrapper<IncompleteTripRes> getIncomplete(long strfId) {
        long signedUserId = 101L;
        long strfLocationId = tripMapper.selStrfLocationId(strfId); // 상품의 locationId 찾기
        long now = new Date().getTime();
        List<TripIdMergeDto> dtoList = tripMapper.selIncompleteTripList(signedUserId); // 로그인 유저의 여행 리스트 데이터 DB 에서 가져오기
        if(dtoList == null || dtoList.isEmpty()) { // 일정 없으면 return
            return new ResponseWrapper<>(ResponseCode.OK.getCode(), null);
        }

        IncompleteTripRes res = new IncompleteTripRes();
        List<IncompleteTripDto> locateTripList = new ArrayList<>();
        List<IncompleteTripDto> totalTripList = new ArrayList<>();
        for (TripIdMergeDto groupId : dtoList) {
            IncompleteTripDto temp = null;
            long endAt = getMilliTime(groupId.getIncompleteTripList().get(0).getEndAt());
            if(now > endAt) continue; // 해당 여행의 endAt 이 현재시간보다 과거일시 완료된 여행이므로 넘김
            for (IncompleteTripDto dto : groupId.getIncompleteTripList()) { // tripId 를 그룹으로 잡은 리스트
                if(dto.getLocationId() == strfLocationId) {
                    temp = dto;
                    break;
                } else {
                    temp = dto;
                }
            }
            if(temp == null) break;
            temp.setTotalDay(totalDay(temp.getStartAt(), temp.getEndAt())); // 총 일자 계산해서 totalDay 에 저장
            if(temp.getLocationId() == strfLocationId) { // 상품의 locationId 를 이미 가지고 있는 여행이라면 locateTripList 에 저장, 아니라면 totalTripList 에 저장
                locateTripList.add(temp);
            } else {
                totalTripList.add(temp);
            }
        }
        res.setMatchTripId(locateTripList);
        res.setNoMatchTripId(totalTripList);
        return new ResponseWrapper<>(ResponseCode.OK.getCode(), res);
    }

    /*
    * shcedule 테이블에 상품(일정)을 저장. 이동수단과 거리는 미정
    * trip_location 테이블에 상품의 location 이 존재하지 않으면 저장.
    * */
//    public ResultResponse postIncomplete(PostStrfScheduleReq req) {
//        long tripId = req.getTripId();
//        long strfId = req.getStrfId();
//        Long existLocation = tripMapper.existLocation(tripId, strfId);
//
//        if(existLocation == null) {
//            long locationId = tripMapper.selStrfLocationId(strfId);
//            tripMapper.insTripLocation(tripId, List.of(locationId));
//        }
//        tripMapper.insScheMemo(req);
//        tripMapper.insSchedule(req);
//        return ResultResponse.success();
//    }

    /*
    * 길찾기
    * */
    public ResponseWrapper<List<FindPathRes>> getTransPort(FindPathReq req) {

        log.info("odsayConst.getBaseUrl = {}", odsayApiConst.getBaseUrl());
        log.info("odsayConst.getSearchPubTransPathUrl = {}", odsayApiConst.getSearchPubTransPathUrl());
        String json = httpPostRequestReturnJson(req);

        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            PubTransPathVo pathVo =  objectMapper.convertValue(jsonNode.at("/result")
                    , new TypeReference<>() {});
            log.info("pathVo = {}", pathVo);
            if(pathVo.getPath() == null) {
                return new ResponseWrapper<>(ResponseCode.WRONG_XY_VALUE.getCode(), null);
            }
            List<FindPathRes> res = new ArrayList<>();
            for (PathTypeVo pathList : pathVo.getPath()) { // path 리스트
                FindPathRes path = new FindPathRes(); // res에 담을 객체 생성
                PathInfoVo info = pathList.getInfo();
                String pathName = Optional.ofNullable(PathType.getKeyByValue(pathList.getPathType())).orElse(PathType.WALK).getName();
                path.setPathType(pathName);
                path.setTotalTime(info.getTotalTime());
                path.setTotalDistance(info.getTotalDistance());
                if(pathVo.getSearchType() == 0) { // 도시내 이동
                    path.setPayment(pathList.getInfo().getPayment());
                } else { // 도시와 도시간 이동
                    path.setPayment(pathList.getInfo().getTotalPayment());
                }
                res.add(path);
            }
            return new ResponseWrapper<>(ResponseCode.OK.getCode(), res);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseWrapper<>(ResponseCode.SERVER_ERROR.getCode(), null);
        }
    }

    @Transactional
    public ResultResponse postSchedule(PostScheduleReq req) {
        long tripId = req.getTripId();
        long strfId = req.getStrfId();
        Long existLocation = tripMapper.existLocation(tripId, strfId);

        if(existLocation == null) {
            long locationId = tripMapper.selStrfLocationId(strfId);
            tripMapper.insTripLocation(tripId, List.of(locationId));
        }

        PathType keyByName = PathType.getKeyByName(req.getPathType());
        if(keyByName != null) {
            req.setPathTypeValue(keyByName.getValue());
        }
        tripMapper.insScheMemo(req);
        tripMapper.insSchedule(req);
        return ResultResponse.success();
    }

    /*
    * 일정메모 순서 변경
    * */
    @Transactional
    public ResultResponse patchSeq(PatchSeqReq req) {
        long tripId = req.getTripId();
        long scheduleId = req.getScheduleId();
        int originSeq = req.getOriginSeq();
        int destSeq = req.getDestSeq();
        Integer destDay = req.getDestDay();
        boolean ahead = false;

        try {
            ScheduleDto scheduleDto = tripMapper.selScheduleAndScheMemoByScheduleId(scheduleId, tripId);
            boolean notFirst = scheduleDto.isNotFirst();

            if(originSeq > destSeq) {
                // 목적지 seq 가 기존 seq 보다 작은 값일 경우 앞으로 이동으로 간주.
                // 기존 seq-1 과 목적지 seq 사이의 seq 를 전부 +1
                ahead = true;
                originSeq -= 1;
            } else {
                // 반대의 경우, 기존 seq+1 과 목적지 seq 사이의 seq 를 전부 -1
                originSeq += 1;
            }
            tripMapper.updateBetweenSeq(tripId, originSeq, destSeq, ahead);
            tripMapper.updateSeq(scheduleId, destSeq);
            if(destDay != null) { // destDay 가 있다면 DB 수정
                tripMapper.updateDay(scheduleId, destDay);
            }

            if(scheduleDto.getScheOrMemo().equals("MEMO")) {
                log.info("메모 변경 완료");
                return ResultResponse.success();
            }

            /*
            * 기본 로직
            * 1. (완료)A의 원래 자리의 다음 일정 거리, 시간, 수단을 원래 자리의 이전 일정 위치로 계산.
            * 2. A의 변경된 위치의 이전 일정과 거리, 시간, 수단을 재 계산
            * 3. A의 변경된 위치의 다음 일정의 거리, 시간, 수단을 재 계산
            *
            * A의 원래 자리가 첫 일정이라면 (위치가 변동된 일정은 A)
            * 1-1. A의 원래 자리의 다음 일정 거리, 시간, 수단을 NULL 로 변경
            *
            * A의 원래 자리가 마지막 일정이라면
            * 1-1. 변경 없음
            *
            * A의 변경된 위치가 첫 일정이라면
            * 2-1. 변경 없음
            *
            * A의 변경된 위치가 마지막 일정이라면
            * 3-1. 변경 없음
            * */
            FindPathReq findPathReq;
            if(!notFirst) { // 원래 자리가 첫 일정이라면
                tripMapper.updateSchedule(false, scheduleDto.getNextScheduleId(), 0, 0, 0);
            } else if (scheduleDto.getNextScheduleStrfId() != null) { // 원래 자리가 마지막 일정이 아니라면
//                // 원래 자리의 다음 일정 거리, 시간, 수단을 원래 자리의 이전 일정 위치로 계산.
                setSchedulePath(true, scheduleDto.getPrevScheduleStrfId(), scheduleDto.getNextScheduleStrfId(), scheduleDto.getNextScheduleId());
            }
//
//            // 여기서 부터는 목적지의 변경
            scheduleDto = tripMapper.selScheduleAndScheMemoByScheduleId(scheduleId, tripId); // 변경된 위치의 정보로 새로 불러옴
            if(!scheduleDto.isNotFirst()) {
                tripMapper.updateSchedule(false, scheduleDto.getScheduleMemoId(), 0, 0, 0);
            } else if(scheduleDto.getNextScheduleStrfId() != null) {
                // A의 변경된 위치의 다음 일정의 거리, 시간, 수단을 재 계산
                setSchedulePath(true, scheduleDto.getStrfId(), scheduleDto.getNextScheduleStrfId(), scheduleDto.getNextScheduleId());
//
//                // A의 변경된 위치의 이전 일정과 거리, 시간, 수단을 재 계산
                setSchedulePath(true, scheduleDto.getPrevScheduleStrfId(), scheduleDto.getStrfId(), scheduleId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        log.info("일정 순서 변경 완료");
        return ResultResponse.success();
    }

    private void setSchedulePath(boolean isNotFirst, long prevScheduleStrfId, long nextScheduleStrfId, long nextScheduleId) {
        List<StrfLatAndLngDto> strfLatAndLngDtoList = tripMapper.selStrfLatAndLng(prevScheduleStrfId, nextScheduleStrfId);
        FindPathReq findPathReq = new FindPathReq();
        setOdsayParams(findPathReq, strfLatAndLngDtoList, prevScheduleStrfId);
        String json = httpPostRequestReturnJson(findPathReq);
        PathTypeVo firstPath = getFirstPathType(json);
        int pathType = firstPath.getPathType();
        int distance = firstPath.getInfo().getTotalDistance();
        int duration = firstPath.getInfo().getTotalTime();
        tripMapper.updateSchedule(isNotFirst, nextScheduleId, distance, duration, pathType);
    }

    /*
    * 일정 삭제
    * 1. 만약 첫번째 일정을 삭제한다면 다음으로 첫번째 일정이 되는 일정의 시간, 거리, 이동수단을 null 로 바꿔야함
    * 2. 삭제하는 일정의 seq 보다 seq가 높은 일정+메모들의 seq 를 모두 -1 해주어야함
    * 3. sche_memo 의 category 가 SCHE 인 가장 가까운 seq 의 일정은 거리, 시간, 이동수단을 삭제하는 일정의 이전 일정과 다시 맞추어야함
    * 4. 먄약 삭제하는 일정이 마지막 일정이라면 그냥 일정만 삭제하면 됨
    * */
    @Transactional
    public ResultResponse deleteSchedule(long scheduleId) {

        long tripId = tripMapper.selScheduleByScheduleId(scheduleId); // 삭제할 일정의 여행 ID
        ScheduleDto scheduleDto = tripMapper.selScheduleAndScheMemoByScheduleId(scheduleId, tripId); // 삭제할 일정
        if(!scheduleDto.isNotFirst()) { // 첫번째 일정일때는 다음일정의 거리, 시간, 이동수단을 null 로 바꾸고 끝.
            tripMapper.updateSeqScheMemo(scheduleDto.getTripId(), scheduleDto.getSeq()); // ScheMemo 의 시퀀스 변경
            tripMapper.updateSchedule(scheduleDto.isNotFirst(), scheduleDto.getNextScheduleId(), 0, 0, 0); // 삭제한 일정의 다음 일정 정보 수정
            tripMapper.deleteSchedule(scheduleId); // schedule 삭제
            tripMapper.deleteScheMemo(scheduleId); // ScheMemo 삭제
            return ResultResponse.success();
        }
        List<StrfLatAndLngDto> prevAndNextStrfLatAndLng = tripMapper.selStrfLatAndLng(scheduleDto.getPrevScheduleStrfId(), scheduleDto.getNextScheduleStrfId()); // 이걸로 API 요청
        FindPathReq params = new FindPathReq();
        setOdsayParams(params, prevAndNextStrfLatAndLng, scheduleDto.getPrevScheduleStrfId());
        String json = httpPostRequestReturnJson(params);

        try {
            PathTypeVo firstPath = getFirstPathType(json);
            int pathType = firstPath.getPathType();
            int distance = firstPath.getInfo().getTotalDistance();
            int duration = firstPath.getInfo().getTotalTime();

            tripMapper.updateSeqScheMemo(scheduleDto.getTripId(), scheduleDto.getSeq()); // ScheMemo 의 시퀀스 변경
            tripMapper.updateSchedule(scheduleDto.isNotFirst(), scheduleDto.getNextScheduleId(), distance, duration, pathType); // 삭제한 일정의 다음 일정 정보 수정
            tripMapper.deleteSchedule(scheduleId); // schedule 삭제
            tripMapper.deleteScheMemo(scheduleId); // ScheMemo 삭제
            return ResultResponse.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ResultResponse.severError();
        }
    }

    @Transactional
    public ResultResponse deleteTripUser(DeleteTripUserReq req) {
        long signedUserId = 101L;
        long tripId = req.getTripId();
        long targetUserId = req.getTargetUserId();

        Long managerId = tripMapper.selTripById(tripId);
        if(managerId != req.getLeaderId() || signedUserId != targetUserId || managerId == targetUserId) {
            return ResultResponse.forbidden();
        }

        tripMapper.disableTripUser(tripId, targetUserId);
        return ResultResponse.success();
    }

    private long getMilliTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        try {
            return sdf.parse(time).getTime();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private long totalDay(String startAt, String endAt) {

        LocalDate startDate = LocalDate.of(
                Integer.parseInt(startAt.substring(0, 4)),
                Integer.parseInt(startAt.substring(5, 7)),
                Integer.parseInt(startAt.substring(8, 10)));

        LocalDate endDate = LocalDate.of(
                Integer.parseInt(endAt.substring(0, 4)),
                Integer.parseInt(endAt.substring(5, 7)),
                Integer.parseInt(endAt.substring(8, 10)));

        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    private String httpPostRequestReturnJson(FindPathReq req) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(odsayApiConst.getParamApiKeyName(), odsayApiConst.getParamApiKeyValue());
        formData.add(odsayApiConst.getParamStartLatName(), req.getStartLatSY());
        formData.add(odsayApiConst.getParamStartLngName(), req.getStartLngSX());
        formData.add(odsayApiConst.getParamEndLatName(), req.getEndLatEY());
        formData.add(odsayApiConst.getParamEndLngName(), req.getEndLngEX());

        return webClient.post()
                .uri(odsayApiConst.getSearchPubTransPathUrl())
                .body(BodyInserters.fromFormData(formData))
                .retrieve() //통신 시도
                .bodyToMono(String.class) // 결과물을 String변환
                .block(); //비동기 > 동기
        // log.info("json = {}", json);
    }

    private void setOdsayParams(FindPathReq params, List<StrfLatAndLngDto> LatLngDtoList, long prevScheduleStrfId) {
        for(StrfLatAndLngDto strfLatAndLngDto : LatLngDtoList) {
            if(strfLatAndLngDto.getStrfId() == prevScheduleStrfId) {
                params.setStartLngSX(strfLatAndLngDto.getLng());
                params.setStartLatSY(strfLatAndLngDto.getLat());
            } else {
                params.setEndLngEX(strfLatAndLngDto.getLng());
                params.setEndLatEY(strfLatAndLngDto.getLat());
            }
        }
    }

    private PathTypeVo getFirstPathType(String json) {
        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            PubTransPathVo pathVo =  objectMapper.convertValue(jsonNode.at("/result")
                    , new TypeReference<>() {});
            log.info("pathVo = {}", pathVo);
            if(pathVo.getPath() == null) {
                throw new RuntimeException();
            }
            return pathVo.getPath().get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
