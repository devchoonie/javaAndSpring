package kr.choonie.shop.make;

import kr.choonie.shop.dto.DtoInterface;
import kr.choonie.shop.dto.PageDto;
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.Map;

public class PageInfo {

	private static final int LIST_COUNT = 6; //한화면에 보여줄 목록 개수

	private static final int PAGE_NUMBER_COUNT = 3; //한꺼번에 보여줄 화면하단 페이지 쪽수

	//마이바티스 용
	public static PageDto getPageDto(int requestPage, int totalRecord, int rowCount){
		if(totalRecord <= 0){
			return new PageDto(); //테이블 조회 레코드가 없으면 널 리턴
		}

		if(rowCount != 1 && rowCount != 3 && rowCount != 5  && rowCount != 10 && rowCount != 20 && rowCount != 30){
			rowCount = LIST_COUNT;
		}

		int totalPage = totalRecord/rowCount + 1; //총 페이지
		totalPage = totalRecord%rowCount == 0 ? totalPage - 1 : totalPage;

		if(requestPage <= 0){
			requestPage = 1;
		}else if(requestPage > totalPage){
			requestPage = totalPage;
		}


		int prePage = requestPage - 1; //이전 페이지
		if(prePage <= 0){
			prePage = 1;
		}

		int nextPage = 0; //다음 페이지
		if(requestPage < totalPage){
			nextPage = requestPage +1;
		}


		int pageStart = (requestPage / PAGE_NUMBER_COUNT) * PAGE_NUMBER_COUNT + 1;
		pageStart = requestPage % PAGE_NUMBER_COUNT == 0 ? requestPage-(PAGE_NUMBER_COUNT-1) : pageStart;
		pageStart = pageStart <= 0 ? 1 : pageStart;
		int pageEnd = pageStart + PAGE_NUMBER_COUNT -1;
		if(pageEnd > totalPage){
			pageEnd = totalPage;
		}
		pageEnd = pageEnd <= 0 ? 1 : pageEnd;

		int startRow = (requestPage*rowCount) - (rowCount-1);
		int endRow = startRow + (rowCount-1);

		Map<String,Object> map = new HashMap<String,Object>();
		PageDto pageDto = new PageDto();
		pageDto.setRequestPage(requestPage);
		pageDto.setTotalPage(totalPage); //전체페이지
		pageDto.setPrePage(prePage); //현재 보고있는 페이지의 이전페이지
		pageDto.setNextPage(nextPage); //현재 보고있는 페이지의 다음페이지
		pageDto.setListCount(rowCount); //한화면에 보여울 아이템 갯수
		pageDto.setPageStart(pageStart); //쪽번호의 시작번호
		pageDto.setPageEnd(pageEnd); ////쪽번호의 끝번호
		pageDto.setStartRow(startRow); //db에서 조회할 시작 레코드 row 번호 X 디비는 인덱스 0부터 시작 아래에 startRecord활용
		pageDto.setEndRow(endRow); //db에서 조회할 마지막 레코드 row 번호
		pageDto.setStartRecord(startRow-1); //db에서 조회할 마지막 레코드 row 번호


		return pageDto;
	}

	//Spring Pageable용
	public static Map<String, Integer> getStartEndPageMap(Page<? extends DtoInterface> page, int requestPage){

        int totalPage = page.getTotalPages();

		int pageStart = (requestPage / PAGE_NUMBER_COUNT) * PAGE_NUMBER_COUNT + 1;
		pageStart = requestPage % PAGE_NUMBER_COUNT == 0 ? requestPage-(PAGE_NUMBER_COUNT-1) : pageStart;
		pageStart = pageStart <= 0 ? 1 : pageStart;
		int pageEnd = pageStart + PAGE_NUMBER_COUNT -1;
		if(pageEnd > totalPage){
			pageEnd = totalPage;
		}
		pageEnd = pageEnd <= 0 ? 1 : pageEnd;


		return Map.of("startPage", pageStart, "endPage", pageEnd);

	}


}
