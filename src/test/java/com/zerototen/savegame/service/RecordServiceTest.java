package com.zerototen.savegame.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.zerototen.savegame.dto.CreateRecordServiceDto;
import com.zerototen.savegame.dto.RecordResponse;
import com.zerototen.savegame.dto.UpdateRecordServiceDto;
import com.zerototen.savegame.entity.Record;
import com.zerototen.savegame.repository.RecordRepository;
import com.zerototen.savegame.type.Category;
import com.zerototen.savegame.type.PayType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private RecordService recordService;

    @Nested
    @DisplayName("지출 등록")
    class TestCreate {

        @Test
        @DisplayName("성공")
        void success() {
            //given
            CreateRecordServiceDto serviceDto = CreateRecordServiceDto.builder()
                .memberId(1L)
                .amount(10000)
                .category(Category.FOOD)
                .paidFor("가게")
                .memo("메모")
                .useDate(LocalDate.of(2023, 1, 1))
                .payType(PayType.CASH)
                .build();

            // Login과 연동 시 memberId 검증 부분 추가

            ArgumentCaptor<Record> argumentCaptor = ArgumentCaptor.forClass(Record.class);

            //when
            recordService.create(serviceDto);

            //then
            then(recordRepository).should().save(argumentCaptor.capture());

        }
        /* Login과 연동 시 추가
        @Test
        @DisplayName("지출 등록 실패 - 존재하지 않는 사용자")
        void fail_NotFoundUser() {
            //given

            //when

            //then
        }
        */
    }

    @Nested
    @DisplayName("지출 조회 (가계부 메인)")
    class TestGetInfos {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @DisplayName("필수값만 입력")
            void inputRequiredOnly() {
                //given
                List<Record> records = new ArrayList<>();
                int cLength = Category.values().length;
                int pLength = PayType.values().length;

                for (int i = 5; i >= 1; i--) {
                    Record record = Record.builder()
                        .id((long) i)
                        .memberId(1L)
                        .amount(i * 10000)
                        .category(Category.values()[i % cLength])
                        .paidFor("가게" + i)
                        .memo("메모" + i)
                        .useDate(LocalDate.of(2023, 6, i))
                        .payType(PayType.values()[i % pLength])
                        .build();
                    records.add(record);
                }

                given(recordRepository.findByMemberIdAndUseDateDescWithOptional(anyLong(),
                    any(LocalDate.class), any(LocalDate.class), isNull())).willReturn(records);

                //when
                List<RecordResponse> recordResponses = recordService.getInfos(1L,
                    LocalDate.of(2023, 6, 1), LocalDate.of(2023, 6, 30), null);

                //then
                then(recordRepository).should()
                    .findByMemberIdAndUseDateDescWithOptional(anyLong(), any(LocalDate.class),
                        any(LocalDate.class), isNull());
                for (int i = 5; i >= 1; i--) {
                    assertEquals(i, recordResponses.get(5 - i).getRecordId());
                    assertEquals(i * 10000, recordResponses.get(5 - i).getAmount());
                    assertEquals(Category.values()[i % cLength].getName(),
                        recordResponses.get(5 - i).getCategory());
                    assertEquals("가게" + i, recordResponses.get(5 - i).getPaidFor());
                    assertEquals("메모" + i, recordResponses.get(5 - i).getMemo());
                    assertEquals(LocalDate.of(2023, 6, i), recordResponses.get(5 - i).getUseDate());
                }
            }

            @Test
            @DisplayName("모든값 입력")
            void inputAll() {
                //given
                List<Record> records = new ArrayList<>();
                List<String> categories = new ArrayList<>();
                int cLength = Category.values().length;
                int pLength = PayType.values().length;

                for (int i = 5; i >= 1; i--) {
                    Record record = Record.builder()
                        .id((long) i)
                        .memberId(1L)
                        .amount(i * 10000)
                        .category(Category.values()[i % cLength])
                        .paidFor("가게" + i)
                        .memo("메모" + i)
                        .useDate(LocalDate.of(2023, 6, i))
                        .payType(PayType.values()[i % pLength])
                        .build();
                    records.add(record);
                    categories.add(Category.values()[i % cLength].getName());
                }

                given(recordRepository.findByMemberIdAndUseDateDescWithOptional(
                    anyLong(), any(LocalDate.class), any(LocalDate.class), anyList()))
                    .willReturn(records);

                //when
                List<RecordResponse> recordResponses = recordService.getInfos(1L,
                    LocalDate.of(2023, 6, 1), LocalDate.of(2023, 6, 30), categories);

                //then
                then(recordRepository).should().findByMemberIdAndUseDateDescWithOptional(
                    anyLong(), any(LocalDate.class), any(LocalDate.class), anyList());
                for (int i = 5; i >= 1; i--) {
                    assertEquals(i, recordResponses.get(5 - i).getRecordId());
                    assertEquals(i * 10000, recordResponses.get(5 - i).getAmount());
                    assertEquals(Category.values()[i % cLength].getName(),
                        recordResponses.get(5 - i).getCategory());
                    assertEquals("가게" + i, recordResponses.get(5 - i).getPaidFor());
                    assertEquals("메모" + i, recordResponses.get(5 - i).getMemo());
                    assertEquals(LocalDate.of(2023, 6, i), recordResponses.get(5 - i).getUseDate());
                }
            }
        }

        @Nested
        @DisplayName("실패")
        class Fail {

            @Test
            @DisplayName("시작일이 종료일보다 이후")
            void startDateIsAfterEndDate() {
                //given
                LocalDate startDate = LocalDate.of(2023, 6, 1);
                LocalDate endDate = LocalDate.of(2023, 5, 31);

                //when
                RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> recordService.getInfos(1L, startDate, endDate, null));

                //then
                assertEquals("조회시작일이 조회종료일 이후입니다", exception.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("지출 수정")
    class TestUpdate {

        @Test
        @DisplayName("성공")
        void success() {
            //given
            UpdateRecordServiceDto serviceDto = UpdateRecordServiceDto.builder()
                .id(1L)
                .memberId(2L)
                .amount(10000)
                .category(Category.FOOD)
                .paidFor("가게")
                .memo("메모")
                .useDate(LocalDate.of(2023, 1, 1))
                .payType(PayType.CASH)
                .build();

            Record record = Record.builder()
                .id(1L)
                .memberId(2L)
                .amount(5000)
                .category(Category.CULTURE)
                .paidFor("병원")
                .memo("메모메모")
                .useDate(LocalDate.of(2023, 6, 6))
                .payType(PayType.CARD)
                .build();

            given(recordRepository.findById(anyLong())).willReturn(Optional.of(record));

            //when
            recordService.update(serviceDto);

            //then
            then(recordRepository).should().findById(anyLong());
            assertEquals(1L, record.getId());
            assertEquals(2L, record.getMemberId());
            assertEquals(10000, record.getAmount());
            assertEquals(Category.FOOD, record.getCategory());
            assertEquals("가게", record.getPaidFor());
            assertEquals("메모", record.getMemo());
            assertEquals(LocalDate.of(2023, 1, 1), record.getUseDate());
            assertEquals(PayType.CASH, record.getPayType());

        }

        @Nested
        @DisplayName("실패")
        class Fail {

            @Test
            @DisplayName("찾을 수 없는 지출내역")
            void notFoundRecord() {
                //given
                UpdateRecordServiceDto serviceDto = UpdateRecordServiceDto.builder()
                    .id(1L)
                    .memberId(2L)
                    .amount(10000)
                    .category(Category.FOOD)
                    .paidFor("가게")
                    .memo("메모")
                    .useDate(LocalDate.of(2023, 1, 1))
                    .payType(PayType.CASH)
                    .build();

                given(recordRepository.findById(anyLong())).willReturn(Optional.empty());

                //when
                // Custom Exception 연동 시 수정 예정
                RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> recordService.update(serviceDto));

                //then
                then(recordRepository).should().findById(anyLong());
                assertEquals("Not found record", exception.getMessage());

            }

            @Test
            @DisplayName("일치하지 않는 사용자")
            void notMatchMember() {
                //given
                UpdateRecordServiceDto serviceDto = UpdateRecordServiceDto.builder()
                    .id(1L)
                    .memberId(2L)
                    .amount(10000)
                    .category(Category.FOOD)
                    .paidFor("가게")
                    .memo("메모")
                    .useDate(LocalDate.of(2023, 1, 1))
                    .payType(PayType.CASH)
                    .build();

                given(recordRepository.findById(anyLong())).willReturn(
                    Optional.of(Record.builder()
                        .id(1L)
                        .memberId(1L)
                        .amount(5000)
                        .category(Category.CULTURE)
                        .paidFor("병원")
                        .memo("메모메모")
                        .useDate(LocalDate.of(2023, 6, 6))
                        .payType(PayType.CARD)
                        .build()));

                //when
                // Custom Exception 연동 시 수정 예정
                RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> recordService.update(serviceDto));

                //then
                then(recordRepository).should().findById(anyLong());
                assertEquals("Not match member", exception.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("지출 삭제")
    class TestDelete {

        @Test
        @DisplayName("성공")
        void success() {
            //given
            given(recordRepository.findById(anyLong())).willReturn(Optional.of(Record.builder()
                .id(1L)
                .memberId(2L)
                .amount(10000)
                .category(Category.FOOD)
                .paidFor("가게")
                .memo("메모")
                .useDate(LocalDate.of(2023, 1, 1))
                .payType(PayType.CASH)
                .build()));

            ArgumentCaptor<Record> argumentCaptor = ArgumentCaptor.forClass(Record.class);

            //when
            recordService.delete(1L, 2L);

            //then
            then(recordRepository).should().findById(anyLong());
            then(recordRepository).should().delete(argumentCaptor.capture());
        }

        @Nested
        @DisplayName("실패")
        class Fail {

            @Test
            @DisplayName("찾을 수 없는 지출내역")
            void notFoundRecord() {
                //given
                given(recordRepository.findById(anyLong())).willReturn(Optional.empty());

                //when
                // Custom Exception 연동 시 수정 예정
                RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> recordService.delete(1L, 2L));

                //then
                then(recordRepository).should().findById(anyLong());
                then(recordRepository).should(never()).delete(any(Record.class));
                assertEquals("Not found record", exception.getMessage());

            }

            @Test
            @DisplayName("일치하지 않는 사용자")
            void notMatchMember() {
                //given
                given(recordRepository.findById(anyLong())).willReturn(
                    Optional.of(Record.builder()
                        .id(1L)
                        .memberId(2L)
                        .amount(10000)
                        .category(Category.FOOD)
                        .paidFor("가게")
                        .memo("메모")
                        .useDate(LocalDate.of(2023, 1, 1))
                        .payType(PayType.CASH)
                        .build()));

                //when
                // Custom Exception 연동 시 수정 예정
                RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> recordService.delete(1L, 1L));

                //then
                then(recordRepository).should().findById(anyLong());
                then(recordRepository).should(never()).delete(any(Record.class));
                assertEquals("Not match member", exception.getMessage());
            }
        }
    }

}