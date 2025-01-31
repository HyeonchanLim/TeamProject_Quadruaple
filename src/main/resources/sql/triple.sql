

# 유저
CREATE TABLE user(
                     user_id BIGINT PRIMARY KEY auto_increment
    , profile_pic VARCHAR(500)
    , name VARCHAR(20) NOT null
    , email VARCHAR(50) NOT NULL
    , pw VARCHAR(200) NOT NULL
    , state TINYINT NOT NULL
    , created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

# 임시 비밀번호
CREATE TABLE temporary_pw (
user_id BIGINT PRIMARY key
, tp_pw VARCHAR(300) NOT NULL
, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
, FOREIGN KEY(user_id) REFERENCES user(user_id)
);

# 사업자 번호
CREATE TABLE business_num(
                             busi_num varchar(20) PRIMARY KEY
    , user_id BIGINT NOT NULL
    , FOREIGN KEY(user_id) REFERENCES user(user_id)
    , CONSTRAINT busi_user UNIQUE (busi_num, user_id)
);

#지역
CREATE TABLE location(
                         location_id BIGINT PRIMARY KEY AUTO_INCREMENT
    , title varchar(50) NOT NULL UNIQUE
    , location_pic varchar(200) NOT NULL
);

# 지역 세부
CREATE TABLE location_detail (
                                 location_detail_id BIGINT PRIMARY KEY AUTO_INCREMENT
    , location_id BIGINT NOT NULL
    , detail_title varchar(50) NOT NULL
    , FOREIGN KEY(location_id) REFERENCES location (location_id)
);

#숙소/맛집/축제/관광
CREATE TABLE stay_tour_restaur_fest(
                                       strf_id BIGINT PRIMARY KEY AUTO_INCREMENT
    , category enum('STAY', 'TOUR', 'RESTAUR', 'FEST') NOT NULL
    , title varchar(100) NOT NULL
    , lat double NOT NULL
    , lng double NOT NULL
    , address varchar(100) NOT NULL
    , location_detail_id BIGINT NOT NULL
    , post varchar(50)
    , tell varchar(50)
    , start_at date
    , end_at date
    , `open_check` time
    , `close_check` time
    , rest_date varchar(100)
    , `explain` varchar(600)
    , `detail` text
    , created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
    , busi_num varchar(20) NOT NULL
    , FOREIGN KEY(busi_num) REFERENCES business_num(busi_num)
    , FOREIGN KEY(location_detail_id) REFERENCES location_detail(location_detail_id)
);

#여행
CREATE TABLE trip(
                     trip_id BIGINT PRIMARY KEY AUTO_INCREMENT
    , title VARCHAR(200) NOT NULL
    , manager_id BIGINT NOT NULL
    , start_at date NOT NULL
    , end_at date NOT NULL
    , created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    , FOREIGN KEY(manager_id) REFERENCES user(user_id)
);

#여행 참여 인원
CREATE TABLE trip_user(
                          trip_user_id BIGINT PRIMARY KEY AUTO_INCREMENT
    , trip_id BIGINT NOT NULL
    , user_id BIGINT NOT NULL
    , created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
    , `disable` TINYINT NOT NULL DEFAULT '0'
    , FOREIGN KEY(trip_id) REFERENCES trip (trip_id)
    , FOREIGN KEY(user_id) REFERENCES user (user_id)
);

# 여행 장소
CREATE TABLE trip_location (
                               trip_id BIGINT NOT NULL
    , location_id BIGINT NOT NULL
    , FOREIGN KEY(trip_id) REFERENCES trip(trip_id)
    , FOREIGN KEY(location_id) REFERENCES location(location_id)
);

# 일정_메모
CREATE TABLE sche_memo(
                          schedule_memo_id BIGINT PRIMARY KEY AUTO_INCREMENT
    , trip_id BIGINT NOT NULL
    , day int NOT NULL
    , seq int NOT NULL
    , created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
    , category enum('SCHE', 'MEMO') NOT NULL
    , FOREIGN KEY(trip_id) REFERENCES trip (trip_id)
);

# 메모
CREATE TABLE memo(
                     memo_id BIGINT PRIMARY KEY
    , trip_user_id bigint NOT null
    , title varchar(50) NOT NULL
    , content varchar(1000) NOT NULL
    , updated_at datetime
    , FOREIGN KEY(memo_id) REFERENCES sche_memo (schedule_memo_id)
    , FOREIGN KEY(trip_user_id) REFERENCES trip_user(trip_user_id)
);

#일정
CREATE TABLE schedule(
                         schedule_id BIGINT PRIMARY KEY
    , `distance` DOUBLE
    , duration INT
    , pathtype TINYINT
    , strf_id BIGINT NOT NULL
    , updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP
    , FOREIGN KEY(schedule_id) REFERENCES sche_memo (schedule_memo_id)
    , FOREIGN KEY(strf_id) REFERENCES stay_tour_restaur_fest (strf_id)
);

#상품 사진
CREATE TABLE strf_pic(
                         pic_name VARCHAR(100)
    , strf_id BIGINT NOT NULL
    , PRIMARY KEY (pic_name, strf_id)
    , FOREIGN KEY(strf_id) REFERENCES stay_tour_restaur_fest (strf_id)
);

# 메뉴
CREATE TABLE menu(
                     menu_id BIGINT PRIMARY KEY AUTO_INCREMENT
    , strf_id BIGINT NOT NULL
    , title varchar(50) NOT NULL
    , price int NOT NULL
    , menu_pic varchar(200)
    , FOREIGN KEY(strf_id) REFERENCES stay_tour_restaur_fest(strf_id)
);

# 예약
CREATE TABLE booking(
    booking_id BIGINT PRIMARY KEY AUTO_INCREMENT
    , check_in DATETIME NOT NULL
    , check_out DATETIME NOT NULL
    , user_id BIGINT NOT NULL
    , menu_id BIGINT NOT NULL
    , final_payment int not null
    , FOREIGN KEY(user_id) REFERENCES user (user_id)
    , FOREIGN KEY(menu_id) REFERENCES menu (menu_id)
);

# 편의시설
CREATE TABLE amenity(
                        amenity_id BIGINT PRIMARY KEY auto_increment
    , title VARCHAR(20) NOT NULL
);

# 편의정보
CREATE TABLE amenipoint(
                           amenity_id BIGINT NOT NULL
    , strf_id BIGINT NOT NULL
    , created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    , FOREIGN KEY(amenity_id) REFERENCES amenity(amenity_id)
    , FOREIGN KEY(strf_id) REFERENCES stay_tour_restaur_fest(strf_id)
);

# 가계부
CREATE TABLE `daily_expense` (
    de_id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT
    , `for` varchar(200) NOT NULL
);

# 결제인원
CREATE TABLE `paid_user` (
     trip_user_id BIGINT NOT NULL
    , de_id BIGINT NOT NULL
    , price int NOT NULL
    , PRIMARY KEY(trip_user_id, de_id)
    , FOREIGN KEY (trip_user_id) REFERENCES trip_user(trip_user_id)
    , FOREIGN KEY (de_id) REFERENCES `daily_expense`(de_id)
);

# 후기
CREATE TABLE `review` (
                          review_id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT
    , content varchar(500) NOT NULL
    , rating TINYINT NOT NULL
    , strf_id BIGINT NOT NULL
    , user_id BIGINT NOT NULL
    , created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
    , updated_at datetime
    , FOREIGN KEY (user_id) REFERENCES user(user_id)
    , FOREIGN KEY (strf_id) REFERENCES stay_tour_restaur_fest(strf_id)
);

# 후기 사진
CREATE TABLE review_pic(
                           title VARCHAR(100) NOT NULL
    , review_id BIGINT NOT NULL
    , PRIMARY KEY (title, review_id)
    , FOREIGN KEY(review_id) REFERENCES `review` (review_id)
);

# 쿠폰
CREATE TABLE coupon(
                       coupon_id BIGINT PRIMARY KEY AUTO_INCREMENT
    , title VARCHAR(200) NOT NULL
    , expired_at DATETIME NOT NULL
    , discountper INT NOT NULL
    , distribute_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

# 쿠폰 수령자
CREATE TABLE receive_coupon(
                               receive_id BIGINT PRIMARY KEY AUTO_INCREMENT
    , user_id BIGINT NOT NULL
    , coupon_id BIGINT NOT NULL
    , created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    , FOREIGN KEY(user_id) REFERENCES user (user_id)
    , FOREIGN KEY(coupon_id) REFERENCES coupon (coupon_id)
    , UNIQUE KEY (user_id, coupon_id)
);

# 쿠폰 사용
CREATE TABLE used_coupon(
                            receive_id BIGINT NOT NULL
    , booking_id BIGINT NOT NULL
    , FOREIGN KEY(receive_id) REFERENCES receive_coupon (receive_id)
    , FOREIGN KEY(booking_id) REFERENCES booking (booking_id)
);

# 권한
CREATE TABLE `role`(
                       `role` ENUM('USER', 'BUIS', 'ADMIN')
    , user_id BIGINT NOT NULL
    , PRIMARY KEY (`role`, user_id)
    , FOREIGN KEY(user_id) REFERENCES user (user_id)
);

# 검색어
CREATE TABLE search_word(
                            txt varchar(100)
    , user_id BIGINT NOT NULL
    , search_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    , FOREIGN KEY(user_id) REFERENCES user (user_id)
);

# 알람
CREATE TABLE notice(
                       notice_id BIGINT PRIMARY KEY AUTO_INCREMENT
    , content VARCHAR(800) NOT NULL
    , created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    , category enum('NOTICE', 'TRIP', 'AD', 'BUSI')  NOT NULL
);

# 알람 수령
CREATE TABLE notice_receive(
                               user_id BIGINT NOT NULL
    , notice_id BIGINT NOT NULL
    , created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    , `open` TINYINT NOT NULL DEFAULT 0
    , PRIMARY KEY (user_id, notice_id)
    , FOREIGN KEY(user_id) REFERENCES user (user_id)
    , FOREIGN KEY(notice_id) REFERENCES notice (notice_id)
);

# 최근 본 목록
CREATE TABLE recent(
                       user_id BIGINT NOT NULL
    , strf_id BIGINT NOT NULL
    , inquired_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    , undo_recent TINYINT NOT NULL DEFAULT 0
    , PRIMARY KEY (user_id, strf_id)
    , FOREIGN KEY (user_id) REFERENCES user (user_id)
    , FOREIGN KEY (strf_id) REFERENCES stay_tour_restaur_fest (strf_id)
);

# 찜 목록
CREATE TABLE wishlist(
                         user_id BIGINT NOT NULL
    , strf_id BIGINT NOT NULL
    , FOREIGN KEY(user_id) REFERENCES user (user_id)
    , FOREIGN KEY(strf_id) REFERENCES stay_tour_restaur_fest (strf_id)
);

# 지역 상품권
CREATE TABLE ticket(
                       ticket_id BIGINT PRIMARY KEY AUTO_INCREMENT
    , price int NOT NULL
    , expiration date NOT NULL
    , created_at datetime NOT NULL
    , location_id BIGINT NOT NULL
    , sale int NOT NULL
    , FOREIGN KEY(location_id) REFERENCES location (location_id)
);

# 참석 여부
CREATE TABLE attendance(
                           ticket_id BIGINT NOT NULL
    , strf_id BIGINT NOT NULL
    , attendance TINYINT NOT NULL
    , FOREIGN KEY(ticket_id) REFERENCES ticket (ticket_id)
    , FOREIGN KEY(strf_id) REFERENCES stay_tour_restaur_fest (strf_id)
);

# 상품권 확인
CREATE TABLE ticket_check(
                             user_id BIGINT NOT NULL
    , ticket_id BIGINT NOT NULL
    , sale_price int NOT NULL
    , FOREIGN KEY(user_id) REFERENCES user (user_id)
    , FOREIGN KEY(ticket_id) REFERENCES ticket (ticket_id)
);

# 패키지
CREATE TABLE package (
                         package_id BIGINT PRIMARY KEY AUTO_INCREMENT
    , discount int NOT NULL
    , start_at date NOT NULL
    , end_at date NOT NULL
    , notification varchar(1000)
    , tell varchar(20) NOT NULL
    , location_id BIGINT NOT NULL
    , FOREIGN KEY(location_id) REFERENCES location (location_id)
);

# 패키지 메뉴
CREATE TABLE package_menu (
                              package_id BIGINT NOT NULL
    , menu_id BIGINT NOT NULL
    , `day` int NOT NULL
    , start_at time NOT NULL
    , notification varchar(1000) NOT NULL
    , FOREIGN KEY(package_id) REFERENCES package (package_id)
    , FOREIGN KEY(menu_id) REFERENCES menu (menu_id)
);

# 패키지 참석자
CREATE TABLE package_join (
                              package_id BIGINT NOT NULL
    , user_id BIGINT NOT NULL
    , FOREIGN KEY(package_id) REFERENCES package (package_id)
    , FOREIGN KEY(user_id) REFERENCES user (user_id)
);

# 채팅방
CREATE TABLE chat_room (
                           chat_room_id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT
    , title varchar(100) NOT NULL
    , created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);

# 채팅 참여
CREATE TABLE chat_join (
                           cj_id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT
    , user_id BIGINT NOT NULL
    , chat_room_id BIGINT NOT NULL
    , FOREIGN KEY(user_id) REFERENCES user (user_id)
    , FOREIGN KEY(chat_room_id) REFERENCES chat_room (chat_room_id)
    , CONSTRAINT user_chat UNIQUE (user_id,chat_room_id)
);

# 채팅
CREATE TABLE chat (
                      chat_id BIGINT PRIMARY KEY AUTO_INCREMENT
    , cj_id BIGINT NOT NULL
    , content varchar(1000) NOT NULL
    , created_at datetime NOT NULL DEFAULT current_timestamp
    , FOREIGN KEY(cj_id) REFERENCES chat_join (cj_id)
);


# 여행기
CREATE TABLE trip_review(
                            trip_review_id BIGINT PRIMARY KEY AUTO_INCREMENT
    , trip_id BIGINT NOT null
    , user_id BIGINT NOT null
    , title VARCHAR(70) NOT null
    , content TEXT NOT NULL
    , created_at DATETIME NOT NULL DEFAULT current_timestamp
    , FOREIGN KEY(trip_id) REFERENCES trip (trip_id)
    , FOREIGN KEY(user_id) REFERENCES user (user_id)
);
# 여행기 사진
CREATE TABLE trip_review_pic(
                                trip_review_pic VARCHAR(200)
    , trip_review_id BIGINT
    , FOREIGN KEY(trip_review_id) REFERENCES trip_review (trip_review_id)
    , PRIMARY KEY(trip_review_pic,trip_review_id)
);
# 여행기 붐업
CREATE TABLE trip_like(
                          user_id bigint
    , trip_review_id bigint
    , FOREIGN KEY(user_id) REFERENCES user(user_id)
    , FOREIGN KEY(trip_review_id) REFERENCES trip_review(trip_review_id)
    , PRIMARY KEY(user_id,trip_review_id)
);
