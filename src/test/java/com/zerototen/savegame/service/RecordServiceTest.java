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

import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.Record;
import com.zerototen.savegame.domain.dto.CreateRecordServiceDto;
import com.zerototen.savegame.domain.dto.RecordAnalysisResponse;
import com.zerototen.savegame.domain.dto.RecordAnalysisServiceDto;
import com.zerototen.savegame.domain.dto.response.RecordResponse;
import com.zerototen.savegame.domain.dto.UpdateRecordServiceDto;
import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.domain.type.PayType;
import com.zerototen.savegame.exception.CustomException;
import com.zerototen.savegame.exception.ErrorCode;
import com.zerototen.savegame.repository.MemberRepository;
import com.zerototen.savegame.repository.RecordRepository;
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
    private MemberRepository memberRepository;

    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private RecordService recordService;

    private static final int CATEGORY_LENGTH = Category.values().length;
    private static final int PAYTYPE_LENGTH = PayType.values().length;

    @Nested
    @DisplayName("지출 등록")
    class TestCreate {

        @Test
        @DisplayName("성공")
        void success() {
            //given
            CreateRecordServiceDto serviceDto = getCreateServiceDto();
            Member member = getMember();

            given(memberRepository.findById(serviceDto.getMemberId()))
                .willReturn(Optional.of(member));

            ArgumentCaptor<Record> argumentCaptor = ArgumentCaptor.forClass(Record.class);

            //when
            recordService.create(serviceDto);

            //then
            then(recordRepository).should().save(argumentCaptor.capture());

        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void fail_NotFoundUser() {
            //given
            CreateRecordServiceDto serviceDto = getCreateServiceDto();

            given(memberRepository.findById(serviceDto.getMemberId()))
                .willReturn(Optional.empty());

            //when
            CustomException exception = assertThrows(CustomException.class, () -> recordService.create(serviceDto));

            //then
            then(recordRepository).should(never()).save(any());
            assertEquals(ErrorCode.NOT_FOUND_MEMBER, exception.getErrorCode());
        }
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
                List<Record> records = getRecords(5);

                given(recordRepository.findByMemberIdAndUseDateDescWithOptional(anyLong(), any(LocalDate.class),
                    any(LocalDate.class), isNull()))
                    .willReturn(records);

                //when
                List<RecordResponse> recordResponses = recordService.getInfos(2L,
                    LocalDate.of(2023, 6, 1), LocalDate.of(2023, 6, 30), null);

                //then
                then(recordRepository).should()
                    .findByMemberIdAndUseDateDescWithOptional(anyLong(), any(LocalDate.class),
                        any(LocalDate.class), isNull());
                int size = recordResponses.size();
                for (int i = size; i >= 1; i--) {
                    assertEquals(i, recordResponses.get(size - i).getRecordId());
                    assertEquals(i * 10000, recordResponses.get(size - i).getAmount());
                    assertEquals(Category.values()[(i - 1) % CATEGORY_LENGTH].getName(),
                        recordResponses.get(size - i).getCategory());
                    assertEquals("가게" + i, recordResponses.get(size - i).getPaidFor());
                    assertEquals("메모" + i, recordResponses.get(size - i).getMemo());
                    assertEquals(LocalDate.of(2023, 6, i), recordResponses.get(size - i).getUseDate());
                    assertEquals(PayType.values()[(i - 1) % PAYTYPE_LENGTH].getName(),
                        recordResponses.get(size - i).getPayType());
                }
            }

            @Test
            @DisplayName("모든값 입력")
            void inputAll() {
                //given
                List<Record> records = getRecords(10);
                List<String> categories = getCategories(10);

                given(recordRepository.findByMemberIdAndUseDateDescWithOptional(anyLong(), any(LocalDate.class),
                    any(LocalDate.class), anyList()))
                    .willReturn(records);

                //when
                List<RecordResponse> recordResponses = recordService.getInfos(2L,
                    LocalDate.of(2023, 6, 1), LocalDate.of(2023, 6, 30), categories);

                //then
                then(recordRepository).should().findByMemberIdAndUseDateDescWithOptional(
                    anyLong(), any(LocalDate.class), any(LocalDate.class), anyList());
                int size = recordResponses.size();
                for (int i = recordResponses.size(); i >= 1; i--) {
                    assertEquals(i, recordResponses.get(size - i).getRecordId());
                    assertEquals(i * 10000, recordResponses.get(size - i).getAmount());
                    assertEquals(Category.values()[(i - 1) % CATEGORY_LENGTH].getName(),
                        recordResponses.get(size - i).getCategory());
                    assertEquals("가게" + i, recordResponses.get(size - i).getPaidFor());
                    assertEquals("메모" + i, recordResponses.get(size - i).getMemo());
                    assertEquals(LocalDate.of(2023, 6, i), recordResponses.get(size - i).getUseDate());
                    assertEquals(PayType.values()[(i - 1) % PAYTYPE_LENGTH].getName(),
                        recordResponses.get(size - i).getPayType());
                }
            }
        }

        @Test
        @DisplayName("실패 - 시작일이 종료일보다 이후")
        void fail_StartDateIsAfterEndDate() {
            //given
            LocalDate startDate = LocalDate.of(2023, 6, 1);
            LocalDate endDate = LocalDate.of(2023, 5, 31);

            //when
            CustomException exception = assertThrows(CustomException.class,
                () -> recordService.getInfos(2L, startDate, endDate, null));

            //then
            assertEquals(ErrorCode.STARTDATE_AFTER_ENDDATE, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("지출 조회 (가계부 분석)")
    class TestGetAnalysisInfos {

        @Test
        @DisplayName("성공")
        void success() {
            //given
            List<RecordAnalysisServiceDto> serviceDtos = getRecordAnalysisServiceDtos(10);

            given(recordRepository.findByMemberIdAndUseDateAndAmountSumDesc(anyLong(), any(LocalDate.class),
                any(LocalDate.class)))
                .willReturn(serviceDtos);

            //when
            List<RecordAnalysisResponse> responses = recordService.getAnalysisInfo(1L, 2023, 6);

            //then
            then(recordRepository).should().findByMemberIdAndUseDateAndAmountSumDesc(anyLong(), any(LocalDate.class),
                any(LocalDate.class));
            int size = responses.size();
            for (int i = size; i >= 1; i--) {
                assertEquals(Category.values()[(i - 1) % CATEGORY_LENGTH].getName(),
                    responses.get(size - i).getCategory());
                assertEquals(i * 10000L, responses.get(size - i).getTotal());
            }
        }

        @Nested
        @DisplayName("실패")
        class Fail {

            @Test
            @DisplayName("카테고리가 null")
            void categoryIsNull() {
                //given
                List<RecordAnalysisServiceDto> serviceDtos = getRecordAnalysisServiceDtos(10);
                serviceDtos.get(0).setCategory(null);

                given(recordRepository.findByMemberIdAndUseDateAndAmountSumDesc(anyLong(), any(LocalDate.class),
                    any(LocalDate.class)))
                    .willReturn(serviceDtos);

                //when
                CustomException exception = assertThrows(CustomException.class,
                    () -> recordService.getAnalysisInfo(1L, 2023, 6));

                //then
                assertEquals(ErrorCode.CATEGORY_IS_NULL, exception.getErrorCode());
            }

            @Test
            @DisplayName("합계가 0 이하")
            void invalidTotal_ZeroOrBelow() {
                //given
                List<RecordAnalysisServiceDto> serviceDtos = getRecordAnalysisServiceDtos(10);
                serviceDtos.get(0).setTotal(0L);

                given(recordRepository.findByMemberIdAndUseDateAndAmountSumDesc(anyLong(), any(LocalDate.class),
                    any(LocalDate.class)))
                    .willReturn(serviceDtos);

                //when
                CustomException exception = assertThrows(CustomException.class,
                    () -> recordService.getAnalysisInfo(1L, 2023, 6));

                //then
                assertEquals(ErrorCode.INVALID_TOTAL, exception.getErrorCode());
            }

            @Test
            @DisplayName("합계가 null")
            void invalidTotal_IsNull() {
                //given
                List<RecordAnalysisServiceDto> serviceDtos = getRecordAnalysisServiceDtos(10);
                serviceDtos.get(0).setTotal(null);

                given(recordRepository.findByMemberIdAndUseDateAndAmountSumDesc(anyLong(), any(LocalDate.class),
                    any(LocalDate.class)))
                    .willReturn(serviceDtos);

                //when
                CustomException exception = assertThrows(CustomException.class,
                    () -> recordService.getAnalysisInfo(1L, 2023, 6));

                //then
                assertEquals(ErrorCode.INVALID_TOTAL, exception.getErrorCode());
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
            UpdateRecordServiceDto serviceDto = getUpdateServiceDto();

            Record record = getRecord();

            given(recordRepository.findById(anyLong()))
                .willReturn(Optional.of(record));

            //when
            recordService.update(serviceDto);

            //then
            then(recordRepository).should().findById(anyLong());
            assertEquals(1L, record.getId());
            assertEquals(2L, record.getMemberId());
            assertEquals(20000, record.getAmount());
            assertEquals(Category.BEAUTY, record.getCategory());
            assertEquals("미용실", record.getPaidFor());
            assertEquals("커트", record.getMemo());
            assertEquals(LocalDate.of(2023, 5, 5), record.getUseDate());
            assertEquals(PayType.CASH, record.getPayType());

        }

        @Nested
        @DisplayName("실패")
        class Fail {

            @Test
            @DisplayName("찾을 수 없는 지출내역")
            void notFoundRecord() {
                //given
                UpdateRecordServiceDto serviceDto = getUpdateServiceDto();

                given(recordRepository.findById(anyLong()))
                    .willReturn(Optional.empty());

                //when
                // Custom Exception 연동 시 수정 예정
                CustomException exception = assertThrows(CustomException.class,
                    () -> recordService.update(serviceDto));

                //then
                then(recordRepository).should().findById(anyLong());
                assertEquals(ErrorCode.NOT_FOUND_RECORD, exception.getErrorCode());

            }

            @Test
            @DisplayName("일치하지 않는 사용자")
            void notMatchMember() {
                //given
                UpdateRecordServiceDto serviceDto = getUpdateServiceDto();

                Record record = getRecord();
                record.setMemberId(3L);

                given(recordRepository.findById(anyLong()))
                    .willReturn(Optional.of(record));

                //when
                CustomException exception = assertThrows(CustomException.class,
                    () -> recordService.update(serviceDto));

                //then
                then(recordRepository).should().findById(anyLong());
                assertEquals(ErrorCode.NOT_MATCH_MEMBER, exception.getErrorCode());
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
            Record record = getRecord();

            given(recordRepository.findById(anyLong()))
                .willReturn(Optional.of(record));

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
                given(recordRepository.findById(anyLong()))
                    .willReturn(Optional.empty());

                //when
                CustomException exception = assertThrows(CustomException.class,
                    () -> recordService.delete(2L, 2L));

                //then
                then(recordRepository).should().findById(anyLong());
                then(recordRepository).should(never()).delete(any(Record.class));
                assertEquals(ErrorCode.NOT_FOUND_RECORD, exception.getErrorCode());

            }

            @Test
            @DisplayName("일치하지 않는 사용자")
            void notMatchMember() {
                //given

                Record record = getRecord();

                given(recordRepository.findById(anyLong()))
                    .willReturn(Optional.of(record));

                //when
                CustomException exception = assertThrows(CustomException.class,
                    () -> recordService.delete(1L, 3L));

                //then
                then(recordRepository).should().findById(anyLong());
                then(recordRepository).should(never()).delete(any(Record.class));
                assertEquals(ErrorCode.NOT_MATCH_MEMBER, exception.getErrorCode());
            }
        }
    }

    private Member getMember() {
        return Member.builder()
            .id(2L)
            .email("abc@gmail.com")
            .nickname("Nick")
            .password("1")
            .profileImageUrl("default.png")
            .build();
    }

    private CreateRecordServiceDto getCreateServiceDto() {
        return CreateRecordServiceDto.builder()
            .memberId(2L)
            .amount(10000)
            .category(Category.FOOD)
            .paidFor("식당")
            .memo("국밥")
            .useDate(LocalDate.of(2023, 1, 1))
            .payType(PayType.CASH)
            .build();
    }

    private UpdateRecordServiceDto getUpdateServiceDto() {
        return UpdateRecordServiceDto.builder()
            .id(1L)
            .memberId(2L)
            .amount(20000)
            .category(Category.BEAUTY)
            .paidFor("미용실")
            .memo("커트")
            .useDate(LocalDate.of(2023, 5, 5))
            .payType(PayType.CASH)
            .build();
    }

    private List<RecordAnalysisServiceDto> getRecordAnalysisServiceDtos(int size) {
        List<RecordAnalysisServiceDto> serviceDtos = new ArrayList<>();

        size = size < 1 ? 1 : Math.min(size, CATEGORY_LENGTH);

        for (int i = size; i >= 1; i--) {
            RecordAnalysisServiceDto serviceDto = RecordAnalysisServiceDto.builder()
                .total(10000L * i)
                .category(Category.values()[(i - 1) % CATEGORY_LENGTH])
                .build();
            serviceDtos.add(serviceDto);
        }

        return serviceDtos;
    }

    private Record getRecord() {
        return Record.builder()
            .id(1L)
            .memberId(2L)
            .amount(10000)
            .category(Category.MEDICAL)
            .paidFor("병원")
            .memo("감기")
            .useDate(LocalDate.of(2023, 6, 6))
            .payType(PayType.CARD)
            .build();
    }

    private List<Record> getRecords(int size) {
        List<Record> records = new ArrayList<>();

        size = Math.max(size, 1);

        for (int i = size; i >= 1; i--) {
            Record record = Record.builder()
                .id((long) i)
                .memberId(2L)
                .amount(10000 * i)
                .category(Category.values()[(i - 1) % CATEGORY_LENGTH])
                .paidFor("가게" + i)
                .memo("메모" + i)
                .useDate(LocalDate.of(2023, 6, i))
                .payType(PayType.values()[(i - 1) % PAYTYPE_LENGTH])
                .build();
            records.add(record);
        }

        return records;
    }

    private List<String> getCategories(int size) {
        List<String> categories = new ArrayList<>();

        size = size < 1 ? 1 : Math.min(size, CATEGORY_LENGTH);

        for (int i = size; i >= 1; i--) {
            categories.add(Category.values()[(i - 1) % CATEGORY_LENGTH].getName());
        }

        return categories;
    }

}