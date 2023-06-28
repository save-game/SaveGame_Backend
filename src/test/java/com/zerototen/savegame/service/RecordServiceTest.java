package com.zerototen.savegame.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.zerototen.savegame.domain.dto.CreateRecordServiceDto;
import com.zerototen.savegame.domain.dto.RecordAnalysisServiceDto;
import com.zerototen.savegame.domain.dto.UpdateRecordServiceDto;
import com.zerototen.savegame.domain.dto.response.RecordAnalysisResponse;
import com.zerototen.savegame.domain.dto.response.RecordResponse;
import com.zerototen.savegame.domain.dto.response.ResponseDto;
import com.zerototen.savegame.domain.entity.Member;
import com.zerototen.savegame.domain.entity.Record;
import com.zerototen.savegame.domain.type.Category;
import com.zerototen.savegame.domain.type.PayType;
import com.zerototen.savegame.exception.ErrorCode;
import com.zerototen.savegame.repository.RecordRepository;
import com.zerototen.savegame.security.TokenProvider;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
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

    @Mock
    private TokenProvider tokenProvider;

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
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

            ArgumentCaptor<Record> argumentCaptor = ArgumentCaptor.forClass(Record.class);

            //when
            ResponseDto<?> responseDto = recordService.create(request, serviceDto);

            //then
            then(recordRepository).should().save(argumentCaptor.capture());
            assertTrue(responseDto.isSuccess());
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
                HttpServletRequest request = mock(HttpServletRequest.class);
                Member member = getMember();
                ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
                List<Record> records = getRecords(member, 5);

                willReturn(validateCheckResponse)
                    .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

                given(recordRepository.findByMemberAndUseDateDescWithOptional(any(Member.class),
                    any(LocalDate.class),
                    any(LocalDate.class), isNull()))
                    .willReturn(records);

                //when
                ResponseDto<?> responseDto = recordService.getInfos(request,
                    LocalDate.of(2023, 6, 1), LocalDate.of(2023, 6, 30), null);
                List<RecordResponse> recordResponses = (List<RecordResponse>) responseDto.getData();

                //then
                assertTrue(responseDto.isSuccess());
                then(recordRepository).should()
                    .findByMemberAndUseDateDescWithOptional(any(Member.class), any(LocalDate.class),
                        any(LocalDate.class), isNull());
                int size = recordResponses.size();
                for (int i = size; i >= 1; i--) {
                    assertEquals(i, recordResponses.get(size - i).getRecordId());
                    assertEquals(i * 10000, recordResponses.get(size - i).getAmount());
                    assertEquals(Category.values()[(i - 1) % CATEGORY_LENGTH].getName(),
                        recordResponses.get(size - i).getCategory());
                    assertEquals("가게" + i, recordResponses.get(size - i).getPaidFor());
                    assertEquals("메모" + i, recordResponses.get(size - i).getMemo());
                    assertEquals(LocalDate.of(2023, 6, i),
                        recordResponses.get(size - i).getUseDate());
                    assertEquals(PayType.values()[(i - 1) % PAYTYPE_LENGTH].getName(),
                        recordResponses.get(size - i).getPayType());
                }
            }

            @Test
            @DisplayName("모든값 입력")
            void inputAll() {
                //given
                HttpServletRequest request = mock(HttpServletRequest.class);
                Member member = getMember();
                ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
                List<Record> records = getRecords(member, 10);
                List<String> categories = getCategories(10);

                willReturn(validateCheckResponse)
                    .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

                given(recordRepository.findByMemberAndUseDateDescWithOptional(any(Member.class),
                    any(LocalDate.class),
                    any(LocalDate.class), anyList()))
                    .willReturn(records);

                //when
                ResponseDto<?> responseDto = recordService.getInfos(request,
                    LocalDate.of(2023, 6, 1), LocalDate.of(2023, 6, 30), categories);
                List<RecordResponse> recordResponses = (List<RecordResponse>) responseDto.getData();

                //then
                assertTrue(responseDto.isSuccess());
                then(recordRepository).should().findByMemberAndUseDateDescWithOptional(
                    any(Member.class), any(LocalDate.class), any(LocalDate.class), anyList());
                int size = recordResponses.size();
                for (int i = recordResponses.size(); i >= 1; i--) {
                    assertEquals(i, recordResponses.get(size - i).getRecordId());
                    assertEquals(i * 10000, recordResponses.get(size - i).getAmount());
                    assertEquals(Category.values()[(i - 1) % CATEGORY_LENGTH].getName(),
                        recordResponses.get(size - i).getCategory());
                    assertEquals("가게" + i, recordResponses.get(size - i).getPaidFor());
                    assertEquals("메모" + i, recordResponses.get(size - i).getMemo());
                    assertEquals(LocalDate.of(2023, 6, i),
                        recordResponses.get(size - i).getUseDate());
                    assertEquals(PayType.values()[(i - 1) % PAYTYPE_LENGTH].getName(),
                        recordResponses.get(size - i).getPayType());
                }
            }
        }

        @Test
        @DisplayName("실패 - 시작일이 종료일보다 이후")
        void fail_StartDateIsAfterEndDate() {
            //given
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
            LocalDate startDate = LocalDate.of(2023, 6, 1);
            LocalDate endDate = LocalDate.of(2023, 5, 31);

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

            //when
            ResponseDto<?> responseDto = recordService.getInfos(request, startDate, endDate, null);

            //then
            assertFalse(responseDto.isSuccess());
            assertEquals(ErrorCode.STARTDATE_AFTER_ENDDATE.getDetail(), responseDto.getData());
        }
    }

    @Nested
    @DisplayName("지출 조회 (가계부 분석)")
    class TestGetAnalysisInfos {

        @Test
        @DisplayName("성공")
        void success() {
            //given
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
            List<RecordAnalysisServiceDto> serviceDtos = getRecordAnalysisServiceDtos(10);

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

            given(recordRepository.findByMemberAndUseDateAndAmountSumDesc(any(Member.class),
                any(LocalDate.class),
                any(LocalDate.class)))
                .willReturn(serviceDtos);

            //when
            ResponseDto<?> responseDto = recordService.getAnalysisInfo(request, 2023, 6);
            List<RecordAnalysisResponse> responses = (List<RecordAnalysisResponse>) responseDto.getData();

            //then
            assertTrue(responseDto.isSuccess());
            then(recordRepository).should()
                .findByMemberAndUseDateAndAmountSumDesc(any(Member.class), any(LocalDate.class),
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
                HttpServletRequest request = mock(HttpServletRequest.class);
                Member member = getMember();
                ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
                List<RecordAnalysisServiceDto> serviceDtos = getRecordAnalysisServiceDtos(10);
                serviceDtos.get(0).setCategory(null);

                willReturn(validateCheckResponse)
                    .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

                given(recordRepository.findByMemberAndUseDateAndAmountSumDesc(any(Member.class),
                    any(LocalDate.class),
                    any(LocalDate.class)))
                    .willReturn(serviceDtos);

                //when
                ResponseDto<?> responseDto = recordService.getAnalysisInfo(request, 2023, 6);

                //then
                assertFalse(responseDto.isSuccess());
                assertEquals(ErrorCode.CATEGORY_IS_NULL.getDetail(), responseDto.getData());
            }

            @Test
            @DisplayName("합계가 0 이하")
            void invalidTotal_ZeroOrBelow() {
                //given
                HttpServletRequest request = mock(HttpServletRequest.class);
                Member member = getMember();
                ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
                List<RecordAnalysisServiceDto> serviceDtos = getRecordAnalysisServiceDtos(10);
                serviceDtos.get(0).setTotal(0L);

                willReturn(validateCheckResponse)
                    .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

                given(recordRepository.findByMemberAndUseDateAndAmountSumDesc(any(Member.class),
                    any(LocalDate.class),
                    any(LocalDate.class)))
                    .willReturn(serviceDtos);

                //when
                ResponseDto<?> responseDto = recordService.getAnalysisInfo(request, 2023, 6);

                //then
                assertFalse(responseDto.isSuccess());
                assertEquals(ErrorCode.INVALID_TOTAL.getDetail(), responseDto.getData());
            }

            @Test
            @DisplayName("합계가 null")
            void invalidTotal_IsNull() {
                //given
                HttpServletRequest request = mock(HttpServletRequest.class);
                Member member = getMember();
                ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
                List<RecordAnalysisServiceDto> serviceDtos = getRecordAnalysisServiceDtos(10);
                serviceDtos.get(0).setTotal(null);

                willReturn(validateCheckResponse)
                    .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

                given(recordRepository.findByMemberAndUseDateAndAmountSumDesc(any(Member.class),
                    any(LocalDate.class),
                    any(LocalDate.class)))
                    .willReturn(serviceDtos);

                //when
                ResponseDto<?> responseDto = recordService.getAnalysisInfo(request, 2023, 6);

                //then
                assertFalse(responseDto.isSuccess());
                assertEquals(ErrorCode.INVALID_TOTAL.getDetail(), responseDto.getData());
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
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
            Record record = getRecord(member);

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

            given(recordRepository.findById(anyLong()))
                .willReturn(Optional.of(record));

            //when
            ResponseDto<?> responseDto = recordService.update(request, serviceDto);

            //then
            assertTrue(responseDto.isSuccess());
            assertEquals("Update Success", responseDto.getData());
            then(recordRepository).should().findById(anyLong());
            assertEquals(1L, record.getId());
            assertEquals(2L, record.getMember().getId());
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
                HttpServletRequest request = mock(HttpServletRequest.class);
                Member member = getMember();
                ResponseDto<?> validateCheckResponse = ResponseDto.success(member);

                willReturn(validateCheckResponse)
                    .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

                given(recordRepository.findById(anyLong()))
                    .willReturn(Optional.empty());

                //when
                // Custom Exception 연동 시 수정 예정
                ResponseDto<?> responseDto = recordService.update(request, serviceDto);

                //then
                then(recordRepository).should().findById(anyLong());
                assertFalse(responseDto.isSuccess());
                assertEquals(ErrorCode.NOT_FOUND_RECORD.getDetail(), responseDto.getData());
            }

            @Test
            @DisplayName("일치하지 않는 사용자")
            void notMatchMember() {
                //given
                UpdateRecordServiceDto serviceDto = getUpdateServiceDto();
                HttpServletRequest request = mock(HttpServletRequest.class);
                Member requestMember = getMember();
                Member recordMember = getMember();
                recordMember.setId(3L);
                ResponseDto<?> validateCheckResponse = ResponseDto.success(requestMember);
                Record record = getRecord(recordMember);

                willReturn(validateCheckResponse)
                    .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

                given(recordRepository.findById(anyLong()))
                    .willReturn(Optional.of(record));

                //when
                ResponseDto<?> responseDto = recordService.update(request, serviceDto);

                //then
                then(recordRepository).should().findById(anyLong());
                assertFalse(responseDto.isSuccess());
                assertEquals(ErrorCode.NOT_MATCH_MEMBER.getDetail(), responseDto.getData());
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
            HttpServletRequest request = mock(HttpServletRequest.class);
            Member member = getMember();
            ResponseDto<?> validateCheckResponse = ResponseDto.success(member);
            Record record = getRecord(member);

            willReturn(validateCheckResponse)
                .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

            given(recordRepository.findById(anyLong()))
                .willReturn(Optional.of(record));

            ArgumentCaptor<Record> argumentCaptor = ArgumentCaptor.forClass(Record.class);

            //when
            ResponseDto<?> responseDto = recordService.delete(request, 1L);

            //then
            assertTrue(responseDto.isSuccess());
            assertEquals("Delete Success", responseDto.getData());
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
                HttpServletRequest request = mock(HttpServletRequest.class);
                Member member = getMember();
                ResponseDto<?> validateCheckResponse = ResponseDto.success(member);

                willReturn(validateCheckResponse)
                    .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

                given(recordRepository.findById(anyLong()))
                    .willReturn(Optional.empty());

                //when
                ResponseDto<?> responseDto = recordService.delete(request, 2L);

                //then
                then(recordRepository).should().findById(anyLong());
                then(recordRepository).should(never()).delete(any(Record.class));
                assertFalse(responseDto.isSuccess());
                assertEquals(ErrorCode.NOT_FOUND_RECORD.getDetail(), responseDto.getData());

            }

            @Test
            @DisplayName("일치하지 않는 사용자")
            void notMatchMember() {
                //given
                HttpServletRequest request = mock(HttpServletRequest.class);
                Member recordMember = getMember();
                Member requestMember = getMember();
                requestMember.setId(3L);
                ResponseDto<?> validateCheckResponse = ResponseDto.success(requestMember);
                Record record = getRecord(recordMember);

                willReturn(validateCheckResponse)
                    .given(tokenProvider).validateCheck(any(HttpServletRequest.class));

                given(recordRepository.findById(anyLong()))
                    .willReturn(Optional.of(record));

                //when
                ResponseDto<?> responseDto = recordService.delete(request, 1L);

                //then
                then(recordRepository).should().findById(anyLong());
                then(recordRepository).should(never()).delete(any(Record.class));
                assertFalse(responseDto.isSuccess());
                assertEquals(ErrorCode.NOT_MATCH_MEMBER.getDetail(), responseDto.getData());
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

    private Record getRecord(Member member) {
        return Record.builder()
            .id(1L)
            .member(member)
            .amount(10000)
            .category(Category.MEDICAL)
            .paidFor("병원")
            .memo("감기")
            .useDate(LocalDate.of(2023, 6, 6))
            .payType(PayType.CARD)
            .build();
    }

    private List<Record> getRecords(Member member, int size) {
        List<Record> records = new ArrayList<>();

        size = Math.max(size, 1);

        for (int i = size; i >= 1; i--) {
            Record record = Record.builder()
                .id((long) i)
                .member(member)
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